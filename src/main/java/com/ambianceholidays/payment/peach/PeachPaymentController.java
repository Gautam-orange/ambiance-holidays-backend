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
import java.util.UUID;

/**
 * Peach Payments V2 Hosted Checkout controller.
 *
 *  POST /payments/peach/initiate  — creates a PENDING booking + a V2 Peach
 *                                   checkout, returns the redirectUrl the
 *                                   frontend should send the browser to.
 *  GET  /payments/peach/status    — polls Peach for the result by checkoutId
 *                                   and updates booking + payment.
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

        // Peach V2 validates shopperResultUrl against a strict FQDN regex —
        // localhost is rejected. Point it at our backend's /return endpoint
        // (which sits behind the ngrok tunnel in dev) so we can then redirect
        // the customer's browser to the local SPA.
        String shopperResultUrl = backendBaseUrl() + "/payments/peach/return";

        PeachCheckoutService.CreateResult result = peach.createCheckout(
                booking.getTotalCents(), resolvedCurrency, booking.getReference(), shopperResultUrl);

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setMethod(PaymentMethod.PEACH);
        payment.setAmountCents(booking.getTotalCents());
        payment.setCurrency(resolvedCurrency);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPeachCheckoutId(result.checkoutId());
        paymentRepo.save(payment);

        // Stash sessionKey for the status handler so we can finalize the cart.
        redis.opsForValue().set(SESSION_KEY_PREFIX + result.checkoutId(),
                sessionKey, Duration.ofHours(2));
        redis.opsForValue().set(CURRENCY_KEY_PREFIX + result.checkoutId(),
                resolvedCurrency, Duration.ofHours(2));

        log.info("Peach V2 checkout {} initiated for booking {} ({} {})",
                result.checkoutId(), booking.getReference(), resolvedCurrency,
                booking.getTotalCents());

        return ApiResponse.ok(new InitiateResponse(
                booking.getId(), booking.getReference(),
                result.checkoutId(), result.redirectUrl()));
    }

    /**
     * Polled by the SPA after Peach redirects the customer back.
     *
     * Accepts either ?checkoutId=<peach-checkout-id> (legacy) or
     * ?ref=<value>. ref can be: our booking reference (matches Booking.reference
     * → traced to payment), the original checkoutId, or anything we stored as
     * peach_checkout_id. Falls back through the candidates in that order.
     */
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
        String resolvedCheckoutId = payment.getPeachCheckoutId();

        if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
            return ApiResponse.ok(StatusResponse.from(payment, true));
        }
        if (payment.getStatus() == PaymentStatus.FAILED) {
            return ApiResponse.ok(StatusResponse.from(payment, false));
        }

        PeachCheckoutService.StatusResult result = peach.getStatus(resolvedCheckoutId);
        payment.setPeachResultCode(result.code());
        payment.setPeachResultDesc(result.description());
        payment.setPeachPaymentId(result.paymentId());

        Booking booking = payment.getBooking();
        if (result.success()) {
            payment.setStatus(PaymentStatus.SUCCEEDED);
            payment.setPaidAt(Instant.now());
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepo.save(booking);
            paymentRepo.save(payment);

            String sessionKey = redis.opsForValue().get(SESSION_KEY_PREFIX + resolvedCheckoutId);
            try {
                bookingService.finalizeBooking(booking, sessionKey);
            } catch (Exception e) {
                log.warn("finalizeBooking failed for {} but payment is already saved: {}",
                        booking.getReference(), e.getMessage());
            }
            redis.delete(SESSION_KEY_PREFIX + resolvedCheckoutId);
            redis.delete(CURRENCY_KEY_PREFIX + resolvedCheckoutId);
            log.info("Peach payment SUCCESS for booking {} (code={})",
                    booking.getReference(), result.code());
            return ApiResponse.ok(StatusResponse.from(payment, true));
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepo.save(payment);
            log.info("Peach payment FAILED for booking {} (code={}, desc={})",
                    booking.getReference(), result.code(), result.description());
            return ApiResponse.ok(StatusResponse.from(payment, false));
        }
    }

    /**
     * Peach V2 redirects the customer's browser here after the hosted checkout
     * completes. Peach uses POST (form-encoded body with id, resourcePath,
     * etc.) but some plugin variants use GET — we accept both. The id is
     * extracted from query params first, then form body, then resourcePath
     * tail. Then we 303-redirect to the SPA's /payment/return route.
     */
    @RequestMapping(value = "/return", method = { org.springframework.web.bind.annotation.RequestMethod.GET,
                                                  org.springframework.web.bind.annotation.RequestMethod.POST })
    @Transactional
    public void returnFromPeach(@RequestParam(required = false) java.util.Map<String, String> params,
                                HttpServletResponse response) throws IOException {
        log.info("Peach return params: {}", params.keySet());

        String merchantTxId = params.get("merchantTransactionId");
        String resourcePath = params.get("resourcePath");
        String pathTail = resourcePath != null ? resourcePath.substring(resourcePath.lastIndexOf('/') + 1) : null;
        String id = params.get("id");
        String checkoutId = params.get("checkoutId");
        String resultCode = params.get("result.code");
        String resultDesc = params.get("result.description");

        // Peach posts result.code + result.description directly in the body —
        // process it here, no need to query /v2/checkout/{id}/status afterwards.
        if (merchantTxId != null && !merchantTxId.isBlank()) {
            bookingRepo.findByReference(merchantTxId).ifPresent(booking -> {
                paymentRepo.findByBookingId(booking.getId()).stream().findFirst().ifPresent(payment -> {
                    payment.setPeachResultCode(resultCode);
                    payment.setPeachResultDesc(resultDesc);
                    payment.setPeachPaymentId(id);
                    boolean success = peach.isSuccessCode(resultCode);
                    if (success) {
                        payment.setStatus(PaymentStatus.SUCCEEDED);
                        payment.setPaidAt(Instant.now());
                        booking.setStatus(BookingStatus.CONFIRMED);
                        bookingRepo.save(booking);
                        paymentRepo.save(payment);
                        String sessionKey = redis.opsForValue().get(SESSION_KEY_PREFIX + payment.getPeachCheckoutId());
                        try {
                            bookingService.finalizeBooking(booking, sessionKey);
                        } catch (Exception e) {
                            log.warn("finalizeBooking failed for {}: {}", booking.getReference(), e.getMessage());
                        }
                        redis.delete(SESSION_KEY_PREFIX + payment.getPeachCheckoutId());
                        redis.delete(CURRENCY_KEY_PREFIX + payment.getPeachCheckoutId());
                        log.info("Peach payment SUCCESS for booking {} (code={})", booking.getReference(), resultCode);
                    } else {
                        payment.setStatus(PaymentStatus.FAILED);
                        paymentRepo.save(payment);
                        log.info("Peach payment FAILED for booking {} (code={}, desc={})",
                                booking.getReference(), resultCode, resultDesc);
                    }
                });
            });
        }

        String redirectKey = firstNonBlank(merchantTxId, pathTail, checkoutId, id);
        StringBuilder url = new StringBuilder(props.getReturnBaseUrl()).append("/payment/return");
        if (redirectKey != null && !redirectKey.isBlank()) {
            url.append("?ref=").append(URLEncoder.encode(redirectKey, StandardCharsets.UTF_8));
        }
        log.info("Peach return redirect → {} (merchantTxId={}, code={})", url, merchantTxId, resultCode);
        response.setStatus(HttpServletResponse.SC_SEE_OTHER);
        response.setHeader("Location", url.toString());
    }

    /** Best-effort backend base URL for shopperResultUrl. Honours PEACH_BACKEND_BASE_URL when set. */
    private String backendBaseUrl() {
        String configured = System.getenv("PEACH_BACKEND_BASE_URL");
        if (configured != null && !configured.isBlank()) return configured;
        return "http://localhost:8080/api/v1";
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) if (v != null && !v.isBlank()) return v;
        return null;
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
