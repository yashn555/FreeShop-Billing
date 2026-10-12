package com.freeshop.billing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.freeshop.billing.ui.nav.AppNav
import com.freeshop.billing.ui.theme.FreeShopTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FreeShopTheme {
                AppNav()
            }
        }
    }
}
