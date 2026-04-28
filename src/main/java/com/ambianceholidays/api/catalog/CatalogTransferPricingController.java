package com.ambianceholidays.api.catalog;

import com.ambianceholidays.api.transferpricing.TransferPricingService;
import com.ambianceholidays.api.transferpricing.dto.TransferPriceQuoteResponse;
import com.ambianceholidays.api.transferpricing.dto.TransferPricingTierResponse;
import com.ambianceholidays.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalog/transfer-pricing")
public class CatalogTransferPricingController {

    private final TransferPricingService service;

    public CatalogTransferPricingController(TransferPricingService service) {
        this.service = service;
    }

    /** Returns all active pricing tiers (for frontend display). */
    @GetMapping
    public ApiResponse<List<TransferPricingTierResponse>> tiers() {
        return service.listActive();
    }

    /** Returns the price for a given straight-line distance in km. */
    @GetMapping("/quote")
    public ApiResponse<TransferPriceQuoteResponse> quote(@RequestParam("distanceKm") int distanceKm) {
        return service.quoteByDistance(distanceKm);
    }
}
