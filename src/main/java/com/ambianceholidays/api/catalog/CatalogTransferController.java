package com.ambianceholidays.api.catalog;

import com.ambianceholidays.api.transfer.TransferService;
import com.ambianceholidays.api.transfer.dto.TransferRouteResponse;
import com.ambianceholidays.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/catalog/transfers")
public class CatalogTransferController {

    private final TransferService service;

    public CatalogTransferController(TransferService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<TransferRouteResponse>> search(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return service.search(from, to);
    }

    /**
     * Single transfer route by id — backs the public TransferDetails page.
     * Was 500'ing because the controller only had a list endpoint.
     */
    @GetMapping("/{id}")
    public ApiResponse<TransferRouteResponse> getById(@PathVariable UUID id) {
        return service.getById(id);
    }
}
