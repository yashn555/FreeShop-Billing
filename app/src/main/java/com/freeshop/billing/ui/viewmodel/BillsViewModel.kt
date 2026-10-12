package com.freeshop.billing.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.freeshop.billing.data.db.AppDatabase
import com.freeshop.billing.data.db.entity.Bill
import com.freeshop.billing.data.repository.BillRepository
import com.freeshop.billing.data.repository.ShopRepository
import com.freeshop.billing.domain.PaymentStatus
import com.freeshop.billing.domain.paymentStatus
import com.freeshop.billing.pdf.BillPdfGenerator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

enum class DocTypeFilter { ALL, INVOICES, QUOTATIONS }
enum class PaymentStatusFilter { ALL, PAID, PARTIAL, UNPAID }

class BillsViewModel(
    private val repo: BillRepository,
    private val shopRepo: ShopRepository,
    private val shopId: Long,
    private val context: Context,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _docTypeFilter = MutableStateFlow(DocTypeFilter.ALL)
    val docTypeFilter: StateFlow<DocTypeFilter> = _docTypeFilter

    private val _paymentStatusFilter = MutableStateFlow(PaymentStatusFilter.ALL)
    val paymentStatusFilter: StateFlow<PaymentStatusFilter> = _paymentStatusFilter

    private val _dateFrom = MutableStateFlow<String?>(null)
    val dateFrom: StateFlow<String?> = _dateFrom

    private val _dateTo = MutableStateFlow<String?>(null)
    val dateTo: StateFlow<String?> = _dateTo

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private fun parseDate(s: String?): Long? {
        if (s.isNullOrBlank()) return null
        return try {
            dateFormat.parse(s.trim())?.time
        } catch (e: Exception) {
            null
        }
    }

    private data class FilterState(
        val query: String,
        val docTypeFilter: DocTypeFilter,
        val paymentStatusFilter: PaymentStatusFilter,
        val dateFrom: String?,
        val dateTo: String?,
    )

    private val filterState: Flow<FilterState> = combine(
        _query,
        _docTypeFilter,
        _paymentStatusFilter,
        _dateFrom,
        _dateTo,
    ) { q, docFilter, statusFilter, dFrom, dTo ->
        FilterState(q, docFilter, statusFilter, dFrom, dTo)
    }

    val bills: StateFlow<List<Bill>> = combine(
        repo.observeAll(shopId),
        filterState,
    ) { allBills, filter ->
        val fromTime = parseDate(filter.dateFrom)
        val toTime = parseDate(filter.dateTo)?.let { it + 86400000L - 1L }

        allBills.filter { bill ->
            // Search query
            val matchesQuery = if (filter.query.isBlank()) true else {
                bill.billNo.contains(filter.query, ignoreCase = true) || bill.customerName.contains(filter.query, ignoreCase = true)
            }

            // Doc type
            val matchesDocType = when (filter.docTypeFilter) {
                DocTypeFilter.ALL -> true
                DocTypeFilter.INVOICES -> bill.documentType == "INVOICE"
                DocTypeFilter.QUOTATIONS -> bill.documentType == "QUOTATION"
            }

            // Payment status
            val matchesStatus = when (filter.paymentStatusFilter) {
                PaymentStatusFilter.ALL -> true
                PaymentStatusFilter.PAID -> bill.paymentStatus == PaymentStatus.PAID
                PaymentStatusFilter.PARTIAL -> bill.paymentStatus == PaymentStatus.PARTIAL
                PaymentStatusFilter.UNPAID -> bill.paymentStatus == PaymentStatus.UNPAID
            }

            // Date range
            val billTime = parseDate(bill.date)
            val matchesFrom = if (fromTime != null && billTime != null) billTime >= fromTime else true
            val matchesTo = if (toTime != null && billTime != null) billTime <= toTime else true

            matchesQuery && matchesDocType && matchesStatus && matchesFrom && matchesTo
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setQuery(q: String) { _query.value = q }
    fun setDocTypeFilter(f: DocTypeFilter) { _docTypeFilter.value = f }
    fun setPaymentStatusFilter(f: PaymentStatusFilter) { _paymentStatusFilter.value = f }
    fun setDateFrom(d: String?) { _dateFrom.value = d }
    fun setDateTo(d: String?) { _dateTo.value = d }
    fun clearDateRange() {
        _dateFrom.value = null
        _dateTo.value = null
    }

    fun viewBill(bill: Bill) {
        val file = File(bill.pdfPath)
        if (!file.exists()) return
        val uri  = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Open PDF").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun shareBill(bill: Bill) {
        val file = File(bill.pdfPath)
        if (!file.exists()) return
        val uri  = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share Bill").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun deleteBill(bill: Bill, onDone: () -> Unit) {
        viewModelScope.launch {
            repo.delete(bill)
            if (bill.pdfPath.isNotEmpty()) File(bill.pdfPath).delete()
            onDone()
        }
    }

    fun convertToInvoice(quotation: Bill, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val shop = shopRepo.getById(shopId) ?: return@launch
                val newBillNo = "${shop.invoicePrefix}${shop.nextInvoiceNo}"
                val newBill = quotation.copy(
                    id = 0,
                    billNo = newBillNo,
                    documentType = "INVOICE",
                    isConverted = false,
                    createdAt = System.currentTimeMillis(),
                )
                val pdfFile = BillPdfGenerator.generate(context, newBill, shop)
                val newBillWithPdf = newBill.copy(pdfPath = pdfFile.absolutePath)
                repo.insert(newBillWithPdf)

                // Mark original quotation as converted
                repo.update(quotation.copy(isConverted = true))

                // Bump invoice counter
                shopRepo.update(shop.copy(nextInvoiceNo = shop.nextInvoiceNo + 1))

                onComplete()
            } catch (e: Exception) {
                // handle error if needed
            }
        }
    }

    class Factory(private val context: Context, private val shopId: Long) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = AppDatabase.getInstance(context)
            return BillsViewModel(
                repo = BillRepository(db.billDao()),
                shopRepo = ShopRepository(db.shopDao()),
                shopId = shopId,
                context = context,
            ) as T
        }
    }
}
