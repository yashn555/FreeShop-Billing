package com.freeshop.billing.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freeshop.billing.ui.components.AppButton
import com.freeshop.billing.ui.theme.*

@Composable
fun LandingScreen(
    onRegister: () -> Unit,
    onLogin: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(AccentNavy, AccentNavyDark))),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Icon(
                Icons.Default.Store,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(72.dp),
            )
            Text(
                "FreeShop Billing",
                color      = Color.White,
                fontSize   = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center,
            )
            Text(
                "Professional invoicing for your business",
                color     = Color.White.copy(alpha = 0.8f),
                fontSize  = 16.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick  = onRegister,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = AccentNavy),
            ) {
                Text("Create a Shop", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            OutlinedButton(
                onClick  = onLogin,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border   = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White.copy(alpha = 0.7f)),
            ) {
                Text("Login to Existing Shop", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}
