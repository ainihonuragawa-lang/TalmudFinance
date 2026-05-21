package com.talmudfinance.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.talmudfinance.app.ui.navigation.AppNavigation
import com.talmudfinance.app.ui.theme.TalmudFinanceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            TalmudFinanceTheme {
                AppNavigation()
            }
        }
    }
}
