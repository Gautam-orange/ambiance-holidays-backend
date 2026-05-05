package com.ambianceholidays.api.booking;

import com.ambianceholidays.api.booking.dto.*;
import com.ambianceholidays.common.dto.ApiResponse;
import com.ambianceholidays.domain.booking.BookingItemType;
import com.ambianceholidays.domain.car.CarAvailabilityRepository;
import com.ambianceholidays.domain.car.CarRepository;
import com.ambianceholidays.domain.car.RatePeriod;
import com.ambianceholidays.domain.cart.CartItem;
import com.ambianceholidays.domain.cart.CartItemRepository;
import com.ambianceholidays.domain.settings.SystemSettingRepository;
import com.ambianceholidays.domain.tour.DayTripRepository;
import com.ambianceholidays.domain.tour.TourRepository;
import com.ambianceholidays.domain.transfer.TransferRouteRepository;
import com.ambianceholidays.domain.transferpricing.TransferPricingTierRepository;
import com.ambianceholidays.exception.BusinessException;
import com.ambianceholidays.pricing.PricingEngine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class CartService {

    private static final int CART_TTL_DAYS = 7;

    private final CartItemRepository cartRepo;
    private final SystemSettingRepository settingRepo;
    private final PricingEngine pricingEngine;
    private final CarRepository carRepo;
    private final CarAvailabilityRepository carAvailabilityRepo;
    private final TourRepository tourRepo;
    private final DayTripRepository dayTripRepo;
    private final TransferRouteRepository transferRouteRepo;
    private final TransferPricingTierRepository transferPricingRepo;

    public CartService(CartItemRepository cartRepo, SystemSettingRepository settingRepo,
            PricingEngine pricingEngine, CarRepository carRepo,
            CarAvailabilityRepository carAvailabilityRepo,
            TourRepository tourRepo, DayTripRepository dayTripRepo,
            TransferRouteRepository transferRouteRepo,
            TransferPricingTierRepository transferPricingRepo) {
        this.cartRepo = cartRepo;
        this.settingRepo = settingRepo;
        this.pricingEngine = pricingEngine;
        this.carRepo = carRepo;
        this.carAvailabilityRepo = carAvailabilityRepo;
        this.tourRepo = tourRepo;
        this.dayTripRepo = dayTripRepo;
        this.transferRouteRepo = transferRouteRepo;
        this.transferPricingRepo = transferPricingRepo;
    }

    @Transactional(readOnly = true)
    public ApiResponse<CartSummaryResponse> getCart(String sessionKey, BigDecimal agentMarkupPercent) {
        List<CartItem> items = cartRepo.findBySessionKeyAndExpiresAtAfter(sessionKey, Instant.now());
        return ApiResponse.ok(buildSummary(items, agentMarkupPercent));
    }

    public ApiResponse<CartSummaryResponse> addItem(String sessionKey, AddCartItemRequest req) {
        if (req.serviceDate() != null && req.serviceDate().isBefore(java.time.LocalDate.now())) {
            throw BusinessException.badRequest("PAST_DATE", "Service date cannot be in the past");
        }

        CartItem item = new CartItem();
        item.setSessionKey(sessionKey);
        item.setItemType(req.itemType());
        item.setRefId(req.refId());
        item.setQuantity(req.quantity());
        item.setOptions(req.options());
        item.setExpiresAt(Instant.now().plus(CART_TTL_DAYS, ChronoUnit.DAYS));

        int unitPrice = resolvePrice(req.itemType(), req.refId(), req.options());
        item.setUnitPriceCents(unitPrice);

        cartRepo.save(item);

        List<CartItem> all = cartRepo.findBySessionKeyAndExpiresAtAfter(sessionKey, Instant.now());
        return ApiResponse.ok(buildSummary(all, BigDecimal.ZERO));
    }

    public ApiResponse<CartSummaryResponse> removeItem(String sessionKey, UUID itemId) {
        CartItem item = cartRepo.findById(itemId)
                .filter(i -> i.getSessionKey().equals(sessionKey))
                .orElseThrow(() -> BusinessException.notFound("CartItem"));
        cartRepo.delete(item);

        List<CartItem> all = cartRepo.findBySessionKeyAndExpiresAtAfter(sessionKey, Instant.now());
        return ApiResponse.ok(buildSummary(all, BigDecimal.ZERO));
    }

    public ApiResponse<Void> clearCart(String sessionKey) {
        cartRepo.deleteBySessionKey(sessionKey);
        return ApiResponse.ok(null);
    }

    public List<CartItem> getActiveItems(String sessionKey) {
        return cartRepo.findBySessionKeyAndExpiresAtAfter(sessionKey, Instant.now());
    }

    private CartSummaryResponse buildSummary(List<CartItem> items, BigDecimal agentMarkupPercent) {
        BigDecimal vatRate = getVatRate();
        BigDecimal markupRate = agentMarkupPercent != null ? agentMarkupPercent : BigDecimal.ZERO;
        // Commission removed from invoicing — always zero in cart totals.
        BigDecimal commissionRate = BigDecimal.ZERO;
        int subtotal = items.stream().mapToInt(i -> i.getUnitPriceCents() * i.getQuantity()).sum();
        var result = pricingEngine.calculate(subtotal, markupRate, commissionRate, vatRate);
        return new CartSummaryResponse(
                items.stream().map(this::enrich).toList(),
                result.subtotalCents(),
                result.vatCents(),
                result.markupCents(),
                result.commissionCents(),
                markupRate,
                result.totalCents(),
                items.size()
        );
    }

    /** Resolve a human-readable title + cover image for a cart line by looking up the referenced product. */
    private CartItemResponse enrich(CartItem c) {
        String title = null;
        String imageUrl = null;
        try {
            switch (c.getItemType()) {
                case CAR_RENTAL -> {
                    var car = carRepo.findById(c.getRefId()).orElse(null);
                    if (car != null) { title = car.getName(); imageUrl = car.getCoverImageUrl(); }
                }
                case TOUR -> {
                    var tour = tourRepo.findById(c.getRefId()).orElse(null);
                    if (tour != null) { title = tour.getTitle(); imageUrl = tour.getCoverImageUrl(); }
                }
                case DAY_TRIP -> {
                    var trip = dayTripRepo.findById(c.getRefId()).orElse(null);
                    if (trip != null) { title = trip.getTitle(); imageUrl = trip.getCoverImageUrl(); }
                }
                case CAR_TRANSFER -> {
                    // refId is usually a TransferPricingTier id, so the route lookup typically misses.
                    // Derive the user-facing title from options.pickupLocation/dropoffLocation, and pull
                    // the car image from options.carId. Fall back to the route lookup if neither exists.
                    var opts = c.getOptions();
                    if (opts != null) {
                        Object pickup = opts.get("pickupLocation");
                        Object dropoff = opts.get("dropoffLocation");
                        if (pickup != null && dropoff != null) {
                            title = pickup + " → " + dropoff;
                        }
                        Object carIdObj = opts.get("carId");
                        if (carIdObj != null) {
                            try {
                                var car = carRepo.findById(UUID.fromString(carIdObj.toString())).orElse(null);
                                if (car != null) imageUrl = car.getCoverImageUrl();
                            } catch (IllegalArgumentException ignored) { /* malformed carId — skip */ }
                        }
                    }
                    if (title == null) {
                        var route = transferRouteRepo.findById(c.getRefId()).orElse(null);
                        if (route != null) {
                            title = route.getFromLocation() + " → " + route.getToLocation();
                        }
                    }
                }
                case HOTEL -> { /* not implemented */ }
            }
        } catch (Exception e) {
            // Lookup failure is non-fatal — fall back to itemType label on the frontend.
        }
        return CartItemResponse.from(c, title, imageUrl);
    }

    private BigDecimal getDefaultCommissionRate() {
        return settingRepo.findById("default_commission")
                .map(s -> new BigDecimal(s.getValue()))
                .orElse(new BigDecimal("10.00"));
    }

    private BigDecimal getVatRate() {
        return settingRepo.findById("vat_rate")
                .map(s -> new BigDecimal(s.getValue()))
                .orElse(new BigDecimal("15.00"));
    }

    /**
     * Validate that the requested adult count does not exceed the chosen car's
     * passenger capacity. Frontend disables the +button at capacity, but the
     * cart endpoint is reachable programmatically so we double-check server-side.
     * Accepts both `adults` (rentals/transfers) and `paxAdults` (legacy/tours)
     * keys for forward-compatibility.
     */
    private void validateAdultCount(Map<String, Object> options, short capacity, String carName) {
        if (options == null) return;
        Integer adults = null;
        if (options.get("adults") instanceof Number n)        adults = n.intValue();
        else if (options.get("paxAdults") instanceof Number n) adults = n.intValue();
        if (adults == null) return;
        if (adults < 1) {
            throw BusinessException.badRequest("INVALID_PAX",
                    "Number of adults must be at least 1.");
        }
        if (adults > capacity) {
            throw BusinessException.badRequest("PAX_OVER_CAPACITY",
                    carName + " seats only " + capacity + " passenger" + (capacity == 1 ? "" : "s")
                    + " — you selected " + adults + ".");
        }
    }

    private int resolvePrice(BookingItemType type, UUID refId, Map<String, Object> options) {
        return switch (type) {
            case CAR_RENTAL -> {
                var car = carRepo.findByIdAndDeletedAtIsNull(refId)
                        .orElseThrow(() -> BusinessException.notFound("Car"));
                if (car.getStatus().name().equals("INACTIVE"))
                    throw BusinessException.badRequest("PRODUCT_INACTIVE", "Car is not available for booking");

                // Pax-vs-capacity guard. The frontend caps the +button at the
                // car's seat count, but cart-add can be hit programmatically.
                validateAdultCount(options, car.getPassengerCapacity(), car.getName());

                int rentalDays = options != null && options.get("rentalDays") instanceof Number n
                        ? Math.max(1, n.intValue()) : 1;
                // Availability check against blocked dates
                java.time.LocalDate svcDate = options != null && options.get("pickupDate") instanceof String s
                        ? java.time.LocalDate.parse(s) : null;
                if (svcDate != null) {
                    java.time.LocalDate dropDate = svcDate.plusDays(rentalDays - 1);
                    long conflicts = carAvailabilityRepo.countOverlapping(refId, svcDate, dropDate);
                    if (conflicts > 0)
                        throw BusinessException.conflict("CAR_UNAVAILABLE",
                                "Car is not available for the selected dates");
                }
                int dailyRate = car.getRates().stream()
                        .filter(r -> r.getPeriod() == RatePeriod.DAILY)
                        .findFirst()
                        .map(r -> r.getAmountCents())
                        .orElseThrow(() -> BusinessException.badRequest("NO_RATE", "Car has no daily rate configured"));
                int basePrice = dailyRate * rentalDays;
                // Add any selected extra services
                int extrasTotal = 0;
                if (options != null && options.get("selectedExtras") instanceof List<?> extList) {
                    for (Object e : extList) {
                        if (e instanceof Map<?, ?> extraMap
                                && extraMap.get("priceCents") instanceof Number n) {
                            extrasTotal += n.intValue();
                        }
                    }
                }
                yield basePrice + extrasTotal;
            }
            case CAR_TRANSFER -> {
                // Pricing model: each car carries its own PER_KM rate bands
                // (CarRate rows with kmFrom/kmTo). Final price = the band whose
                // [kmFrom, kmTo] contains options.distanceKm.
                //
                // refId for a CAR_TRANSFER cart line is the chosen Car id.
                // options.distanceKm is the computed straight-line distance.
                int priceCents = 0;
                int distanceKm = options != null && options.get("distanceKm") instanceof Number n
                        ? n.intValue() : 0;

                if (refId != null) {
                    // Try as a Car id first (the new model)
                    var carOpt = carRepo.findByIdAndDeletedAtIsNull(refId);
                    if (carOpt.isPresent()) {
                        validateAdultCount(options, carOpt.get().getPassengerCapacity(), carOpt.get().getName());
                        var matchingBand = carOpt.get().getRates().stream()
                                .filter(r -> r.getPeriod() == RatePeriod.PER_KM)
                                .filter(r -> {
                                    int from = r.getKmFrom() != null ? r.getKmFrom() : 0;
                                    int to   = r.getKmTo()   != null ? r.getKmTo()   : Integer.MAX_VALUE;
                                    return distanceKm >= from && distanceKm <= to;
                                })
                                .findFirst();
                        if (matchingBand.isPresent()) {
                            priceCents = matchingBand.get().getAmountCents();
                        } else {
                            throw BusinessException.badRequest(
                                    "NO_RATE_BAND",
                                    "This car has no transfer rate covering " + distanceKm + " km. Please contact us for a custom quote."
                            );
                        }
                    } else {
                        // Legacy fallback: refId points at a TransferPricingTier (old cart items)
                        var tier = transferPricingRepo.findById(refId);
                        if (tier.isPresent() && tier.get().isActive()) {
                            priceCents = tier.get().getPriceCents();
                        }
                    }
                }

                // Final safety net — pre-computed price from the frontend.
                if (priceCents == 0 && options != null && options.get("unitPriceCents") instanceof Number n) {
                    priceCents = n.intValue();
                }
                if (priceCents == 0) throw BusinessException.badRequest("NO_PRICE", "Could not determine transfer price");
                yield priceCents;
            }
            case TOUR -> {
                var tour = tourRepo.findById(refId)
                        .orElseThrow(() -> BusinessException.notFound("Tour"));
                if (tour.getStatus().name().equals("INACTIVE"))
                    throw BusinessException.badRequest("PRODUCT_INACTIVE", "Tour is not available for booking");
                int adults   = options != null && options.get("paxAdults")   instanceof Number n ? n.intValue() : 1;
                int children = options != null && options.get("paxChildren") instanceof Number n ? n.intValue() : 0;
                int infants  = options != null && options.get("paxInfants")  instanceof Number n ? n.intValue() : 0;
                yield (tour.getAdultPriceCents() * adults)
                    + (tour.getChildPriceCents() * children)
                    + (tour.getInfantPriceCents() * infants);
            }
            case DAY_TRIP -> {
                var trip = dayTripRepo.findById(refId)
                        .orElseThrow(() -> BusinessException.notFound("DayTrip"));
                if (trip.getStatus() != null && trip.getStatus().name().equals("INACTIVE"))
                    throw BusinessException.badRequest("PRODUCT_INACTIVE", "Day trip is not available for booking");
                int adults   = options != null && options.get("paxAdults")   instanceof Number n ? n.intValue() : 1;
                int children = options != null && options.get("paxChildren") instanceof Number n ? n.intValue() : 0;
                int childPrice = trip.getChildPriceCents() > 0 ? trip.getChildPriceCents() : trip.getAdultPriceCents();
                yield (trip.getAdultPriceCents() * adults) + (childPrice * children);
            }
            case HOTEL -> throw BusinessException.badRequest("NOT_SUPPORTED",
                    "Hotel booking not yet supported");
        };
    }
}
