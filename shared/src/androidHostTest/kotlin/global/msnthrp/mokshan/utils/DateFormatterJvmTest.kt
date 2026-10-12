package global.msnthrp.mokshan.utils

import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/** JVM-only checks: the default time zone and locale of the device must not change the result. */
class DateFormatterJvmTest {

    private val originalZone = TimeZone.getDefault()
    private val originalLocale = Locale.getDefault()

    // 2024-03-15T00:00:00Z
    private val march15 = 1_710_460_800_000L

    @AfterTest
    fun restoreDefaults() {
        TimeZone.setDefault(originalZone)
        Locale.setDefault(originalLocale)
    }

    @Test
    fun parsedTimestamp_isMidnightUtc_inAnyTimeZone() {
        listOf("UTC", "America/Los_Angeles", "Pacific/Kiritimati", "Asia/Kolkata", "Europe/Moscow").forEach { zone ->
            TimeZone.setDefault(TimeZone.getTimeZone(zone))

            assertEquals(march15, DateFormatter("yyyy-MM-dd").parse("2024-03-15"), zone)
        }
    }

    @Test
    fun shownDay_isTheParsedDay_inAnyTimeZone() {
        val formatter = DateFormatter("yyyy-MM-dd")
        listOf("America/Los_Angeles", "Pacific/Kiritimati", "Europe/Moscow").forEach { zone ->
            TimeZone.setDefault(TimeZone.getTimeZone(zone))
            Locale.setDefault(Locale.US)

            assertEquals("2024-03-15", formatter.format(formatter.parse("2024-03-15")), zone)
        }
    }

    @Test
    fun parsing_ignoresNonGregorianDefaultLocales() {
        listOf("th-TH-u-ca-buddhist", "ar-SA", "fa-IR", "ja-JP-u-ca-japanese").forEach { tag ->
            Locale.setDefault(Locale.forLanguageTag(tag))

            assertEquals(march15, DateFormatter("yyyy-MM-dd").parse("2024-03-15"), tag)
        }
    }

    @Test
    fun formattedMonthName_followsDeviceLocale() {
        Locale.setDefault(Locale.US)
        assertEquals("15 Mar 2024", DateFormatter("dd MMM yyyy").format(march15))

        Locale.setDefault(Locale.forLanguageTag("ru"))
        assertNotEquals("15 Mar 2024", DateFormatter("dd MMM yyyy").format(march15))
        assertTrue(DateFormatter("dd MMM yyyy").format(march15).startsWith("15 "))
    }

    @Test
    fun sameFormatter_canBeUsedFromManyThreads() {
        val formatter = DateFormatter("yyyy-MM-dd")
        val pool = Executors.newFixedThreadPool(8)
        try {
            val results = (1..400).map { index ->
                val day = "2024-03-%02d".format(1 + index % 28)
                pool.submit(Callable { day to formatter.format(formatter.parse(day)) })
            }.map { it.get() }

            results.forEach { (expected, actual) -> assertEquals(expected, actual) }
        } finally {
            pool.shutdown()
        }
    }
}
