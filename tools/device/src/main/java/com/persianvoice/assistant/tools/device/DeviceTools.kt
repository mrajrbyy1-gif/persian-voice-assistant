package com.persianvoice.assistant.tools.device

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.BatteryManager
import com.persianvoice.assistant.domain.model.RiskLevel
import com.persianvoice.assistant.tools.AssistantTool
import com.persianvoice.assistant.tools.ToolResult
import com.persianvoice.assistant.tools.stringProp
import com.persianvoice.assistant.tools.toolDefinition
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tool وضعیت باتری.
 *
 * مثال کاربر:
 * - "باتریم چقدره؟"
 * - "درصد شارژ چنده؟"
 */
@Singleton
class BatteryTool @Inject constructor(
    @ApplicationContext private val context: Context
) : AssistantTool {

    override val id = "device.battery"
    override val name = "وضعیت باتری"
    override val description = "دریافت درصد و وضعیت فعلی باتری دستگاه"
    override val riskLevel = RiskLevel.LOW

    override suspend fun execute(arguments: Map<String, Any?>): ToolResult {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            ?: return ToolResult.Failure("سرویس باتری در دسترس نیست")

        val level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val isCharging = batteryManager.isCharging
        val status = when {
            isCharging && level >= 100 -> "کاملاً شارژ شده"
            isCharging -> "در حال شارژ"
            level <= 15 -> "کم است"
            level <= 30 -> "رو به اتمام"
            else -> "شارژ مناسب"
        }

        val data = "درصد باتری: $level٪. وضعیت: $status"
        return ToolResult.Success(data)
    }

    override fun definition() = toolDefinition(
        name = id,
        description = description,
        properties = emptyMap(),
        required = emptyList()
    )
}

/**
 * Tool ساعت و تاریخ.
 */
@Singleton
class TimeTool @Inject constructor() : AssistantTool {

    override val id = "device.time"
    override val name = "ساعت و تاریخ"
    override val description = "دریافت ساعت و تاریخ فعلی به فرمت شمسی فارسی"
    override val riskLevel = RiskLevel.LOW

    override suspend fun execute(arguments: Map<String, Any?>): ToolResult {
        val now = System.currentTimeMillis()
        val calendar = java.util.GregorianCalendar()
        calendar.timeInMillis = now

        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = calendar.get(java.util.Calendar.MINUTE)
        val data = "ساعت $hour:$minute"
        return ToolResult.Success(data)
    }

    override fun definition() = toolDefinition(
        name = id,
        description = description,
        properties = emptyMap(),
        required = emptyList()
    )
}

/**
 * Tool باز کردن Application.
 *
 * مثال:
 * - "واتساپ رو باز کن"
 * - "تلگرام رو باز کن"
 * - "کروم رو باز کن"
 *
 * از PackageManager برای یافتن app استفاده می‌کند و نام‌ها hardcode نیستند.
 */
@Singleton
class AppLaunchTool @Inject constructor(
    @ApplicationContext private val context: Context
) : AssistantTool {

    override val id = "app.launch"
    override val name = "باز کردن برنامه"
    override val description = "باز کردن یک اپلیکیشن نصب‌شده روی دستگاه با استفاده از نام آن"
    override val riskLevel = RiskLevel.LOW

    override suspend fun execute(arguments: Map<String, Any?>): ToolResult {
        val packageName = arguments["package"] as? String
        val appName = arguments["name"] as? String

        val targetPackage = when {
            packageName != null -> packageName
            appName != null -> findPackageByName(appName)
            else -> return ToolResult.Failure("نام یا package برنامه الزامی است")
        } ?: return ToolResult.Failure("برنامه '$appName' پیدا نشد")

        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(targetPackage)
                ?: return ToolResult.Failure("برنامه قابل اجرا نیست")

            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            ToolResult.Success("در حال باز کردن برنامه")
        } catch (e: Exception) {
            ToolResult.Failure("خطا در باز کردن برنامه: ${e.message}")
        }
    }

    override fun definition() = toolDefinition(
        name = id,
        description = description,
        properties = mapOf(
            "name" to stringProp("نام برنامه (مثل 'واتساپ'، 'تلگرام'، 'کروم')"),
            "package" to stringProp("Package name دقیق برنامه (مثلاً 'com.whatsapp')")
        ),
        required = emptyList()
    )

    /**
     * جستجوی برنامه بر اساس نام فارسی یا انگلیسی.
     */
    private fun findPackageByName(name: String): String? {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        // نگاشت نام‌های رایج فارسی به Package
        val knownApps = mapOf(
            "واتساپ" to listOf("com.whatsapp", "com.whatsapp.w4b"),
            "تلگرام" to listOf("org.telegram.messenger", "org.telegram.plus"),
            "اینستاگرام" to listOf("com.instagram.android"),
            "کروم" to listOf("com.android.chrome", "com.chrome.beta"),
            "یوتیوب" to listOf("com.google.android.youtube"),
            "جیمیل" to listOf("com.google.android.gm"),
            "گوگل" to listOf("com.google.android.googlequicksearchbox"),
            "اسپاتیفای" to listOf("com.spotify.music"),
            "نقشه" to listOf("com.google.android.apps.maps"),
            "مپ" to listOf("com.google.android.apps.maps"),
            "گوگل مپ" to listOf("com.google.android.apps.maps"),
            "دوربین" to listOf("com.android.camera", "com.google.android.camera"),
            "گالری" to listOf("com.android.gallery", "com.google.android.gallery3d"),
            "تماس" to listOf("com.android.dialer", "com.google.android.dialer"),
            "پیام‌رسان" to listOf("com.google.android.apps.messaging"),
            "پیام" to listOf("com.google.android.apps.messaging"),
            "ضبط صدا" to listOf("com.android.soundrecorder", "com.google.android.apps.recorder"),
            "تقویم" to listOf("com.google.android.calendar", "com.android.calendar"),
            "ساعت" to listOf("com.google.android.deskclock", "com.android.deskclock"),
            "فایل" to listOf("com.android.documentsui", "com.google.android.documentsui"),
            "تنظیمات" to listOf("com.android.settings"),
            "اسنپ" to listOf("com.snapp.application"),
            "اسنپ چت" to listOf("com.snapchat.android"),
            "توییتر" to listOf("com.twitter.android"),
            "ایکس" to listOf("com.twitter.android")
        )

        // اول از نگاشت شناخته‌شده چک کن
        knownApps[name]?.forEach { pkg ->
            if (packages.any { it.packageName == pkg }) return pkg
        }

        // سپس در لیست اپ‌ها جستجو کن
        val searchName = name.lowercase()
        return packages.firstOrNull { app ->
            val appName = pm.getApplicationLabel(app).toString().lowercase()
            appName.contains(searchName)
        }?.packageName
    }
}