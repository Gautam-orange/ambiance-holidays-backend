package com.ambianceholidays.domain.newsletter;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * A single row in `newsletter_subscribers`. The schema was created in V1 +
 * V3 (double-opt-in columns) but no JPA entity existed, leaving the admin
 * newsletter page broken — this fills the gap.
 */
@Entity
@Table(name = "newsletter_subscribers")
public class NewsletterSubscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "subscribed_at", nullable = false)
    private Instant subscribedAt = Instant.now();

    @Column(name = "unsubscribed_at")
    private Instant unsubscribedAt;

    @Column(name = "confirmation_token_hash", columnDefinition = "TEXT")
    private String confirmationTokenHash;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getSubscribedAt() { return subscribedAt; }
    public void setSubscribedAt(Instant subscribedAt) { this.subscribedAt = subscribedAt; }
    public Instant getUnsubscribedAt() { return unsubscribedAt; }
    public void setUnsubscribedAt(Instant unsubscribedAt) { this.unsubscribedAt = unsubscribedAt; }
    public String getConfirmationTokenHash() { return confirmationTokenHash; }
    public void setConfirmationTokenHash(String confirmationTokenHash) { this.confirmationTokenHash = confirmationTokenHash; }
    public Instant getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(Instant confirmedAt) { this.confirmedAt = confirmedAt; }
}
