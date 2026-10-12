package com.freeshop.billing.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.freeshop.billing.ui.components.*
import com.freeshop.billing.ui.theme.*
import com.freeshop.billing.ui.viewmodel.SettingsViewModel
import java.io.File

@Composable
fun SettingsScreen(
    shopId: Long,
    onBack: () -> Unit,
    onLogout: () -> Unit,
) {
    val ctx = LocalContext.current
    val vm: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(ctx, shopId))
    val shop by vm.shop.collectAsStateWithLifecycle()
    val saving by vm.saving.collectAsStateWithLifecycle()

    var toast by remember { mutableStateOf<ToastMessage?>(null) }

    val s = shop ?: return

    // Mutable form state synced from shop
    var shopName    by remember(s.id) { mutableStateOf(s.name) }
    var phone       by remember(s.id) { mutableStateOf(s.phone) }
    var address     by remember(s.id) { mutableStateOf(s.address) }
    var city        by remember(s.id) { mutableStateOf(s.city) }
    var state       by remember(s.id) { mutableStateOf(s.state) }
    var pincode     by remember(s.id) { mutableStateOf(s.pincode) }
    var gstin       by remember(s.id) { mutableStateOf(s.gstin) }
    var prefix      by remember(s.id) { mutableStateOf(s.invoicePrefix) }
    var taxEnabled  by remember(s.id) { mutableStateOf(s.taxEnabled) }
    var taxLabel    by remember(s.id) { mutableStateOf(s.taxLabel) }
    var taxRate     by remember(s.id) { mutableStateOf(s.taxRate.toString()) }
    var bankName    by remember(s.id) { mutableStateOf(s.bankName) }
    var accountNo   by remember(s.id) { mutableStateOf(s.accountNo) }
    var ifsc        by remember(s.id) { mutableStateOf(s.ifsc) }
    var upiId       by remember(s.id) { mutableStateOf(s.upiId) }
    var termsText   by remember(s.id) { mutableStateOf(s.terms) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) vm.pickLogo(uri) { err -> toast = ToastMessage(err, ToastType.ERROR) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopBar("Settings", onBack = onBack, actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                })
            },
            containerColor = BgBlue,
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // ── Business Identity ──────────────────────────────────────────
                SectionCard("Business Identity") {
                    // Logo
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier.size(72.dp).clip(RoundedCornerShape(10.dp)).background(LineBlue),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (s.logoPath.isNotEmpty() && File(s.logoPath).exists()) {
                                AsyncImage(model = File(s.logoPath), contentDescription = "Logo", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            } else {
                                Icon(Icons.Default.Store, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                            }
                        }
                        OutlinedButton(
                            onClick = { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            shape = RoundedCornerShape(8.dp),
                        ) { Text("Change Logo") }
                    }
                    Spacer(Modifier.height(10.dp))
                    AppInput(shopName, { shopName = it }, "Shop Name *")
                    Spacer(Modifier.height(8.dp))
                    AppInput(phone, { phone = it }, "Phone", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                    Spacer(Modifier.height(8.dp))
                    AppInput(gstin, { gstin = it }, "GSTIN")
                }

                // ── Address ────────────────────────────────────────────────────
                SectionCard("Address") {
                    AppInput(address, { address = it }, "Street Address")
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppInput(city, { city = it }, "City", modifier = Modifier.weight(1f))
                        AppInput(state, { state = it }, "State", modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    AppInput(pincode, { pincode = it }, "Pincode", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }

                // ── Billing Preferences ────────────────────────────────────────
                SectionCard("Billing Preferences") {
                    AppInput(prefix, { prefix = it }, "Invoice Prefix (e.g. INV-)")
                    Spacer(Modifier.height(8.dp))
                    Text("Next Invoice No: ${s.nextInvoiceNo}", fontSize = 13.sp, color = TextMuted)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Enable Tax", fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Switch(checked = taxEnabled, onCheckedChange = { taxEnabled = it }, colors = SwitchDefaults.colors(checkedTrackColor = AccentNavy))
                    }
                    if (taxEnabled) {
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppInput(taxLabel, { taxLabel = it }, "Tax Label", modifier = Modifier.weight(1f))
                            AppInput(taxRate,  { taxRate  = it }, "Tax Rate %", modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                        }
                    }
                }

                // ── Bank Details ───────────────────────────────────────────────
                SectionCard("Bank Details") {
                    AppInput(bankName,  { bankName  = it }, "Bank Name")
                    Spacer(Modifier.height(8.dp))
                    AppInput(accountNo, { accountNo = it }, "Account Number", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    Spacer(Modifier.height(8.dp))
                    AppInput(ifsc,      { ifsc      = it }, "IFSC Code")
                    Spacer(Modifier.height(8.dp))
                    AppInput(upiId,     { upiId     = it }, "UPI ID")
                }

                // ── Terms ──────────────────────────────────────────────────────
                SectionCard("Terms & Conditions") {
                    OutlinedTextField(
                        value = termsText, onValueChange = { termsText = it },
                        label = { Text("Terms") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3, maxLines = 6,
                        shape = RoundedCornerShape(10.dp),
                    )
                }

                AppButton(
                    text = if (saving) "Saving..." else "Save Settings",
                    onClick = {
                        vm.save(
                            s.copy(
                                name = shopName.trim(), phone = phone.trim(), address = address.trim(),
                                city = city.trim(), state = state.trim(), pincode = pincode.trim(),
                                gstin = gstin.trim(), invoicePrefix = prefix.trim(),
                                taxEnabled = taxEnabled, taxLabel = taxLabel.trim(),
                                taxRate = taxRate.toDoubleOrNull() ?: s.taxRate,
                                bankName = bankName.trim(), accountNo = accountNo.trim(),
                                ifsc = ifsc.trim(), upiId = upiId.trim(), terms = termsText.trim(),
                            ),
                            onSuccess = { toast = ToastMessage("Settings saved", ToastType.OK) },
                            onError   = { err -> toast = ToastMessage(err, ToastType.ERROR) },
                        )
                    },
                    enabled  = !saving,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(20.dp))
            }
        }
        AppToastHost(toast) { toast = null }
    }
}
