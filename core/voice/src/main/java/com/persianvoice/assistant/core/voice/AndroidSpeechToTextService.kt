package com.persianvoice.assistant.core.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.persianvoice.assistant.domain.model.SpeechResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * پیاده‌سازی STT با استفاده از Android SpeechRecognizer.
 * در فاز 5 کامل می‌شود. این پیاده‌سازی برای Persian (fa-IR) از موتور Google استفاده می‌کند.
 */
@Singleton
class AndroidSpeechToTextService @Inject constructor(
    @ApplicationContext private val context: Context
) : SpeechToTextService {

    private var recognizer: SpeechRecognizer? = null
    private val _results = MutableSharedFlow<SpeechResult>(
        replay = 0,
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private var listening = false

    override fun startListening(language: String) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            return
        }

        if (recognizer == null) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createListener())
            }
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        }

        listening = true
        recognizer?.startListening(intent)
    }

    override fun stopListening() {
        recognizer?.stopListening()
        listening = false
    }

    override fun cancel() {
        recognizer?.cancel()
        listening = false
    }

    override fun observeResult(): Flow<SpeechResult> = _results.asSharedFlow()

    override fun isListening(): Boolean = listening

    override suspend fun isLanguageAvailable(language: String): Boolean {
        // بررسی اینکه آیا زبان در لیست زبان‌های موجود است
        return try {
            Locale.forLanguageTag(language).displayName.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    private fun createListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {
            listening = false
        }

        override fun onError(error: Int) {
            listening = false
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "خطای ضبط صدا"
                SpeechRecognizer.ERROR_CLIENT -> "خطای سرویس‌گیرنده"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "مجوز میکروفون داده نشده"
                SpeechRecognizer.ERROR_NETWORK -> "خطای شبکه"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "پایان زمان شبکه"
                SpeechRecognizer.ERROR_NO_MATCH -> "نتیجه‌ای یافت نشد"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "سرویس مشغول"
                SpeechRecognizer.ERROR_SERVER -> "خطای سرور"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "پایان زمان گفتار"
                else -> "خطای ناشناخته"
            }
            _results.tryEmit(SpeechResult(text = errorMessage, isFinal = true))
        }

        override fun onResults(results: Bundle?) {
            listening = false
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val confidence = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
            matches?.firstOrNull()?.let { text ->
                _results.tryEmit(
                    SpeechResult(
                        text = text,
                        isFinal = true,
                        confidence = confidence?.firstOrNull()
                    )
                )
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            matches?.firstOrNull()?.let { text ->
                _results.tryEmit(SpeechResult(text = text, isFinal = false))
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    fun destroy() {
        recognizer?.destroy()
        recognizer = null
    }
}