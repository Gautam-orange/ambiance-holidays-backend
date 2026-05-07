package com.ambianceholidays.api.newsletter;

import com.ambianceholidays.common.dto.ApiResponse;
import com.ambianceholidays.domain.newsletter.NewsletterSubscriber;
import com.ambianceholidays.domain.newsletter.NewsletterSubscriberRepository;
import com.ambianceholidays.exception.BusinessException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Admin newsletter management. Backs the AdminNewsletterPage which previously
 * called these paths but had no controller — the page rendered "No subscribers
 * yet" forever and 500'd on actions.
 *
 * Endpoints:
 *   GET    /admin/newsletter                list active + unconfirmed subscribers
 *   GET    /admin/newsletter/export         CSV of every subscriber
 *   DELETE /admin/newsletter/{id}           soft unsubscribe
 */
@RestController
@RequestMapping("/admin/newsletter")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS')")
public class AdminNewsletterController {

    private final NewsletterSubscriberRepository repo;

    public AdminNewsletterController(NewsletterSubscriberRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> list() {
        List<NewsletterSubscriber> all = repo.findAll(Sort.by(Sort.Direction.DESC, "subscribedAt"));
        // The frontend reads either `data.content` (paginated shape) or `data` (list shape) —
        // we wrap in `content` so the page works without further changes.
        // HashMap (not Map.of) because confirmedAt can be null and Map.of rejects nulls.
        List<Map<String, Object>> rows = all.stream().<Map<String, Object>>map(s -> {
            java.util.HashMap<String, Object> row = new java.util.HashMap<>();
            row.put("id", s.getId().toString());
            row.put("email", s.getEmail());
            row.put("subscribedAt", s.getSubscribedAt() != null ? s.getSubscribedAt().toString() : "");
            row.put("confirmedAt", s.getConfirmedAt() != null ? s.getConfirmedAt().toString() : null);
            row.put("isActive", s.isActive());
            return row;
        }).toList();
        return ApiResponse.ok(Map.of("content", rows, "total", rows.size()));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv() {
        StringBuilder sb = new StringBuilder("Email,Subscribed,Confirmed,Active\n");
        DateTimeFormatter fmt = DateTimeFormatter.ISO_INSTANT;
        for (NewsletterSubscriber s : repo.findAll(Sort.by(Sort.Direction.DESC, "subscribedAt"))) {
            sb.append('"').append(s.getEmail().replace("\"", "\"\"")).append("\",")
              .append(s.getSubscribedAt() != null ? fmt.format(s.getSubscribedAt()) : "").append(',')
              .append(s.getConfirmedAt() != null ? fmt.format(s.getConfirmedAt()) : "").append(',')
              .append(s.isActive()).append('\n');
        }
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"newsletter-subscribers.csv\"")
                .body(bytes);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> unsubscribe(@PathVariable UUID id) {
        NewsletterSubscriber s = repo.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Subscriber"));
        // Soft unsubscribe — keep the row for audit / re-confirm flow.
        s.setActive(false);
        s.setUnsubscribedAt(Instant.now());
        repo.save(s);
        return ApiResponse.ok(null);
    }
}
