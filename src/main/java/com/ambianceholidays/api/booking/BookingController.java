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
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final BookingRepository bookingRepo;
    private final PdfService pdfService;
    private final UserRepository userRepo;

    public BookingController(BookingService bookingService, BookingRepository bookingRepo,
            PdfService pdfService, UserRepository userRepo) {
        this.bookingService = bookingService;
        this.bookingRepo = bookingRepo;
        this.pdfService = pdfService;
        this.userRepo = userRepo;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BookingResponse> checkout(
            @RequestHeader(value = "X-Cart-Id", required = false) String cartId,
            @AuthenticationPrincipal SecurityPrincipal principal,
            @Valid @RequestBody CheckoutRequest req) {
        String sessionKey = principal != null ? "user:" + principal.getUserId()
                : (cartId != null && !cartId.isBlank() ? "guest:" + cartId : "guest:anonymous");
        var actor = principal != null ? userRepo.findById(principal.getUserId()).orElse(null) : null;
        return bookingService.checkout(sessionKey, req, actor);
    }

    @GetMapping
    public ApiResponse<List<BookingResponse>> list(
            @AuthenticationPrincipal SecurityPrincipal principal,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var actor = userRepo.findById(principal.getUserId()).orElseThrow();
        return bookingService.list(search, status, null, null, null, null, page, size, actor);
    }

    @GetMapping("/{id}")
    public ApiResponse<BookingResponse> get(@PathVariable UUID id,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        var actor = userRepo.findById(principal.getUserId()).orElseThrow();
        return bookingService.get(id, actor);
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<BookingResponse> cancel(@PathVariable UUID id,
            @RequestParam(required = false, defaultValue = "") String reason,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        var actor = userRepo.findById(principal.getUserId()).orElseThrow();
        return bookingService.cancel(id, reason, "CUSTOMER", actor);
    }

    @GetMapping("/{id}/invoice")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable UUID id,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        var actor = userRepo.findById(principal.getUserId()).orElseThrow();
        Booking booking = bookingRepo.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Booking"));
        bookingService.get(id, actor); // access check
        byte[] pdf = pdfService.generateInvoice(booking);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"invoice-" + booking.getReference() + ".pdf\"")
                .body(pdf);
    }
}
