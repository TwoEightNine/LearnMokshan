package global.msnthrp.mokshan.utils

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val utc = TimeZone.getTimeZone("UTC")

/**
 * Formats calendar dates (the content files carry dates without time), so both directions use UTC:
 * the day that is parsed is the day that is shown, whatever the device time zone is.
 * Parsing is strict and locale independent; formatting follows the device locale (month names).
 * A new [SimpleDateFormat] is created per call because the class is not thread-safe.
 */
actual fun DateFormatter(pattern: String): DateFormatter = object : DateFormatter {

    override fun parse(s: String): Long {
        val format = SimpleDateFormat(pattern, Locale.ROOT).apply {
            isLenient = false
            timeZone = utc
        }
        val position = ParsePosition(0)
        val date = format.parse(s, position)
        return if (date != null && position.index == s.length) date.time else 0L
    }

    override fun format(ts: Long): String {
        val format = SimpleDateFormat(pattern, Locale.getDefault()).apply { timeZone = utc }
        return format.format(Date(ts))
    }
}
