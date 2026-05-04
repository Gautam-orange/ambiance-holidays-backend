package com.ambianceholidays.payment.peach;

import com.ambianceholidays.api.booking.BookingService;
import com.ambianceholidays.api.booking.dto.CheckoutRequest;
import com.ambianceholidays.common.dto.ApiResponse;
import com.ambianceholidays.domain.booking.Booking;
import com.ambianceholidays.domain.booking.BookingRepository;
import com.ambianceholidays.domain.booking.BookingStatus;
import com.ambianceholidays.domain.payment.Payment;
import com.ambianceholidays.domain.payment.PaymentMethod;
import com.ambianceholidays.domain.payment.PaymentRepository;
import com.ambianceholidays.domain.payment.PaymentStatus;
import com.ambianceholidays.domain.user.UserRepository;
import com.ambianceholidays.exception.BusinessException;
import com.ambianceholidays.security.SecurityPrincipal;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Peach Payments V2 Hosted Checkout controller.
 *
 *  POST /payments/peach/initiate         create PENDING booking + Peach checkout, return redirectUrl
 *  GET  /payments/peach/status           SPA polls — re-queries Peach if not yet terminal
 *  POST /payments/peach/retry/{bookingId}  re-create checkout against an existing FAILED booking
 *  GET|POST /payments/peach/return       browser redirect from Peach; verifies via API then redirects to SPA
 *  POST /payments/peach/webhook          server-to-server callback from Peach (no body trust — verifies via API)
 */
@RestController
@RequestMapping("/payments/peach")
public class PeachPaymentController {

    private static final Logger log = LoggerFactory.getLogger(PeachPaymentController.class);
    private static final String SESSION_KEY_PREFIX = "peach:session:";
    private static final String CURRENCY_KEY_PREFIX = "peach:currency:";

    private final BookingService bookingService;
    private final BookingRepository bookingRepo;
    private final PaymentRepository paymentRepo;
    private final UserRepository userRepo;
    private final PeachCheckoutService peach;
    private final PeachProperties props;
    private final StringRedisTemplate redis;

    public PeachPaymentController(BookingService bookingService, BookingRepository bookingRepo,
            PaymentRepository paymentRepo, UserRepository userRepo,
            PeachCheckoutService peach, PeachProperties props, StringRedisTemplate redis) {
        this.bookingService = bookingService;
        this.bookingRepo = bookingRepo;
        this.paymentRepo = paymentRepo;
        this.userRepo = userRepo;
        this.peach = peach;
        this.props = props;
        this.redis = redis;
    }

    @PostMapping("/initiate")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ApiResponse<InitiateResponse> initiate(
            @RequestHeader(value = "X-Cart-Id", required = false) String cartId,
            @AuthenticationPrincipal SecurityPrincipal principal,
            @RequestParam(required = false) String currency,
            @Valid @RequestBody CheckoutRequest req) {

        String sessionKey = principal != null ? "user:" + principal.getUserId()
                : (cartId != null && !cartId.isBlank() ? "guest:" + cartId : "guest:anonymous");
        var actor = principal != null ? userRepo.findById(principal.getUserId()).orElse(null) : null;

        Booking booking = bookingService.createPendingBooking(sessionKey, req, actor);
        String resolvedCurrency = (currency != null && !currency.isBlank())
                ? currency.toUpperCase() : props.getDefaultCurrency();

        return ApiResponse.ok(startCheckout(booking, resolvedCurrency, sessionKey, /*newPayment=*/true));
    }

    /**
     * Re-attempt payment against an existing booking whose previous Payment is FAILED.
     * Marks the failed payment row as superseded by creating a fresh PENDING Payment +
     * new Peach checkoutId. Booking.reference stays the same so user-facing IDs are stable.
     */
    @PostMapping("/retry/{bookingId}")
    @Transactional
    public ApiResponse<InitiateResponse> retry(@PathVariable UUID bookingId,
                                               @AuthenticationPrincipal SecurityPrincipal principal,
                                               @RequestHeader(value = "X-Cart-Id", required = false) String cartId,
                                               @RequestParam(required = false) String currency) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> BusinessException.notFound("Booking"));

        if (booking.getStatus() == BookingStatus.CONFIRMED || booking.getStatus() == BookingStatus.CANCELLED) {
            throw BusinessException.badRequest("BOOKING_NOT_RETRIABLE",
                    "Cannot retry payment for a " + booking.getStatus() + " booking");
        }

        // Refuse retry if there's already a PENDING/SUCCEEDED Payment we should be honouring.
        boolean hasOpenPayment = paymentRepo.findByBookingId(booking.getId()).stream()
                .anyMatch(p -> p.getStatus() == PaymentStatus.PENDING || p.getStatus() == PaymentStatus.SUCCEEDED);
        if (hasOpenPayment) {
            throw BusinessException.badRequest("PAYMENT_ALREADY_OPEN",
                    "An existing pending or successful payment exists for this booking");
        }

        String sessionKey = principal != null ? "user:" + principal.getUserId()
                : (cartId != null && !cartId.isBlank() ? "guest:" + cartId : "guest:anonymous");
        String resolvedCurrency = (currency != null && !currency.isBlank())
                ? currency.toUpperCase() : props.getDefaultCurrency();

        return ApiResponse.ok(startCheckout(booking, resolvedCurrency, sessionKey, /*newPayment=*/true));
    }

    /** Shared between /initiate and /retry. */
    private InitiateResponse startCheckout(Booking booking, String currency, String sessionKey, boolean newPayment) {
        String shopperResultUrl = backendBaseUrl() + "/payments/peach/return";
        PeachCheckoutService.CreateResult result = peach.createCheckout(
                booking.getTotalCents(), currency, booking.getReference(), shopperResultUrl);

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setMethod(PaymentMethod.PEACH);
        payment.setAmountCents(booking.getTotalCents());
        payment.setCurrency(currency);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPeachCheckoutId(result.checkoutId());
        paymentRepo.save(payment);

        redis.opsForValue().set(SESSION_KEY_PREFIX + result.checkoutId(),
                sessionKey, Duration.ofHours(2));
        redis.opsForValue().set(CURRENCY_KEY_PREFIX + result.checkoutId(),
                currency, Duration.ofHours(2));

        log.info("Peach checkout {} created for booking {} ({} {}{})",
                result.checkoutId(), booking.getReference(), currency,
                booking.getTotalCents(), newPayment ? "" : " [retry]");

        return new InitiateResponse(booking.getId(), booking.getReference(),
                result.checkoutId(), result.redirectUrl());
    }

    @GetMapping("/status")
    @Transactional
    public ApiResponse<StatusResponse> status(@RequestParam(required = false) String checkoutId,
                                              @RequestParam(required = false) String ref) {
        String key = firstNonBlank(checkoutId, ref);
        if (key == null || key.isBlank()) {
            throw BusinessException.badRequest("MISSING_REF", "Provide checkoutId or ref");
        }
        Payment payment = paymentRepo.findByPeachCheckoutId(key)
                .or(() -> bookingRepo.findByReference(key)
                        .flatMap(b -> paymentRepo.findByBookingId(b.getId()).stream().findFirst()))
                .orElseThrow(() -> BusinessException.notFound("Payment"));

        // Already finalised — short-circuit, no Peach call needed.
        if (payment.getStatus() == PaymentStatus.SUCCEEDED) return ApiResponse.ok(StatusResponse.from(payment, true));
        if (payment.getStatus() == PaymentStatus.FAILED)    return ApiResponse.ok(StatusResponse.from(payment, false));

        // Authoritative: ask Peach.
        PeachCheckoutService.StatusResult result = peach.getStatus(payment.getPeachCheckoutId());
        applyResult(payment, result.code(), result.description(), result.paymentId(), result.success(), result.pending());
        return ApiResponse.ok(StatusResponse.from(payment, payment.getStatus() == PaymentStatus.SUCCEEDED));
    }

    /**
     * Browser redirect from Peach. We DO NOT trust the result.code in the redirect —
     * it can be tampered with. Resolve the checkoutId, then call Peach's /v2/checkout/{id}/status
     * API (authoritative) and apply that. Then redirect the browser to the SPA.
     */
    @RequestMapping(value = "/return", method = { RequestMethod.GET, RequestMethod.POST })
    @Transactional
    public void returnFromPeach(@RequestParam(required = false) Map<String, String> params,
                                HttpServletResponse response) throws IOException {
        log.info("Peach return params: {}", params.keySet());

        String merchantTxId = params.get("merchantTransactionId");
        String resourcePath = params.get("resourcePath");
        String pathTail = resourcePath != null ? resourcePath.substring(resourcePath.lastIndexOf('/') + 1) : null;
        String id = params.get("id");
        String checkoutId = firstNonBlank(params.get("checkoutId"), id, pathTail);

        // Resolve payment by any available identifier — checkoutId first, then merchantTxId.
        Payment payment = null;
        if (checkoutId != null && !checkoutId.isBlank()) {
            payment = paymentRepo.findByPeachCheckoutId(checkoutId).orElse(null);
        }
        if (payment == null && merchantTxId != null && !merchantTxId.isBlank()) {
            payment = bookingRepo.findByReference(merchantTxId)
                    .flatMap(b -> paymentRepo.findByBookingId(b.getId()).stream().findFirst())
                    .orElse(null);
        }

        if (payment != null && payment.getStatus() == PaymentStatus.PENDING) {
            try {
                PeachCheckoutService.StatusResult result = peach.getStatus(payment.getPeachCheckoutId());
                applyResult(payment, result.code(), result.description(), result.paymentId(),
                        result.success(), result.pending());
            } catch (Exception e) {
                // Don't break the redirect if the verify call fails — the SPA will poll /status.
                log.warn("Peach /status verify on return failed for {}: {}",
                        payment.getPeachCheckoutId(), e.getMessage());
            }
        }

        String redirectKey = firstNonBlank(merchantTxId, pathTail, checkoutId, id);
        StringBuilder url = new StringBuilder(props.getReturnBaseUrl()).append("/payment/return");
        if (redirectKey != null && !redirectKey.isBlank()) {
            url.append("?ref=").append(URLEncoder.encode(redirectKey, StandardCharsets.UTF_8));
        }
        log.info("Peach return redirect → {} (merchantTxId={})", url, merchantTxId);
        response.setStatus(HttpServletResponse.SC_SEE_OTHER);
        response.setHeader("Location", url.toString());
    }

    /**
     * Peach server-to-server webhook. Configure this endpoint URL in the Peach
     * merchant dashboard. Peach posts the result asynchronously when payment
     * is finalised — this guarantees we receive the outcome even if the
     * customer closes the browser before the redirect.
     *
     * Body shape (typical): { "id": "<checkoutId>", "merchantTransactionId": "...",
     *   "result": { "code": "000.000.000", "description": "..." }, "paymentId": "..." }
     *
     * We do NOT trust body codes. We extract the checkoutId then re-query
     * Peach's status API to get the authoritative result.
     */
    @PostMapping("/webhook")
    @Transactional
    public ApiResponse<Map<String, String>> webhook(@RequestBody(required = false) Map<String, Object> body,
                                                     @RequestParam(required = false) Map<String, String> queryParams) {
        Map<String, String> q = queryParams != null ? queryParams : Map.of();
        String checkoutId = stringFrom(body, "id");
        if (checkoutId == null) checkoutId = stringFrom(body, "checkoutId");
        if (checkoutId == null) checkoutId = q.get("id");
        if (checkoutId == null) checkoutId = q.get("checkoutId");

        String merchantTxId = stringFrom(body, "merchantTransactionId");
        if (merchantTxId == null) merchantTxId = q.get("merchantTransactionId");

        log.info("Peach webhook received: checkoutId={} merchantTxId={}", checkoutId, merchantTxId);

        Payment payment = null;
        if (checkoutId != null) {
            payment = paymentRepo.findByPeachCheckoutId(checkoutId).orElse(null);
        }
        if (payment == null && merchantTxId != null) {
            payment = bookingRepo.findByReference(merchantTxId)
                    .flatMap(b -> paymentRepo.findByBookingId(b.getId()).stream().findFirst())
                    .orElse(null);
        }
        if (payment == null) {
            log.warn("Peach webhook: no Payment found for checkoutId={} merchantTxId={}", checkoutId, merchantTxId);
            // 200 anyway so Peach doesn't retry forever
            return ApiResponse.ok(Map.of("status", "ignored", "reason", "payment-not-found"));
        }

        // Idempotent: if already terminal, ack and skip.
        if (payment.getStatus() == PaymentStatus.SUCCEEDED || payment.getStatus() == PaymentStatus.FAILED) {
            return ApiResponse.ok(Map.of("status", "ack", "paymentStatus", payment.getStatus().name()));
        }

        // Authoritative re-query.
        PeachCheckoutService.StatusResult result = peach.getStatus(payment.getPeachCheckoutId());
        applyResult(payment, result.code(), result.description(), result.paymentId(),
                result.success(), result.pending());

        return ApiResponse.ok(Map.of(
                "status", "ok",
                "paymentStatus", payment.getStatus().name(),
                "resultCode", result.code() != null ? result.code() : ""));
    }

    /**
     * Single source of truth for translating a Peach result into Payment + Booking state.
     * Idempotent: re-applying the same terminal result is a no-op.
     */
    private void applyResult(Payment payment, String resultCode, String resultDesc,
                             String paymentId, boolean success, boolean pending) {
        if (payment.getStatus() == PaymentStatus.SUCCEEDED || payment.getStatus() == PaymentStatus.FAILED) {
            return; // already terminal, don't overwrite
        }

        payment.setPeachResultCode(resultCode);
        payment.setPeachResultDesc(resultDesc);
        if (paymentId != null && !paymentId.isBlank()) payment.setPeachPaymentId(paymentId);

        Booking booking = payment.getBooking();
        String checkoutId = payment.getPeachCheckoutId();

        if (success) {
            payment.setStatus(PaymentStatus.SUCCEEDED);
            payment.setPaidAt(Instant.now());
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepo.save(booking);
            paymentRepo.save(payment);

            String sessionKey = redis.opsForValue().get(SESSION_KEY_PREFIX + checkoutId);
            try {
                bookingService.finalizeBooking(booking, sessionKey);
            } catch (Exception e) {
                log.warn("finalizeBooking failed for {} but payment is already saved: {}",
                        booking.getReference(), e.getMessage());
            }
            redis.delete(SESSION_KEY_PREFIX + checkoutId);
            redis.delete(CURRENCY_KEY_PREFIX + checkoutId);
            log.info("Peach payment SUCCESS for booking {} (code={})", booking.getReference(), resultCode);
        } else if (pending) {
            // Don't move Payment to a terminal state — keep PENDING.
            paymentRepo.save(payment);
            log.info("Peach payment still PENDING for booking {} (code={}, desc={})",
                    booking.getReference(), resultCode, resultDesc);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepo.save(payment);
            log.info("Peach payment FAILED for booking {} (code={}, desc={})",
                    booking.getReference(), resultCode, resultDesc);
        }
    }

    /** Best-effort backend base URL for shopperResultUrl. Reads app.peach.backend-base-url, then PEACH_BACKEND_BASE_URL env var. */
    private String backendBaseUrl() {
        String configured = props.getBackendBaseUrl();
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("PEACH_BACKEND_BASE_URL");
        }
        if (configured != null && !configured.isBlank()) return configured;
        return "http://localhost:8080/api/v1";
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) if (v != null && !v.isBlank()) return v;
        return null;
    }

    @SuppressWarnings("unchecked")
    private static String stringFrom(Map<String, Object> body, String key) {
        if (body == null) return null;
        Object v = body.get(key);
        if (v == null) {
            // try nested "result.code"-style dotted access
            int dot = key.indexOf('.');
            if (dot > 0) {
                Object outer = body.get(key.substring(0, dot));
                if (outer instanceof Map<?, ?> m) {
                    Object inner = m.get(key.substring(dot + 1));
                    return inner != null ? inner.toString() : null;
                }
            }
            return null;
        }
        return v.toString();
    }

    public record InitiateResponse(UUID bookingId, String bookingReference,
                                   String checkoutId, String redirectUrl) {}

    public record StatusResponse(boolean success, String bookingId, String bookingReference,
                                 BookingStatus bookingStatus, PaymentStatus paymentStatus,
                                 String resultCode, String resultDescription) {
        static StatusResponse from(Payment p, boolean success) {
            Booking b = p.getBooking();
            return new StatusResponse(success,
                    b.getId().toString(), b.getReference(), b.getStatus(),
                    p.getStatus(), p.getPeachResultCode(), p.getPeachResultDesc());
        }
    }
}
