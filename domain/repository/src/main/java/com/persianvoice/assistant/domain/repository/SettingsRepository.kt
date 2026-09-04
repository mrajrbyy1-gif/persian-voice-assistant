package com.persianvoice.assistant.domain.repository

import com.persianvoice.assistant.domain.model.AiProviderConfig

/**
 * تنظیمات - شامل Provider، API Key، Theme، Voice.
 */
interface SettingsRepository {
    suspend fun getAiProviders(): List<AiProviderConfig>
    suspend fun getActiveProvider(): AiProviderConfig?
    suspend fun setActiveProvider(providerId: String)
    suspend fun saveProvider(provider: AiProviderConfig)
    suspend fun deleteProvider(providerId: String)
    suspend fun getApiKey(providerId: String): String?
    suspend fun saveApiKey(providerId: String, apiKey: String)

    suspend fun getVoiceLanguage(): String
    suspend fun setVoiceLanguage(lang: String)
    suspend fun getSpeechSpeed(): Float
    suspend fun setSpeechSpeed(speed: Float)
    suspend fun getSpeechPitch(): Float
    suspend fun setSpeechPitch(pitch: Float)
    suspend fun isAutoSpeakEnabled(): Boolean
    suspend fun setAutoSpeakEnabled(enabled: Boolean)
    suspend fun isDarkModeEnabled(): Boolean
    suspend fun setDarkModeEnabled(enabled: Boolean)
    suspend fun isWakeWordEnabled(): Boolean
    suspend fun setWakeWordEnabled(enabled: Boolean)
    suspend fun getConfirmationLevel(): String
    suspend fun setConfirmationLevel(level: String)

    suspend fun clearAllSecrets()
}