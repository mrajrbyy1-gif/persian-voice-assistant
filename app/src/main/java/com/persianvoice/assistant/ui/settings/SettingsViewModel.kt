package com.persianvoice.assistant.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.persianvoice.assistant.domain.model.AiProviderConfig
import com.persianvoice.assistant.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _providers = MutableStateFlow<List<AiProviderConfig>>(emptyList())
    val providers: StateFlow<List<AiProviderConfig>> = _providers.asStateFlow()

    private val _settings = MutableStateFlow(SettingsState())
    val settings: StateFlow<SettingsState> = _settings.asStateFlow()

    init {
        viewModelScope.launch {
            loadSettings()
        }
    }

    private suspend fun loadSettings() {
        _settings.value = SettingsState(
            voiceLanguage = settingsRepository.getVoiceLanguage(),
            speechSpeed = settingsRepository.getSpeechSpeed(),
            speechPitch = settingsRepository.getSpeechPitch(),
            autoSpeak = settingsRepository.isAutoSpeakEnabled(),
            darkMode = settingsRepository.isDarkModeEnabled(),
            wakeWord = settingsRepository.isWakeWordEnabled(),
            confirmationLevel = settingsRepository.getConfirmationLevel()
        )
        _providers.value = settingsRepository.getAiProviders()
    }

    fun saveApiKey(providerId: String, apiKey: String) {
        viewModelScope.launch {
            settingsRepository.saveApiKey(providerId, apiKey)
        }
    }

    fun setActiveProvider(providerId: String) {
        viewModelScope.launch {
            settingsRepository.setActiveProvider(providerId)
        }
    }

    fun setSpeechSpeed(speed: Float) {
        viewModelScope.launch {
            settingsRepository.setSpeechSpeed(speed)
            _settings.value = _settings.value.copy(speechSpeed = speed)
        }
    }

    fun setSpeechPitch(pitch: Float) {
        viewModelScope.launch {
            settingsRepository.setSpeechPitch(pitch)
            _settings.value = _settings.value.copy(speechPitch = pitch)
        }
    }

    fun setAutoSpeak(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoSpeakEnabled(enabled)
            _settings.value = _settings.value.copy(autoSpeak = enabled)
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkModeEnabled(enabled)
            _settings.value = _settings.value.copy(darkMode = enabled)
        }
    }

    fun setWakeWord(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setWakeWordEnabled(enabled)
            _settings.value = _settings.value.copy(wakeWord = enabled)
        }
    }

    fun setConfirmationLevel(level: String) {
        viewModelScope.launch {
            settingsRepository.setConfirmationLevel(level)
            _settings.value = _settings.value.copy(confirmationLevel = level)
        }
    }

    fun clearAllSecrets() {
        viewModelScope.launch {
            settingsRepository.clearAllSecrets()
        }
    }
}

data class SettingsState(
    val voiceLanguage: String = "fa-IR",
    val speechSpeed: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val autoSpeak: Boolean = true,
    val darkMode: Boolean = false,
    val wakeWord: Boolean = false,
    val confirmationLevel: String = "Smart"
)