package com.persianvoice.assistant.tools.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.persianvoice.assistant.domain.model.RiskLevel
import com.persianvoice.assistant.tools.AssistantTool
import com.persianvoice.assistant.tools.ToolResult
import com.persianvoice.assistant.tools.stringProp
import com.persianvoice.assistant.tools.toolDefinition
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tool تنظیم آلارم.
 *
 * مثال:
 * - "فردا ساعت ۷ صبح بیدارم کن"
 * - "ساعت ۸ آلارم بذار"
 */
@Singleton
class AlarmCreateTool @Inject constructor(
    @ApplicationContext private val context: Context
) : AssistantTool {

    override val id = "alarm.create"
    override val name = "تنظیم آلارم"
    override val description = "تنظیم آلارم در ساعت و تاریخ مشخص"
    override val riskLevel = RiskLevel.MEDIUM

    override suspend fun execute(arguments: Map<String, Any?>): ToolResult {
        val hour = (arguments["hour"] as? Number)?.toInt()
            ?: return ToolResult.Failure("پارامتر hour الزامی است")
        val minute = (arguments["minute"] as? Number)?.toInt() ?: 0
        val label = arguments["label"] as? String ?: "یادآور"

        if (hour !in 0..23 || minute !in 0..59) {
            return ToolResult.Failure("ساعت یا دقیقه نامعتبر است")
        }

        return try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                ?: return ToolResult.Failure("سرویس آلارم در دسترس نیست")

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                // اگر زمان گذشته، فردا تنظیم شود
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val intent = Intent("com.persianvoice.assistant.ALARM_TRIGGERED").apply {
                putExtra("label", label)
                setPackage(context.packageName)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                calendar.timeInMillis.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                @Suppress("DEPRECATION")
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }

            ToolResult.Success("آلارم برای ساعت $hour:$minute تنظیم شد")
        } catch (e: Exception) {
            ToolResult.Failure("خطا در تنظیم آلارم: ${e.message}")
        }
    }

    override fun definition() = toolDefinition(
        name = id,
        description = description,
        properties = mapOf(
            "hour" to stringProp("ساعت (۰ تا ۲۳)"),
            "minute" to stringProp("دقیقه (۰ تا ۵۹)"),
            "label" to stringProp("برچسب آلارم")
        ),
        required = listOf("hour")
    )
}