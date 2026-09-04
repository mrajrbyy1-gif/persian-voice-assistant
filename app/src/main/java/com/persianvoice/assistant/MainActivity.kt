package com.persianvoice.assistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.persianvoice.assistant.ui.PersianVoiceAssistantRoot
import com.persianvoice.assistant.ui.theme.PersianVoiceAssistantTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity اصلی - نقطه ورود UI
 * تمام صفحات از طریق Navigation Compose نمایش داده می‌شوند.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            PersianVoiceAssistantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PersianVoiceAssistantRoot()
                }
            }
        }
    }
}