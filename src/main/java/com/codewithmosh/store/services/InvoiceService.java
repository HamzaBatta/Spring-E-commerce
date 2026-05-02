package com.codewithmosh.store.services;

import com.codewithmosh.store.entities.Invoice;
import com.codewithmosh.store.entities.InvoiceStatus;
import com.codewithmosh.store.entities.Order;
import com.codewithmosh.store.exceptions.OrderNotFoundException;
import com.codewithmosh.store.repositories.InvoiceRepository;
import com.codewithmosh.store.repositories.OrderRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;

    @Value("${invoices.dir:target/invoices}")
    private String invoicesDir;

    /**
     * Generates an invoice PDF for the given orderId.
     * This method is idempotent: if a DONE invoice already exists, it returns silently.
     */
    @Transactional
    public String generateInvoice(Long orderId) {
        // Idempotency: if already generated, return path
        Optional<Invoice> existing = invoiceRepository.findByOrderId(orderId);
        if (existing.isPresent() && existing.get().getStatus() == InvoiceStatus.DONE) {
            log.info("Invoice already exists for order {} -> {}", orderId, existing.get().getFilePath());
            return existing.get().getFilePath();
        }

        // Load order with items
        var order = orderRepository.findWithItemsById(orderId).orElseThrow(OrderNotFoundException::new);

        // Create invoice record with PENDING status
        Invoice invoice = Invoice.builder()
                .orderId(orderId)
                .status(InvoiceStatus.PENDING)
                .createdAt(Instant.now())
                .build();
        invoice = invoiceRepository.save(invoice);

        // Ensure dir exists
        File dir = new File(invoicesDir);
        if (!dir.exists()) dir.mkdirs();

        String timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now()).replaceAll(":", "-");
        String filename = String.format("invoice-order-%d-%s.pdf", orderId, timestamp);
        File out = new File(dir, filename);

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 18);
                cs.newLineAtOffset(50, 700);
                cs.showText("Invoice");

                cs.newLineAtOffset(0, -30);
                cs.setFont(PDType1Font.HELVETICA, 12);
                cs.showText("Order ID: " + orderId);

                cs.newLineAtOffset(0, -20);
                cs.showText("Date: " + Instant.now().toString());

                cs.newLineAtOffset(0, -30);
                cs.showText("Customer: " + (order.getUser() != null ? order.getUser().getName() : "N/A"));

                cs.newLineAtOffset(0, -30);
                cs.showText("--- Items ---");

                BigDecimal total = BigDecimal.ZERO;
                for (var item : order.getItems()) {
                    cs.newLineAtOffset(0, -20);
                    BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    String line = String.format("%s x%d  @ %.2f  => %.2f",
                            item.getProduct().getName(),
                            item.getQuantity(),
                            item.getUnitPrice(),
                            lineTotal);
                    cs.showText(line);
                    total = total.add(lineTotal);
                }

                cs.newLineAtOffset(0, -30);
                cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
                cs.showText(String.format("Total: %.2f", total));

                cs.endText();
            }

            doc.save(out);
        } catch (IOException e) {
            log.error("Failed to write invoice PDF for order {}", orderId, e);
            invoice.setStatus(InvoiceStatus.FAILED);
            invoiceRepository.save(invoice);
            throw new RuntimeException("Invoice generation failed", e);
        }

        // Update invoice record
        invoice.setFilePath(out.getAbsolutePath());
        invoice.setStatus(InvoiceStatus.DONE);
        invoice.setUpdatedAt(Instant.now());
        invoiceRepository.save(invoice);

        log.info("Generated invoice {} for order {}", out.getAbsolutePath(), orderId);
        return out.getAbsolutePath();
    }
}
