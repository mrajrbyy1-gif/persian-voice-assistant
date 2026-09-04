package com.persianvoice.assistant.tools.calendar

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import com.persianvoice.assistant.domain.model.RiskLevel
import com.persianvoice.assistant.tools.AssistantTool
import com.persianvoice.assistant.tools.ToolResult
import com.persianvoice.assistant.tools.stringProp
import com.persianvoice.assistant.tools.toolDefinition
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tool ایجاد رویداد تققویم.
 *
 * مثال:
 * - "برای فردا ساعت ۱۰ جلسه ثبت کن"
 */
@Singleton
class CalendarCreateTool @Inject constructor(
    @ApplicationContext private val context: Context
) : AssistantTool {

    override val id = "calendar.create_event"
    override val name = "ایجاد رویداد تققویم"
    override val description = "ایجاد رویداد جدید در تققویم دستگاه"
    override val riskLevel = RiskLevel.MEDIUM

    override suspend fun execute(arguments: Map<String, Any?>): ToolResult {
        val title = arguments["title"] as? String
            ?: return ToolResult.Failure("پارامتر title الزامی است")
        val startTime = (arguments["start_time"] as? Number)?.toLong()
            ?: return ToolResult.Failure("پارامتر start_time الزامی است")
        val durationMinutes = (arguments["duration_minutes"] as? Number)?.toInt() ?: 60
        val description = arguments["description"] as? String

        return try {
            val calendarId = getPrimaryCalendarId()
                ?: return ToolResult.Failure("تققویم اصلی یافت نشد")

            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.TITLE, title)
                description?.let { put(CalendarContract.Events.DESCRIPTION, it) }
                put(CalendarContract.Events.DTSTART, startTime)
                put(CalendarContract.Events.DTEND, startTime + durationMinutes * 60_000L)
                put(CalendarContract.Events.EVENT_TIMEZONE, java.util.TimeZone.getDefault().id)
                put(CalendarContract.Events.HAS_ALARM, 1)
            }

            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            val eventId = uri?.lastPathSegment?.toLongOrNull()
                ?: return ToolResult.Failure("خطا در ایجاد رویداد")

            ToolResult.Success("رویداد '$title' ایجاد شد")
        } catch (e: SecurityException) {
            ToolResult.RequiresPermission
        } catch (e: Exception) {
            ToolResult.Failure("خطا در ایجاد رویداد: ${e.message}")
        }
    }

    override fun definition() = toolDefinition(
        name = id,
        description = description,
        properties = mapOf(
            "title" to stringProp("عنوان رویداد"),
            "start_time" to stringProp("زمان شروع (Unix timestamp میلی‌ثانیه)"),
            "duration_minutes" to stringProp("مدت زمان رویداد به دقیقه"),
            "description" to stringProp("توضیحات رویداد (اختیاری)")
        ),
        required = listOf("title", "start_time")
    )

    private fun getPrimaryCalendarId(): Long? {
        val projection = arrayOf(CalendarContract.Calendars._ID)
        val selection = "${CalendarContract.Calendars.VISIBLE} = 1"
        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection, selection, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getLong(0)
            }
        }
        return null
    }
}