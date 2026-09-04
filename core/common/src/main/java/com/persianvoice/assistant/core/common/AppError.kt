package com.persianvoice.assistant.core.common

/**
 * خطاهای ساختاریافته اپ - برای Error Handling بهتر.
 */
sealed class AppError(message: String) : Exception(message) {
    data class NetworkError(override val message: String = "خطای شبکه") : AppError(message)
    data class AiProviderError(override val message: String = "خطای سرویس هوش مصنوعی") : AppError(message)
    data class PermissionError(override val message: String = "مجوز لازم نیست") : AppError(message)
    data class ToolError(override val message: String = "خطای اجرای ابزار") : AppError(message)
    data class ValidationError(override val message: String = "ورودی نامعتبر") : AppError(message)
    data class DatabaseError(override val message: String = "خطای پایگاه داده") : AppError(message)
    data class SpeechError(override val message: String = "خطای تشخیص گفتار") : AppError(message)
    data class UnknownError(override val message: String = "خطای ناشناخته") : AppError(message)
}

/**
 * تبدیل Exception های عمومی به AppError.
 */
fun Throwable.toAppError(): AppError = when (this) {
    is AppError -> this
    is java.net.UnknownHostException,
    is java.net.SocketTimeoutException -> AppError.NetworkError()
    is java.io.IOException -> AppError.NetworkError(message ?: "خطای شبکه")
    else -> AppError.UnknownError(message ?: this::class.java.simpleName)
}

/**
 * تبدیل خطا به پیام فارسی user-friendly.
 */
fun AppError.toUserMessage(): String = when (this) {
    is AppError.NetworkError -> "اتصال به اینترنت برقرار نیست."
    is AppError.AiProviderError -> "اتصال به سرویس هوش مصنوعی برقرار نشد. لطفاً اتصال اینترنت یا تنظیمات Provider را بررسی کنید."
    is AppError.PermissionError -> "برای این کار به مجوز نیاز دارم."
    is AppError.ToolError -> message
    is AppError.ValidationError -> message
    is AppError.DatabaseError -> "خطا در دسترسی به حافظه."
    is AppError.SpeechError -> "در تشخیص گفتار مشکلی پیش آمد."
    is AppError.UnknownError -> "خطای ناشناخته‌ای رخ داد."
}