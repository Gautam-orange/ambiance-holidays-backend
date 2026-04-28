package com.ambianceholidays.domain.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByBookingIdOrderByCreatedAtDesc(UUID bookingId);
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
