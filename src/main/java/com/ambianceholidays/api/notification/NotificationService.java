package com.ambianceholidays.api.notification;

import com.ambianceholidays.domain.booking.Booking;
import com.ambianceholidays.domain.notification.Notification;
import com.ambianceholidays.domain.notification.NotificationChannel;
import com.ambianceholidays.domain.notification.NotificationRepository;
import com.ambianceholidays.domain.notification.NotificationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notifRepo;
    private final JavaMailSender mailSender;

    public NotificationService(NotificationRepository notifRepo, JavaMailSender mailSender) {
        this.notifRepo = notifRepo;
        this.mailSender = mailSender;
    }

    @Async
    public void sendBookingConfirmation(Booking booking) {
        String subject = "Booking Confirmed — " + booking.getReference();
        String body = String.format("""
                Dear %s,

                Your booking %s has been confirmed.
                Service Date: %s
                Total: Rs %.0f

                Thank you for choosing Ambiance Holidays.
                """,
                booking.getCustomer().getFirstName(),
                booking.getReference(),
                booking.getServiceDate(),
                booking.getTotalCents() / 100.0);

        sendEmail(booking.getCustomer().getEmail(), subject, body,
                booking.getId(), null);
    }

    @Async
    public void sendBookingCancellation(Booking booking) {
        String subject = "Booking Cancelled — " + booking.getReference();
        String body = String.format("""
                Dear %s,

                Your booking %s has been cancelled.
                %s

                If you have questions, please contact us.
                """,
                booking.getCustomer().getFirstName(),
                booking.getReference(),
                booking.getCancelReason() != null ? "Reason: " + booking.getCancelReason() : "");

        sendEmail(booking.getCustomer().getEmail(), subject, body,
                booking.getId(), null);
    }

    @Async
    public void sendEmail(String recipient, String subject, String body,
            UUID bookingId, UUID userId) {
        Notification notif = new Notification();
        notif.setChannel(NotificationChannel.EMAIL);
        notif.setRecipient(recipient);
        notif.setSubject(subject);
        notif.setBody(body);
        notif.setBookingId(bookingId);
        notif.setUserId(userId);

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(recipient);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
            notif.setStatus(NotificationStatus.SENT);
            notif.setSentAt(Instant.now());
        } catch (Exception e) {
            log.warn("Failed to send email to {}: {}", recipient, e.getMessage());
            notif.setStatus(NotificationStatus.FAILED);
            notif.setErrorMsg(e.getMessage());
        }

        notifRepo.save(notif);
    }
}
