package com.persianvoice.assistant.core.common

/**
 * Logger abstraction - اطلاعات حساس هرگز Log نشوند.
 */
interface AppLogger {
    fun debug(message: String)
    fun info(message: String)
    fun warning(message: String)
    fun error(message: String, throwable: Throwable? = null)
}

/**
 * پیاده‌سازی Production - فقط در debug log می‌کند و فیلتر امنیتی دارد.
 */
class AndroidLogger : AppLogger {

    private val sensitivePatterns = listOf(
        Regex("(?i)api[_-]?key\\s*[:=]\\s*\\S+"),
        Regex("(?i)password\\s*[:=]\\s*\\S+"),
        Regex("(?i)token\\s*[:=]\\s*\\S+"),
        Regex("(?i)bearer\\s+\\S+"),
        Regex("sk-\\w+"),
        Regex("sk-\\w{10,}"),
        Regex("\\b\\d{10,}\\b") // شماره تلفن
    )

    private fun sanitize(message: String): String {
        var result = message
        sensitivePatterns.forEach { pattern ->
            result = pattern.replace(result) { match ->
                when {
                    match.value.startsWith("sk-") -> "[API_KEY]"
                    match.value.contains("bearer", ignoreCase = true) -> "[TOKEN]"
                    match.value.contains("key", ignoreCase = true) -> "[KEY=REDACTED]"
                    match.value.contains("password", ignoreCase = true) -> "[PASSWORD=REDACTED]"
                    else -> "[REDACTED]"
                }
            }
        }
        return result
    }

    override fun debug(message: String) {
        if (BuildConfigBridge.isDebug) {
            android.util.Log.d("PVA", sanitize(message))
        }
    }

    override fun info(message: String) {
        if (BuildConfigBridge.isDebug) {
            android.util.Log.i("PVA", sanitize(message))
        }
    }

    override fun warning(message: String) {
        android.util.Log.w("PVA", sanitize(message))
    }

    override fun error(message: String, throwable: Throwable?) {
        android.util.Log.e("PVA", sanitize(message), throwable?.let { sanitize(it.message ?: "exception") })
    }
}

/**
 * Bridge برای دسترسی به BuildConfig.DEBUG بدون import مستقیم.
 */
object BuildConfigBridge {
    @Volatile
    var isDebug: Boolean = false
}