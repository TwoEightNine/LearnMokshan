package global.msnthrp.mokshan.utils

import java.text.SimpleDateFormat
import java.util.Date


actual fun DateFormatter(pattern: String): DateFormatter = object : DateFormatter {

    private val sdf = SimpleDateFormat(pattern)

    override fun parse(s: String): Long {
        return kotlin.runCatching { sdf.parse(s)?.time }.getOrNull() ?: 0L
    }

    override fun format(ts: Long): String {
        return sdf.format(Date(ts))
    }
}