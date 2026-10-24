package com.tayseer.app.utils

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.chrono.HijrahChronology
import java.util.*

object HijriCalendarHelper {
    private val arabicHijriMonths = arrayOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الثاني",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    fun getTodayIsoDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    fun getHijriDate(isoDate: String, offsetDays: Int = 0): String {
        return try {
            val parts = isoDate.split("-").map { it.toInt() }
            val gDate = LocalDate.of(parts[0], parts[1], parts[2]).plusDays(offsetDays.toLong())
            val hDate = HijrahChronology.INSTANCE.date(gDate)

            val hYear = hDate.get(java.time.temporal.ChronoField.YEAR)
            val hMonth = hDate.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)
            val hDay = hDate.get(java.time.temporal.ChronoField.DAY_OF_MONTH)

            val monthName = if (hMonth in 1..12) arabicHijriMonths[hMonth - 1] else "شهر $hMonth"
            "$hDay $monthName $hYear هـ"
        } catch (e: Exception) {
            algorithmicHijriFallback(isoDate, offsetDays)
        }
    }

    fun getCombinedDateDisplay(isoDate: String, offsetDays: Int = 0, isHijriPrimary: Boolean = true): String {
        val hijri = getHijriDate(isoDate, offsetDays)
        val greg = formatGregorianArabic(isoDate)
        return if (isHijriPrimary) {
            "$hijri  •  ($greg)"
        } else {
            "$greg  •  ($hijri)"
        }
    }

    private fun formatGregorianArabic(isoDate: String): String {
        return try {
            val sdfIn = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = sdfIn.parse(isoDate) ?: Date()
            val sdfOut = SimpleDateFormat("dd MMMM yyyy", Locale("ar"))
            sdfOut.format(date)
        } catch (e: Exception) {
            isoDate
        }
    }

    private fun algorithmicHijriFallback(isoDate: String, offsetDays: Int): String {
        return try {
            val parts = isoDate.split("-").map { it.toInt() }
            var y = parts[0]
            var m = parts[1]
            var d = parts[2] + offsetDays

            if (m < 3) {
                y -= 1
                m += 12
            }
            val a = y / 100
            val b = 2 - a + (a / 4)
            val jd = (365.25 * (y + 4716)).toLong() + (30.6001 * (m + 1)).toLong() + d + b - 1524.5

            val z = jd - 1948439.5
            val cyc = (z / 10631.0).toLong()
            val rem = z - cyc * 10631
            val j = (rem / 354.36667).toLong()
            val hYear = (cyc * 30 + j).toInt() + 1
            val remDays = (rem - j * 354.36667).toInt()
            val hMonth = Math.min(12, Math.max(1, (remDays / 29.5).toInt() + 1))
            val hDay = Math.min(30, Math.max(1, (remDays % 29.5).toInt() + 1))

            val monthName = arabicHijriMonths[hMonth - 1]
            "$hDay $monthName $hYear هـ"
        } catch (e: Exception) {
            isoDate
        }
    }
}
