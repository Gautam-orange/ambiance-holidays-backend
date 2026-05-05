package com.ambianceholidays.api.notification;

import com.ambianceholidays.api.pdf.PdfService;
import com.ambianceholidays.domain.agent.Agent;
import com.ambianceholidays.domain.booking.Booking;
import com.ambianceholidays.domain.notification.Notification;
import com.ambianceholidays.domain.notification.NotificationChannel;
import com.ambianceholidays.domain.notification.NotificationRepository;
import com.ambianceholidays.domain.notification.NotificationStatus;
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

    public NotificationService(NotificationRepository notifRepo, JavaMailSender mailSender,
            PdfService pdfService) {
        this.notifRepo = notifRepo;
        this.mailSender = mailSender;
        this.pdfService = pdfService;
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
}
