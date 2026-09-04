package com.persianvoice.assistant.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.persianvoice.assistant.ui.home.HomeScreen
import com.persianvoice.assistant.ui.settings.SettingsScreen

/**
 * تمام route های اپ در این فایل تعریف می‌شوند.
 */
object Routes {
    const val HOME = "home"
    const val CONVERSATION = "conversation/{conversationId}"
    const val SETTINGS = "settings"
    const val SETTINGS_AI = "settings/ai"
    const val SETTINGS_VOICE = "settings/voice"
    const val SETTINGS_MEMORY = "settings/memory"
    const val SETTINGS_PERMISSIONS = "settings/permissions"
    const val ABOUT = "about"

    fun conversation(id: Long) = "conversation/$id"
}

/**
 * NavHost اصلی - تمام صفحات اینجا mount می‌شوند.
 */
@Composable
fun PersianVoiceAssistantNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen()
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        // سایر صفحات در فازهای بعدی
    }
}