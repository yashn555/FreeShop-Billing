package com.freeshop.billing.pdf

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.freeshop.billing.data.db.entity.Bill
import com.freeshop.billing.data.db.entity.Shop
import com.freeshop.billing.domain.BillItem
import com.freeshop.billing.domain.Money
import org.json.JSONArray
import java.io.File

object BillPdfGenerator {

    // A4 at 72 dpi (points): 595 x 842
    private const val PAGE_W = 595f
    private const val PAGE_H = 842f
    private const val MARGIN = 36f
    private const val CONTENT_W = PAGE_W - 2 * MARGIN

    // Colors
    private val NAVY   = Color.parseColor("#0F3D91")
    private val GRID   = Color.parseColor("#C8D3E3")
    private val ZEBRA  = Color.parseColor("#F6F9FD")
    private val BLACK  = Color.BLACK
    private val WHITE  = Color.WHITE
    private val TOTAL_BG = Color.parseColor("#EEF3FB")

    // Paints
    private fun paint(color: Int = BLACK, size: Float = 10f, bold: Boolean = false, align: Paint.Align = Paint.Align.LEFT) =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = size
            typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            textAlign = align
        }

    private fun linePaint(color: Int, stroke: Float = 0.5f) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        this.strokeWidth = stroke
        style = Paint.Style.STROKE
    }

    private fun fillPaint(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }

    fun generate(context: Context, bill: Bill, shop: Shop): File {
        val billsDir = File(context.filesDir, "bills").also { it.mkdirs() }
        val outFile  = File(billsDir, "${bill.billNo.replace("/", "_")}.pdf")

        val items = parseItems(bill.itemsJson)
        val doc   = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_W.toInt(), PAGE_H.toInt(), 1).create()
        val page  = doc.startPage(pageInfo)
        val canvas = page.canvas

        var y = MARGIN

        // ── Header ──────────────────────────────────────────────────────────
        // Logo (if exists)
        if (shop.logoPath.isNotEmpty()) {
            val logoFile = File(shop.logoPath)
            if (logoFile.exists()) {
                val bmp = BitmapFactory.decodeFile(logoFile.absolutePath)
                if (bmp != null) {
                    val size = 60f
                    val scaled = Bitmap.createScaledBitmap(bmp, size.toInt(), size.toInt(), true)
                    canvas.drawBitmap(scaled, MARGIN, y, null)
                }
            }
        }

        // Shop name
        canvas.drawText(shop.name, MARGIN + 70f, y + 14f, paint(NAVY, 16f, true))
        if (shop.phone.isNotEmpty())
            canvas.drawText("Ph: ${shop.phone}", MARGIN + 70f, y + 26f, paint(BLACK, 8f))
        val addrParts = listOfNotNull(
            shop.address.ifEmpty { null }, shop.city.ifEmpty { null },
            shop.state.ifEmpty { null }, shop.pincode.ifEmpty { null }
        )
        if (addrParts.isNotEmpty())
            canvas.drawText(addrParts.joinToString(", "), MARGIN + 70f, y + 37f, paint(BLACK, 8f))
        if (shop.gstin.isNotEmpty())
            canvas.drawText("GSTIN: ${shop.gstin}", MARGIN + 70f, y + 48f, paint(BLACK, 8f))

        y += 65f

        // ── Accent divider ───────────────────────────────────────────────────
        canvas.drawRect(MARGIN, y, PAGE_W - MARGIN, y + 3f, fillPaint(NAVY))
        y += 10f

        // ── TAX INVOICE / INVOICE / QUOTATION label ─────────────────────────
        val label = if (bill.documentType == "QUOTATION") "QUOTATION" else if (shop.taxEnabled) "TAX INVOICE" else "INVOICE"
        canvas.drawText(label, PAGE_W / 2f, y + 12f, paint(NAVY, 14f, true, Paint.Align.CENTER))
        y += 22f

        // ── Meta grid (bill no, date, customer) ──────────────────────────────
        val col1 = MARGIN
        val col2 = MARGIN + CONTENT_W / 2f
        val pSmall = paint(BLACK, 8f)
        val pBold  = paint(BLACK, 8f, true)
        canvas.drawText("Bill No:", col1, y, pSmall);    canvas.drawText(bill.billNo, col1 + 50f, y, pBold)
        canvas.drawText("Date:",   col2, y, pSmall);     canvas.drawText(bill.date,   col2 + 50f, y, pBold)
        y += 14f
        canvas.drawText("Customer:", col1, y, pSmall);   canvas.drawText(bill.customerName, col1 + 50f, y, pBold)
        if (bill.customerPhone.isNotEmpty()) {
            canvas.drawText("Phone:", col2, y, pSmall);  canvas.drawText(bill.customerPhone, col2 + 50f, y, pBold)
        }
        y += 14f
        if (bill.customerGstin.isNotEmpty()) {
            canvas.drawText("GSTIN:", col1, y, pSmall);  canvas.drawText(bill.customerGstin, col1 + 50f, y, pBold)
            y += 14f
        }
        if (bill.customerAddress.isNotEmpty()) {
            canvas.drawText("Address:", col1, y, pSmall); canvas.drawText(bill.customerAddress, col1 + 50f, y, pBold)
            y += 14f
        }
        y += 6f

        // ── Items table ──────────────────────────────────────────────────────
        val hasTax = shop.taxEnabled && items.any { it.taxRate > 0 }
        val cols: List<Pair<String, Float>> = buildList {
            add("#" to 20f)
            add("Item" to (if (hasTax) 140f else 180f))
            add("HSN" to 50f)
            add("Qty" to 35f)
            add("Rate" to 60f)
            if (hasTax) add("${shop.taxLabel}%" to 40f)
            add("Amount" to 70f)
        }
        val rowH = 14f

        // Table header
        canvas.drawRect(MARGIN, y, PAGE_W - MARGIN, y + rowH, fillPaint(NAVY))
        var cx = MARGIN + 3f
        cols.forEach { (name, w) ->
            canvas.drawText(name, cx, y + rowH - 4f, paint(WHITE, 8f, true))
            cx += w
        }
        y += rowH

        // Rows
        items.forEachIndexed { idx, item ->
            val bg = if (idx % 2 == 0) WHITE else ZEBRA
            canvas.drawRect(MARGIN, y, PAGE_W - MARGIN, y + rowH, fillPaint(bg))
            cx = MARGIN + 3f
            val rowData = buildList<String> {
                add("${idx + 1}")
                add(item.name)
                add(item.hsn)
                add(item.qty.toDisplayString())
                add(Money.format(item.rate))
                if (hasTax) add("${item.taxRate}%")
                add(Money.format(item.lineTotal))
            }
            rowData.zip(cols).forEach { (text, colSpec) ->
                canvas.drawText(text.take(22), cx, y + rowH - 4f, paint(BLACK, 7.5f))
                cx += colSpec.second
            }
            // Bottom grid line
            canvas.drawLine(MARGIN, y + rowH, PAGE_W - MARGIN, y + rowH, linePaint(GRID))
            y += rowH
        }

        // Table border
        canvas.drawRect(MARGIN, y - rowH * items.size - rowH, PAGE_W - MARGIN, y, linePaint(GRID))
        y += 10f

        // ── Summary block ────────────────────────────────────────────────────
        val sumX = MARGIN + CONTENT_W * 0.55f
        val sumW = CONTENT_W * 0.45f

        fun summaryRow(label: String, value: String, bold: Boolean = false, bg: Int? = null) {
            if (bg != null) canvas.drawRect(sumX, y - 10f, MARGIN + CONTENT_W, y + 4f, fillPaint(bg))
            canvas.drawText(label, sumX + 4f, y, if (bold) pBold else pSmall)
            canvas.drawText(value, MARGIN + CONTENT_W - 4f, y, paint(BLACK, 8f, bold, Paint.Align.RIGHT))
            y += 14f
        }

        summaryRow("Subtotal", Money.format(bill.subtotal))
        if (bill.discount > 0) summaryRow("Discount", "- ${Money.format(bill.discount)}")
        if (hasTax) summaryRow(shop.taxLabel, Money.format(bill.taxAmount))
        summaryRow("TOTAL", Money.format(bill.grandTotal), bold = true, bg = TOTAL_BG)
        if (bill.documentType != "QUOTATION") {
            summaryRow("Received (${bill.paymentMode})", Money.format(bill.received))
            summaryRow("Balance", Money.format(bill.balance), bold = bill.balance > 0)
        }

        y += 6f

        // ── Amount in words ──────────────────────────────────────────────────
        val words = Money.amountInWords(bill.grandTotal)
        canvas.drawText("Amount: $words", MARGIN, y, paint(BLACK, 8f, true))
        y += 18f

        // ── Bank details ─────────────────────────────────────────────────────
        val hasBankDetails = shop.bankName.isNotEmpty() || shop.accountNo.isNotEmpty() || shop.upiId.isNotEmpty()
        if (hasBankDetails) {
            canvas.drawText("Bank Details", MARGIN, y, paint(NAVY, 9f, true))
            y += 12f
            if (shop.bankName.isNotEmpty())  { canvas.drawText("Bank: ${shop.bankName}",  MARGIN, y, pSmall); y += 11f }
            if (shop.accountNo.isNotEmpty()) { canvas.drawText("A/C:  ${shop.accountNo}", MARGIN, y, pSmall); y += 11f }
            if (shop.ifsc.isNotEmpty())      { canvas.drawText("IFSC: ${shop.ifsc}",       MARGIN, y, pSmall); y += 11f }
            if (shop.upiId.isNotEmpty())     { canvas.drawText("UPI:  ${shop.upiId}",      MARGIN, y, pSmall); y += 11f }
            y += 4f
        }

        // ── Terms ─────────────────────────────────────────────────────────────
        if (shop.terms.isNotEmpty()) {
            canvas.drawText("Terms & Conditions", MARGIN, y, paint(NAVY, 9f, true))
            y += 12f
            shop.terms.lines().take(5).forEach { line ->
                canvas.drawText(line, MARGIN, y, paint(BLACK, 7.5f))
                y += 11f
            }
            y += 4f
        }

        // ── Signature ─────────────────────────────────────────────────────────
        canvas.drawLine(PAGE_W - MARGIN - 100f, y, PAGE_W - MARGIN, y, linePaint(BLACK, 0.8f))
        canvas.drawText("Authorized Signature", PAGE_W - MARGIN - 50f, y + 10f, paint(BLACK, 7f, align = Paint.Align.CENTER))

        // Notes
        if (bill.notes.isNotEmpty()) {
            canvas.drawText("Notes: ${bill.notes}", MARGIN, PAGE_H - MARGIN, paint(BLACK, 7.5f))
        }

        doc.finishPage(page)
        outFile.outputStream().use { doc.writeTo(it) }
        doc.close()

        return outFile
    }

    private fun Double.toDisplayString(): String =
        if (this == this.toLong().toDouble()) "%.0f".format(this) else "%.2f".format(this)

    fun parseItems(json: String): List<BillItem> {
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                BillItem(
                    name    = obj.optString("name"),
                    hsn     = obj.optString("hsn"),
                    qty     = obj.optDouble("qty", 1.0),
                    rate    = obj.optDouble("rate", 0.0),
                    taxRate = obj.optDouble("taxRate", 0.0),
                    unit    = obj.optString("unit", "PCS"),
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
