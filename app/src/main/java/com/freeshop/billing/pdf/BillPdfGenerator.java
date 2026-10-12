package com.freeshop.billing.pdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;

import com.freeshop.billing.data.db.entity.Bill;
import com.freeshop.billing.data.db.entity.Shop;
import com.freeshop.billing.domain.BillItem;
import com.freeshop.billing.domain.Money;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class BillPdfGenerator {

    private BillPdfGenerator() {}

    // A4 at 72 dpi (points): 595 x 842
    private static final float PAGE_W = 595f;
    private static final float PAGE_H = 842f;
    private static final float MARGIN = 36f;
    private static final float CONTENT_W = PAGE_W - 2 * MARGIN;

    // Colors
    private static final int NAVY = Color.parseColor("#0F3D91");
    private static final int GRID = Color.parseColor("#C8D3E3");
    private static final int ZEBRA = Color.parseColor("#F6F9FD");
    private static final int BLACK = Color.BLACK;
    private static final int WHITE = Color.WHITE;
    private static final int TOTAL_BG = Color.parseColor("#EEF3FB");

    private static Paint createPaint(int color, float size, boolean bold, Paint.Align align) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setTextSize(size);
        p.setTypeface(bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        p.setTextAlign(align);
        return p;
    }

    private static Paint createLinePaint(int color, float stroke) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setStrokeWidth(stroke);
        p.setStyle(Paint.Style.STROKE);
        return p;
    }

    private static Paint createFillPaint(int color) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setStyle(Paint.Style.FILL);
        return p;
    }

    private static class ColumnSpec {
        final String header;
        final float width;

        ColumnSpec(String header, float width) {
            this.header = header;
            this.width = width;
        }
    }

    public static File generate(Context context, Bill bill, Shop shop) throws IOException {
        File billsDir = new File(context.getFilesDir(), "bills");
        if (!billsDir.exists()) {
            billsDir.mkdirs();
        }
        File outFile = new File(billsDir, bill.getBillNo().replace("/", "_") + ".pdf");

        List<BillItem> items = parseItems(bill.getItemsJson());
        PdfDocument doc = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder((int) PAGE_W, (int) PAGE_H, 1).create();
        PdfDocument.Page page = doc.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        float y = MARGIN;

        // ── Header ──────────────────────────────────────────────────────────
        // Logo (if exists)
        if (shop.getLogoPath() != null && !shop.getLogoPath().isEmpty()) {
            File logoFile = new File(shop.getLogoPath());
            if (logoFile.exists()) {
                Bitmap bmp = BitmapFactory.decodeFile(logoFile.getAbsolutePath());
                if (bmp != null) {
                    float size = 60f;
                    Bitmap scaled = Bitmap.createScaledBitmap(bmp, (int) size, (int) size, true);
                    canvas.drawBitmap(scaled, MARGIN, y, null);
                }
            }
        }

        // Shop name & details
        canvas.drawText(shop.getName() != null ? shop.getName() : "", MARGIN + 70f, y + 14f, createPaint(NAVY, 16f, true, Paint.Align.LEFT));
        if (shop.getPhone() != null && !shop.getPhone().isEmpty()) {
            canvas.drawText("Ph: " + shop.getPhone(), MARGIN + 70f, y + 26f, createPaint(BLACK, 8f, false, Paint.Align.LEFT));
        }

        List<String> addrParts = new ArrayList<>();
        if (shop.getAddress() != null && !shop.getAddress().isEmpty()) addrParts.add(shop.getAddress());
        if (shop.getCity() != null && !shop.getCity().isEmpty()) addrParts.add(shop.getCity());
        if (shop.getState() != null && !shop.getState().isEmpty()) addrParts.add(shop.getState());
        if (shop.getPincode() != null && !shop.getPincode().isEmpty()) addrParts.add(shop.getPincode());
        if (!addrParts.isEmpty()) {
            canvas.drawText(String.join(", ", addrParts), MARGIN + 70f, y + 37f, createPaint(BLACK, 8f, false, Paint.Align.LEFT));
        }

        if (shop.getGstin() != null && !shop.getGstin().isEmpty()) {
            canvas.drawText("GSTIN: " + shop.getGstin(), MARGIN + 70f, y + 48f, createPaint(BLACK, 8f, false, Paint.Align.LEFT));
        }

        y += 65f;

        // ── Accent divider ───────────────────────────────────────────────────
        canvas.drawRect(MARGIN, y, PAGE_W - MARGIN, y + 3f, createFillPaint(NAVY));
        y += 10f;

        // ── TAX INVOICE / INVOICE / QUOTATION label ─────────────────────────
        String label;
        if ("QUOTATION".equals(bill.getDocumentType())) {
            label = "QUOTATION";
        } else if (shop.isTaxEnabled()) {
            label = "TAX INVOICE";
        } else {
            label = "INVOICE";
        }
        canvas.drawText(label, PAGE_W / 2f, y + 12f, createPaint(NAVY, 14f, true, Paint.Align.CENTER));
        y += 22f;

        // ── Meta grid (bill no, date, customer) ──────────────────────────────
        float col1 = MARGIN;
        float col2 = MARGIN + CONTENT_W / 2f;
        Paint pSmall = createPaint(BLACK, 8f, false, Paint.Align.LEFT);
        Paint pBold = createPaint(BLACK, 8f, true, Paint.Align.LEFT);

        canvas.drawText("Bill No:", col1, y, pSmall);
        canvas.drawText(bill.getBillNo(), col1 + 50f, y, pBold);
        canvas.drawText("Date:", col2, y, pSmall);
        canvas.drawText(bill.getDate(), col2 + 50f, y, pBold);
        y += 14f;

        canvas.drawText("Customer:", col1, y, pSmall);
        canvas.drawText(bill.getCustomerName(), col1 + 50f, y, pBold);
        if (bill.getCustomerPhone() != null && !bill.getCustomerPhone().isEmpty()) {
            canvas.drawText("Phone:", col2, y, pSmall);
            canvas.drawText(bill.getCustomerPhone(), col2 + 50f, y, pBold);
        }
        y += 14f;

        if (bill.getCustomerGstin() != null && !bill.getCustomerGstin().isEmpty()) {
            canvas.drawText("GSTIN:", col1, y, pSmall);
            canvas.drawText(bill.getCustomerGstin(), col1 + 50f, y, pBold);
            y += 14f;
        }

        if (bill.getCustomerAddress() != null && !bill.getCustomerAddress().isEmpty()) {
            canvas.drawText("Address:", col1, y, pSmall);
            canvas.drawText(bill.getCustomerAddress(), col1 + 50f, y, pBold);
            y += 14f;
        }
        y += 6f;

        // ── Items table ──────────────────────────────────────────────────────
        boolean hasTax = shop.isTaxEnabled();
        boolean itemsHaveTax = false;
        for (BillItem item : items) {
            if (item.getTaxRate() > 0) {
                itemsHaveTax = true;
                break;
            }
        }
        hasTax = hasTax && itemsHaveTax;

        List<ColumnSpec> cols = new ArrayList<>();
        cols.add(new ColumnSpec("#", 20f));
        cols.add(new ColumnSpec("Item", hasTax ? 140f : 180f));
        cols.add(new ColumnSpec("HSN", 50f));
        cols.add(new ColumnSpec("Qty", 35f));
        cols.add(new ColumnSpec("Rate", 60f));
        if (hasTax) {
            cols.add(new ColumnSpec(shop.getTaxLabel() + "%", 40f));
        }
        cols.add(new ColumnSpec("Amount", 70f));

        float rowH = 14f;

        // Table header
        canvas.drawRect(MARGIN, y, PAGE_W - MARGIN, y + rowH, createFillPaint(NAVY));
        float cx = MARGIN + 3f;
        for (ColumnSpec col : cols) {
            canvas.drawText(col.header, cx, y + rowH - 4f, createPaint(WHITE, 8f, true, Paint.Align.LEFT));
            cx += col.width;
        }
        y += rowH;

        // Rows
        for (int idx = 0; idx < items.size(); idx++) {
            BillItem item = items.get(idx);
            int bg = (idx % 2 == 0) ? WHITE : ZEBRA;
            canvas.drawRect(MARGIN, y, PAGE_W - MARGIN, y + rowH, createFillPaint(bg));
            cx = MARGIN + 3f;

            List<String> rowData = new ArrayList<>();
            rowData.add(String.valueOf(idx + 1));
            rowData.add(item.getName());
            rowData.add(item.getHsn());
            rowData.add(toDisplayString(item.getQty()));
            rowData.add(Money.format(item.getRate()));
            if (hasTax) {
                rowData.add(toDisplayString(item.getTaxRate()) + "%");
            }
            rowData.add(Money.format(item.getLineTotal()));

            for (int c = 0; c < cols.size(); c++) {
                String text = rowData.get(c);
                if (text.length() > 22) {
                    text = text.substring(0, 22);
                }
                canvas.drawText(text, cx, y + rowH - 4f, createPaint(BLACK, 7.5f, false, Paint.Align.LEFT));
                cx += cols.get(c).width;
            }

            // Bottom grid line
            canvas.drawLine(MARGIN, y + rowH, PAGE_W - MARGIN, y + rowH, createLinePaint(GRID, 0.5f));
            y += rowH;
        }

        // Table border
        canvas.drawRect(MARGIN, y - rowH * items.size() - rowH, PAGE_W - MARGIN, y, createLinePaint(GRID, 0.5f));
        y += 10f;

        // ── Summary block ────────────────────────────────────────────────────
        float sumX = MARGIN + CONTENT_W * 0.55f;

        y = drawSummaryRow(canvas, sumX, y, "Subtotal", Money.format(bill.getSubtotal()), false, null, pBold, pSmall);
        if (bill.getDiscount() > 0) {
            y = drawSummaryRow(canvas, sumX, y, "Discount", "- " + Money.format(bill.getDiscount()), false, null, pBold, pSmall);
        }
        if (hasTax) {
            y = drawSummaryRow(canvas, sumX, y, shop.getTaxLabel(), Money.format(bill.getTaxAmount()), false, null, pBold, pSmall);
        }
        y = drawSummaryRow(canvas, sumX, y, "TOTAL", Money.format(bill.getGrandTotal()), true, TOTAL_BG, pBold, pSmall);
        if (!"QUOTATION".equals(bill.getDocumentType())) {
            y = drawSummaryRow(canvas, sumX, y, "Received (" + bill.getPaymentMode() + ")", Money.format(bill.getReceived()), false, null, pBold, pSmall);
            y = drawSummaryRow(canvas, sumX, y, "Balance", Money.format(bill.getBalance()), bill.getBalance() > 0, null, pBold, pSmall);
        }

        y += 6f;

        // ── Amount in words ──────────────────────────────────────────────────
        String words = Money.amountInWords(bill.getGrandTotal());
        canvas.drawText("Amount: " + words, MARGIN, y, createPaint(BLACK, 8f, true, Paint.Align.LEFT));
        y += 18f;

        // ── Bank details ─────────────────────────────────────────────────────
        boolean hasBank = (shop.getBankName() != null && !shop.getBankName().isEmpty())
                || (shop.getAccountNo() != null && !shop.getAccountNo().isEmpty())
                || (shop.getUpiId() != null && !shop.getUpiId().isEmpty());
        if (hasBank) {
            canvas.drawText("Bank Details", MARGIN, y, createPaint(NAVY, 9f, true, Paint.Align.LEFT));
            y += 12f;
            if (shop.getBankName() != null && !shop.getBankName().isEmpty()) {
                canvas.drawText("Bank: " + shop.getBankName(), MARGIN, y, pSmall);
                y += 11f;
            }
            if (shop.getAccountNo() != null && !shop.getAccountNo().isEmpty()) {
                canvas.drawText("A/C:  " + shop.getAccountNo(), MARGIN, y, pSmall);
                y += 11f;
            }
            if (shop.getIfsc() != null && !shop.getIfsc().isEmpty()) {
                canvas.drawText("IFSC: " + shop.getIfsc(), MARGIN, y, pSmall);
                y += 11f;
            }
            if (shop.getUpiId() != null && !shop.getUpiId().isEmpty()) {
                canvas.drawText("UPI:  " + shop.getUpiId(), MARGIN, y, pSmall);
                y += 11f;
            }
            y += 4f;
        }

        // ── Terms ─────────────────────────────────────────────────────────────
        if (shop.getTerms() != null && !shop.getTerms().isEmpty()) {
            canvas.drawText("Terms & Conditions", MARGIN, y, createPaint(NAVY, 9f, true, Paint.Align.LEFT));
            y += 12f;
            String[] termLines = shop.getTerms().split("\r?\n");
            int maxTerms = Math.min(5, termLines.length);
            for (int i = 0; i < maxTerms; i++) {
                canvas.drawText(termLines[i], MARGIN, y, createPaint(BLACK, 7.5f, false, Paint.Align.LEFT));
                y += 11f;
            }
            y += 4f;
        }

        // ── Signature ─────────────────────────────────────────────────────────
        canvas.drawLine(PAGE_W - MARGIN - 100f, y, PAGE_W - MARGIN, y, createLinePaint(BLACK, 0.8f));
        canvas.drawText("Authorized Signature", PAGE_W - MARGIN - 50f, y + 10f, createPaint(BLACK, 7f, false, Paint.Align.CENTER));

        // Notes (bottom-left)
        if (bill.getNotes() != null && !bill.getNotes().isEmpty()) {
            canvas.drawText("Notes: " + bill.getNotes(), MARGIN, PAGE_H - MARGIN, createPaint(BLACK, 7.5f, false, Paint.Align.LEFT));
        }

        doc.finishPage(page);
        try (FileOutputStream out = new FileOutputStream(outFile)) {
            doc.writeTo(out);
        }
        doc.close();

        return outFile;
    }

    private static float drawSummaryRow(Canvas canvas, float sumX, float y, String label, String value,
                                         boolean bold, Integer bg, Paint pBold, Paint pSmall) {
        if (bg != null) {
            canvas.drawRect(sumX, y - 10f, MARGIN + CONTENT_W, y + 4f, createFillPaint(bg));
        }
        canvas.drawText(label, sumX + 4f, y, bold ? pBold : pSmall);
        canvas.drawText(value, MARGIN + CONTENT_W - 4f, y, createPaint(BLACK, 8f, bold, Paint.Align.RIGHT));
        return y + 14f;
    }

    private static String toDisplayString(double value) {
        if (value == (long) value) {
            return String.format(Locale.getDefault(), "%.0f", value);
        } else {
            return String.format(Locale.getDefault(), "%.2f", value);
        }
    }

    public static List<BillItem> parseItems(String json) {
        List<BillItem> list = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) {
            return list;
        }
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                list.add(new BillItem(
                        obj.optString("name"),
                        obj.optString("hsn"),
                        obj.optDouble("qty", 1.0),
                        obj.optDouble("rate", 0.0),
                        obj.optDouble("taxRate", 0.0),
                        obj.optString("unit", "PCS")
                ));
            }
        } catch (Exception ignored) {}
        return list;
    }
}
