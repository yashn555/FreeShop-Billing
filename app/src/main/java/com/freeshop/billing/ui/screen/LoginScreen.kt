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
import com.freeshop.billing.ui.theme.*
import com.freeshop.billing.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onSuccess: () -> Unit,
    onBack: () -> Unit,
) {
    val ctx = LocalContext.current
    val vm: AuthViewModel = viewModel(factory = AuthViewModel.Factory(ctx))
    val loading by vm.loading.collectAsStateWithLifecycle()
    val error   by vm.error.collectAsStateWithLifecycle()

    var email by remember { mutableStateOf("") }
    var pass  by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(BgBlue)) {
        Scaffold(
            topBar = { TopBar("Login", onBack = onBack) },
            containerColor = BgBlue,
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                AppInput(
                    value = email, onValueChange = { email = it; vm.clearError() },
                    label = "Email", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )
                OutlinedTextField(
                    value = pass, onValueChange = { pass = it; vm.clearError() },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    isError = error != null,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                )
                if (error != null) Text(error!!, color = DangerRed)
                AppButton(
                    text     = if (loading) "Logging in..." else "Login",
                    onClick  = { vm.login(email, pass, onSuccess) {} },
                    enabled  = !loading,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        if (loading) LoadingOverlay(true)
    }
}
