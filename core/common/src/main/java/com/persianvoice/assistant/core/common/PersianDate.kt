package com.persianvoice.assistant.core.common

/**
 * تبدیل تاریخ میلادی به شمسی برای نمایش فارسی.
 * فقط در Presentation Layer استفاده شود.
 */
object PersianDate {

    private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    private val persianMonthNames = arrayOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    private val persianDayNames = arrayOf(
        "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه"
    )

    /**
     * تبدیل timestamp میلادی به تاریخ شمسی فارسی.
     */
    fun format(timestamp: Long): String {
        val calendar = java.util.GregorianCalendar()
        calendar.timeInMillis = timestamp

        val gy = calendar.get(java.util.Calendar.YEAR)
        val gm = calendar.get(java.util.Calendar.MONTH) + 1
        val gd = calendar.get(java.util.Calendar.DAY_OF_MONTH)

        val (jy, jm, jd) = gregorianToJalali(gy, gm, gd)

        return "${persianDayNames[calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1]} " +
                "${toPersianDigits(jd)} ${persianMonthNames[jm - 1]} ${toPersianDigits(jy)}"
    }

    fun formatShort(timestamp: Long): String {
        val calendar = java.util.GregorianCalendar()
        calendar.timeInMillis = timestamp

        val gy = calendar.get(java.util.Calendar.YEAR)
        val gm = calendar.get(java.util.Calendar.MONTH) + 1
        val gd = calendar.get(java.util.Calendar.DAY_OF_MONTH)

        val (jy, jm, jd) = gregorianToJalali(gy, gm, gd)

        return "${toPersianDigits(jy)}/${toPersianDigits(jm)}/${toPersianDigits(jd)}"
    }

    fun formatTime(timestamp: Long): String {
        val calendar = java.util.GregorianCalendar()
        calendar.timeInMillis = timestamp
        val h = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val m = calendar.get(java.util.Calendar.MINUTE)
        return "${toPersianDigits(h)}:${toPersianDigits(m)}"
    }

    private fun toPersianDigits(number: Int): String {
        return number.toString().map { ch ->
            if (ch.isDigit()) persianDigits[ch.digitToInt()] else ch
        }.joinToString("")
    }

    /**
     * تبدیل میلادی به شمسی (Jalali calendar)
     */
    private fun gregorianToJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val g_d_m = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        var jy: Int
        val gy2 = if (gm > 2) gy + 1 else gy
        var days =
            355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) +
                    gd + g_d_m[gm - 1]
        jy = -1595 + (33 * (days / 12053))
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += ((days - 1) / 365)
            days = (days - 1) % 365
        }
        val jm = if (days < 186) 1 + (days / 31) else 7 + ((days - 186) / 30)
        val jd = 1 + if (days < 186) days % 31 else (days - 186) % 30
        return Triple(jy, jm, jd)
    }
}