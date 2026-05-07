package com.ambianceholidays.domain.newsletter;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NewsletterSubscriberRepository extends JpaRepository<NewsletterSubscriber, UUID> {
    Optional<NewsletterSubscriber> findByEmail(String email);
    Optional<NewsletterSubscriber> findByConfirmationTokenHash(String confirmationTokenHash);
}
