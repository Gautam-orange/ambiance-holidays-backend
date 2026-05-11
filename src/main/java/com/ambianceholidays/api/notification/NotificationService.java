package com.ambianceholidays.api.notification;

import com.ambianceholidays.api.pdf.PdfService;
import com.ambianceholidays.domain.agent.Agent;
import com.ambianceholidays.domain.booking.Booking;
import com.ambianceholidays.domain.notification.Notification;
import com.ambianceholidays.domain.notification.NotificationChannel;
import com.ambianceholidays.domain.notification.NotificationRepository;
import com.ambianceholidays.domain.notification.NotificationStatus;
import com.ambianceholidays.domain.user.User;
import com.ambianceholidays.domain.user.UserRepository;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notifRepo;
    private final JavaMailSender mailSender;
    private final PdfService pdfService;
    private final UserRepository userRepo;

    public NotificationService(NotificationRepository notifRepo, JavaMailSender mailSender,
            PdfService pdfService, UserRepository userRepo) {
        this.notifRepo = notifRepo;
        this.mailSender = mailSender;
        this.pdfService = pdfService;
        this.userRepo = userRepo;
    }

    /** Cached recipient list for ops notifications. Reloaded per call so newly
     *  promoted admins receive subsequent emails without a restart. */
    private java.util.List<String> adminRecipients() {
        return userRepo.findActiveAdmins().stream()
                .map(User::getEmail).filter(e -> e != null && !e.isBlank())
                .toList();
    }

    @Async
    public void sendBookingConfirmation(Booking booking) {
        String subject = "Booking Confirmed — " + booking.getReference();
        String currency = pdfService.resolveCurrency(booking);
        String moneySymbol = pdfService.currencySymbol(currency);
        String body = String.format("""
                Dear %s,

                Your booking %s has been confirmed.
                Service Date: %s
                Total: %s%,.2f %s

                Your invoice and travel voucher are attached to this email. You
                can also download them any time from "My Bookings" in the portal.

                Thank you for choosing Ambiance Holidays.
                """,
                booking.getCustomer().getFirstName(),
                booking.getReference(),
                booking.getServiceDate(),
                moneySymbol,
                booking.getTotalCents() / 100.0,
                currency);

        try {
            byte[] invoicePdf = pdfService.generateInvoice(booking, currency);
            byte[] voucherPdf = pdfService.generateVoucher(booking);
            sendEmailWithAttachments(
                    booking.getCustomer().getEmail(), subject, body, booking.getId(),
                    "invoice-" + booking.getReference() + ".pdf", invoicePdf,
                    "voucher-" + booking.getReference() + ".pdf", voucherPdf);
        } catch (Exception e) {
            log.warn("Failed to attach booking PDFs for {}: {} — falling back to plain confirmation",
                    booking.getReference(), e.getMessage());
            sendEmail(booking.getCustomer().getEmail(), subject, body, booking.getId(), null);
        }
    }

    @Async
    public void sendAgentApproval(Agent agent) {
        var user = agent.getUser();
        String subject = "Your Ambiance Holidays partner account is approved";
        String body = String.format("""
                Dear %s,

                Good news — your partner account for %s has been approved by our team.
                You can now sign in and start booking on behalf of your customers.

                Sign in: https://ambianceholidays.ciadmin.in/auth/login
                Email:   %s

                Welcome to the Ambiance Holidays B2B network.
                """,
                user.getFirstName(),
                agent.getCompanyName(),
                user.getEmail());

        sendEmail(user.getEmail(), subject, body, null, user.getId());
    }

    /** Synchronously extract primitives on the caller's thread, then dispatch
     *  the actual email send asynchronously. Avoids LazyInitializationException
     *  / JdbcValuesSourceProcessingState corruption when an @Async thread would
     *  otherwise race with the controller's BookingResponse.from(b) lazy loads
     *  on the same Hibernate session. */
    public void sendBookingCancellation(Booking booking) {
        sendBookingCancellation(
                booking.getCustomer().getEmail(),
                booking.getCustomer().getFirstName(),
                booking.getReference(),
                booking.getCancelReason(),
                booking.getId());
    }

    @Async
    public void sendBookingCancellation(String email, String firstName, String reference,
            String cancelReason, UUID bookingId) {
        String subject = "Booking Cancelled — " + reference;
        String body = String.format("""
                Dear %s,

                Your booking %s has been cancelled.
                %s

                If you have questions, please contact us.
                """,
                firstName,
                reference,
                cancelReason != null ? "Reason: " + cancelReason : "");

        sendEmail(email, subject, body, bookingId, null);
    }

    /**
     * Send an email with two PDF attachments (invoice + voucher). Records a
     * single Notification row tagged to the booking so retries / audits are
     * straightforward. Falls back to logging only — never throws to the caller.
     */
    @Async
    public void sendEmailWithAttachments(String recipient, String subject, String body,
            UUID bookingId,
            String invoiceFileName, byte[] invoicePdf,
            String voucherFileName, byte[] voucherPdf) {
        Notification notif = new Notification();
        notif.setChannel(NotificationChannel.EMAIL);
        notif.setRecipient(recipient);
        notif.setSubject(subject);
        notif.setBody(body);
        notif.setBookingId(bookingId);

        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(body);
            if (invoicePdf != null && invoicePdf.length > 0) {
                helper.addAttachment(invoiceFileName, new ByteArrayResource(invoicePdf));
            }
            if (voucherPdf != null && voucherPdf.length > 0) {
                helper.addAttachment(voucherFileName, new ByteArrayResource(voucherPdf));
            }
            mailSender.send(mime);
            notif.setStatus(NotificationStatus.SENT);
            notif.setSentAt(Instant.now());
        } catch (Exception e) {
            log.warn("Failed to send email with attachments to {}: {}", recipient, e.getMessage());
            notif.setStatus(NotificationStatus.FAILED);
            notif.setErrorMsg(e.getMessage());
        }
        notifRepo.save(notif);
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

    // ── Lifecycle notifications added for cross-platform email coverage ──────

    /** Agent-side acknowledgement on signup ("we got your application"). */
    @Async
    public void sendAgentRegistrationReceipt(String email, String firstName, String companyName) {
        String subject = "We received your Ambiance Holidays partner application";
        String body = String.format("""
                Dear %s,

                Thanks for applying to join the Ambiance Holidays B2B network with
                %s.

                Our team reviews each partner application within 1–2 business days.
                You'll receive a follow-up email as soon as your account is
                approved (or if we need any additional information).

                — Ambiance Holidays
                """,
                firstName, companyName);
        sendEmail(email, subject, body, null, null);
    }

    /** Admin-side notification when a new agent signs up. Goes to every active
     *  SUPER_ADMIN / ADMIN_OPS user so someone always sees the request. */
    @Async
    public void sendAdminAgentRegistration(String agentEmail, String firstName, String lastName,
            String companyName, String country) {
        String subject = "New partner application — " + companyName;
        String body = String.format("""
                A new partner has applied for an account.

                Company:  %s
                Contact:  %s %s
                Email:    %s
                Country:  %s

                Open the admin Agent Management screen to review and approve.
                """,
                companyName, firstName, lastName, agentEmail, country);
        for (String to : adminRecipients()) sendEmail(to, subject, body, null, null);
    }

    /** Admin-side notification on approve / reject so the rest of the ops team
     *  stays in the loop. */
    @Async
    public void sendAdminAgentApprovalAction(String actionLabel, String companyName,
            String actorEmail) {
        String subject = "Partner " + actionLabel + " — " + companyName;
        String body = String.format("""
                Action: %s
                Partner: %s
                By:     %s
                """,
                actionLabel, companyName, actorEmail);
        for (String to : adminRecipients()) sendEmail(to, subject, body, null, null);
    }

    /** Admin-side notification on every new booking. */
    @Async
    public void sendAdminBookingCreated(String reference, String customerName, String customerEmail,
            String agentName, java.time.LocalDate serviceDate, int totalCents, String currency,
            UUID bookingId) {
        String subject = "New booking — " + reference;
        String body = String.format("""
                A new booking was created.

                Reference:   %s
                Customer:    %s (%s)
                Agent:       %s
                Service:     %s
                Total:       %s %,.2f
                """,
                reference, customerName, customerEmail,
                agentName != null ? agentName : "(direct customer)",
                serviceDate != null ? serviceDate.toString() : "—",
                currency != null ? currency : "USD",
                totalCents / 100.0);
        for (String to : adminRecipients()) sendEmail(to, subject, body, bookingId, null);
    }

    /** Admin-side notification on every booking cancellation. */
    @Async
    public void sendAdminBookingCancelled(String reference, String customerName, String reason,
            UUID bookingId) {
        String subject = "Booking cancelled — " + reference;
        String body = String.format("""
                A booking was cancelled.

                Reference: %s
                Customer:  %s
                Reason:    %s
                """,
                reference, customerName, reason != null && !reason.isBlank() ? reason : "—");
        for (String to : adminRecipients()) sendEmail(to, subject, body, bookingId, null);
    }

    /** Same-day reminder sent on the morning of the booking's service date. */
    @Async
    public void sendBookingDayReminder(String email, String firstName, String reference,
            java.time.LocalDate serviceDate, UUID bookingId) {
        String subject = "Reminder: your booking is today — " + reference;
        String body = String.format("""
                Dear %s,

                A quick reminder that your booking %s is today (%s).

                Have a great trip — and don't hesitate to reach out if anything
                comes up on the day.

                — Ambiance Holidays
                """,
                firstName, reference, serviceDate);
        sendEmail(email, subject, body, bookingId, null);
    }

    /** 15-minutes-before-pickup nudge so the agent / customer is ready when
     *  the driver arrives. */
    @Async
    public void sendImminentBookingReminder(String email, String firstName, String reference,
            String pickupLocation, java.time.Instant startAt, UUID bookingId) {
        String subject = "Your pickup starts in 15 minutes — " + reference;
        String body = String.format("""
                Dear %s,

                Heads-up — your booking %s is scheduled to begin in about 15
                minutes.

                Pickup: %s
                Time:   %s

                Please be ready at the pickup point.

                — Ambiance Holidays
                """,
                firstName, reference,
                pickupLocation != null ? pickupLocation : "(see booking detail)",
                startAt);
        sendEmail(email, subject, body, bookingId, null);
    }
}
