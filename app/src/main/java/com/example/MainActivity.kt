package com.example

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.toArgb
import androidx.fragment.app.FragmentActivity
import com.example.notifications.ReminderReceiver
import com.example.ui.screens.DompetKuAppNavigation
import com.example.ui.theme.DompetKuTheme
import com.example.ui.theme.Slate950

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Slate950.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(Slate950.toArgb())
        )
        ReminderReceiver.ensureChannel(this)
        setContent {
            DompetKuTheme {
                DompetKuAppNavigation()
            }
        }
    }
}
