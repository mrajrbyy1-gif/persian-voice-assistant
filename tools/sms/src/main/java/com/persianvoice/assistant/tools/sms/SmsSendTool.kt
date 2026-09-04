package com.persianvoice.assistant.tools.sms

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
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
 * Tool ارسال SMS.
 * نیاز به تأیید کاربر دارد (HIGH risk).
 *
 * مثال:
 * - "به علی پیام بده فردا ساعت ۸ میام"
 */
@Singleton
class SmsSendTool @Inject constructor(
    @ApplicationContext private val context: Context
) : AssistantTool {

    override val id = "sms.send"
    override val name = "ارسال پیام"
    override val description = "ارسال پیام کوتاه به یک شماره تلفن یا مخاطب"
    override val riskLevel = RiskLevel.HIGH

    override suspend fun execute(arguments: Map<String, Any?>): ToolResult {
        val number = (arguments["number"] as? String)?.let { normalizeNumber(it) }
            ?: return ToolResult.Failure("پارامتر number الزامی است")
        val message = arguments["message"] as? String
            ?: return ToolResult.Failure("پارامتر message الزامی است")

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) !=
            PackageManager.PERMISSION_GRANTED) {
            return ToolResult.RequiresPermission
        }

        return try {
            val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            } ?: return ToolResult.Failure("سرویس SMS در دسترس نیست")

            // تقسیم پیام‌های بلند به بخش‌های کوچکتر
            val parts = smsManager.divideMessage(message)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(number, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(number, null, message, null, null)
            }
            ToolResult.Success("پیام ارسال شد")
        } catch (e: SecurityException) {
            ToolResult.RequiresPermission
        } catch (e: Exception) {
            ToolResult.Failure("خطا در ارسال پیام: ${e.message}")
        }
    }

    override fun definition() = toolDefinition(
        name = id,
        description = description,
        properties = mapOf(
            "number" to stringProp("شماره تلفن گیرنده"),
            "message" to stringProp("متن پیام برای ارسال")
        ),
        required = listOf("number", "message")
    )

    private fun normalizeNumber(number: String): String {
        val persianDigits = listOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
        var result = number
        persianDigits.forEachIndexed { i, fa -> result = result.replace(fa, i.toString()) }
        return result.replace("[^0-9+]".toRegex(), "")
    }
}