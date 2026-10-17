package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.roverpulse.ui.dashboard.RoverDashboardScreen
import com.example.ui.theme.RoverPulseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RoverPulseTheme(darkTheme = true) {
                RoverDashboardScreen()
            }
        }
    }
}
