package com.freeshop.billing.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freeshop.billing.ui.theme.*

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    secondary: Boolean = false,
) {
    if (secondary) {
        OutlinedButton(
            onClick  = onClick,
            modifier = modifier.height(44.dp),
            enabled  = enabled,
            shape    = RoundedCornerShape(10.dp),
            colors   = ButtonDefaults.outlinedButtonColors(contentColor = AccentNavy),
        ) {
            Text(text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    } else {
        Button(
            onClick  = onClick,
            modifier = modifier.height(44.dp),
            enabled  = enabled,
            shape    = RoundedCornerShape(10.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = AccentNavy,
                contentColor   = Color.White,
                disabledContainerColor = AccentNavy.copy(alpha = 0.5f),
            ),
        ) {
            Text(text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}
