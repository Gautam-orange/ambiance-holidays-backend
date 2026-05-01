package com.ambianceholidays.api.booking;

import com.ambianceholidays.api.booking.dto.*;
import com.ambianceholidays.api.notification.NotificationService;
import com.ambianceholidays.common.dto.ApiResponse;
import com.ambianceholidays.common.dto.PageMeta;
import com.ambianceholidays.domain.agent.Agent;
import com.ambianceholidays.domain.agent.AgentRepository;
import com.ambianceholidays.domain.booking.*;
import com.ambianceholidays.domain.cart.CartItem;
import com.ambianceholidays.domain.customer.Customer;
import com.ambianceholidays.domain.customer.CustomerRepository;
import com.ambianceholidays.domain.payment.*;
import com.ambianceholidays.domain.settings.SystemSettingRepository;
import com.ambianceholidays.domain.user.User;
import com.ambianceholidays.exception.BusinessException;
import com.ambianceholidays.pricing.PricingEngine;
import com.ambianceholidays.pricing.PricingResult;
import com.ambianceholidays.security.SecurityPrincipal;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional
public class BookingService {

    private final BookingRepository bookingRepo;
    private final CustomerRepository customerRepo;
    private final AgentRepository agentRepo;
    private final PaymentRepository paymentRepo;
    private final SystemSettingRepository settingRepo;
    private final PricingEngine pricingEngine;
    private final CartService cartService;
    private final NotificationService notificationService;

    public BookingService(BookingRepository bookingRepo, CustomerRepository customerRepo,
            AgentRepository agentRepo, PaymentRepository paymentRepo,
            SystemSettingRepository settingRepo, PricingEngine pricingEngine,
            CartService cartService, NotificationService notificationService) {
        this.bookingRepo = bookingRepo;
        this.customerRepo = customerRepo;
        this.agentRepo = agentRepo;
        this.paymentRepo = paymentRepo;
        this.settingRepo = settingRepo;
        this.pricingEngine = pricingEngine;
        this.cartService = cartService;
        this.notificationService = notificationService;
    }

    // ── Checkout ─────────────────────────────────────────────────────────────

    public ApiResponse<BookingResponse> checkout(String sessionKey, CheckoutRequest req, User actor) {
        Booking booking = createPendingBooking(sessionKey, req, actor);
        finalizeBooking(booking, sessionKey);
        return ApiResponse.ok(BookingResponse.from(booking));
    }

    /**
     * Creates a Booking with PENDING status from the active cart.
     * Does NOT clear the cart and does NOT send confirmation — those happen in finalizeBooking.
     * Used by the payment-required flow (Peach) so we can hold the booking while the user pays.
     */
    public Booking createPendingBooking(String sessionKey, CheckoutRequest req, User actor) {
        List<CartItem> cartItems = cartService.getActiveItems(sessionKey);
        if (cartItems.isEmpty()) throw BusinessException.badRequest("EMPTY_CART", "Cart is empty");

        // Resolve or create customer. We update the row even if it already
        // exists so the latest details (whatsapp / nationality / address)
        // captured at checkout aren't silently dropped on repeat customers.
        Customer customer = customerRepo.findByEmailAndDeletedAtIsNull(req.customerEmail())
                .map(existing -> {
                    existing.setFirstName(req.customerFirstName());
                    existing.setLastName(req.customerLastName());
                    if (req.customerPhone() != null) existing.setPhone(req.customerPhone());
                    if (req.whatsappNumber() != null) existing.setWhatsapp(req.whatsappNumber());
                    if (req.nationality() != null)   existing.setNationality(req.nationality());
                    if (req.address() != null)       existing.setAddress(req.address());
                    return customerRepo.save(existing);
                })
                .orElseGet(() -> {
                    Customer c = new Customer();
                    c.setFirstName(req.customerFirstName());
                    c.setLastName(req.customerLastName());
                    c.setEmail(req.customerEmail());
                    c.setPhone(req.customerPhone());
                    c.setWhatsapp(req.whatsappNumber());
                    c.setNationality(req.nationality());
                    c.setAddress(req.address());
                    return customerRepo.save(c);
                });

        // Resolve agent (if actor has B2B_AGENT role)
        Agent agent = null;
        if (actor != null && actor.getRole().name().equals("B2B_AGENT")) {
            agent = agentRepo.findByUserIdAndDeletedAtIsNull(actor.getId()).orElse(null);
        }

        BigDecimal vatRate = getSetting("vat_rate", new BigDecimal("15.00"));
        BigDecimal markupRate = agent != null ? agent.getMarkupPercent() : BigDecimal.ZERO;
        BigDecimal commissionRate = agent != null ? agent.getCommissionRate() : BigDecimal.ZERO;

        Booking booking = new Booking();
        booking.setReference(generateReference());
        booking.setCustomer(customer);
        booking.setAgent(agent);
        booking.setServiceDate(req.serviceDate());
        booking.setSpecialRequests(req.specialRequests());
        booking.setCreatedBy(actor);
        booking.setVatRate(vatRate);
        booking.setMarkupRate(markupRate);
        booking.setCommissionRate(commissionRate);

        int subtotal = 0;
        for (CartItem ci : cartItems) {
            BookingItem item = new BookingItem();
            item.setBooking(booking);
            item.setItemType(ci.getItemType());
            item.setRefId(ci.getRefId());
            item.setQuantity(ci.getQuantity());
            item.setUnitPriceCents(ci.getUnitPriceCents());
            item.setTotalCents(ci.getUnitPriceCents() * ci.getQuantity());
            item.setServiceDate(req.serviceDate());
            if (ci.getOptions() != null) {
                extractItemOptions(item, ci.getOptions());
            }
            booking.getItems().add(item);
            subtotal += item.getTotalCents();
        }

        PricingResult pricing = pricingEngine.calculate(subtotal, markupRate, commissionRate, vatRate);
        booking.setSubtotalCents(pricing.subtotalCents());
        booking.setMarkupCents(pricing.markupCents());
        booking.setCommissionCents(pricing.commissionCents());
        booking.setVatCents(pricing.vatCents());
        booking.setTotalCents(pricing.totalCents());

        return bookingRepo.save(booking);
    }

    /** Clear the cart and send the booking confirmation email. Call after payment success. */
    public void finalizeBooking(Booking booking, String sessionKey) {
        if (sessionKey != null) cartService.clearCart(sessionKey);
        notificationService.sendBookingConfirmation(booking);
    }

    // ── List / Get ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ApiResponse<List<BookingResponse>> list(String search, BookingStatus status,
            String agentId, String dateFrom, String dateTo, Boolean isEnquiry,
            int page, int size, User actor) {
        Specification<Booking> spec = buildSpec(search, status, agentId, dateFrom, dateTo, isEnquiry, actor);
        Page<Booking> pg = bookingRepo.findAll(spec, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ApiResponse.ok(pg.getContent().stream().map(BookingResponse::from).toList(),
                PageMeta.of(page, size, pg.getTotalElements()));
    }

    @Transactional(readOnly = true)
    public ApiResponse<BookingResponse> get(UUID id, User actor) {
        Booking b = bookingRepo.findById(id).orElseThrow(() -> BusinessException.notFound("Booking"));
        checkAccess(b, actor);
        return ApiResponse.ok(BookingResponse.from(b));
    }

    // ── Status management ─────────────────────────────────────────────────────

    public ApiResponse<BookingResponse> updateStatus(UUID id, BookingStatus newStatus, User actor) {
        Booking b = bookingRepo.findById(id).orElseThrow(() -> BusinessException.notFound("Booking"));
        validateTransition(b.getStatus(), newStatus);
        b.setStatus(newStatus);
        bookingRepo.save(b);
        return ApiResponse.ok(BookingResponse.from(b));
    }

    private void validateTransition(BookingStatus current, BookingStatus next) {
        boolean allowed = switch (current) {
            case PENDING    -> next == BookingStatus.CONFIRMED || next == BookingStatus.CANCELLED;
            case CONFIRMED  -> next == BookingStatus.CANCELLED;
            case CANCELLED  -> false;
        };
        if (!allowed) throw BusinessException.badRequest("INVALID_TRANSITION",
                "Cannot transition booking from " + current + " to " + next);
    }

    public ApiResponse<BookingResponse> cancel(UUID id, String reason, String cancelledByType, User actor) {
        Booking b = bookingRepo.findById(id).orElseThrow(() -> BusinessException.notFound("Booking"));
        if (b.getStatus() == BookingStatus.CANCELLED) throw BusinessException.badRequest("ALREADY_CANCELLED", "Already cancelled");
        if (b.getServiceDate() != null && b.getServiceDate().isBefore(java.time.LocalDate.now()))
            throw BusinessException.badRequest("PAST_SERVICE_DATE", "Cannot cancel a booking whose service date has passed");
        checkAccess(b, actor);

        long hoursUntil = ChronoUnit.HOURS.between(Instant.now(),
                b.getServiceDate().atStartOfDay().toInstant(ZoneOffset.UTC));
        int fee = pricingEngine.cancellationFee(b.getTotalCents(), Math.max(0, hoursUntil));

        b.setStatus(BookingStatus.CANCELLED);
        b.setCancelReason(reason);
        b.setCancelledByType(cancelledByType != null ? cancelledByType : "ADMIN");
        b.setCancelledAt(Instant.now());
        b.setCancelledBy(actor);
        b.setCancellationFeeCents(fee);
        bookingRepo.save(b);
        notificationService.sendBookingCancellation(b);
        return ApiResponse.ok(BookingResponse.from(b));
    }

    // ── Enquiry management ────────────────────────────────────────────────────

    public ApiResponse<BookingResponse> convertEnquiry(UUID id, User actor) {
        Booking b = bookingRepo.findById(id).orElseThrow(() -> BusinessException.notFound("Booking"));
        if (!b.isEnquiry()) throw BusinessException.badRequest("NOT_ENQUIRY", "Booking is not an enquiry");
        if (b.getStatus() != BookingStatus.PENDING)
            throw BusinessException.badRequest("INVALID_STATE", "Only pending enquiries can be converted");

        b.setStatus(BookingStatus.CONFIRMED);
        b.setEnquiry(false);
        b.setEnquiryConvertedAt(Instant.now());
        bookingRepo.save(b);
        notificationService.sendBookingConfirmation(b);
        return ApiResponse.ok(BookingResponse.from(b));
    }

    public ApiResponse<BookingResponse> declineEnquiry(UUID id, String reason, User actor) {
        Booking b = bookingRepo.findById(id).orElseThrow(() -> BusinessException.notFound("Booking"));
        if (!b.isEnquiry()) throw BusinessException.badRequest("NOT_ENQUIRY", "Booking is not an enquiry");
        if (b.getStatus() != BookingStatus.PENDING)
            throw BusinessException.badRequest("INVALID_STATE", "Only pending enquiries can be declined");

        b.setStatus(BookingStatus.CANCELLED);
        b.setCancelReason(reason);
        b.setCancelledByType("ADMIN");
        b.setCancelledAt(Instant.now());
        b.setEnquiryDeclinedAt(Instant.now());
        b.setCancelledBy(actor);
        bookingRepo.save(b);
        notificationService.sendBookingCancellation(b);
        return ApiResponse.ok(BookingResponse.from(b));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Specification<Booking> buildSpec(String search, BookingStatus status, String agentId,
            String dateFrom, String dateTo, Boolean isEnquiry, User actor) {
        return (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                Predicate byRef = cb.like(cb.lower(root.get("reference")), pattern);
                // Also search by customer name / email via join
                var customerJoin = root.join("customer", jakarta.persistence.criteria.JoinType.LEFT);
                Predicate byEmail = cb.like(cb.lower(customerJoin.get("email")), pattern);
                Predicate byFirstName = cb.like(cb.lower(customerJoin.get("firstName")), pattern);
                Predicate byLastName  = cb.like(cb.lower(customerJoin.get("lastName")),  pattern);
                preds.add(cb.or(byRef, byEmail, byFirstName, byLastName));
            }
            if (status != null) preds.add(cb.equal(root.get("status"), status));
            if (agentId != null) preds.add(cb.equal(root.get("agent").get("id"), UUID.fromString(agentId)));
            // Date range on service date
            if (dateFrom != null) {
                preds.add(cb.greaterThanOrEqualTo(root.get("serviceDate"),
                        java.time.LocalDate.parse(dateFrom)));
            }
            if (dateTo != null) {
                preds.add(cb.lessThanOrEqualTo(root.get("serviceDate"),
                        java.time.LocalDate.parse(dateTo)));
            }
            // Enquiry flag
            if (isEnquiry != null) {
                preds.add(cb.equal(root.get("isEnquiry"), isEnquiry));
            }
            // B2B agents see bookings linked to their agent record OR created by them
            if (actor != null && actor.getRole().name().equals("B2B_AGENT")) {
                UUID userId = actor.getId();
                Optional<Agent> agentOpt = agentRepo.findByUserIdAndDeletedAtIsNull(userId);

                // Predicate: booking was created by this user
                Predicate byCreator = cb.equal(root.get("createdBy").get("id"), userId);

                if (agentOpt.isPresent()) {
                    // Predicate: booking is linked to this agent record
                    Predicate byAgent = cb.equal(root.get("agent").get("id"), agentOpt.get().getId());
                    preds.add(cb.or(byAgent, byCreator));
                } else {
                    // No agent record found – fall back to creator-only filter
                    preds.add(byCreator);
                }
            }
            return cb.and(preds.toArray(new Predicate[0]));
        };
    }

    private void checkAccess(Booking b, User actor) {
        if (actor == null) return;
        if (actor.getRole().name().equals("B2B_AGENT")) {
            // Allow access if the booking was created by this user
            boolean createdByActor = b.getCreatedBy() != null
                    && b.getCreatedBy().getId().equals(actor.getId());
            if (createdByActor) return;

            // Allow access if the booking is linked to this user's agent record
            Agent a = agentRepo.findByUserIdAndDeletedAtIsNull(actor.getId()).orElse(null);
            boolean linkedToAgent = a != null
                    && b.getAgent() != null
                    && a.getId().equals(b.getAgent().getId());
            if (linkedToAgent) return;

            throw BusinessException.forbidden("Access denied");
        }
    }

    private void extractItemOptions(BookingItem item, Map<String, Object> opts) {
        if (opts.get("pickupLocation") instanceof String s)  item.setPickupLocation(s);
        if (opts.get("dropoffLocation") instanceof String s) item.setDropoffLocation(s);
        // Frontend sends "adults" for rentals/transfers and "paxAdults" as fallback
        if (opts.get("adults") instanceof Number n)    item.setPaxAdults(n.shortValue());
        if (opts.get("paxAdults") instanceof Number n) item.setPaxAdults(n.shortValue());
        if (opts.get("paxChildren") instanceof Number n) item.setPaxChildren(n.shortValue());
        if (opts.get("paxInfants") instanceof Number n)  item.setPaxInfants(n.shortValue());
        // Frontend sends rentalDays as a number
        if (opts.get("rentalDays") instanceof Number n) item.setRentalDays(n.shortValue());
        if (opts.get("notes") instanceof String s) item.setNotes(s);
        // Trip type for transfers
        if (opts.get("tripType") instanceof String s) {
            try {
                item.setTripType(com.ambianceholidays.domain.transfer.TransferTripType.valueOf(s));
            } catch (IllegalArgumentException ignored) {}
        }
        // Persist selected extra services as BookingExtra rows
        if (opts.get("selectedExtras") instanceof List<?> extList) {
            for (Object e : extList) {
                if (e instanceof Map<?, ?> extraMap) {
                    String name = extraMap.get("name") instanceof String s ? s : null;
                    Number priceCents = extraMap.get("priceCents") instanceof Number n ? n : null;
                    if (name != null && priceCents != null) {
                        BookingExtra extra = new BookingExtra();
                        extra.setBookingItem(item);
                        extra.setLabel(name);
                        extra.setQuantity((short) 1);
                        extra.setUnitPriceCents(priceCents.intValue());
                        extra.setTotalCents(priceCents.intValue());
                        item.getExtras().add(extra);
                    }
                }
            }
        }
    }

    private BigDecimal getSetting(String key, BigDecimal fallback) {
        return settingRepo.findById(key).map(s -> new BigDecimal(s.getValue())).orElse(fallback);
    }

    private String generateReference() {
        String ref;
        do {
            ref = "AMB-" + String.format("%06d", (int)(Math.random() * 1_000_000));
        } while (bookingRepo.existsByReference(ref));
        return ref;
    }
}
