package com.ambianceholidays.api.booking;

import com.ambianceholidays.api.booking.dto.*;
import com.ambianceholidays.api.pdf.PdfService;
import com.ambianceholidays.common.dto.ApiResponse;
import com.ambianceholidays.domain.booking.Booking;
import com.ambianceholidays.domain.booking.BookingRepository;
import com.ambianceholidays.domain.booking.BookingStatus;
import com.ambianceholidays.domain.user.UserRepository;
import com.ambianceholidays.exception.BusinessException;
import com.ambianceholidays.security.SecurityPrincipal;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/bookings")
public class AdminBookingController {

    private final BookingService bookingService;
    private final BookingRepository bookingRepo;
    private final PdfService pdfService;
    private final UserRepository userRepo;

    public AdminBookingController(BookingService bookingService, BookingRepository bookingRepo,
            PdfService pdfService, UserRepository userRepo) {
        this.bookingService = bookingService;
        this.bookingRepo = bookingRepo;
        this.pdfService = pdfService;
        this.userRepo = userRepo;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS','FLEET_MANAGER')")
    public ApiResponse<List<BookingResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) String agentId,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) Boolean enquiry,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return bookingService.list(search, status, agentId, dateFrom, dateTo, enquiry, page, size, null);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS','FLEET_MANAGER')")
    public ApiResponse<BookingResponse> get(@PathVariable UUID id) {
        return bookingService.get(id, null);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS')")
    public ApiResponse<BookingResponse> updateStatus(@PathVariable UUID id,
            @RequestParam BookingStatus status,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        var actor = userRepo.findById(principal.getUserId()).orElseThrow();
        return bookingService.updateStatus(id, status, actor);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS')")
    public ApiResponse<BookingResponse> cancel(@PathVariable UUID id,
            @RequestBody(required = false) java.util.Map<String, String> body,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        var actor = userRepo.findById(principal.getUserId()).orElseThrow();
        String reason = body != null ? body.getOrDefault("reason", "") : "";
        String byType = body != null ? body.getOrDefault("cancelledByType", "ADMIN") : "ADMIN";
        return bookingService.cancel(id, reason, byType, actor);
    }


    @GetMapping("/{id}/invoice")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS','FLEET_MANAGER')")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable UUID id) {
        Booking booking = bookingRepo.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Booking"));
        byte[] pdf = pdfService.generateInvoice(booking);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"invoice-" + booking.getReference() + ".pdf\"")
                .body(pdf);
    }

    @GetMapping("/{id}/voucher")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS','FLEET_MANAGER')")
    public ResponseEntity<byte[]> downloadVoucher(@PathVariable UUID id) {
        Booking booking = bookingRepo.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Booking"));
        byte[] pdf = pdfService.generateVoucher(booking);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"voucher-" + booking.getReference() + ".pdf\"")
                .body(pdf);
    }
}
