package com.persianvoice.assistant.tools.calculator

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorToolTest {

    private val tool = CalculatorTool()

    @Test
    fun `calculate simple multiplication`() = runTest {
        val result = tool.execute(mapOf("expression" to "25 * 47"))
        assertTrue(result is ToolResult.Success)
        assertEquals("1175.0", (result as ToolResult.Success).data)
    }

    @Test
    fun `calculate with Persian digits`() = runTest {
        val result = tool.execute(mapOf("expression" to "۲۵ * ۴۷"))
        assertTrue(result is ToolResult.Success)
        assertEquals("1175.0", (result as ToolResult.Success).data)
    }

    @Test
    fun `calculate with parenthesized expression`() = runTest {
        val result = tool.execute(mapOf("expression" to "(10 + 5) * 2"))
        assertTrue(result is ToolResult.Success)
        assertEquals("30.0", (result as ToolResult.Success).data)
    }

    @Test
    fun `returns failure for invalid expression`() = runTest {
        val result = tool.execute(mapOf("expression" to "abc"))
        assertTrue(result is ToolResult.Failure)
    }

    @Test
    fun `returns failure for missing expression`() = runTest {
        val result = tool.execute(emptyMap())
        assertTrue(result is ToolResult.Failure)
    }
}