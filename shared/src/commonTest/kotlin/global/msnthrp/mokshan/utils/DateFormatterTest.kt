package global.msnthrp.mokshan.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class DateFormatterTest {

    private val formatter = DateFormatter("yyyy-MM-dd")
    private val hour = 60 * 60 * 1000L

    @Test
    fun parsedDate_isFormattedBackToTheSameString() {
        listOf("2024-03-15", "2023-12-31", "2020-02-29", "2025-01-01").forEach { date ->
            assertEquals(date, formatter.format(formatter.parse(date)), date)
        }
    }

    @Test
    fun laterDates_haveLargerTimestamps() {
        assertTrue(formatter.parse("2024-03-16") > formatter.parse("2024-03-15"))
        assertTrue(formatter.parse("2025-01-01") > formatter.parse("2024-12-31"))
    }

    @Test
    fun consecutiveDays_areAboutOneDayApart() {
        val diff = formatter.parse("2024-03-16") - formatter.parse("2024-03-15")

        assertTrue(diff in (23 * hour)..(25 * hour), "diff=$diff")
    }

    @Test
    fun unparsableInput_givesZero() {
        assertEquals(0L, formatter.parse("yesterday"))
        assertEquals(0L, formatter.parse(""))
        assertEquals(0L, formatter.parse("15.03.2024"))
    }

    @Test
    fun parsing_isLenient_knownBugB19() {
        // KNOWN BUG B19: invalid calendar values roll over, trailing garbage is ignored
        assertNotEquals(0L, formatter.parse("2024-13-45"))
        assertEquals(formatter.parse("2024-03-15"), formatter.parse("2024-03-15 trailing garbage"))
    }

    @Test
    fun differentPatterns_formatIndependently() {
        val timestamp = formatter.parse("2024-03-15")

        assertEquals("15/03/2024", DateFormatter("dd/MM/yyyy").format(timestamp))
    }
}
