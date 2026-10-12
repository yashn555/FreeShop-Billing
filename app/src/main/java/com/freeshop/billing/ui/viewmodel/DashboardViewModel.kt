package com.freeshop.billing.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.freeshop.billing.data.db.AppDatabase
import com.freeshop.billing.data.db.entity.Bill
import com.freeshop.billing.data.db.entity.Customer
import com.freeshop.billing.data.db.entity.Product
import com.freeshop.billing.data.db.entity.Shop
import com.freeshop.billing.data.repository.*
import com.freeshop.billing.domain.BillItem
import com.freeshop.billing.domain.Money
import com.freeshop.billing.domain.Validators
import com.freeshop.billing.pdf.BillPdfGenerator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class DraftItem(
    val id: Long = System.nanoTime(),
    val name: String = "",
    val hsn: String = "",
    val qty: String = "1",
    val rate: String = "",
    val taxRate: String = "0",
)

data class DashboardUiState(
    val shop: Shop? = null,
    val customers: List<Customer> = emptyList(),
    val products: List<Product> = emptyList(),

    // Customer section
    val customerName: String = "",
    val customerPhone: String = "",
    val customerGstin: String = "",
    val customerAddress: String = "",

    // Bill details
    val billNo: String = "",
    val date: String   = "",
    val discount: String = "0",
    val paymentMode: String = "CASH",
    val received: String = "",
    val notes: String = "",

    // Items
    val draftItems: List<DraftItem> = listOf(DraftItem()),

    // Document Type
    val documentType: String = "INVOICE",

    // Status
    val generating: Boolean = false,
    val savedPdfPath: String = "",
    val toastMessage: String? = null,
    val toastError: Boolean = false,
)

val DashboardUiState.subtotal: Double get() =
    draftItems.sumOf { it.qty.toDoubleOrNull()?.times(it.rate.toDoubleOrNull() ?: 0.0) ?: 0.0 }

val DashboardUiState.taxAmount: Double get() =
    if (shop?.taxEnabled == true)
        draftItems.sumOf {
            val lineTotal = (it.qty.toDoubleOrNull() ?: 0.0) * (it.rate.toDoubleOrNull() ?: 0.0)
            lineTotal * (it.taxRate.toDoubleOrNull() ?: 0.0) / 100.0
        }
    else 0.0

val DashboardUiState.grandTotal: Double get() =
    maxOf(0.0, subtotal + taxAmount - (discount.toDoubleOrNull() ?: 0.0))

val DashboardUiState.receivedAmount: Double get() =
    received.toDoubleOrNull() ?: if (documentType == "QUOTATION") 0.0 else grandTotal

val DashboardUiState.balance: Double get() =
    grandTotal - receivedAmount

class DashboardViewModel(
    private val shopId: Long,
    private val shopRepo: ShopRepository,
    private val customerRepo: CustomerRepository,
    private val productRepo: ProductRepository,
    private val billRepo: BillRepository,
    private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state
    val documentType: StateFlow<String> = _state.map { it.documentType }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "INVOICE")

    init {
        viewModelScope.launch {
            shopRepo.observe(shopId).collect { shop ->
                _state.update { s ->
                    val billNo = shop?.let { "${it.invoicePrefix}${it.nextInvoiceNo}" } ?: s.billNo
                    s.copy(shop = shop, billNo = billNo, date = s.date.ifEmpty { todayStr() })
                }
            }
        }
        viewModelScope.launch {
            customerRepo.observeAll(shopId).collect { list ->
                _state.update { it.copy(customers = list) }
            }
        }
        viewModelScope.launch {
            productRepo.observeAll(shopId).collect { list ->
                _state.update { it.copy(products = list) }
            }
        }
    }

    // --- Updaters ---
    fun updateDocumentType(type: String) = _state.update { it.copy(documentType = type) }
    fun updateCustomerName(v: String)    = _state.update { it.copy(customerName = v) }
    fun updateCustomerPhone(v: String)   = _state.update { it.copy(customerPhone = v) }
    fun updateCustomerGstin(v: String)   = _state.update { it.copy(customerGstin = v) }
    fun updateCustomerAddress(v: String) = _state.update { it.copy(customerAddress = v) }
    fun selectCustomer(c: Customer) {
        _state.update { it.copy(customerName = c.name, customerPhone = c.phone, customerGstin = c.gstin, customerAddress = c.address) }
    }
    fun updateBillNo(v: String)     = _state.update { it.copy(billNo = v) }
    fun updateDate(v: String)       = _state.update { it.copy(date = v) }
    fun updateDiscount(v: String)   = _state.update { it.copy(discount = v) }
    fun updatePaymentMode(v: String)= _state.update { it.copy(paymentMode = v) }
    fun updateReceived(v: String)   = _state.update { it.copy(received = v) }
    fun updateNotes(v: String)      = _state.update { it.copy(notes = v) }
    fun updateItem(id: Long, item: DraftItem) = _state.update { s -> s.copy(draftItems = s.draftItems.map { if (it.id == id) item else it }) }
    fun addItem() = _state.update { it.copy(draftItems = it.draftItems + DraftItem()) }
    fun removeItem(id: Long) = _state.update { s -> s.copy(draftItems = s.draftItems.filter { it.id != id }) }
    fun autofillProduct(draftId: Long, p: Product) = _state.update { s ->
        s.copy(draftItems = s.draftItems.map { d ->
            if (d.id == draftId) d.copy(name = p.name, hsn = p.hsn, rate = p.price.toString(), taxRate = p.taxRate.toString())
            else d
        })
    }

    fun generate(onSuccess: (String) -> Unit) {
        val s = _state.value
        val shop = s.shop ?: run { toast("Shop not loaded", true); return }

        // Validators (F10)
        val customerName = Validators.validateCustomerName(s.customerName)
        if (s.billNo.isBlank()) { toast("Bill number is required", true); return }
        val items = s.draftItems.map {
            BillItem(it.name, it.hsn, it.qty.toDoubleOrNull() ?: 0.0, it.rate.toDoubleOrNull() ?: 0.0, it.taxRate.toDoubleOrNull() ?: 0.0)
        }
        val itemError = Validators.validateItems(items)
        if (itemError != null) { toast(itemError, true); return }
        val discountVal = s.discount.toDoubleOrNull() ?: 0.0
        val discError = Validators.validateDiscount(discountVal, s.subtotal)
        if (discError != null) { toast(discError, true); return }
        val receivedVal = s.received.toDoubleOrNull() ?: if (s.documentType == "QUOTATION") 0.0 else s.grandTotal
        val recError = Validators.validateReceived(receivedVal)
        if (recError != null) { toast(recError, true); return }

        _state.update { it.copy(generating = true) }
        viewModelScope.launch {
            try {
                // Unique billNo check
                if (!billRepo.isBillNoUnique(shopId, s.billNo)) {
                    toast("Bill number '${s.billNo}' already exists", true)
                    _state.update { it.copy(generating = false) }
                    return@launch
                }

                val grandTotal = s.grandTotal
                val balance = grandTotal - receivedVal

                // Serialize items to JSON
                val itemsJson = JSONArray().also { arr ->
                    items.forEach { item ->
                        arr.put(JSONObject().apply {
                            put("name", item.name); put("hsn", item.hsn); put("qty", item.qty)
                            put("rate", item.rate); put("taxRate", item.taxRate); put("unit", item.unit)
                        })
                    }
                }.toString()

                val bill = Bill(
                    shopId = shopId, billNo = s.billNo, date = s.date.ifEmpty { todayStr() },
                    customerName = customerName, customerPhone = s.customerPhone,
                    customerGstin = s.customerGstin, customerAddress = s.customerAddress,
                    itemsJson = itemsJson, subtotal = s.subtotal, discount = discountVal,
                    taxAmount = s.taxAmount, grandTotal = grandTotal,
                    received = receivedVal, balance = balance,
                    paymentMode = s.paymentMode, notes = s.notes,
                    documentType = s.documentType,
                )

                // Generate PDF first (so we can attach path)
                val pdfFile = BillPdfGenerator.generate(context, bill, shop)
                val billWithPdf = bill.copy(pdfPath = pdfFile.absolutePath)
                val billId = billRepo.insert(billWithPdf)

                // Bump invoice counter
                shopRepo.update(shop.copy(nextInvoiceNo = shop.nextInvoiceNo + 1))

                _state.update { it.copy(generating = false, savedPdfPath = pdfFile.absolutePath) }
                val docLabel = if (s.documentType == "QUOTATION") "Quotation" else "Bill"
                toast("$docLabel generated and saved as PDF", false)

                // Open PDF via FileProvider
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(intent, "Open Bill PDF").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                onSuccess(pdfFile.absolutePath)
            } catch (e: Exception) {
                _state.update { it.copy(generating = false) }
                toast("Error: ${e.message}", true)
            }
        }
    }

    fun resetForm() {
        val shop = _state.value.shop
        val nextBillNo = shop?.let { "${it.invoicePrefix}${it.nextInvoiceNo}" } ?: ""
        _state.update {
            DashboardUiState(
                shop = shop,
                customers = it.customers,
                products  = it.products,
                billNo    = nextBillNo,
                date      = todayStr(),
                draftItems = listOf(DraftItem()),
                documentType = "INVOICE",
            )
        }
    }

    fun clearToast() = _state.update { it.copy(toastMessage = null) }

    private fun toast(msg: String, error: Boolean) {
        _state.update { it.copy(toastMessage = msg, toastError = error) }
    }

    private fun todayStr(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    class Factory(private val context: Context, private val shopId: Long) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = AppDatabase.getInstance(context)
            return DashboardViewModel(
                shopId, ShopRepository(db.shopDao()),
                CustomerRepository(db.customerDao()),
                ProductRepository(db.productDao()),
                BillRepository(db.billDao()),
                context,
            ) as T
        }
    }
}
