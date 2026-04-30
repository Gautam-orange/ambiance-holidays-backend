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
 * Peach Payments V1 Hosted Checkout controller.
 *
 *  POST /payments/peach/initiate  → builds the form params + signature, returns
 *                                   them to the frontend which auto-submits to
 *                                   testsecure.peachpayments.com/checkout
 *  POST /payments/peach/return    → Peach POSTs the result here. We update the
 *                                   booking and 302-redirect the customer's
 *                                   browser to the SPA /payment/return page.
 *  GET  /payments/peach/status    → SPA polls this to render success/failure UI.
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

    /**
     * Create a PENDING booking and return the signed Peach form params.
     * The frontend then auto-submits an HTML form to Peach's hosted page.
     */
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

        // Peach must POST back to OUR backend so we can verify the signature
        // server-side, update the payment row, then redirect the customer's
        // browser to the SPA result page. (A SPA route can't read POST body.)
        String shopperResultUrl = backendBaseUrl() + "/payments/peach/return";

        PeachCheckoutService.CreateResult result = peach.createCheckout(
                booking.getTotalCents(), resolvedCurrency, booking.getReference(), shopperResultUrl);

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setMethod(PaymentMethod.PEACH);
        payment.setAmountCents(booking.getTotalCents());
        payment.setCurrency(resolvedCurrency);
        payment.setStatus(PaymentStatus.PENDING);
        // V1 has no separate checkoutId — we use the booking reference as the local key.
        payment.setPeachCheckoutId(result.checkoutId());
        paymentRepo.save(payment);

        redis.opsForValue().set(SESSION_KEY_PREFIX + result.checkoutId(),
                sessionKey, Duration.ofHours(2));
        redis.opsForValue().set(CURRENCY_KEY_PREFIX + result.checkoutId(),
                resolvedCurrency, Duration.ofHours(2));

        log.info("Peach V1 checkout initiated for booking {} ({} {})",
                booking.getReference(), resolvedCurrency, booking.getTotalCents());

        return ApiResponse.ok(new InitiateResponse(
                booking.getId(), booking.getReference(),
                result.checkoutId(), result.submitUrl(), result.formFields()));
    }

    /**
     * Receives Peach's POST result, verifies the signature, updates the booking,
     * then redirects the customer's browser to the SPA result page with query
     * parameters describing the outcome.
     *
     * Mounted under /api/v1/payments/peach/return — make sure the merchant
     * configures shopperResultUrl accordingly.
     */
    @PostMapping(value = "/return", consumes = "application/x-www-form-urlencoded")
    @Transactional
    public void returnFromPeach(@RequestParam Map<String, String> params,
                                HttpServletResponse response) throws IOException {
        String merchantTxId = params.get("merchantTransactionId");
        String resultCode   = params.get("result.code");
        String resultDesc   = params.get("result.description");
        String paymentId    = params.get("id");
        String signature    = params.get("signature");

        if (merchantTxId == null || merchantTxId.isBlank()) {
            log.warn("Peach return POST missing merchantTransactionId — params={}", params.keySet());
            redirectToFrontend(response, null, false, "INVALID_RETURN");
            return;
        }

        // Verify HMAC signature so we know this POST really came from Peach.
        if (!peach.verifyResultSignature(params, signature)) {
            log.error("Peach return signature INVALID for txId={}", merchantTxId);
            redirectToFrontend(response, merchantTxId, false, "BAD_SIGNATURE");
            return;
        }

        Payment payment = paymentRepo.findByPeachCheckoutId(merchantTxId).orElse(null);
        if (payment == null) {
            log.warn("Peach return for unknown merchantTxId={}", merchantTxId);
            redirectToFrontend(response, merchantTxId, false, "UNKNOWN_BOOKING");
            return;
        }

        payment.setPeachResultCode(resultCode);
        payment.setPeachResultDesc(resultDesc);
        payment.setPeachPaymentId(paymentId);

        Booking booking = payment.getBooking();
        boolean success = peach.isSuccessCode(resultCode);

        if (success) {
            payment.setStatus(PaymentStatus.SUCCEEDED);
            payment.setPaidAt(Instant.now());
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepo.save(booking);
            paymentRepo.save(payment);

            String sessionKey = redis.opsForValue().get(SESSION_KEY_PREFIX + merchantTxId);
            try {
                bookingService.finalizeBooking(booking, sessionKey);
            } catch (Exception e) {
                log.warn("finalizeBooking failed for {} but payment is saved: {}",
                        booking.getReference(), e.getMessage());
            }
            redis.delete(SESSION_KEY_PREFIX + merchantTxId);
            redis.delete(CURRENCY_KEY_PREFIX + merchantTxId);
            log.info("Peach payment SUCCESS for booking {} (code={})",
                    booking.getReference(), resultCode);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepo.save(payment);
            log.info("Peach payment FAILED for booking {} (code={}, desc={})",
                    booking.getReference(), resultCode, resultDesc);
        }

        redirectToFrontend(response, booking.getReference(), success, resultCode);
    }

    /**
     * Status check used by the SPA result page to render the final outcome
     * (the result was already persisted by the /return handler so this is
     * just a read).
     */
    @GetMapping("/status")
    @Transactional(readOnly = true)
    public ApiResponse<StatusResponse> status(@RequestParam String checkoutId) {
        Payment payment = paymentRepo.findByPeachCheckoutId(checkoutId)
                .orElseThrow(() -> BusinessException.notFound("Payment"));
        boolean success = payment.getStatus() == PaymentStatus.SUCCEEDED;
        return ApiResponse.ok(StatusResponse.from(payment, success));
    }

    private void redirectToFrontend(HttpServletResponse response, String ref,
                                    boolean success, String code) throws IOException {
        StringBuilder url = new StringBuilder(props.getReturnBaseUrl()).append("/payment/return?");
        url.append("status=").append(success ? "success" : "failed");
        if (ref != null) url.append("&ref=").append(URLEncoder.encode(ref, StandardCharsets.UTF_8));
        if (code != null) url.append("&code=").append(URLEncoder.encode(code, StandardCharsets.UTF_8));
        response.setStatus(HttpServletResponse.SC_SEE_OTHER);
        response.setHeader("Location", url.toString());
    }

    /** Best-effort backend base URL for shopperResultUrl. Honours app.peach.backend-base-url if set. */
    private String backendBaseUrl() {
        String configured = System.getenv("PEACH_BACKEND_BASE_URL");
        if (configured != null && !configured.isBlank()) return configured;
        // Fallback — local dev. Production should set the env var.
        return "http://localhost:8080/api/v1";
    }

    /**
     * @param submitUrl   URL the frontend should POST the form to
     * @param formFields  All form fields including the signature
     */
    public record InitiateResponse(UUID bookingId, String bookingReference,
                                   String checkoutId, String submitUrl,
                                   Map<String, String> formFields) {}

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
