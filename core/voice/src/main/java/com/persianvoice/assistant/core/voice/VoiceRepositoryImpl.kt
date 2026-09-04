package com.persianvoice.assistant.core.voice

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.persianvoice.assistant.domain.model.SpeechResult
import com.persianvoice.assistant.domain.repository.VoiceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * پیاده‌سازی VoiceRepository که STT و TTS را ترکیب می‌کند.
 */
@Singleton
class VoiceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val stt: SpeechToTextService,
    private val tts: TextToSpeechService
) : VoiceRepository {

    init {
        tts.initialize()
    }

    override suspend fun hasMicrophonePermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED

    override suspend fun isSttAvailable(): Boolean =
        android.speech.SpeechRecognizer.isRecognitionAvailable(context)

    override suspend fun startListening(language: String) {
        if (hasMicrophonePermission() && isSttAvailable()) {
            stt.startListening(language)
        }
    }

    override suspend fun stopListening() = stt.stopListening()

    override fun observeSpeechResults(): Flow<SpeechResult> = stt.observeResult()

    override suspend fun isTtsAvailable(): Boolean = true

    override suspend fun speak(text: String, language: String) =
        tts.speak(text, language)

    override suspend fun stopSpeaking() = tts.stop()

    override suspend fun setSpeechSpeed(speed: Float) = tts.setSpeechRate(speed)

    override suspend fun setSpeechPitch(pitch: Float) = tts.setPitch(pitch)
}