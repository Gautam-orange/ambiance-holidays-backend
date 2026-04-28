package com.ambianceholidays.api.booking;

import com.ambianceholidays.api.booking.dto.BookingResponse;
import com.ambianceholidays.common.dto.ApiResponse;
import com.ambianceholidays.domain.user.UserRepository;
import com.ambianceholidays.security.SecurityPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/enquiries")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS')")
@RequiredArgsConstructor
public class AdminEnquiryController {

    private final BookingService bookingService;
    private final UserRepository userRepo;

    @PostMapping("/{id}/convert")
    public ApiResponse<BookingResponse> convert(@PathVariable UUID id,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        var actor = userRepo.findById(principal.getUserId()).orElseThrow();
        return bookingService.convertEnquiry(id, actor);
    }

    @PostMapping("/{id}/decline")
    public ApiResponse<BookingResponse> decline(@PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        var actor = userRepo.findById(principal.getUserId()).orElseThrow();
        String reason = body != null ? body.getOrDefault("reason", "") : "";
        return bookingService.declineEnquiry(id, reason, actor);
    }
}
