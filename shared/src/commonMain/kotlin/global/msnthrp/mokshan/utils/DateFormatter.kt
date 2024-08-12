package global.msnthrp.mokshan.utils

interface DateFormatter {
    fun parse(s: String): Long
    fun format(ts: Long): String
}

expect fun DateFormatter(pattern: String): DateFormatter