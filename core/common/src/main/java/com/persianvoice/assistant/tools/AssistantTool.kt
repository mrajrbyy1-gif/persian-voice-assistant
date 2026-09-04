package com.persianvoice.assistant.tools

import com.persianvoice.assistant.domain.model.RiskLevel
import com.persianvoice.assistant.domain.model.ToolDefinition
import com.persianvoice.assistant.domain.model.ToolParameters
import com.persianvoice.assistant.domain.model.ToolProperty

/**
 * Base interface برای تمام Tool ها.
 */
interface AssistantTool {
    val id: String
    val name: String
    val description: String
    val riskLevel: RiskLevel

    /**
     * اجرای Tool با آرگومان‌ها.
     */
    suspend fun execute(arguments: Map<String, Any?>): ToolResult

    /**
     * تعریف Tool برای ارسال به LLM.
     */
    fun definition(): ToolDefinition
}

/**
 * نتیجه اجرای Tool.
 */
sealed class ToolResult {
    data class Success(val data: String) : ToolResult()
    data class Failure(val error: String) : ToolResult()
    data object RequiresPermission : ToolResult()
}

/**
 * سازنده ToolDefinition با DSL.
 */
fun toolDefinition(
    name: String,
    description: String,
    properties: Map<String, ToolProperty>,
    required: List<String> = emptyList()
): ToolDefinition = ToolDefinition(
    name = name,
    description = description,
    parameters = ToolParameters(
        type = "object",
        properties = properties,
        required = required
    )
)

fun stringProp(description: String, enum: List<String>? = null) =
    ToolProperty(type = "string", description = description, enum = enum)

fun numberProp(description: String) =
    ToolProperty(type = "number", description = description)

fun booleanProp(description: String) =
    ToolProperty(type = "boolean", description = description)