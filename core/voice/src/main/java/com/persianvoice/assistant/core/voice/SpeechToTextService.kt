package com.persianvoice.assistant.core.voice

import com.persianvoice.assistant.domain.model.SpeechResult
import kotlinx.coroutines.flow.Flow

/**
 * Interface سرویس تشخیص گفتار.
 * پیاده‌سازی‌های مختلف (Android، Google Cloud، Whisper، و ...) قابل تعویض هستند.
 */
interface SpeechToTextService {

    /**
     * شروع Listening با زبان مشخص.
     * پیش‌فرض: fa-IR
     */
    fun startListening(language: String = "fa-IR")

    /**
     * توقف Listening.
     */
    fun stopListening()

    /**
     * لغو Listening.
     */
    fun cancel()

    /**
     * نتایج تشخیص گفتار به صورت Flow.
     * هر بار که نتیجه‌ای آماده شد، emit می‌شود.
     */
    fun observeResult(): Flow<SpeechResult>

    /**
     * آیا سرویس در حالت Listening است؟
     */
    fun isListening(): Boolean

    /**
     * آیا زبان مورد نظر پشتیبانی می‌شود؟
     */
    suspend fun isLanguageAvailable(language: String): Boolean
}