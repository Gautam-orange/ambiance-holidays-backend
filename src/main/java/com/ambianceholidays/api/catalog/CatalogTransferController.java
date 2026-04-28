package com.ambianceholidays.api.catalog;

import com.ambianceholidays.api.transfer.TransferService;
import com.ambianceholidays.api.transfer.dto.TransferRouteResponse;
import com.ambianceholidays.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
