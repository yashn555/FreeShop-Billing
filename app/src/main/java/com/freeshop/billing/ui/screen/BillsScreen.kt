package com.freeshop.billing.ui.screen

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freeshop.billing.data.db.entity.Bill
import com.freeshop.billing.domain.Money
import com.freeshop.billing.domain.PaymentStatus
import com.freeshop.billing.domain.paymentStatus
import com.freeshop.billing.ui.components.*
import com.freeshop.billing.ui.theme.*
import com.freeshop.billing.ui.viewmodel.BillsViewModel
import com.freeshop.billing.ui.viewmodel.DocTypeFilter
import com.freeshop.billing.ui.viewmodel.PaymentStatusFilter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BillsScreen(shopId: Long, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val vm: BillsViewModel = viewModel(factory = BillsViewModel.Factory(ctx, shopId))
    val bills by vm.bills.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    val docTypeFilter by vm.docTypeFilter.collectAsStateWithLifecycle()
    val paymentStatusFilter by vm.paymentStatusFilter.collectAsStateWithLifecycle()
    val dateFrom by vm.dateFrom.collectAsStateWithLifecycle()
    val dateTo by vm.dateTo.collectAsStateWithLifecycle()

    var confirmDelete by remember { mutableStateOf<Bill?>(null) }
    var toast by remember { mutableStateOf<ToastMessage?>(null) }

    fun showDatePicker(initialDateStr: String?, onDateSelected: (String) -> Unit) {
        val cal = Calendar.getInstance()
        if (!initialDateStr.isNullOrBlank()) {
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                sdf.parse(initialDateStr)?.let { cal.time = it }
            } catch (_: Exception) {}
        }
        DatePickerDialog(
            ctx,
            { _, year, month, dayOfMonth ->
                val formatted = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year)
                onDateSelected(formatted)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = { TopBar("Bills History", onBack = onBack) },
            containerColor = BgBlue,
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(10.dp))
                // a) Search box
                OutlinedTextField(
                    value = query,
                    onValueChange = { vm.setQuery(it) },
                    label = { Text("Search by bill no or customer") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) IconButton(onClick = { vm.setQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))

                // b) Document type chips
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = docTypeFilter == DocTypeFilter.ALL,
                        onClick = { vm.setDocTypeFilter(DocTypeFilter.ALL) },
                        label = { Text("All Docs") },
                    )
                    FilterChip(
                        selected = docTypeFilter == DocTypeFilter.INVOICES,
                        onClick = { vm.setDocTypeFilter(DocTypeFilter.INVOICES) },
                        label = { Text("Invoices") },
                    )
                    FilterChip(
                        selected = docTypeFilter == DocTypeFilter.QUOTATIONS,
                        onClick = { vm.setDocTypeFilter(DocTypeFilter.QUOTATIONS) },
                        label = { Text("Quotations") },
                    )
                }

                Spacer(Modifier.height(4.dp))

                // c) Payment status chips
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = paymentStatusFilter == PaymentStatusFilter.ALL,
                        onClick = { vm.setPaymentStatusFilter(PaymentStatusFilter.ALL) },
                        label = { Text("All Status") },
                    )
                    FilterChip(
                        selected = paymentStatusFilter == PaymentStatusFilter.PAID,
                        onClick = { vm.setPaymentStatusFilter(PaymentStatusFilter.PAID) },
                        label = { Text("Paid") },
                    )
                    FilterChip(
                        selected = paymentStatusFilter == PaymentStatusFilter.PARTIAL,
                        onClick = { vm.setPaymentStatusFilter(PaymentStatusFilter.PARTIAL) },
                        label = { Text("Partial") },
                    )
                    FilterChip(
                        selected = paymentStatusFilter == PaymentStatusFilter.UNPAID,
                        onClick = { vm.setPaymentStatusFilter(PaymentStatusFilter.UNPAID) },
                        label = { Text("Unpaid") },
                    )
                }

                Spacer(Modifier.height(4.dp))

                // d) Date range pickers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = { showDatePicker(dateFrom) { vm.setDateFrom(it) } },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (dateFrom != null) "From: $dateFrom" else "From Date", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { showDatePicker(dateTo) { vm.setDateTo(it) } },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (dateTo != null) "To: $dateTo" else "To Date", fontSize = 12.sp)
                    }

                    if (dateFrom != null || dateTo != null) {
                        IconButton(onClick = { vm.clearDateRange() }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Dates", tint = DangerRed)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                if (bills.isEmpty()) {
                    EmptyState(Icons.Default.ReceiptLong, if (query.isBlank() && dateFrom == null && dateTo == null) "No bills yet" else "No matching bills")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(bills, key = { it.id }) { bill ->
                            BillCard(
                                bill = bill,
                                onView = { vm.viewBill(bill) },
                                onShare = { vm.shareBill(bill) },
                                onDelete = { confirmDelete = bill },
                                onConvert = {
                                    vm.convertToInvoice(bill) {
                                        toast = ToastMessage("Converted to Invoice", ToastType.OK)
                                    }
                                },
                            )
                        }
                        item { Spacer(Modifier.height(12.dp)) }
                    }
                }
            }
        }

        confirmDelete?.let { bill ->
            AlertDialog(
                onDismissRequest = { confirmDelete = null },
                title = { Text("Delete Bill") },
                text  = { Text("Delete bill '${bill.billNo}'? This cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        vm.deleteBill(bill) { toast = ToastMessage("Bill deleted", ToastType.OK) }
                        confirmDelete = null
                    }) { Text("Delete", color = DangerRed) }
                },
                dismissButton = { TextButton(onClick = { confirmDelete = null }) { Text("Cancel") } },
            )
        }

        AppToastHost(toast) { toast = null }
    }
}

@Composable
private fun BillCard(
    bill: Bill,
    onView: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onConvert: () -> Unit,
) {
    val (chipBg, chipText) = when (bill.paymentStatus) {
        PaymentStatus.PAID -> Color(0xFFE8F7EE) to Color(0xFF15803D)
        PaymentStatus.PARTIAL -> Color(0xFFFEF3C7) to Color(0xFFB45309)
        PaymentStatus.UNPAID -> Color(0xFFFDEAEA) to Color(0xFFC0392B)
    }

    Card(
        shape  = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(bill.billNo, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AccentNavy)
                        if (bill.documentType == "QUOTATION") {
                            Surface(
                                color = AccentNavy.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp),
                            ) {
                                Text(
                                    "QUOTATION",
                                    color = AccentNavy,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                )
                            }
                        }
                        Surface(
                            color = chipBg,
                            shape = RoundedCornerShape(4.dp),
                        ) {
                            Text(
                                bill.paymentStatus.name,
                                color = chipText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            )
                        }
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(bill.customerName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(bill.date, fontSize = 12.sp, color = TextMuted)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(Money.format(bill.grandTotal), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                    if (bill.documentType == "QUOTATION" && bill.isConverted) {
                        Spacer(Modifier.height(4.dp))
                        Text("Converted", fontSize = 12.sp, color = TextMuted, fontStyle = FontStyle.Italic)
                    }
                }
            }
            if (bill.documentType != "QUOTATION" && bill.balance > 0.01) {
                Spacer(Modifier.height(4.dp))
                Text("Balance: ${Money.format(bill.balance)}", color = DangerRed, fontSize = 12.sp)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = LineBlue)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = onView,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("View", fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Share", fontSize = 13.sp)
                }
                if (bill.documentType == "QUOTATION" && !bill.isConverted) {
                    Button(
                        onClick = onConvert,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentNavy),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text("Convert", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = DangerRed)
                }
            }
        }
    }
}
