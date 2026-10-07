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
import com.freeshop.billing.data.db.entity.Product
import com.freeshop.billing.ui.components.*
import com.freeshop.billing.ui.theme.*
import com.freeshop.billing.ui.viewmodel.ItemsViewModel

@Composable
fun ItemsScreen(shopId: Long, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val vm: ItemsViewModel = viewModel(factory = ItemsViewModel.Factory(ctx, shopId))
    val items by vm.items.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()

    var name    by remember { mutableStateOf("") }
    var unit    by remember { mutableStateOf("PCS") }
    var price   by remember { mutableStateOf("") }
    var taxRate by remember { mutableStateOf("0") }
    var hsn     by remember { mutableStateOf("") }

    var confirmDelete by remember { mutableStateOf<Product?>(null) }
    var toast by remember { mutableStateOf<ToastMessage?>(null) }

    LaunchedEffect(error) {
        if (error != null) { toast = ToastMessage(error!!, ToastType.ERROR); vm.clearError() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = { TopBar("Items / Products", onBack = onBack) },
            containerColor = BgBlue,
        ) { padding ->
            LazyColumn(
                modifier = Modifier.padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    SectionCard("Add Item") {
                        AppInput(name,    { name    = it }, "Name *")
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppInput(unit, { unit = it }, "Unit", modifier = Modifier.weight(1f))
                            AppInput(hsn,  { hsn  = it }, "HSN",  modifier = Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppInput(price,   { price   = it }, "Price *",  modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                            AppInput(taxRate, { taxRate = it }, "Tax %",    modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                        }
                        Spacer(Modifier.height(10.dp))
                        AppButton(
                            text = "Add Item",
                            onClick = {
                                vm.add(name, unit, price, taxRate, hsn) {
                                    name = ""; unit = "PCS"; price = ""; taxRate = "0"; hsn = ""
                                    toast = ToastMessage("Item added", ToastType.OK)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                if (items.isEmpty()) {
                    item { EmptyState(Icons.Default.Inventory2, "No items yet") }
                } else {
                    items(items, key = { it.id }) { p ->
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
                                    Text(p.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("${p.unit}  Rs ${p.price}", color = TextMuted, fontSize = 13.sp)
                                        if (p.taxRate > 0) Text("Tax: ${p.taxRate}%", color = AccentNavy, fontSize = 12.sp)
                                        if (p.hsn.isNotEmpty()) Text("HSN: ${p.hsn}", color = TextMuted, fontSize = 12.sp)
                                    }
                                }
                                IconButton(onClick = { confirmDelete = p }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = DangerRed)
                                }
                            }
                        }
                    }
                }
            }
        }

        confirmDelete?.let { p ->
            AlertDialog(
                onDismissRequest = { confirmDelete = null },
                title = { Text("Delete Item") },
                text  = { Text("Delete '${p.name}'?") },
                confirmButton = {
                    TextButton(onClick = { vm.delete(p); confirmDelete = null; toast = ToastMessage("Item deleted", ToastType.OK) }) {
                        Text("Delete", color = DangerRed)
                    }
                },
                dismissButton = { TextButton(onClick = { confirmDelete = null }) { Text("Cancel") } },
            )
        }

        AppToastHost(toast) { toast = null }
    }
}
