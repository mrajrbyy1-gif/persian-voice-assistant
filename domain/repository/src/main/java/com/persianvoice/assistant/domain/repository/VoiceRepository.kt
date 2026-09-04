package com.persianvoice.assistant.domain.repository

import com.persianvoice.assistant.domain.model.SpeechResult
import kotlinx.coroutines.flow.Flow

/**
 * Voice Repository - ترکیب STT و TTS در یک Interface.
 */
interface VoiceRepository {
    // STT
    suspend fun hasMicrophonePermission(): Boolean
    suspend fun isSttAvailable(): Boolean
    suspend fun startListening(language: String = "fa-IR")
    suspend fun stopListening()
    fun observeSpeechResults(): Flow<SpeechResult>

    // TTS
    suspend fun isTtsAvailable(): Boolean
    suspend fun speak(text: String, language: String = "fa-IR")
    suspend fun stopSpeaking()
    suspend fun setSpeechSpeed(speed: Float)
    suspend fun setSpeechPitch(pitch: Float)
}