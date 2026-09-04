package com.persianvoice.assistant.core.voice

import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * پیاده‌سازی TTS با استفاده از Android TextToSpeech.
 * در فاز 6 کامل می‌شود. برای فارسی ممکن است نیاز به نصب موتور TTS فارسی باشد.
 */
@Singleton
class AndroidTextToSpeechService @Inject constructor(
    @ApplicationContext private val context: Context
) : TextToSpeechService {

    private var tts: TextToSpeech? = null
    private var initialized = false
    private var currentLanguage: Locale = Locale("fa", "IR")
    private var rate: Float = 1.0f
    private var pitch: Float = 1.0f

    fun initialize(onReady: (Boolean) -> Unit = {}) {
        tts = TextToSpeech(context) { status ->
            initialized = status == TextToSpeech.SUCCESS
            if (initialized) {
                tts?.language = currentLanguage
                tts?.setSpeechRate(rate)
                tts?.setPitch(pitch)
            }
            onReady(initialized)
        }

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {}
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {}
        })
    }

    override suspend fun speak(text: String, language: String) {
        if (!initialized) return

        val locale = parseLocale(language)
        if (locale != currentLanguage) {
            currentLanguage = locale
            tts?.language = locale
        }

        val utteranceId = "utt_${System.currentTimeMillis()}"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } else {
            @Suppress("DEPRECATION")
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null)
        }
    }

    override fun stop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts?.stop()
        } else {
            @Suppress("DEPRECATION")
            tts?.stop()
        }
    }

    override suspend fun isLanguageAvailable(language: String): Boolean {
        if (!initialized) return false
        val locale = parseLocale(language)
        return tts?.isLanguageAvailable(locale) == TextToSpeech.LANG_AVAILABLE ||
                tts?.isLanguageAvailable(locale) == TextToSpeech.LANG_COUNTRY_AVAILABLE
    }

    override fun setSpeechRate(rate: Float) {
        this.rate = rate
        tts?.setSpeechRate(rate)
    }

    override fun setPitch(pitch: Float) {
        this.pitch = pitch
        tts?.setPitch(pitch)
    }

    override fun isSpeaking(): Boolean = tts?.isSpeaking == true

    private fun parseLocale(language: String): Locale {
        val parts = language.split("-")
        return if (parts.size >= 2) {
            Locale(parts[0], parts[1])
        } else {
            Locale(language)
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        initialized = false
    }
}