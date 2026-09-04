package com.persianvoice.assistant.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class KeywordIntentDetectorTest {

    private val detector = KeywordIntentDetector()

    @Test
    fun `detects call intent for phrase زنگ بزن`() = runTest {
        val match = detector.invoke("به علی زنگ بزن")
        assertEquals(AssistantIntent.CALL_CONTACT, match.intent)
    }

    @Test
    fun `detects sms intent for phrase پیام بده`() = runTest {
        val match = detector.invoke("به علی پیام بده سلام")
        assertEquals(AssistantIntent.SEND_SMS, match.intent)
    }

    @Test
    fun `detects battery intent`() = runTest {
        val match = detector.invoke("باتری چقدره؟")
        assertEquals(AssistantIntent.GET_BATTERY, match.intent)
    }

    @Test
    fun `detects weather intent`() = runTest {
        val match = detector.invoke("هوا چطوره؟")
        assertEquals(AssistantIntent.GET_WEATHER, match.intent)
    }

    @Test
    fun `detects calculator intent`() = runTest {
        val match = detector.invoke("25 ضربدر 47 چقدر میشه؟")
        assertEquals(AssistantIntent.CALCULATE, match.intent)
    }

    @Test
    fun `detects alarm intent`() = runTest {
        val match = detector.invoke("فردا ساعت ۷ بیدارم کن")
        assertEquals(AssistantIntent.SET_ALARM, match.intent)
    }

    @Test
    fun `falls back to general chat for unrecognized input`() = runTest {
        val match = detector.invoke("یه داستان بگو")
        assertEquals(AssistantIntent.GENERAL_CHAT, match.intent)
    }
}