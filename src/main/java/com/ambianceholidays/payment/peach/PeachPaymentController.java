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
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

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
     * Create a booking (PENDING) and a Peach checkout. Returns the redirectUrl
     * the frontend should send the browser to.
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
        String shopperResultUrl = props.getReturnBaseUrl() + "/payment/return";

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

        // Stash sessionKey + currency for the return handler so we can finalize the cart
        redis.opsForValue().set(SESSION_KEY_PREFIX + result.checkoutId(),
                sessionKey, Duration.ofHours(2));
        redis.opsForValue().set(CURRENCY_KEY_PREFIX + result.checkoutId(),
                resolvedCurrency, Duration.ofHours(2));

        log.info("Peach checkout {} initiated for booking {} ({} {})",
                result.checkoutId(), booking.getReference(), resolvedCurrency,
                booking.getTotalCents());

        return ApiResponse.ok(new InitiateResponse(
                booking.getId(), booking.getReference(),
                result.checkoutId(), result.redirectUrl()));
    }

    /**
     * Called by the frontend after Peach redirects the user back. Polls Peach
     * for the final result and updates booking + payment.
     */
    @GetMapping("/status")
    @Transactional
    public ApiResponse<StatusResponse> status(@RequestParam String checkoutId) {
        Payment payment = paymentRepo.findByPeachCheckoutId(checkoutId)
                .orElseThrow(() -> BusinessException.notFound("Payment"));

        // If already finalized, return cached state — Peach status endpoint can be
        // called repeatedly (e.g. user refreshes the return page), but we only
        // want to flip the booking once.
        if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
            return ApiResponse.ok(StatusResponse.from(payment, true));
        }
        if (payment.getStatus() == PaymentStatus.FAILED) {
            return ApiResponse.ok(StatusResponse.from(payment, false));
        }

        PeachCheckoutService.StatusResult result = peach.getStatus(checkoutId);
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

            String sessionKey = redis.opsForValue().get(SESSION_KEY_PREFIX + checkoutId);
            try {
                bookingService.finalizeBooking(booking, sessionKey);
            } catch (Exception e) {
                log.warn("finalizeBooking failed for {} but payment is already saved: {}",
                        booking.getReference(), e.getMessage());
            }
            redis.delete(SESSION_KEY_PREFIX + checkoutId);
            redis.delete(CURRENCY_KEY_PREFIX + checkoutId);
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
