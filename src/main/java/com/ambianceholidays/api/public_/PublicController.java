package com.ambianceholidays.api.public_;

import com.ambianceholidays.api.notification.NotificationService;
import com.ambianceholidays.common.dto.ApiResponse;
import com.ambianceholidays.domain.newsletter.NewsletterSubscriber;
import com.ambianceholidays.domain.newsletter.NewsletterSubscriberRepository;
import com.ambianceholidays.exception.BusinessException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/public")
public class PublicController {

    private final NotificationService notificationService;
    private final NewsletterSubscriberRepository newsletterRepo;

    public PublicController(NotificationService notificationService,
            NewsletterSubscriberRepository newsletterRepo) {
        this.notificationService = notificationService;
        this.newsletterRepo = newsletterRepo;
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
        // Persist (or reactivate) the subscriber row so the admin newsletter
        // page actually has data to show.
        String email = req.email().toLowerCase().trim();
        NewsletterSubscriber sub = newsletterRepo.findByEmail(email)
                .orElseGet(() -> {
                    NewsletterSubscriber s = new NewsletterSubscriber();
                    s.setEmail(email);
                    s.setSubscribedAt(Instant.now());
                    return s;
                });
        sub.setActive(true);
        sub.setUnsubscribedAt(null);
        // For now we auto-confirm — full double-opt-in (token + email) can be
        // wired later via the /confirm/{token} endpoint below.
        if (sub.getConfirmedAt() == null) sub.setConfirmedAt(Instant.now());
        newsletterRepo.save(sub);

        notificationService.sendEmail(
                "info@ambianceholidays.mu",
                "Newsletter Subscription: " + email,
                "New newsletter subscription from: " + email, null, null);
        return ApiResponse.ok("You have been subscribed to our newsletter.");
    }

    /**
     * Mark a subscriber as confirmed via an email token. Public — no auth.
     * Called from `/newsletter/confirm/:token` on the SPA. The token format /
     * generation is left to the email-template renderer; for now we treat the
     * token as the subscriber's id to keep the contract straightforward.
     */
    @PostMapping("/newsletter/confirm/{token}")
    public ApiResponse<String> confirmNewsletter(@PathVariable String token) {
        // Lookup either by stored hash (preferred) or fall back to id.
        NewsletterSubscriber sub = newsletterRepo.findByConfirmationTokenHash(token).orElse(null);
        if (sub == null) {
            try {
                sub = newsletterRepo.findById(java.util.UUID.fromString(token)).orElse(null);
            } catch (IllegalArgumentException ignore) { /* token isn't a uuid */ }
        }
        if (sub == null) throw BusinessException.notFound("Subscription");
        if (sub.getConfirmedAt() == null) sub.setConfirmedAt(Instant.now());
        sub.setActive(true);
        sub.setConfirmationTokenHash(null);
        newsletterRepo.save(sub);
        return ApiResponse.ok("Subscription confirmed.");
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
