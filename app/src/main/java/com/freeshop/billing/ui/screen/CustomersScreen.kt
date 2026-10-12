package com.freeshop.billing.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freeshop.billing.data.db.entity.Customer
import com.freeshop.billing.ui.components.*
import com.freeshop.billing.ui.theme.*
import com.freeshop.billing.ui.viewmodel.CustomersViewModel

@Composable
fun CustomersScreen(shopId: Long, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val vm: CustomersViewModel = viewModel(factory = CustomersViewModel.Factory(ctx, shopId))
    val customers by vm.customers.collectAsStateWithLifecycle()
    val error     by vm.error.collectAsStateWithLifecycle()

    var name    by remember { mutableStateOf("") }
    var phone   by remember { mutableStateOf("") }
    var email   by remember { mutableStateOf("") }
    var gstin   by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    var confirmDelete by remember { mutableStateOf<Customer?>(null) }
    var toast by remember { mutableStateOf<ToastMessage?>(null) }

    LaunchedEffect(error) {
        if (error != null) { toast = ToastMessage(error!!, ToastType.ERROR); vm.clearError() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = { TopBar("Customers", onBack = onBack) },
            containerColor = BgBlue,
        ) { padding ->
            LazyColumn(
                modifier = Modifier.padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // ── Add form ────────────────────────────────────────────────────
                item {
                    SectionCard("Add Customer") {
                        AppInput(name,    { name    = it }, "Name *")
                        Spacer(Modifier.height(8.dp))
                        AppInput(phone,   { phone   = it }, "Phone",   keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                        Spacer(Modifier.height(8.dp))
                        AppInput(email,   { email   = it }, "Email",   keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                        Spacer(Modifier.height(8.dp))
                        AppInput(gstin,   { gstin   = it }, "GSTIN")
                        Spacer(Modifier.height(8.dp))
                        AppInput(address, { address = it }, "Address", singleLine = false)
                        Spacer(Modifier.height(10.dp))
                        AppButton(
                            text = "Add Customer",
                            onClick = {
                                vm.add(name, phone, email, gstin, address) {
                                    name = ""; phone = ""; email = ""; gstin = ""; address = ""
                                    toast = ToastMessage("Customer added", ToastType.OK)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                // ── List ────────────────────────────────────────────────────────
                if (customers.isEmpty()) {
                    item { EmptyState(Icons.Default.PeopleOutline, "No customers yet") }
                } else {
                    items(customers, key = { it.id }) { c ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            elevation = CardDefaults.cardElevation(1.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(c.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                    if (c.phone.isNotEmpty()) Text(c.phone, color = TextMuted, fontSize = 13.sp)
                                    if (c.gstin.isNotEmpty()) Text("GSTIN: ${c.gstin}", color = TextMuted, fontSize = 12.sp)
                                }
                                IconButton(onClick = { confirmDelete = c }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = DangerRed)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Delete confirm dialog
        confirmDelete?.let { c ->
            AlertDialog(
                onDismissRequest = { confirmDelete = null },
                title = { Text("Delete Customer") },
                text  = { Text("Delete '${c.name}'?") },
                confirmButton = {
                    TextButton(onClick = { vm.delete(c); confirmDelete = null; toast = ToastMessage("Customer deleted", ToastType.OK) }) {
                        Text("Delete", color = DangerRed)
                    }
                },
                dismissButton = { TextButton(onClick = { confirmDelete = null }) { Text("Cancel") } },
            )
        }

        AppToastHost(toast) { toast = null }
    }
}
