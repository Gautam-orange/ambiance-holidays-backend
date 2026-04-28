package com.ambianceholidays.api.pdf;

import com.ambianceholidays.domain.booking.Booking;
import com.ambianceholidays.domain.booking.BookingItem;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 20, Font.BOLD, new Color(15, 23, 42));
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(71, 85, 105));
    private static final Font BODY_FONT = new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(51, 65, 85));
    private static final Font LABEL_FONT = new Font(Font.HELVETICA, 8, Font.BOLD, new Color(100, 116, 139));
    private static final Color BRAND_COLOR = new Color(14, 165, 233);
    private static final Color LIGHT_BG = new Color(248, 250, 252);

    public byte[] generateInvoice(Booking booking) {
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
                addTableCell(table, formatMoney(item.getUnitPriceCents()));
                addTableCell(table, formatMoney(item.getTotalCents()));
            }
            doc.add(table);

            // Totals
            addLine(doc);
            PdfPTable totals = new PdfPTable(2);
            totals.setWidthPercentage(50);
            totals.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totals.setSpacingBefore(8);

            addTotalRow(totals, "Subtotal", formatMoney(booking.getSubtotalCents()));
            if (booking.getMarkupCents() > 0)
                addTotalRow(totals, "Markup (" + booking.getMarkupRate() + "%)", formatMoney(booking.getMarkupCents()));
            if (booking.getCommissionCents() > 0)
                addTotalRow(totals, "Commission (" + booking.getCommissionRate() + "%)", formatMoney(booking.getCommissionCents()));
            addTotalRow(totals, "VAT (" + booking.getVatRate() + "%)", formatMoney(booking.getVatCents()));

            PdfPCell totalLabel = new PdfPCell(new Phrase("TOTAL DUE", new Font(Font.HELVETICA, 10, Font.BOLD)));
            totalLabel.setBorder(Rectangle.TOP);
            totalLabel.setPadding(6);
            PdfPCell totalValue = new PdfPCell(new Phrase(formatMoney(booking.getTotalCents()),
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

    private String formatMoney(int cents) {
        return String.format("Rs %,.0f", cents / 100.0);
    }
}
