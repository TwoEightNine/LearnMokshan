package global.msnthrp.mokshan.android.core.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URLDecoder

class RouteArgumentsTest {

    /** What `Uri.decode` does: percent sequences only, `+` stays `+`. */
    private fun uriDecode(value: String) = URLDecoder.decode(value.replace("+", "%2B"), "UTF-8")

    private val awkwardValues = listOf(
        "",
        "plain",
        "two words",
        "Cats & dogs",
        "a=b",
        "what?",
        "Where? Where to? Where from?",
        "100%",
        "50% off + more",
        "a#b",
        "{curly}",
        "path/like\\value",
        "https://raw.githubusercontent.com/x/y/master/content/articles/a.json?x=1&y=2#z",
        "Кто это?",
        "Счёт: числа до 10",
        "emoji ✋ and 7️⃣",
        "  leading and trailing  ",
        "new\nline",
    )

    @Test
    fun encodedValue_hasNoCharactersThatStructureARoute() {
        awkwardValues.forEach { value ->
            val encoded = encodeRouteArgument(value)

            assertTrue(encoded, encoded.none { it in "&=?#{}/\\ +" })
        }
    }

    @Test
    fun encodedValue_isDecodedBackToTheOriginal() {
        awkwardValues.forEach { value ->
            assertEquals(value, uriDecode(encodeRouteArgument(value)))
        }
    }

    @Test
    fun spacesBecomePercentTwenty_notPlus() {
        assertEquals("a%20b", encodeRouteArgument("a b"))
        assertEquals("a%2Bb", encodeRouteArgument("a+b"))
    }

    @Test
    fun plainAsciiWordsAreKeptReadable() {
        assertEquals("Topic", encodeRouteArgument("Topic"))
        assertEquals("a-b_c.d", encodeRouteArgument("a-b_c.d"))
    }

    @Test
    fun cyrillicIsEncodedAsUtf8() {
        assertEquals("%D0%A1%D1%87%D1%91%D1%82", encodeRouteArgument("Счёт"))
    }
}
