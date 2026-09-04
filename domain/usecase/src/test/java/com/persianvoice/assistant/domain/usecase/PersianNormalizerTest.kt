package com.persianvoice.assistant.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PersianNormalizerTest {

    private val normalizer = PersianNormalizer()

    @Test
    fun `normalize replaces Persian digits with standard digits`() {
        val result = normalizer.normalize("۱۲۳۴۵")
        assertEquals("12345", result)
    }

    @Test
    fun `normalize replaces Arabic digits with standard digits`() {
        val result = normalizer.normalize("١٢٣٤٥")
        assertEquals("12345", result)
    }

    @Test
    fun `normalize replaces Arabic yeh and keh`() {
        val result = normalizer.normalize("علي كاظم")
        assertEquals("علی کاظم", result)
    }

    @Test
    fun `normalize removes extra spaces`() {
        val result = normalizer.normalize("سلام    دنیا")
        assertEquals("سلام دنیا", result)
    }
}