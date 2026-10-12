package com.freeshop.billing.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freeshop.billing.ui.theme.*
import kotlinx.coroutines.delay

enum class ToastType { INFO, OK, ERROR }

data class ToastMessage(
    val message: String,
    val type: ToastType = ToastType.INFO,
    val id: Long = System.currentTimeMillis(),
)

@Composable
fun AppToastHost(toast: ToastMessage?, onDismiss: () -> Unit) {
    LaunchedEffect(toast?.id) {
        if (toast != null) {
            delay(3200)
            onDismiss()
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        AnimatedVisibility(
            visible = toast != null,
            enter   = slideInVertically(tween(300)) { -it } + fadeIn(tween(300)),
            exit    = slideOutVertically(tween(250)) { -it } + fadeOut(tween(250)),
        ) {
            if (toast != null) {
                val bg = when (toast.type) {
                    ToastType.INFO  -> AccentNavy
                    ToastType.OK    -> OkGreen
                    ToastType.ERROR -> DangerRed
                }
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp, start = 20.dp, end = 20.dp)
                        .background(bg, RoundedCornerShape(10.dp))
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    Text(toast.message, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
