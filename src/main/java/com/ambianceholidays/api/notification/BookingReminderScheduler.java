package com.ambianceholidays.api.notification;

import com.ambianceholidays.domain.booking.Booking;
import com.ambianceholidays.domain.booking.BookingItem;
import com.ambianceholidays.domain.booking.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Sends booking reminder emails on a schedule:
 *
 *   - <b>Day reminder:</b>   morning of the booking's service date
 *   - <b>Imminent reminder:</b> 15 minutes before the earliest item startAt
 *
 * Both reminders are deduped via timestamp flags on the Booking entity so a
 * scheduler restart or overlapping run can't spam recipients.
 */
@Component
public class BookingReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(BookingReminderScheduler.class);
    private static final ZoneId MAURITIUS = ZoneId.of("Indian/Mauritius");

    private final BookingRepository bookingRepo;
    private final NotificationService notificationService;

    public BookingReminderScheduler(BookingRepository bookingRepo,
            NotificationService notificationService) {
        this.bookingRepo = bookingRepo;
        this.notificationService = notificationService;
    }

    /** Runs every 15 minutes — sends the same-day reminder once per booking. */
    @Scheduled(cron = "0 */15 * * * *", zone = "Indian/Mauritius")
    @Transactional
    public void dispatchDayReminders() {
        LocalDate today = LocalDate.now(MAURITIUS);
        var due = bookingRepo.findDueForDayReminder(today);
        for (Booking b : due) {
            try {
                notificationService.sendBookingDayReminder(
                        b.getCustomer().getEmail(),
                        b.getCustomer().getFirstName(),
                        b.getReference(),
                        b.getServiceDate(),
                        b.getId());
                b.setReminderDaySentAt(Instant.now());
            } catch (Exception e) {
                log.warn("Day reminder failed for {}: {}", b.getReference(), e.getMessage());
            }
        }
        if (!due.isEmpty()) bookingRepo.saveAll(due);
    }

    /** Runs every minute — catches bookings whose first pickup is ~15 min out. */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void dispatchImminentReminders() {
        Instant from = Instant.now().plus(14, ChronoUnit.MINUTES);
        Instant to = Instant.now().plus(16, ChronoUnit.MINUTES);
        var due = bookingRepo.findDueForImminentReminder(from, to);
        for (Booking b : due) {
            Optional<BookingItem> earliest = b.getItems().stream()
                    .filter(i -> i.getStartAt() != null)
                    .min((a, c) -> a.getStartAt().compareTo(c.getStartAt()));
            if (earliest.isEmpty()) continue;
            try {
                notificationService.sendImminentBookingReminder(
                        b.getCustomer().getEmail(),
                        b.getCustomer().getFirstName(),
                        b.getReference(),
                        earliest.get().getPickupLocation(),
                        earliest.get().getStartAt(),
                        b.getId());
                b.setReminderImminentSentAt(Instant.now());
            } catch (Exception e) {
                log.warn("Imminent reminder failed for {}: {}", b.getReference(), e.getMessage());
            }
        }
        if (!due.isEmpty()) bookingRepo.saveAll(due);
    }
}
