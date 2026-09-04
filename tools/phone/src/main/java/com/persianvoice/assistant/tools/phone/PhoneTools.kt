package com.persianvoice.assistant.tools.phone

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import com.persianvoice.assistant.domain.model.RiskLevel
import com.persianvoice.assistant.tools.AssistantTool
import com.persianvoice.assistant.tools.ToolResult
import com.persianvoice.assistant.tools.stringProp
import com.persianvoice.assistant.tools.toolDefinition
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tool برقراری تماس تلفنی.
 * نیاز به تأیید کاربر دارد (HIGH risk).
 *
 * مثال:
 * - "با علی تماس بگیر"
 * - "به شماره 09123456789 زنگ بزن"
 */
@Singleton
class PhoneCallTool @Inject constructor(
    @ApplicationContext private val context: Context
) : AssistantTool {

    override val id = "phone.call"
    override val name = "تماس تلفنی"
    override val description = "برقراری تماس تلفنی با مخاطب یا شماره مشخص"
    override val riskLevel = RiskLevel.HIGH

    override suspend fun execute(arguments: Map<String, Any?>): ToolResult {
        val phoneNumber = arguments["number"] as? String
            ?: return ToolResult.Failure("پارامتر number الزامی است")

        val normalized = normalizeNumber(phoneNumber)

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) !=
            PackageManager.PERMISSION_GRANTED) {
            return ToolResult.RequiresPermission
        }

        return try {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$normalized")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ToolResult.Success("در حال برقراری تماس")
        } catch (e: SecurityException) {
            ToolResult.RequiresPermission
        } catch (e: Exception) {
            ToolResult.Failure("خطا در برقراری تماس: ${e.message}")
        }
    }

    override fun definition() = toolDefinition(
        name = id,
        description = description,
        properties = mapOf(
            "number" to stringProp("شماره تلفن یا نام مخاطب برای تماس")
        ),
        required = listOf("number")
    )

    private fun normalizeNumber(number: String): String {
        val persianDigits = listOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
        var result = number
        persianDigits.forEachIndexed { i, fa -> result = result.replace(fa, i.toString()) }
        return result.replace("[^0-9+]".toRegex(), "")
    }
}

/**
 * Tool شماره‌گیری (Dial) - بدون برقراری تماس، فقط باز کردن صفحه شماره‌گیر.
 */
@Singleton
class PhoneDialTool @Inject constructor(
    @ApplicationContext private val context: Context
) : AssistantTool {

    override val id = "phone.dial"
    override val name = "شماره‌گیری"
    override val description = "باز کردن صفحه شماره‌گیر با شماره مشخص (بدون تماس واقعی)"
    override val riskLevel = RiskLevel.LOW

    override suspend fun execute(arguments: Map<String, Any?>): ToolResult {
        val phoneNumber = arguments["number"] as? String
            ?: return ToolResult.Failure("پارامتر number الزامی است")

        return try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phoneNumber.trim()}")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ToolResult.Success("در حال باز کردن شماره‌گیر")
        } catch (e: Exception) {
            ToolResult.Failure("خطا در باز کردن شماره‌گیر: ${e.message}")
        }
    }

    override fun definition() = toolDefinition(
        name = id,
        description = description,
        properties = mapOf(
            "number" to stringProp("شماره تلفن برای نمایش در شماره‌گیر")
        ),
        required = listOf("number")
    )
}