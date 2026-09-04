package com.persianvoice.assistant.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.rememberNavController
import com.persianvoice.assistant.ui.navigation.PersianVoiceAssistantNavHost

/**
 * ریشه UI - تمام صفحات از اینجا mount می‌شوند.
 * به‌صورت پیش‌فرض RTL است.
 */
@Composable
fun PersianVoiceAssistantRoot() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val navController = rememberNavController()
        PersianVoiceAssistantNavHost(navController = navController)
    }
}