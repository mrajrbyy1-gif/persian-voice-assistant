package com.persianvoice.assistant.core.ai

import com.persianvoice.assistant.core.common.AppLogger
import com.persianvoice.assistant.core.network.ConnectivityMonitor
import com.persianvoice.assistant.domain.model.LlmRequest
import com.persianvoice.assistant.domain.model.LlmResponse
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * LLM Router - با Fallback هوشمند.
 *
 * اولویت:
 * 1. Provider انتخاب‌شده توسط کاربر
 * 2. Provider اصلی
 * 3. Provider جایگزین
 * 4. Offline fallback (پاسخ محلی ساده)
 */
@Singleton
class LlmRouter @Inject constructor(
    private val providers: Map<String, LlmProvider>,
    private val connectivityMonitor: ConnectivityMonitor,
    private val logger: AppLogger
) {

    private var activeProviderId: String? = null

    fun setActiveProvider(providerId: String) {
        activeProviderId = providerId
    }

    suspend fun generate(
        request: LlmRequest,
        fallbackOrder: List<String> = emptyList()
    ): LlmResponse {
        val isOnline = connectivityMonitor.isOnline.first()
        if (!isOnline) {
            return offlineFallback(request)
        }

        val order = buildList {
            activeProviderId?.let { add(it) }
            addAll(fallbackOrder)
            addAll(providers.keys)
        }.distinct()

        for (providerId in order) {
            val provider = providers[providerId] ?: continue
            try {
                if (provider.isAvailable()) {
                    logger.info("LLM Router: Using provider $providerId")
                    return provider.generate(request)
                }
            } catch (e: Exception) {
                logger.warning("LLM Router: Provider $providerId failed - ${e.message}")
            }
        }

        logger.error("All LLM providers failed")
        throw IllegalStateException("No LLM provider available")
    }

    suspend fun stream(
        request: LlmRequest,
        onChunk: suspend (String) -> Unit
    ): LlmResponse {
        val isOnline = connectivityMonitor.isOnline.first()
        if (!isOnline) {
            val fallback = offlineFallback(request)
            fallback.text?.let { onChunk(it) }
            return fallback
        }

        val providerId = activeProviderId ?: providers.keys.firstOrNull()
        val provider = providerId?.let { providers[it] }
            ?: throw IllegalStateException("No active LLM provider")

        return provider.stream(request, onChunk)
    }

    private fun offlineFallback(request: LlmRequest): LlmResponse {
        val lastMessage = request.messages.lastOrNull()?.content ?: ""
        return LlmResponse(
            text = "در حالت آفلاین، فقط فرمان‌های ساده پشتیبانی می‌شوند. برای پردازش کامل، لطفاً به اینترنت متصل شوید.",
            finishReason = "offline_fallback"
        )
    }

    fun getAvailableProviders(): List<LlmProvider> = providers.values.toList()
}