package com.persianvoice.assistant.core.network

import com.persianvoice.assistant.core.network.NetworkResult.Failure
import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.math.pow

/**
 * Retry هوشمند - فقط برای خطاهای مناسب.
 * - Network unavailable
 * - Timeout
 * - 5xx
 *
 * برای خطاهای زیر retry نمی‌کند:
 * - 401, 403 (Authentication)
 * - Invalid API Key
 */
class RetryPolicy(
    private val maxAttempts: Int = 3,
    private val initialDelayMs: Long = 1000L,
    private val backoffMultiplier: Double = 2.0,
    private val maxDelayMs: Long = 10000L
) {

    suspend fun <T> execute(block: suspend () -> NetworkResult<T>): NetworkResult<T> {
        var attempt = 0
        var currentDelay = initialDelayMs

        while (attempt < maxAttempts) {
            val result = block()
            attempt++

            if (result is NetworkResult.Success) return result

            if (result is NetworkResult.Failure) {
                if (!shouldRetry(result)) return result
                if (attempt >= maxAttempts) return result

                delay(currentDelay)
                currentDelay = min(
                    (currentDelay * backoffMultiplier).toLong(),
                    maxDelayMs
                )
            } else {
                return result
            }
        }

        return Failure(IllegalStateException("Max retry attempts reached"))
    }

    private fun shouldRetry(failure: Failure): Boolean {
        val code = failure.code
        // اگر کد داده نشده (مثلاً timeout)، retry کن
        if (code == null) return true
        // 4xx (به جز 408 و 429) نباید retry شود
        return when (code) {
            in 500..599 -> true
            408, 429 -> true
            in 400..499 -> false
            else -> false
        }
    }
}