package com.persianvoice.assistant.tools

import com.persianvoice.assistant.core.common.AppLogger
import com.persianvoice.assistant.domain.model.AgentResult
import com.persianvoice.assistant.domain.model.ToolCall
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Agent Engine مرکزی - مغز متفکر دستیار.
 *
 * وظایف:
 * 1. دریافت متن از کاربر
 * 2. تصمیم‌گیری برای Tool یا پاسخ مستقیم
 * 3. اجرای Tool
 * 4. ترکیب نتیجه با LLM برای پاسخ طبیعی
 */
@Singleton
class AgentEngine @Inject constructor(
    private val toolRegistry: ToolRegistry,
    private val logger: AppLogger
) {

    /**
     * پردازش ورودی کاربر و برگرداندن نتیجه.
     * در حالت فعلی، tool execution فقط انجام می‌شود (کنترل LLM در پیاده‌سازی کامل‌تر).
     */
    suspend fun executeToolCall(toolCall: ToolCall): AgentResult {
        val tool = toolRegistry.get(toolCall.name)
            ?: return AgentResult.Error("ابزار ${toolCall.name} یافت نشد")

        val timeoutMs = when (toolCall.name) {
            in networkToolIds -> 15_000L
            else -> 5_000L
        }

        val startTime = System.currentTimeMillis()
        return try {
            val result = withTimeout(timeoutMs) {
                tool.execute(toolCall.arguments)
            }
            val duration = System.currentTimeMillis() - startTime

            when (result) {
                is ToolResult.Success -> AgentResult.Response(
                    text = result.data
                )
                is ToolResult.Failure -> AgentResult.Error(
                    message = result.error
                )
                ToolResult.RequiresPermission -> AgentResult.ToolExecution(toolCall)
            }
        } catch (e: TimeoutCancellationException) {
            logger.warning("Tool ${toolCall.name} timed out after ${timeoutMs}ms")
            AgentResult.Error("اجرای ${toolCall.name} بیش از حد طول کشید")
        } catch (e: Exception) {
            logger.error("Tool ${toolCall.name} failed", e)
            AgentResult.Error(e.message ?: "خطای ناشناخته در اجرای ابزار")
        }
    }

    private val networkToolIds = setOf(
        "web.search",
        "weather.current",
        "weather.forecast",
        "agent.manager.execute"
    )
}