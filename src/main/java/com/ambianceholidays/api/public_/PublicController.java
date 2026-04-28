package com.ambianceholidays.api.public_;

import com.ambianceholidays.api.notification.NotificationService;
import com.ambianceholidays.common.dto.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public")
public class PublicController {

    private final NotificationService notificationService;

    public PublicController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/contact")
    public ApiResponse<String> contact(@Valid @RequestBody ContactRequest req) {
        String body = String.format("""
                New contact form submission:

                Name: %s
                Email: %s
                Phone: %s
                Company: %s

                Message:
                %s
                """,
                req.name(), req.email(),
                req.phone() != null ? req.phone() : "—",
                req.company() != null ? req.company() : "—",
                req.message());

        notificationService.sendEmail(
                "info@ambianceholidays.mu",
                "New Contact: " + req.name(),
                body, null, null);

        return ApiResponse.ok("Message received. We will get back to you shortly.");
    }

    @PostMapping("/newsletter")
    public ApiResponse<String> newsletter(@Valid @RequestBody NewsletterRequest req) {
        notificationService.sendEmail(
                "info@ambianceholidays.mu",
                "Newsletter Subscription: " + req.email(),
                "New newsletter subscription from: " + req.email(), null, null);
        return ApiResponse.ok("You have been subscribed to our newsletter.");
    }

    public record ContactRequest(
            @NotBlank @Size(max = 200) String name,
            @NotBlank @Email String email,
            String phone,
            String company,
            @NotBlank @Size(max = 2000) String message
    ) {}

    public record NewsletterRequest(
            @NotBlank @Email String email
    ) {}
}
