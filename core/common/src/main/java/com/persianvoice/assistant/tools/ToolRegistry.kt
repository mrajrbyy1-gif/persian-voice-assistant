package com.persianvoice.assistant.tools

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Registry مرکزی Tool ها.
 * اضافه کردن Tool جدید نباید نیاز به تغییر Agent Engine داشته باشد.
 */
@Singleton
class ToolRegistry @Inject constructor() {

    private val tools = mutableMapOf<String, AssistantTool>()

    fun register(tool: AssistantTool) {
        tools[tool.id] = tool
    }

    fun get(id: String): AssistantTool? = tools[id]

    fun getAll(): List<AssistantTool> = tools.values.toList()

    fun getAllDefinitions(): List<com.persianvoice.assistant.domain.model.ToolDefinition> =
        tools.values.map { it.definition() }

    fun ids(): Set<String> = tools.keys.toSet()

    fun clear() = tools.clear()
}