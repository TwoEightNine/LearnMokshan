package global.msnthrp.mokshan.utils

actual fun DateFormatter(pattern: String): DateFormatter {
    return object : DateFormatter {
        override fun parse(s: String): Long {
            return 0L
        }

        override fun format(ts: Long): String {
            return ""
        }
    }
}