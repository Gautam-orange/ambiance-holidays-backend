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
    private final com.ambianceholidays.domain.payment.InvoiceRepository invoiceRepo;
    private final SystemSettingRepository settingRepo;
    private final PricingEngine pricingEngine;
    private final CartService cartService;
    private final NotificationService notificationService;
    private final com.ambianceholidays.domain.car.CarRepository carRepo;
    private final com.ambianceholidays.domain.tour.TourRepository tourRepo;
    private final com.ambianceholidays.domain.tour.DayTripRepository dayTripRepo;

    public BookingService(BookingRepository bookingRepo, CustomerRepository customerRepo,
            AgentRepository agentRepo, PaymentRepository paymentRepo,
            com.ambianceholidays.domain.payment.InvoiceRepository invoiceRepo,
            SystemSettingRepository settingRepo, PricingEngine pricingEngine,
            CartService cartService, NotificationService notificationService,
            com.ambianceholidays.domain.car.CarRepository carRepo,
            com.ambianceholidays.domain.tour.TourRepository tourRepo,
            com.ambianceholidays.domain.tour.DayTripRepository dayTripRepo) {
        this.bookingRepo = bookingRepo;
        this.customerRepo = customerRepo;
        this.agentRepo = agentRepo;
        this.paymentRepo = paymentRepo;
        this.invoiceRepo = invoiceRepo;
        this.settingRepo = settingRepo;
        this.pricingEngine = pricingEngine;
        this.cartService = cartService;
        this.notificationService = notificationService;
        this.carRepo = carRepo;
        this.tourRepo = tourRepo;
        this.dayTripRepo = dayTripRepo;
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
        // AM_023: any deactivated user (suspended customer or agent) must be
        // blocked from creating bookings — defence in depth on top of the
        // login-time check. Otherwise a token issued before suspension would
        // still let them check out.
        if (actor != null && !actor.isActive()) {
            throw BusinessException.forbidden("Your account is suspended and cannot create bookings");
        }
        // Fail fast on a suspended / pending agent BEFORE we look at their cart
        // — otherwise the empty-cart message obscures the real reason.
        if (actor != null && actor.getRole().name().equals("B2B_AGENT")) {
            Agent ag = agentRepo.findByUserIdAndDeletedAtIsNull(actor.getId()).orElse(null);
            if (ag != null && ag.getStatus() != com.ambianceholidays.domain.agent.AgentStatus.ACTIVE) {
                throw BusinessException.forbidden("Agent account is " + ag.getStatus().name().toLowerCase()
                        + " and cannot create bookings");
            }
        }

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

        // Resolve agent (if actor has B2B_AGENT role). Suspended/pending agents
        // can't create bookings — short-circuit before we mutate any state.
        Agent agent = null;
        if (actor != null && actor.getRole().name().equals("B2B_AGENT")) {
            agent = agentRepo.findByUserIdAndDeletedAtIsNull(actor.getId()).orElse(null);
            if (agent != null && agent.getStatus() != com.ambianceholidays.domain.agent.AgentStatus.ACTIVE) {
                throw BusinessException.forbidden("Agent account is " + agent.getStatus().name().toLowerCase()
                        + " and cannot create bookings");
            }
        }

        BigDecimal vatRate = getSetting("vat_rate", new BigDecimal("15.00"));
        BigDecimal markupRate = agent != null ? agent.getMarkupPercent() : BigDecimal.ZERO;
        // Commission removed from invoicing — keep field for schema/back-compat but always zero.
        BigDecimal commissionRate = BigDecimal.ZERO;

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
            // Reject any car (rental or transfer) that's been disabled or
            // soft-deleted between add-to-cart and checkout. Otherwise the
            // booking lands and admin has to clean it up after the fact.
            if (ci.getItemType() == com.ambianceholidays.domain.booking.BookingItemType.CAR_RENTAL
                    || ci.getItemType() == com.ambianceholidays.domain.booking.BookingItemType.CAR_TRANSFER) {
                var car = carRepo.findById(ci.getRefId())
                        .orElseThrow(() -> BusinessException.badRequest("CAR_UNAVAILABLE",
                                "A vehicle in your cart is no longer available"));
                if (car.getDeletedAt() != null
                        || car.getStatus() != com.ambianceholidays.domain.car.CarStatus.ACTIVE) {
                    throw BusinessException.badRequest("CAR_UNAVAILABLE",
                            "Vehicle '" + car.getName() + "' is currently unavailable. Please remove it from your cart.");
                }
                // AM_041: re-check car seat capacity in case admin shrank it
                // between add-to-cart and checkout.
                if (ci.getOptions() != null) {
                    Object adultsOpt = ci.getOptions().get("adults");
                    int adultsInCart = adultsOpt instanceof Number n ? n.intValue() : 0;
                    if (adultsInCart > car.getPassengerCapacity()) {
                        throw BusinessException.badRequest("PAX_OVER_MAX",
                                "Vehicle '" + car.getName() + "' seats only "
                                        + car.getPassengerCapacity()
                                        + ". Please reduce the passenger count in your cart.");
                    }
                }
            }
            // AM_041: revalidate tour/day-trip pax against current maxPax —
            // protects against the admin reducing capacity after add-to-cart.
            if (ci.getItemType() == com.ambianceholidays.domain.booking.BookingItemType.TOUR
                    && ci.getOptions() != null) {
                int adults = ci.getOptions().get("paxAdults") instanceof Number n ? n.intValue() : 1;
                int children = ci.getOptions().get("paxChildren") instanceof Number n ? n.intValue() : 0;
                var tour = tourRepo.findById(ci.getRefId()).orElse(null);
                if (tour != null && (adults + children) > tour.getMaxPax()) {
                    throw BusinessException.badRequest("PAX_OVER_MAX",
                            tour.getTitle() + " can host at most " + tour.getMaxPax()
                                    + " guest" + (tour.getMaxPax() == 1 ? "" : "s") + ". Please update your cart.");
                }
            }
            if (ci.getItemType() == com.ambianceholidays.domain.booking.BookingItemType.DAY_TRIP
                    && ci.getOptions() != null) {
                int adults = ci.getOptions().get("paxAdults") instanceof Number n ? n.intValue() : 1;
                int children = ci.getOptions().get("paxChildren") instanceof Number n ? n.intValue() : 0;
                var trip = dayTripRepo.findById(ci.getRefId()).orElse(null);
                if (trip != null && trip.getMaxPax() != null
                        && (adults + children) > trip.getMaxPax()) {
                    throw BusinessException.badRequest("PAX_OVER_MAX",
                            trip.getTitle() + " can host at most " + trip.getMaxPax()
                                    + " guest" + (trip.getMaxPax() == 1 ? "" : "s") + ". Please update your cart.");
                }
            }
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

    /** Clear the cart and send the booking confirmation email. Call after payment success.
     *  <p>
     *  ROOT CAUSE FIX (recurring "cart not cleared after booking" bug):
     *  the caller used to be the sole source of `sessionKey`. The Peach flow
     *  stored that key in Redis with a 2-hour TTL — so anyone who took longer
     *  to complete payment, lost the Redis pod, or hit the non-Peach path
     *  with a null sessionKey ended up here with `sessionKey == null` and
     *  the cart was never cleared. We now ALSO derive the key from the
     *  booking's `createdBy` user, so cart clearing is deterministic. The
     *  explicit param is still honoured for the guest-cart-id path.
     */
    public void finalizeBooking(Booking booking, String sessionKey) {
        if (sessionKey != null) cartService.clearCart(sessionKey);
        // Belt-and-braces: also clear by the user-derived key. If the booking
        // was created by a logged-in actor, the cart lives at "user:{uuid}"
        // regardless of what the original payment-initiate flow remembered.
        if (booking.getCreatedBy() != null) {
            String userKey = "user:" + booking.getCreatedBy().getId();
            if (!userKey.equals(sessionKey)) cartService.clearCart(userKey);
        }
        notificationService.sendBookingConfirmation(booking);
        // Ops-side notification — every new booking goes out to active admins.
        // Extract primitives on the caller's thread so the @Async path doesn't
        // race with this transaction's Hibernate session.
        notificationService.sendAdminBookingCreated(
                booking.getReference(),
                booking.getCustomer().getFullName(),
                booking.getCustomer().getEmail(),
                booking.getAgent() != null ? booking.getAgent().getCompanyName() : null,
                booking.getServiceDate(),
                booking.getTotalCents(),
                null, // currency intentionally null — admin email defaults to USD label
                booking.getId());
    }

    // ── List / Get ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ApiResponse<List<BookingResponse>> list(String search, BookingStatus status,
            String agentId, String dateFrom, String dateTo, Boolean isEnquiry,
            int page, int size, User actor) {
        Specification<Booking> spec = buildSpec(search, status, agentId, dateFrom, dateTo, isEnquiry, actor);
        Page<Booking> pg = bookingRepo.findAll(spec, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        // Batch-fetch latest payment per booking so admin list shows paid/pending/failed without N+1.
        Map<UUID, Payment> latestByBooking = latestPaymentsFor(pg.getContent());
        return ApiResponse.ok(
                pg.getContent().stream()
                        .map(b -> BookingResponse.from(b, latestByBooking.get(b.getId()), null, carRepo))
                        .toList(),
                PageMeta.of(page, size, pg.getTotalElements()));
    }

    @Transactional(readOnly = true)
    public ApiResponse<BookingResponse> get(UUID id, User actor) {
        Booking b = bookingRepo.findById(id).orElseThrow(() -> BusinessException.notFound("Booking"));
        checkAccess(b, actor);
        Payment latest = latestPaymentFor(b.getId());
        // Look up invoice number for the Booking Detail screen (admin display).
        // A booking may legitimately have no Invoice yet (DRAFT status); return null then.
        String invoiceNumber = invoiceRepo.findByBookingId(b.getId()).stream()
                .map(com.ambianceholidays.domain.payment.Invoice::getInvoiceNumber)
                .findFirst().orElse(null);
        return ApiResponse.ok(BookingResponse.from(b, latest, invoiceNumber, carRepo));
    }

    /** Latest (by createdAt desc) Payment for a booking, or null if none. */
    private Payment latestPaymentFor(UUID bookingId) {
        return paymentRepo.findByBookingId(bookingId).stream()
                .max(Comparator.comparing(Payment::getCreatedAt))
                .orElse(null);
    }

    /** Latest Payment per booking ID for the supplied bookings — single batched query. */
    private Map<UUID, Payment> latestPaymentsFor(List<Booking> bookings) {
        if (bookings.isEmpty()) return Map.of();
        List<UUID> ids = bookings.stream().map(Booking::getId).toList();
        Map<UUID, Payment> out = new HashMap<>();
        for (Payment p : paymentRepo.findByBookingIdIn(ids)) {
            UUID bId = p.getBooking().getId();
            Payment existing = out.get(bId);
            if (existing == null || p.getCreatedAt().isAfter(existing.getCreatedAt())) {
                out.put(bId, p);
            }
        }
        return out;
    }

    // ── Status management ─────────────────────────────────────────────────────

    public ApiResponse<BookingResponse> updateStatus(UUID id, BookingStatus newStatus, User actor) {
        Booking b = bookingRepo.findById(id).orElseThrow(() -> BusinessException.notFound("Booking"));
        validateTransition(b.getStatus(), newStatus);
        // Refuse to CONFIRM a booking whose owning agent is suspended /
        // inactive — otherwise an admin can rubber-stamp orders that the
        // platform already locked the agent out of. Cancellation is still
        // allowed regardless (admin needs a way to close stale orders).
        // Agent-only platform — one error code is enough.
        if (newStatus == BookingStatus.CONFIRMED) {
            boolean userInactive = b.getCreatedBy() != null && !b.getCreatedBy().isActive();
            boolean agentInactive = b.getAgent() != null
                    && b.getAgent().getStatus() != com.ambianceholidays.domain.agent.AgentStatus.ACTIVE;
            if (userInactive || agentInactive) {
                throw BusinessException.badRequest("AGENT_SUSPENDED",
                        "Cannot confirm: the agent account is suspended.");
            }
        }
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

        long hoursUntil = b.getServiceDate() != null
                ? ChronoUnit.HOURS.between(Instant.now(),
                        b.getServiceDate().atStartOfDay().toInstant(ZoneOffset.UTC))
                : Long.MAX_VALUE; // unknown service date — treat as far-out, zero cancellation fee.
        int fee = pricingEngine.cancellationFee(b.getTotalCents(), Math.max(0, hoursUntil));

        b.setStatus(BookingStatus.CANCELLED);
        b.setCancelReason(reason);
        b.setCancelledByType(cancelledByType != null ? cancelledByType : "ADMIN");
        b.setCancelledAt(Instant.now());
        b.setCancelledBy(actor);
        b.setCancellationFeeCents(fee);
        bookingRepo.save(b);
        notificationService.sendBookingCancellation(b);
        // Admins-too notification — extract on caller thread to avoid the @Async
        // / lazy-loading race that bit cancel() in the previous batch.
        notificationService.sendAdminBookingCancelled(
                b.getReference(),
                b.getCustomer().getFullName(),
                reason,
                b.getId());
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

    private static final java.time.ZoneId MAURITIUS_TZ = java.time.ZoneId.of("Indian/Mauritius");

    /** Pull a String value from a cart-options map, accepting any of `keys` as aliases. Returns null if missing/blank. */
    private static String optString(Map<String, Object> opts, String... keys) {
        for (String k : keys) {
            Object v = opts.get(k);
            if (v instanceof String s && !s.isBlank()) return s;
        }
        return null;
    }

    /**
     * Combine a YYYY-MM-DD date string with an optional HH:MM time string into a
     * Mauritius-local Instant. Returns null on parse failure or null date.
     */
    private static java.time.Instant combineToInstant(String dateStr, String timeStr) {
        if (dateStr == null) return null;
        try {
            java.time.LocalDate d = java.time.LocalDate.parse(dateStr);
            java.time.LocalTime t = (timeStr != null && !timeStr.isBlank())
                    ? java.time.LocalTime.parse(timeStr.length() == 5 ? timeStr : timeStr.substring(0, 5))
                    : java.time.LocalTime.MIDNIGHT;
            return java.time.ZonedDateTime.of(d, t, MAURITIUS_TZ).toInstant();
        } catch (Exception e) {
            return null;
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

        // Pickup / drop-off datetime — combine the YYYY-MM-DD + HH:MM the user
        // selected into Instants on BookingItem. Frontend uses different key
        // names per flow:
        //   Car rental:   pickupDate / pickupTime / dropoffDate / dropoffTime
        //   Transfer:     date / time   (single-leg)  +  returnDate / returnTime (round-trip)
        //   Tour:         (no per-item time — relies on pickupZone.pickupTime)
        // Read every alias so admin BookingDetails can render the start/end
        // datetime regardless of which flow the booking came from.
        java.time.Instant startAt = combineToInstant(
                optString(opts, "pickupDate", "date", "serviceDate"),
                optString(opts, "pickupTime", "time"));
        if (startAt != null) item.setStartAt(startAt);

        java.time.Instant endAt = combineToInstant(
                optString(opts, "dropoffDate", "returnDate"),
                optString(opts, "dropoffTime", "returnTime"));
        if (endAt != null) item.setEndAt(endAt);

        // HOURLY transfers carry a "hours" duration — append to notes so admin
        // sees it on BookingDetails. (No dedicated column; matches the same
        // approach used for free-form special requests.)
        if (opts.get("hours") instanceof Number n && n.intValue() > 0) {
            String prefix = item.getNotes() != null && !item.getNotes().isBlank() ? item.getNotes() + " | " : "";
            item.setNotes(prefix + "Duration: " + n.intValue() + " hour" + (n.intValue() == 1 ? "" : "s"));
        }
        // Multi-trip stops list. Frontend sends `stops: string[]` for MULTI_TRIP transfers.
        if (opts.get("stops") instanceof List<?> stopList) {
            String[] stops = stopList.stream()
                    .filter(s -> s instanceof String && !((String) s).isBlank())
                    .map(Object::toString)
                    .toArray(String[]::new);
            if (stops.length > 0) item.setStops(stops);
        }
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
