package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val indonesiaLocale = Locale.forLanguageTag("id-ID")
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val shortIdFormat = SimpleDateFormat("d MMM yyyy", indonesiaLocale)
    private val headerIdFormat = SimpleDateFormat("EEEE, d MMMM yyyy", indonesiaLocale)

    fun todayIso(): String = synchronized(isoFormat) {
        isoFormat.format(Date())
    }

    fun daysAgoIso(days: Int): String = synchronized(isoFormat) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        isoFormat.format(cal.time)
    }

    fun daysAheadIso(days: Int): String = synchronized(isoFormat) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, days)
        isoFormat.format(cal.time)
    }

    fun formatHeaderDate(date: Date = Date()): String = synchronized(headerIdFormat) {
        headerIdFormat.format(date)
    }

    fun formatShortIndonesianDate(isoDate: String): String {
        return try {
            val parsed = synchronized(isoFormat) { isoFormat.parse(isoDate) } ?: return isoDate
            synchronized(shortIdFormat) { shortIdFormat.format(parsed) }
        } catch (_: Exception) {
            isoDate
        }
    }

    fun parseIsoToMillis(isoDate: String): Long {
        return try {
            synchronized(isoFormat) { isoFormat.parse(isoDate)?.time } ?: System.currentTimeMillis()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }

    fun millisToIso(millis: Long): String = synchronized(isoFormat) {
        isoFormat.format(Date(millis))
    }

    fun advanceIsoDate(isoDate: String, addMonths: Int = 0, addWeeks: Int = 0): String {
        return try {
            val parsed = synchronized(isoFormat) { isoFormat.parse(isoDate) } ?: Date()
            val cal = Calendar.getInstance().apply {
                time = parsed
                if (addMonths != 0) add(Calendar.MONTH, addMonths)
                if (addWeeks != 0) add(Calendar.WEEK_OF_YEAR, addWeeks)
            }
            synchronized(isoFormat) { isoFormat.format(cal.time) }
        } catch (_: Exception) {
            todayIso()
        }
    }

    fun computeReminderTriggerMillis(
        dueIsoDate: String,
        daysBefore: Int,
        hour: Int,
        minute: Int,
    ): Long {
        val baseTime = parseIsoToMillis(dueIsoDate)
        val cal = Calendar.getInstance().apply {
            timeInMillis = baseTime
            add(Calendar.DAY_OF_YEAR, -daysBefore)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
}

object CurrencyUtils {
    private val indonesiaLocale = Locale.forLanguageTag("id-ID")

    fun formatIdr(amount: Long, isPrivacyHidden: Boolean = false): String {
        if (isPrivacyHidden) return "Rp •••••••"
        val formatter = NumberFormat.getNumberInstance(indonesiaLocale)
        return "Rp ${formatter.format(amount)}"
    }

    fun formatCompactIdr(amount: Long, isPrivacyHidden: Boolean = false): String {
        if (isPrivacyHidden) return "•••"
        val absAmount = kotlin.math.abs(amount)
        val sign = if (amount < 0) "-" else ""
        return when {
            absAmount >= 1_000_000_000L -> {
                val billions = absAmount / 1_000_000_000.0
                val formatted = if (billions % 1.0 == 0.0) "${billions.toLong()}M" else String.format(indonesiaLocale, "%.1fM", billions)
                "${sign}Rp $formatted"
            }
            absAmount >= 1_000_000L -> {
                val millions = absAmount / 1_000_000.0
                val formatted = if (millions >= 10.0 || millions % 1.0 == 0.0) {
                    "${kotlin.math.round(millions).toLong()}jt"
                } else {
                    String.format(indonesiaLocale, "%.1fjt", millions)
                }
                "${sign}Rp $formatted"
            }
            absAmount >= 1_000L -> {
                val thousands = absAmount / 1_000L
                "${sign}Rp ${thousands}rb"
            }
            else -> "${sign}Rp $absAmount"
        }
    }

    fun formatGrouping(digitsOnly: String): String {
        val clean = digitsOnly.filter { it.isDigit() }
        if (clean.isEmpty()) return ""
        val value = clean.toLongOrNull() ?: return ""
        return NumberFormat.getNumberInstance(indonesiaLocale).format(value)
    }

    fun parseDigits(formatted: String): Long {
        val clean = formatted.filter { it.isDigit() }
        return clean.toLongOrNull() ?: 0L
    }
}
