package com.freeshop.billing.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freeshop.billing.ui.components.*
import com.freeshop.billing.ui.theme.BgBlue
import com.freeshop.billing.ui.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    onSuccess: () -> Unit,
    onBack: () -> Unit,
) {
    val ctx = LocalContext.current
    val vm: AuthViewModel = viewModel(factory = AuthViewModel.Factory(ctx))
    val loading by vm.loading.collectAsStateWithLifecycle()
    val error   by vm.error.collectAsStateWithLifecycle()

    var name    by remember { mutableStateOf("") }
    var email   by remember { mutableStateOf("") }
    var pass    by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    LaunchedEffect(error) {
        if (error != null) { /* shown inline */ }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgBlue)) {
        Scaffold(
            topBar = { TopBar("Create Shop", onBack = onBack) },
            containerColor = BgBlue,
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                AppInput(value = name,    onValueChange = { name    = it; vm.clearError() }, label = "Shop Name *")
                AppInput(value = email,   onValueChange = { email   = it; vm.clearError() }, label = "Email *",    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                OutlinedTextField(
                    value = pass, onValueChange = { pass = it; vm.clearError() },
                    label = { Text("Password * (min 6)") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                )
                OutlinedTextField(
                    value = confirm, onValueChange = { confirm = it; vm.clearError() },
                    label = { Text("Confirm Password *") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                )
                if (error != null) Text(error!!, color = com.freeshop.billing.ui.theme.DangerRed)
                AppButton(
                    text    = if (loading) "Creating..." else "Create Shop",
                    onClick = { vm.register(name, email, pass, confirm, onSuccess) },
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        if (loading) LoadingOverlay(true)
    }
}
