package com.ambianceholidays.api.pdf;

import com.ambianceholidays.domain.booking.Booking;
import com.ambianceholidays.domain.booking.BookingItem;
import com.ambianceholidays.domain.payment.Payment;
import com.ambianceholidays.domain.payment.PaymentRepository;
import com.ambianceholidays.domain.payment.PaymentStatus;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;

@Service
public class PdfService {

    private final PaymentRepository paymentRepository;

    public PdfService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 20, Font.BOLD, new Color(15, 23, 42));
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(71, 85, 105));
    private static final Font BODY_FONT = new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(51, 65, 85));
    private static final Font LABEL_FONT = new Font(Font.HELVETICA, 8, Font.BOLD, new Color(100, 116, 139));
    private static final Color BRAND_COLOR = new Color(14, 165, 233);
    private static final Color LIGHT_BG = new Color(248, 250, 252);

    public byte[] generateInvoice(Booking booking) {
        return generateInvoice(booking, resolveCurrency(booking));
    }

    /**
     * Resolve the currency a booking should be displayed in. Looks up its most-recent
     * SUCCEEDED payment first; falls back to any payment row, then to USD. Public so
     * NotificationService can pick the right symbol for confirmation emails too.
     */
    public String resolveCurrency(Booking booking) {
        return paymentRepository.findByBookingId(booking.getId()).stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCEEDED)
                .max(Comparator.comparing(Payment::getCreatedAt, Comparator.nullsFirst(Comparator.naturalOrder())))
                .map(Payment::getCurrency)
                .orElseGet(() -> paymentRepository.findByBookingId(booking.getId()).stream()
                        .max(Comparator.comparing(Payment::getCreatedAt, Comparator.nullsFirst(Comparator.naturalOrder())))
                        .map(Payment::getCurrency)
                        .orElse("USD"));
    }

    public byte[] generateInvoice(Booking booking, String currency) {
        final String currencyCode = currency != null && !currency.isBlank() ? currency.toUpperCase() : "USD";
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            // Header
            Paragraph title = new Paragraph("AMBIANCE HOLIDAYS", TITLE_FONT);
            title.setAlignment(Element.ALIGN_LEFT);
            doc.add(title);

            Paragraph subtitle = new Paragraph("B2B Travel Platform — Mauritius", BODY_FONT);
            subtitle.setSpacingAfter(4);
            doc.add(subtitle);

            addLine(doc);

            // Invoice meta
            PdfPTable meta = new PdfPTable(2);
            meta.setWidthPercentage(100);
            meta.setWidths(new float[]{1, 1});
            meta.setSpacingBefore(12);
            meta.setSpacingAfter(12);

            addMetaCell(meta, "INVOICE NUMBER", "INV-" + booking.getReference(), true);
            addMetaCell(meta, "DATE", DateTimeFormatter.ofPattern("dd MMM yyyy")
                    .withZone(ZoneId.of("Indian/Mauritius"))
                    .format(booking.getCreatedAt()), false);
            addMetaCell(meta, "BOOKING REFERENCE", booking.getReference(), true);
            addMetaCell(meta, "SERVICE DATE", booking.getServiceDate().toString(), false);
            addMetaCell(meta, "STATUS", booking.getStatus().name(), true);
            addMetaCell(meta, "AGENT", booking.getAgent() != null ? booking.getAgent().getCompanyName() : "Direct", false);
            doc.add(meta);

            // Bill to
            Paragraph billTo = new Paragraph("BILL TO", LABEL_FONT);
            billTo.setSpacingBefore(8);
            doc.add(billTo);

            Paragraph customer = new Paragraph(
                    booking.getCustomer().getFirstName() + " " + booking.getCustomer().getLastName()
                    + "\n" + booking.getCustomer().getEmail(), BODY_FONT);
            customer.setSpacingAfter(12);
            doc.add(customer);

            // Items table
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 1, 1, 1});
            table.setSpacingBefore(8);

            addTableHeader(table, "DESCRIPTION");
            addTableHeader(table, "QTY");
            addTableHeader(table, "UNIT PRICE");
            addTableHeader(table, "TOTAL");

            for (BookingItem item : booking.getItems()) {
                addTableCell(table, item.getItemType().name().replace("_", " "));
                addTableCell(table, String.valueOf(item.getQuantity()));
                addTableCell(table, formatMoney(item.getUnitPriceCents(), currencyCode));
                addTableCell(table, formatMoney(item.getTotalCents(), currencyCode));
            }
            doc.add(table);

            // Totals
            addLine(doc);
            PdfPTable totals = new PdfPTable(2);
            totals.setWidthPercentage(50);
            totals.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totals.setSpacingBefore(8);

            addTotalRow(totals, "Subtotal", formatMoney(booking.getSubtotalCents(), currencyCode));
            if (booking.getMarkupCents() > 0)
                addTotalRow(totals, "Markup (" + booking.getMarkupRate() + "%)", formatMoney(booking.getMarkupCents(), currencyCode));
            addTotalRow(totals, "VAT (" + booking.getVatRate() + "%)", formatMoney(booking.getVatCents(), currencyCode));

            PdfPCell totalLabel = new PdfPCell(new Phrase("TOTAL DUE (" + currencyCode + ")", new Font(Font.HELVETICA, 10, Font.BOLD)));
            totalLabel.setBorder(Rectangle.TOP);
            totalLabel.setPadding(6);
            PdfPCell totalValue = new PdfPCell(new Phrase(formatMoney(booking.getTotalCents(), currencyCode),
                    new Font(Font.HELVETICA, 10, Font.BOLD, BRAND_COLOR)));
            totalValue.setBorder(Rectangle.TOP);
            totalValue.setPadding(6);
            totalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totals.addCell(totalLabel);
            totals.addCell(totalValue);
            doc.add(totals);

            // Footer
            addLine(doc);
            Paragraph footer = new Paragraph(
                    "Thank you for choosing Ambiance Holidays. " +
                    "For enquiries: info@ambianceholidays.mu | +230 5XXX XXXX", LABEL_FONT);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(8);
            doc.add(footer);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }

    private void addLine(Document doc) throws DocumentException {
        LineSeparator line = new LineSeparator(1f, 100f, new Color(226, 232, 240), Element.ALIGN_CENTER, -2f);
        doc.add(new Chunk(line));
    }

    private void addMetaCell(PdfPTable table, String label, String value, boolean left) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBackgroundColor(LIGHT_BG);
        cell.setPadding(6);
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + "\n", LABEL_FONT));
        p.add(new Chunk(value, HEADER_FONT));
        cell.addElement(p);
        table.addCell(cell);
    }

    private void addTableHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, LABEL_FONT));
        cell.setBackgroundColor(new Color(241, 245, 249));
        cell.setPadding(6);
        cell.setBorderColor(new Color(203, 213, 225));
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, BODY_FONT));
        cell.setPadding(6);
        cell.setBorderColor(new Color(226, 232, 240));
        table.addCell(cell);
    }

    private void addTotalRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, BODY_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(4);
        PdfPCell valueCell = new PdfPCell(new Phrase(value, BODY_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(4);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private String formatMoney(int cents, String currencyCode) {
        String symbol = switch (currencyCode == null ? "USD" : currencyCode.toUpperCase()) {
            case "USD" -> "$";
            case "EUR" -> "€";
            case "GBP" -> "£";
            case "INR" -> "₹";
            case "MUR" -> "Rs ";
            default    -> currencyCode + " ";
        };
        return String.format("%s%,.2f", symbol, cents / 100.0);
    }

    public String currencySymbol(String currencyCode) {
        return switch (currencyCode == null ? "USD" : currencyCode.toUpperCase()) {
            case "USD" -> "$";
            case "EUR" -> "€";
            case "GBP" -> "£";
            case "INR" -> "₹";
            case "MUR" -> "Rs ";
            default    -> currencyCode + " ";
        };
    }

    /* ── Travel Voucher ─────────────────────────────────────────────────── */

    private static final Color VOUCHER_HEADER_BG = new Color(30, 51, 79);   // dark navy
    private static final Color VOUCHER_TEAL      = new Color(20, 184, 166); // accent under section titles
    private static final Color VOUCHER_BODY      = new Color(30, 41, 59);
    private static final Color VOUCHER_LABEL     = new Color(100, 116, 139);
    private static final Color VOUCHER_CARD_TINT = new Color(247, 248, 250); // alternating section fill (matches design)
    private static final Font  VOUCHER_TITLE     = new Font(Font.HELVETICA, 18, Font.BOLD,   Color.WHITE);
    private static final Font  VOUCHER_SUB       = new Font(Font.HELVETICA, 9,  Font.NORMAL, VOUCHER_TEAL);
    private static final Font  VOUCHER_META_LABEL= new Font(Font.HELVETICA, 8,  Font.NORMAL, new Color(203, 213, 225));
    private static final Font  VOUCHER_META_VAL  = new Font(Font.HELVETICA, 14, Font.BOLD,   Color.WHITE);
    private static final Font  VOUCHER_SECTION   = new Font(Font.HELVETICA, 11, Font.BOLD,   VOUCHER_HEADER_BG);
    private static final Font  VOUCHER_FIELD_LBL = new Font(Font.HELVETICA, 8,  Font.NORMAL, VOUCHER_LABEL);
    private static final Font  VOUCHER_FIELD_VAL = new Font(Font.HELVETICA, 10, Font.BOLD,   VOUCHER_BODY);
    private static final DateTimeFormatter VOUCHER_DATE_FMT =
            DateTimeFormatter.ofPattern("dd-MMM-yyyy").withZone(ZoneId.of("Indian/Mauritius"));

    public byte[] generateVoucher(Booking booking) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            voucherHeader(doc, booking);

            // Each section renders as a "card": title on white/tinted bg + 2-col body.
            // We alternate fills (white, tint, white, …) to match the design mock.
            int idx = 0;

            // ── Booking Details (always first) ──
            renderVoucherCard(doc, "Booking Details", idx++ % 2 == 0, t -> {
                var customer = booking.getCustomer();
                voucherField(t, VoucherIcon.PERSON,   "Guest Name",        customer.getFirstName() + " " + customer.getLastName());
                voucherField(t, VoucherIcon.BADGE,    "Booking Reference", booking.getReference());
                voucherField(t, VoucherIcon.EMAIL,    "Email",             customer.getEmail());
                voucherField(t, VoucherIcon.CALENDAR, "Travel Date",       dateLabel(booking.getServiceDate()));
                voucherField(t, VoucherIcon.PHONE,    "Phone",             customer.getPhone());
                voucherField(t, VoucherIcon.DOCUMENT, "Status",            booking.getStatus() != null ? booking.getStatus().name() : "—");
                voucherField(t, VoucherIcon.WHATSAPP, "WhatsApp",          customer.getWhatsapp());
                voucherField(t, VoucherIcon.CALENDAR, "Booked On",         booking.getCreatedAt() != null ? VOUCHER_DATE_FMT.format(booking.getCreatedAt()) : "—");
                voucherField(t, VoucherIcon.FLAG,     "Nationality",       customer.getNationality());
                voucherField(t, VoucherIcon.AGENT,    "Booked By",         booking.getAgent() != null ? booking.getAgent().getCompanyName() : "Direct");
                if (customer.getAddress() != null && !customer.getAddress().isBlank()) {
                    voucherFieldFullWidth(t, "Address", customer.getAddress());
                }
                if (customer.getPassportNo() != null && !customer.getPassportNo().isBlank()) {
                    voucherField(t, VoucherIcon.BADGE, "Passport No.", customer.getPassportNo());
                    voucherField(t, "", "");
                }
            });

            // ── Per-item sections ──
            for (BookingItem item : booking.getItems()) {
                final boolean white = (idx++ % 2 == 0);
                switch (item.getItemType()) {
                    case CAR_RENTAL   -> renderVoucherCard(doc, "Car Rental Details",  white, t -> populateCarRental(t, item));
                    case CAR_TRANSFER -> renderVoucherCard(doc, "Car Transfer Details", white, t -> populateTransfer(t, item));
                    case TOUR         -> renderVoucherCard(doc, "Tour Details",         white, t -> populateTour(t, item, false));
                    case DAY_TRIP     -> renderVoucherCard(doc, "Local Experience Details", white, t -> populateTour(t, item, true));
                    default -> { idx--; /* unsupported type didn't render a card; rewind toggle */ }
                }
                // Add-ons (rendered as a free row inside the card already)
            }

            // ── Special Requests (only if present) ──
            if (booking.getSpecialRequests() != null && !booking.getSpecialRequests().isBlank()) {
                final boolean white = (idx++ % 2 == 0);
                renderVoucherCardSimple(doc, "Special Requests", white, booking.getSpecialRequests());
            }

            // ── Footer ──
            Paragraph footer = new Paragraph(
                    "This voucher serves as confirmation of your booking with Ambiance Holidays. " +
                    "Please present it at check-in / pickup. " +
                    "Enquiries: info@ambianceholidays.mu", LABEL_FONT);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(20);
            doc.add(footer);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate voucher PDF", e);
        }
    }

    /** Wrap a section (title + body) in a single colored card. */
    private void renderVoucherCard(Document doc, String title, boolean white,
            VoucherCardBuilder builder) throws DocumentException {
        PdfPTable card = new PdfPTable(1);
        card.setWidthPercentage(100);
        card.setSpacingAfter(8);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(white ? Color.WHITE : VOUCHER_CARD_TINT);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(18);

        // Section title
        Paragraph heading = new Paragraph(title, VOUCHER_SECTION);
        heading.setSpacingAfter(2);
        cell.addElement(heading);

        // Teal underline (short, like the mock)
        LineSeparator sep = new LineSeparator(2.5f, 35f, VOUCHER_TEAL, Element.ALIGN_LEFT, -3f);
        cell.addElement(new Chunk(sep));

        Paragraph spacer = new Paragraph(" ");
        spacer.setSpacingAfter(2);
        cell.addElement(spacer);

        // Body — 2-column field grid
        PdfPTable body = newTwoCol();
        builder.build(body);
        cell.addElement(body);

        card.addCell(cell);
        doc.add(card);
    }

    /** Variant for sections whose body is a single free-form string (e.g. Special Requests). */
    private void renderVoucherCardSimple(Document doc, String title, boolean white, String text) throws DocumentException {
        PdfPTable card = new PdfPTable(1);
        card.setWidthPercentage(100);
        card.setSpacingAfter(8);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(white ? Color.WHITE : VOUCHER_CARD_TINT);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(18);

        Paragraph heading = new Paragraph(title, VOUCHER_SECTION);
        heading.setSpacingAfter(2);
        cell.addElement(heading);

        LineSeparator sep = new LineSeparator(2.5f, 35f, VOUCHER_TEAL, Element.ALIGN_LEFT, -3f);
        cell.addElement(new Chunk(sep));

        Paragraph spacer = new Paragraph(" ");
        spacer.setSpacingAfter(2);
        cell.addElement(spacer);

        Paragraph body = new Paragraph(text, VOUCHER_FIELD_VAL);
        cell.addElement(body);

        card.addCell(cell);
        doc.add(card);
    }

    @FunctionalInterface
    private interface VoucherCardBuilder {
        void build(PdfPTable body) throws DocumentException;
    }

    private void populateCarRental(PdfPTable t, BookingItem item) throws DocumentException {
        voucherField(t, "Vehicle",           item.getNotes());
        voucherField(t, "Pick-up Location",  item.getPickupLocation());
        voucherField(t, "Pick-up Date",      dateLabel(item.getServiceDate()));
        voucherField(t, "Drop-off Location", item.getDropoffLocation());
        voucherField(t, "Pick-up Time",      timeLabel(item.getStartAt()));
        voucherField(t, "Drop-off Time",     timeLabel(item.getEndAt()));
        voucherField(t, "Rental Days",       item.getRentalDays() != null ? item.getRentalDays() + " day" + (item.getRentalDays() == 1 ? "" : "s") : "—");
        voucherField(t, "Passengers",        paxLabel(item));
        addExtrasInline(t, item);
    }

    private void populateTransfer(PdfPTable t, BookingItem item) throws DocumentException {
        voucherField(t, "Vehicle",           item.getNotes());
        voucherField(t, "Pick-up Location",  item.getPickupLocation());
        voucherField(t, "Pick-up Date",      dateLabel(item.getServiceDate()));
        voucherField(t, "Drop-off Location", item.getDropoffLocation());
        voucherField(t, "Pick-up Time",      timeLabel(item.getStartAt()));
        voucherField(t, "Trip Type",         item.getTripType() != null ? item.getTripType().name().replace('_', ' ') : "—");
        voucherField(t, "Passengers",        paxLabel(item));
        voucherField(t, "Quantity",          String.valueOf(item.getQuantity()));
        addExtrasInline(t, item);
    }

    private void populateTour(PdfPTable t, BookingItem item, boolean isDayTrip) throws DocumentException {
        voucherField(t, "Activity",     item.getNotes());
        voucherField(t, "Travel Date",  dateLabel(item.getServiceDate()));
        voucherField(t, "Pickup",       item.getPickupLocation());
        voucherField(t, "Pickup Time",  timeLabel(item.getStartAt()));
        voucherField(t, "Passengers",   paxLabel(item));
        voucherField(t, "Quantity",     String.valueOf(item.getQuantity()));
        addExtrasInline(t, item);
    }

    /** Render add-ons as a final full-width row inside the card. */
    private void addExtrasInline(PdfPTable t, BookingItem item) {
        if (item.getExtras() == null || item.getExtras().isEmpty()) return;
        StringBuilder sb = new StringBuilder();
        for (var ex : item.getExtras()) {
            if (sb.length() > 0) sb.append("  •  ");
            sb.append(ex.getLabel() != null ? ex.getLabel() : "Add-on");
            if (ex.getQuantity() > 1) sb.append(" ×").append(ex.getQuantity());
        }
        voucherFieldFullWidth(t, "Add-ons", sb.toString());
    }

    /** A field that spans both columns — used for Address / Add-ons / etc. */
    private void voucherFieldFullWidth(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingTop(6); cell.setPaddingBottom(6);
        cell.setPaddingLeft(0); cell.setPaddingRight(8);
        cell.setColspan(2);
        Paragraph p = new Paragraph();
        p.add(new Chunk(label.toUpperCase() + "\n", VOUCHER_FIELD_LBL));
        p.add(new Chunk(value != null && !value.isBlank() ? value : "—", VOUCHER_FIELD_VAL));
        cell.addElement(p);
        table.addCell(cell);
    }

    private void voucherHeader(Document doc, Booking booking) throws DocumentException {
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{2.2f, 1f});

        // Left: title + sub
        PdfPCell left = new PdfPCell();
        left.setBackgroundColor(VOUCHER_HEADER_BG);
        left.setBorder(Rectangle.NO_BORDER);
        left.setPaddingTop(18); left.setPaddingBottom(18);
        left.setPaddingLeft(20); left.setPaddingRight(10);
        Paragraph title = new Paragraph("Travel Voucher", VOUCHER_TITLE);
        title.setSpacingAfter(2);
        left.addElement(title);
        left.addElement(new Paragraph("Ambiance Holidays", VOUCHER_SUB));
        header.addCell(left);

        // Right: voucher number
        PdfPCell right = new PdfPCell();
        right.setBackgroundColor(VOUCHER_HEADER_BG);
        right.setBorder(Rectangle.NO_BORDER);
        right.setPaddingTop(20); right.setPaddingBottom(18);
        right.setPaddingLeft(10); right.setPaddingRight(20);
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph numLabel = new Paragraph("VOUCHER NUMBER", VOUCHER_META_LABEL);
        numLabel.setAlignment(Element.ALIGN_RIGHT);
        numLabel.setSpacingAfter(2);
        right.addElement(numLabel);
        Paragraph numVal = new Paragraph(booking.getReference(), VOUCHER_META_VAL);
        numVal.setAlignment(Element.ALIGN_RIGHT);
        right.addElement(numVal);
        header.addCell(right);

        doc.add(header);

        Paragraph spacer = new Paragraph(" ");
        spacer.setSpacingAfter(8);
        doc.add(spacer);
    }

    private PdfPTable newTwoCol() {
        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(100);
        try { t.setWidths(new float[]{1, 1}); } catch (DocumentException ignore) {}
        t.setSpacingBefore(2);
        t.setSpacingAfter(8);
        return t;
    }

    /** Small line icons drawn into a PNG via Java2D so the voucher matches the design mock. */
    public enum VoucherIcon { PERSON, EMAIL, PHONE, WHATSAPP, LOCATION, CALENDAR, GLOBE, BADGE, FLAG, AGENT, DOCUMENT }
    private final Map<VoucherIcon, byte[]> iconCache = new EnumMap<>(VoucherIcon.class);

    private byte[] iconBytes(VoucherIcon kind) {
        byte[] cached = iconCache.get(kind);
        if (cached != null) return cached;
        BufferedImage bi = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(VOUCHER_TEAL);
        g.setStroke(new BasicStroke(1.6f));
        switch (kind) {
            case PERSON -> {
                g.draw(new Ellipse2D.Float(6f, 2f, 8f, 8f));         // head
                g.drawArc(2, 11, 16, 14, 0, 180);                    // shoulders
            }
            case EMAIL -> {
                g.drawRect(2, 6, 16, 10);                            // envelope
                g.drawLine(2, 6, 10, 13);
                g.drawLine(18, 6, 10, 13);
            }
            case PHONE -> {
                g.drawRoundRect(6, 2, 8, 16, 2, 2);
                g.drawLine(8, 16, 12, 16);
            }
            case WHATSAPP -> {
                g.draw(new Ellipse2D.Float(2f, 2f, 16f, 16f));
                g.drawArc(7, 9, 6, 6, 200, 180);
            }
            case LOCATION -> {
                g.draw(new Ellipse2D.Float(6f, 4f, 8f, 8f));         // pin head
                int[] xpts = {6, 10, 14}; int[] ypts = {11, 18, 11}; // pin tail
                g.drawPolyline(xpts, ypts, 3);
            }
            case CALENDAR -> {
                g.drawRect(3, 5, 14, 12);
                g.drawLine(3, 9, 17, 9);
                g.drawLine(7, 3, 7, 6);                              // top tabs
                g.drawLine(13, 3, 13, 6);
            }
            case GLOBE -> {
                g.draw(new Ellipse2D.Float(2f, 2f, 16f, 16f));
                g.drawLine(2, 10, 18, 10);
                g.drawArc(5, 2, 10, 16, 0, 360);
            }
            case BADGE -> {
                g.drawRoundRect(3, 3, 14, 14, 2, 2);
                g.drawLine(7, 9, 13, 9);
                g.drawLine(7, 12, 13, 12);
            }
            case FLAG -> {
                g.drawLine(4, 2, 4, 18);
                g.drawLine(4, 3, 16, 3);
                g.drawLine(4, 10, 16, 10);
                g.drawLine(16, 3, 16, 10);
            }
            case AGENT -> {
                g.draw(new Ellipse2D.Float(6f, 1f, 8f, 8f));
                g.drawArc(2, 10, 16, 14, 0, 180);
                g.drawLine(8, 14, 12, 14);                           // tie/lapel hint
            }
            case DOCUMENT -> {
                g.drawRect(5, 2, 10, 16);
                g.drawLine(7, 6, 13, 6);
                g.drawLine(7, 9, 13, 9);
                g.drawLine(7, 12, 11, 12);
            }
        }
        g.dispose();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bi, "PNG", baos);
            byte[] out = baos.toByteArray();
            iconCache.put(kind, out);
            return out;
        } catch (Exception e) {
            iconCache.put(kind, new byte[0]);
            return new byte[0];
        }
    }

    /** Field row with a small leading icon — used in Booking Details to match the mock. */
    private void voucherField(PdfPTable table, VoucherIcon icon, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingTop(6); cell.setPaddingBottom(6);
        cell.setPaddingLeft(0); cell.setPaddingRight(8);

        PdfPTable inner = new PdfPTable(2);
        inner.setWidthPercentage(100);
        try { inner.setWidths(new float[]{1f, 9f}); } catch (DocumentException ignore) {}

        // icon
        PdfPCell iconCell = new PdfPCell();
        iconCell.setBorder(Rectangle.NO_BORDER);
        iconCell.setPaddingTop(2);
        iconCell.setVerticalAlignment(Element.ALIGN_TOP);
        try {
            byte[] bytes = iconBytes(icon);
            if (bytes != null && bytes.length > 0) {
                Image img = Image.getInstance(bytes);
                img.scaleAbsolute(10, 10);
                iconCell.addElement(img);
            }
        } catch (Exception ignore) {}
        inner.addCell(iconCell);

        // label / value
        PdfPCell content = new PdfPCell();
        content.setBorder(Rectangle.NO_BORDER);
        content.setPaddingLeft(0);
        Paragraph p = new Paragraph();
        p.add(new Chunk(label.toUpperCase() + "\n", VOUCHER_FIELD_LBL));
        p.add(new Chunk(value != null && !value.isBlank() ? value : "—", VOUCHER_FIELD_VAL));
        content.addElement(p);
        inner.addCell(content);

        cell.addElement(inner);
        table.addCell(cell);
    }

    private void voucherField(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingTop(6); cell.setPaddingBottom(6);
        cell.setPaddingLeft(0); cell.setPaddingRight(8);
        Paragraph p = new Paragraph();
        p.add(new Chunk(label.toUpperCase() + "\n", VOUCHER_FIELD_LBL));
        p.add(new Chunk(value != null && !value.isBlank() ? value : "—", VOUCHER_FIELD_VAL));
        cell.addElement(p);
        table.addCell(cell);
    }

    private String dateLabel(java.time.LocalDate d) {
        return d != null ? VOUCHER_DATE_FMT.format(d.atStartOfDay(ZoneId.of("Indian/Mauritius")).toInstant()) : "—";
    }

    private String timeLabel(java.time.Instant ts) {
        if (ts == null) return "—";
        return DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.of("Indian/Mauritius")).format(ts);
    }

    private String paxLabel(BookingItem item) {
        StringBuilder sb = new StringBuilder();
        sb.append(item.getPaxAdults()).append(" Adult").append(item.getPaxAdults() == 1 ? "" : "s");
        if (item.getPaxChildren() > 0) sb.append(", ").append(item.getPaxChildren()).append(" Child").append(item.getPaxChildren() == 1 ? "" : "ren");
        if (item.getPaxInfants() > 0)  sb.append(", ").append(item.getPaxInfants()).append(" Infant").append(item.getPaxInfants() == 1 ? "" : "s");
        return sb.toString();
    }
}
