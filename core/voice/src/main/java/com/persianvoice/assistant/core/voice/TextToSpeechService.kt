package com.persianvoice.assistant.core.voice

/**
 * Interface سرویس تبدیل متن به گفتار.
 * پیاده‌سازی‌های مختلف (Android TTS، Google Cloud، Piper، ElevenLabs، و ...) قابل تعویض.
 */
interface TextToSpeechService {

    /**
     * خواندن متن با زبان مشخص.
     */
    suspend fun speak(text: String, language: String = "fa-IR")

    /**
     * توقف پخش.
     */
    fun stop()

    /**
     * آیا زبان مورد نظر در دسترس است؟
     */
    suspend fun isLanguageAvailable(language: String): Boolean

    /**
     * تنظیم سرعت گفتار.
     */
    fun setSpeechRate(rate: Float)

    /**
     * تنظیم زیر و بمی صدا.
     */
    fun setPitch(pitch: Float)

    /**
     * آیا در حال صحبت است؟
     */
    fun isSpeaking(): Boolean
}