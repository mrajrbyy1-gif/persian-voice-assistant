package com.persianvoice.assistant.domain.usecase

import com.persianvoice.assistant.domain.repository.VoiceRepository
import javax.inject.Inject

/**
 * شروع Listening - کنترل میکروفون و STT.
 */
class StartListeningUseCase @Inject constructor(
    private val voiceRepository: VoiceRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        if (!voiceRepository.hasMicrophonePermission()) {
            return Result.failure(SecurityException("Microphone permission required"))
        }
        if (!voiceRepository.isSttAvailable()) {
            return Result.failure(IllegalStateException("STT not available"))
        }
        voiceRepository.startListening("fa-IR")
        return Result.success(Unit)
    }
}

class StopListeningUseCase @Inject constructor(
    private val voiceRepository: VoiceRepository
) {
    suspend operator fun invoke() {
        voiceRepository.stopListening()
    }
}

class SpeakUseCase @Inject constructor(
    private val voiceRepository: VoiceRepository
) {
    suspend operator fun invoke(text: String) {
        voiceRepository.speak(text, "fa-IR")
    }
}

class StopSpeakingUseCase @Inject constructor(
    private val voiceRepository: VoiceRepository
) {
    suspend operator fun invoke() {
        voiceRepository.stopSpeaking()
    }
}