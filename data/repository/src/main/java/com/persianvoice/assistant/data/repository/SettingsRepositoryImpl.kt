package com.persianvoice.assistant.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.persianvoice.assistant.domain.model.AiProviderConfig
import com.persianvoice.assistant.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    /**
     * EncryptedSharedPreferences برای نگهداری امن API Key و سایر Secret ها.
     */
    private val securePrefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val regularPrefs by lazy {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    }

    override suspend fun getAiProviders(): List<AiProviderConfig> {
        // بارگذاری از secure prefs - در فاز 8 کامل می‌شود
        return emptyList()
    }

    override suspend fun getActiveProvider(): AiProviderConfig? = null
    override suspend fun setActiveProvider(providerId: String) {}
    override suspend fun saveProvider(provider: AiProviderConfig) {}
    override suspend fun deleteProvider(providerId: String) {}
    override suspend fun getApiKey(providerId: String): String? =
        securePrefs.getString("api_key_$providerId", null)

    override suspend fun saveApiKey(providerId: String, apiKey: String) {
        securePrefs.edit().putString("api_key_$providerId", apiKey).apply()
    }

    override suspend fun getVoiceLanguage(): String =
        regularPrefs.getString("voice_language", "fa-IR") ?: "fa-IR"

    override suspend fun setVoiceLanguage(lang: String) {
        regularPrefs.edit().putString("voice_language", lang).apply()
    }

    override suspend fun getSpeechSpeed(): Float =
        regularPrefs.getFloat("speech_speed", 1.0f)

    override suspend fun setSpeechSpeed(speed: Float) {
        regularPrefs.edit().putFloat("speech_speed", speed).apply()
    }

    override suspend fun getSpeechPitch(): Float =
        regularPrefs.getFloat("speech_pitch", 1.0f)

    override suspend fun setSpeechPitch(pitch: Float) {
        regularPrefs.edit().putFloat("speech_pitch", pitch).apply()
    }

    override suspend fun isAutoSpeakEnabled(): Boolean =
        regularPrefs.getBoolean("auto_speak", true)

    override suspend fun setAutoSpeakEnabled(enabled: Boolean) {
        regularPrefs.edit().putBoolean("auto_speak", enabled).apply()
    }

    override suspend fun isDarkModeEnabled(): Boolean =
        regularPrefs.getBoolean("dark_mode", false)

    override suspend fun setDarkModeEnabled(enabled: Boolean) {
        regularPrefs.edit().putBoolean("dark_mode", enabled).apply()
    }

    override suspend fun isWakeWordEnabled(): Boolean =
        regularPrefs.getBoolean("wake_word", false)

    override suspend fun setWakeWordEnabled(enabled: Boolean) {
        regularPrefs.edit().putBoolean("wake_word", enabled).apply()
    }

    override suspend fun getConfirmationLevel(): String =
        regularPrefs.getString("confirmation_level", "Smart") ?: "Smart"

    override suspend fun setConfirmationLevel(level: String) {
        regularPrefs.edit().putString("confirmation_level", level).apply()
    }

    override suspend fun clearAllSecrets() {
        securePrefs.edit().clear().apply()
    }
}