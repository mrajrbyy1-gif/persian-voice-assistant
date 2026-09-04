package com.persianvoice.assistant.tools

import com.persianvoice.assistant.domain.model.RiskLevel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolRegistryTest {

    @Test
    fun `register and retrieve tool`() {
        val registry = ToolRegistry()
        val tool = TestTool()
        registry.register(tool)

        assertEquals(tool, registry.get("test.tool"))
        assertTrue(registry.getAll().contains(tool))
    }

    @Test
    fun `returns null for non-existent tool`() {
        val registry = ToolRegistry()
        assertEquals(null, registry.get("nonexistent"))
    }

    private class TestTool : AssistantTool {
        override val id = "test.tool"
        override val name = "Test"
        override val description = "A test tool"
        override val riskLevel = RiskLevel.LOW

        override suspend fun execute(arguments: Map<String, Any?>) =
            ToolResult.Success("ok")

        override fun definition() = toolDefinition(
            name = id,
            description = description,
            properties = mapOf("input" to stringProp("input")),
            required = listOf("input")
        )
    }
}