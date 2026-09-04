package com.persianvoice.assistant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily

// رنگ‌های تم - مطابق colors.xml اما برای Compose
private val PrimaryLight = Color(0xFF1976D2)
private val PrimaryDark = Color(0xFF0D47A1)
private val PrimaryContainerLight = Color(0xFFBBDEFB)
private val AccentLight = Color(0xFFFF5722)

private val PrimaryDarkNight = Color(0xFF90CAF9)
private val PrimaryContainerDark = Color(0xFF1976D2)
private val AccentDark = Color(0xFFFF8A65)

private val LightColors = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = PrimaryDark,
    secondary = AccentLight,
    onSecondary = Color.White,
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF212121),
    surface = Color(0xFFFAFAFA),
    onSurface = Color(0xFF212121),
    error = Color(0xFFD32F2F),
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = PrimaryDarkNight,
    onPrimary = Color.Black,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = Color(0xFFBBDEFB),
    secondary = AccentDark,
    onSecondary = Color.Black,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    error = Color(0xFFEF5350),
    onError = Color.Black
)

// تایپوگرافی فارسی‌پسند
private val PersianTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 30.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
)

/**
 * تم اصلی اپ - RTL و فارسی‌پسند
 */
@Composable
fun PersianVoiceAssistantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = PersianTypography,
        content = content
    )
}