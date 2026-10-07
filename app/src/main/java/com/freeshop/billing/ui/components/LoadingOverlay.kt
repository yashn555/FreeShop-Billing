package com.freeshop.billing.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.freeshop.billing.ui.theme.AccentNavy

@Composable
fun LoadingOverlay(loading: Boolean) {
    if (!loading) return
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = AccentNavy, strokeWidth = androidx.compose.ui.unit.Dp(3f))
    }
}
