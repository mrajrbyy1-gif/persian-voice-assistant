package com.persianvoice.assistant.tools.browser

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.persianvoice.assistant.domain.model.RiskLevel
import com.persianvoice.assistant.tools.AssistantTool
import com.persianvoice.assistant.tools.ToolResult
import com.persianvoice.assistant.tools.stringProp
import com.persianvoice.assistant.tools.toolDefinition
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tool باز کردن Browser با URL.
 *
 * مثال:
 * - "گوگل رو باز کن" -> https://google.com
 * - "سایت GitHub رو باز کن" -> https://github.com
 */
@Singleton
class BrowserTool @Inject constructor(
    @ApplicationContext private val context: Context
) : AssistantTool {

    override val id = "browser.open"
    override val name = "باز کردن مرورگر"
    override val description = "باز کردن یک URL در مرورگر پیش‌فرض دستگاه"
    override val riskLevel = RiskLevel.LOW

    override suspend fun execute(arguments: Map<String, Any?>): ToolResult {
        val url = (arguments["url"] as? String)?.trim()
            ?: return ToolResult.Failure("پارامتر url الزامی است")

        val normalizedUrl = normalizeUrl(url)
        if (!isValidUrl(normalizedUrl)) {
            return ToolResult.Failure("آدرس اینترنتی نامعتبر است")
        }

        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(normalizedUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ToolResult.Success("در حال باز کردن $normalizedUrl")
        } catch (e: Exception) {
            ToolResult.Failure("خطا در باز کردن مرورگر: ${e.message}")
        }
    }

    override fun definition() = toolDefinition(
        name = id,
        description = description,
        properties = mapOf(
            "url" to stringProp("آدرس اینترنتی کامل یا دامنه (مثلاً 'google.com' یا 'https://github.com')")
        ),
        required = listOf("url")
    )

    private fun normalizeUrl(url: String): String {
        val trimmed = url.trim()
        return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "https://$trimmed"
        }
    }

    private fun isValidUrl(url: String): Boolean {
        return try {
            val uri = Uri.parse(url)
            uri.scheme?.startsWith("http") == true && !uri.host.isNullOrBlank()
        } catch (e: Exception) {
            false
        }
    }
}