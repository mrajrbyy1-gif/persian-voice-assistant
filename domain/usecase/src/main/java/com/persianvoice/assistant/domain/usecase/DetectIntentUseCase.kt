package com.persianvoice.assistant.domain.usecase

import com.persianvoice.assistant.domain.model.IntentMatch
import com.persianvoice.assistant.domain.model.AssistantIntent

/**
 * تشخیص Intent از متن فارسی قبل از ارسال به LLM.
 * این UseCase کمک می‌کند command های ساده بدون LLM اجرا شوند.
 */
interface DetectIntentUseCase {
    suspend operator fun invoke(text: String): IntentMatch
}

class PersianNormalizer {
    /**
     * نرمال‌سازی متن فارسی:
     * - جایگزینی ي با ی و ك با ک
     * - اعداد فارسی/عربی به استاندارد
     * - حذف فاصله‌های اضافی
     */
    fun normalize(text: String): String {
        var result = text
        // اعداد فارسی
        result = result.replace("۰", "0").replace("۱", "1").replace("۲", "2")
            .replace("۳", "3").replace("۴", "4").replace("۵", "5")
            .replace("۶", "6").replace("۷", "7").replace("۸", "8").replace("۹", "9")
        // اعداد عربی
        result = result.replace("٠", "0").replace("١", "1").replace("٢", "2")
            .replace("٣", "3").replace("٤", "4").replace("٥", "5")
            .replace("٦", "6").replace("٧", "7").replace("٨", "8").replace("٩", "9")
        // حروف عربی
        result = result.replace("ي", "ی").replace("ك", "ک")
        // فاصله‌های اضافی
        result = result.replace(Regex("\\s+"), " ").trim()
        return result
    }
}

/**
 * پیاده‌سازی پایه Detect Intent با keyword matching.
 * در فاز 9 با LLM ترکیب می‌شود.
 */
class KeywordIntentDetector(
    private val normalizer: PersianNormalizer = PersianNormalizer()
) : DetectIntentUseCase {

    private val callPatterns = listOf(
        "زنگ بزن", "تماس بگیر", "بهش زنگ بزن", "باهاش تماس بگیر",
        "با .* تماس", "به .* زنگ"
    )

    private val smsPatterns = listOf(
        "پیام بده", "اس ام اس کن", "بفرست", "پیام بفرست"
    )

    private val alarmPatterns = listOf(
        "آلارم", "بیدارم کن", "بیدار کن", "یادآور"
    )

    private val calendarPatterns = listOf(
        "جلسه", "رویداد", "قرار ملاقات", "تقویم"
    )

    private val openAppPatterns = listOf(
        "باز کن", "اجرا کن", "رو باز کن"
    )

    private val searchPatterns = listOf(
        "جستجو", "سرچ کن", "گوگل کن", "بگرد"
    )

    private val weatherPatterns = listOf(
        "هوا چطوره", "آب و هوا", "هوای", "دما"
    )

    private val calculatePatterns = listOf(
        "حساب کن", "چند میشه", "ضرب", "تقسیم", "جمع", "منهای",
        "چقدر میشه", "+", "-", "*", "/"
    )

    private val batteryPatterns = listOf(
        "باتری", "شارژ"
    )

    private val timePatterns = listOf(
        "ساعت چنده", "تاریخ", "چنده"
    )

    override suspend operator fun invoke(text: String): IntentMatch {
        val normalized = normalizer.normalize(text)

        return matchByPattern(normalized, callPatterns, AssistantIntent.CALL_CONTACT)
            ?: matchByPattern(normalized, smsPatterns, AssistantIntent.SEND_SMS)
            ?: matchByPattern(normalized, alarmPatterns, AssistantIntent.SET_ALARM)
            ?: matchByPattern(normalized, calendarPatterns, AssistantIntent.CREATE_EVENT)
            ?: matchByPattern(normalized, openAppPatterns, AssistantIntent.OPEN_APP)
            ?: matchByPattern(normalized, searchPatterns, AssistantIntent.SEARCH_WEB)
            ?: matchByPattern(normalized, weatherPatterns, AssistantIntent.GET_WEATHER)
            ?: matchByPattern(normalized, calculatePatterns, AssistantIntent.CALCULATE)
            ?: matchByPattern(normalized, batteryPatterns, AssistantIntent.GET_BATTERY)
            ?: matchByPattern(normalized, timePatterns, AssistantIntent.GET_TIME)
            ?: IntentMatch(AssistantIntent.GENERAL_CHAT, 1.0f)
    }

    private fun matchByPattern(
        text: String,
        patterns: List<String>,
        intent: AssistantIntent
    ): IntentMatch? {
        for (pattern in patterns) {
            if (text.contains(Regex(pattern))) {
                return IntentMatch(intent, 0.9f)
            }
        }
        return null
    }
}