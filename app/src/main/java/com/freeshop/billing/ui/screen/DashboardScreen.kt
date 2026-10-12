package com.freeshop.billing.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freeshop.billing.data.db.entity.Customer
import com.freeshop.billing.data.db.entity.Product
import com.freeshop.billing.domain.Money
import com.freeshop.billing.ui.components.*
import com.freeshop.billing.ui.theme.*
import com.freeshop.billing.ui.viewmodel.*

@Composable
fun DashboardScreen(
    shopId: Long,
    onSettings: () -> Unit,
    onBills: () -> Unit,
    onItems: () -> Unit,
    onCustomers: () -> Unit,
    onLogout: () -> Unit,
) {
    val ctx = LocalContext.current
    val vm: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory(ctx, shopId))
    val state by vm.state.collectAsStateWithLifecycle()
    var showResetDialog by remember { mutableStateOf(false) }

    val toast = state.toastMessage?.let {
        ToastMessage(it, if (state.toastError) ToastType.ERROR else ToastType.OK)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onKeyEvent { ev ->
                if (ev.isCtrlPressed && ev.key == Key.Enter && ev.type == KeyEventType.KeyDown) {
                    vm.generate {}; true
                } else false
            },
    ) {
        Scaffold(
            topBar = {
                TopBar(if (state.documentType == "QUOTATION") "New Quotation" else "New Bill", actions = {
                    IconButton(onClick = onCustomers) { Icon(Icons.Default.PeopleOutline, contentDescription = "Customers") }
                    IconButton(onClick = onItems)     { Icon(Icons.Default.Inventory2, contentDescription = "Items") }
                    IconButton(onClick = onBills)     { Icon(Icons.Default.ReceiptLong, contentDescription = "Bills") }
                    IconButton(onClick = onSettings)  { Icon(Icons.Default.Settings, contentDescription = "Settings") }
                    IconButton(onClick = onLogout)    { Icon(Icons.Default.Logout, contentDescription = "Logout") }
                })
            },
            containerColor = BgBlue,
        ) { padding ->
            BoxWithConstraints(modifier = Modifier.padding(padding).fillMaxSize()) {
                val isWide = maxWidth > 600.dp
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            LeftColumn(state, vm, showResetDialog, { showResetDialog = it })
                        }
                        Column(modifier = Modifier.weight(0.9f).verticalScroll(rememberScrollState())) {
                            SummaryCard(state)
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        LeftColumn(state, vm, showResetDialog, { showResetDialog = it })
                        SummaryCard(state)
                    }
                }
            }
        }

        // Reset confirm dialog
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Reset Form") },
                text  = { Text("Clear all entries and start a new bill?") },
                confirmButton = {
                    TextButton(onClick = { vm.resetForm(); showResetDialog = false }) { Text("Reset") }
                },
                dismissButton = { TextButton(onClick = { showResetDialog = false }) { Text("Cancel") } },
            )
        }

        AppToastHost(toast) { vm.clearToast() }
        if (state.generating) LoadingOverlay(true)
    }
}

@Composable
private fun LeftColumn(
    state: DashboardUiState,
    vm: DashboardViewModel,
    showResetDialog: Boolean,
    setReset: (Boolean) -> Unit,
) {
    // ── Document Type Segmented Toggle ────────────────────────────────────────
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
            .padding(4.dp),
    ) {
        listOf("INVOICE", "QUOTATION").forEach { type ->
            val selected = state.documentType == type
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selected) AccentNavy else Color.Transparent)
                    .clickable { vm.updateDocumentType(type) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (type == "INVOICE") "Invoice" else "Quotation",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (selected) Color.White else TextDark,
                )
            }
        }
    }
    // ── Customer ──────────────────────────────────────────────────────────────
    SectionCard("Customer") {
        CustomerAutocomplete(
            value = state.customerName,
            suggestions = state.customers,
            onValueChange = { vm.updateCustomerName(it) },
            onSelect = { vm.selectCustomer(it) },
        )
        Spacer(Modifier.height(8.dp))
        AppInput(state.customerPhone,   { vm.updateCustomerPhone(it) },   "Phone",   keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
        Spacer(Modifier.height(8.dp))
        AppInput(state.customerGstin,   { vm.updateCustomerGstin(it) },   "GSTIN")
        Spacer(Modifier.height(8.dp))
        AppInput(state.customerAddress, { vm.updateCustomerAddress(it) }, "Address", singleLine = false)
    }

    // ── Items ─────────────────────────────────────────────────────────────────
    SectionCard("Items") {
        val hasTax = state.shop?.taxEnabled == true
        // Header
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("Name", "HSN", "Qty", "Rate").forEach { h ->
                Text(h, fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(when(h) { "Name" -> 2.5f; "HSN" -> 1.2f; "Qty" -> 0.8f; else -> 1.2f }))
            }
            if (hasTax) Text("Tax%", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.8f))
            Text("Total", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.2f))
            Spacer(Modifier.width(32.dp))
        }
        Spacer(Modifier.height(4.dp))

        state.draftItems.forEachIndexed { idx, draft ->
            val lineTotal = (draft.qty.toDoubleOrNull() ?: 0.0) * (draft.rate.toDoubleOrNull() ?: 0.0)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                ItemNameField(
                    value = draft.name,
                    suggestions = state.products,
                    onValueChange = { vm.updateItem(draft.id, draft.copy(name = it)) },
                    onSelect = { vm.autofillProduct(draft.id, it) },
                    modifier = Modifier.weight(2.5f),
                )
                SmallField(draft.hsn,  { vm.updateItem(draft.id, draft.copy(hsn = it)) },  "HSN",  Modifier.weight(1.2f))
                SmallField(draft.qty,  { vm.updateItem(draft.id, draft.copy(qty = it)) },  "Qty",  Modifier.weight(0.8f), KeyboardType.Decimal)
                SmallField(draft.rate, { vm.updateItem(draft.id, draft.copy(rate = it)) }, "Rate", Modifier.weight(1.2f), KeyboardType.Decimal)
                if (hasTax) SmallField(draft.taxRate, { vm.updateItem(draft.id, draft.copy(taxRate = it)) }, "Tax%", Modifier.weight(0.8f), KeyboardType.Decimal)
                Text(Money.format(lineTotal), fontSize = 12.sp, modifier = Modifier.weight(1.2f))
                IconButton(onClick = { if (state.draftItems.size > 1) vm.removeItem(draft.id) }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = DangerRed, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(4.dp))
        }
        TextButton(onClick = { vm.addItem() }) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(" Add Row", fontSize = 13.sp)
        }
    }

    // ── Bill Details ──────────────────────────────────────────────────────────
    SectionCard(if (state.documentType == "QUOTATION") "Quotation Details" else "Bill Details") {
        AppInput(state.billNo, { vm.updateBillNo(it) }, if (state.documentType == "QUOTATION") "Quotation No *" else "Bill No *")
        Spacer(Modifier.height(8.dp))
        AppInput(state.date,   { vm.updateDate(it) },   "Date (DD/MM/YYYY)")
        Spacer(Modifier.height(8.dp))
        AppInput(state.discount, { vm.updateDiscount(it) }, "Discount", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        if (state.documentType != "QUOTATION") {
            Spacer(Modifier.height(8.dp))
            PaymentModeDropdown(state.paymentMode) { vm.updatePaymentMode(it) }
            Spacer(Modifier.height(8.dp))
            AppInput(state.received, { vm.updateReceived(it) }, "Received (blank = total)", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        }
        Spacer(Modifier.height(8.dp))
        AppInput(state.notes, { vm.updateNotes(it) }, "Notes", singleLine = false)
    }

    // ── Actions ───────────────────────────────────────────────────────────────
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        AppButton("Reset", onClick = { setReset(true) }, secondary = true, modifier = Modifier.weight(1f))
        AppButton("Generate", onClick = { vm.generate {} }, enabled = !state.generating, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun CustomerAutocomplete(
    value: String,
    suggestions: List<Customer>,
    onValueChange: (String) -> Unit,
    onSelect: (Customer) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val filtered = suggestions.filter { value.length >= 1 && it.name.contains(value, ignoreCase = true) }

    Column {
        AppInput(
            value = value,
            onValueChange = { onValueChange(it); expanded = it.isNotEmpty() },
            label = "Customer Name (blank = Walk-in)",
        )
        if (expanded && filtered.isNotEmpty()) {
            Card(shape = RoundedCornerShape(8.dp), elevation = CardDefaults.cardElevation(4.dp)) {
                Column {
                    filtered.take(5).forEach { c ->
                        Text(
                            text = c.name + if (c.phone.isNotEmpty()) " | ${c.phone}" else "",
                            modifier = Modifier.fillMaxWidth().clickable { onSelect(c); expanded = false }.padding(12.dp),
                            fontSize = 14.sp,
                        )
                        HorizontalDivider(color = LineBlue)
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemNameField(
    value: String,
    suggestions: List<Product>,
    onValueChange: (String) -> Unit,
    onSelect: (Product) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val filtered = suggestions.filter { value.length >= 1 && it.name.contains(value, ignoreCase = true) }

    Column(modifier = modifier) {
        SmallField(value, { onValueChange(it); expanded = it.isNotEmpty() }, "Name *", Modifier.fillMaxWidth())
        if (expanded && filtered.isNotEmpty()) {
            Card(shape = RoundedCornerShape(6.dp), elevation = CardDefaults.cardElevation(4.dp)) {
                Column {
                    filtered.take(5).forEach { p ->
                        Text(
                            text = p.name,
                            modifier = Modifier.fillMaxWidth().clickable { onSelect(p); expanded = false }.padding(8.dp),
                            fontSize = 13.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SmallField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 10.sp) },
        modifier = modifier,
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentNavy, unfocusedBorderColor = LineBlue),
    )
}

private val PAYMENT_MODES = listOf("CASH", "UPI", "CARD", "NET BANKING", "CREDIT")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentModeDropdown(selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Payment Mode") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentNavy, unfocusedBorderColor = LineBlue),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            PAYMENT_MODES.forEach { mode ->
                DropdownMenuItem(text = { Text(mode) }, onClick = { onSelect(mode); expanded = false })
            }
        }
    }
}

@Composable
fun SummaryCard(state: DashboardUiState) {
    val shop = state.shop ?: return
    val hasTax = shop.taxEnabled
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AccentNavy)
            HorizontalDivider(color = LineBlue)
            SummaryRow("Customer", state.customerName.ifBlank { "Walk-in Customer" })
            SummaryRow("Bill No", state.billNo)
            SummaryRow("Date", state.date)
            if (state.draftItems.any { it.name.isNotBlank() }) {
                Spacer(Modifier.height(4.dp))
                state.draftItems.filter { it.name.isNotBlank() }.forEach { d ->
                    val lt = (d.qty.toDoubleOrNull() ?: 0.0) * (d.rate.toDoubleOrNull() ?: 0.0)
                    SummaryRow(
                        "${d.name} x${d.qty.toDoubleOrNull()?.let { "%.0f".format(it) } ?: d.qty}",
                        Money.format(lt),
                    )
                }
                HorizontalDivider(color = LineBlue)
            }
            SummaryRow("Subtotal", Money.format(state.subtotal))
            val disc = state.discount.toDoubleOrNull() ?: 0.0
            if (disc > 0) SummaryRow("Discount", "- ${Money.format(disc)}")
            if (hasTax && state.taxAmount > 0) SummaryRow(shop.taxLabel, Money.format(state.taxAmount))

            // TOTAL highlighted
            Box(modifier = Modifier.fillMaxWidth().background(AccentNavy.copy(alpha = 0.12f), RoundedCornerShape(8.dp)).padding(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("TOTAL", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AccentNavy)
                    Text(Money.format(state.grandTotal), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AccentNavy)
                }
            }

            if (state.documentType != "QUOTATION") {
                SummaryRow("Received", Money.format(state.receivedAmount))
                SummaryRow("Balance", Money.format(state.balance), if (state.balance > 0.01) DangerRed else OkGreen)
            }

            Spacer(Modifier.height(4.dp))
            Text(
                Money.amountInWords(state.grandTotal),
                fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Medium,
            )

            if (state.savedPdfPath.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = OkGreen, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Saved as PDF", fontSize = 12.sp, color = OkGreen)
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, valueColor: Color = TextDark) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = TextMuted)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = valueColor)
    }
}
