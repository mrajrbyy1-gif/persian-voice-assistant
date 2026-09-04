package com.persianvoice.assistant.domain.usecase

import com.persianvoice.assistant.domain.repository.SettingsRepository

class UpdateSettingsUseCase(private val settingsRepository: SettingsRepository) {
    suspend fun setApiKey(providerId: String, apiKey: String) =
        settingsRepository.saveApiKey(providerId, apiKey)

    suspend fun setActiveProvider(providerId: String) =
        settingsRepository.setActiveProvider(providerId)

    suspend fun setSpeechSpeed(speed: Float) =
        settingsRepository.setSpeechSpeed(speed)

    suspend fun setSpeechPitch(pitch: Float) =
        settingsRepository.setSpeechPitch(pitch)

    suspend fun setVoiceLanguage(lang: String) =
        settingsRepository.setVoiceLanguage(lang)

    suspend fun setDarkMode(enabled: Boolean) =
        settingsRepository.setDarkModeEnabled(enabled)

    suspend fun setAutoSpeak(enabled: Boolean) =
        settingsRepository.setAutoSpeakEnabled(enabled)

    suspend fun setWakeWord(enabled: Boolean) =
        settingsRepository.setWakeWordEnabled(enabled)
}