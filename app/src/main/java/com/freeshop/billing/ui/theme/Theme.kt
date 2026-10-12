package com.freeshop.billing.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AppColorScheme = lightColorScheme(
    primary          = AccentNavy,
    onPrimary        = CardWhite,
    primaryContainer = SummaryBg,
    background       = BgBlue,
    onBackground     = TextDark,
    surface          = CardWhite,
    onSurface        = TextDark,
    surfaceVariant   = BgBlue,
    onSurfaceVariant = TextMuted,
    error            = DangerRed,
    outline          = LineBlue,
)

@Composable
fun FreeShopTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography  = AppTypography,
        shapes      = AppShapes,
        content     = content,
    )
}
