package global.msnthrp.mokshan.android.features.articles.article

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArticleNavigationTest {

    @Test
    fun route_containsTitleAndEncodedUrl() {
        val route = ArticleRouter(url = "https://t/dir/a.json?x=1&y=2", title = "Grammar").route()

        assertEquals("article?title=Grammar&url=https%3A%2F%2Ft%2Fdir%2Fa.json%3Fx%3D1%26y%3D2", route)
    }

    @Test
    fun route_withoutTitle_hasEmptyTitleArgument() {
        assertTrue(ArticleRouter("https://t/a.json").route().startsWith("article?title=&url="))
    }

    @Test
    fun routeUrl_roundTripsThroughUrlDecoding() {
        val url = "https://t/статья вторая.json"

        val encoded = ArticleRouter(url).route().substringAfter("&url=")

        assertEquals(url, java.net.URLDecoder.decode(encoded, "UTF-8"))
    }

    @Test
    fun title_isNotEncoded_knownBugB21() {
        // KNOWN BUG B21: '&' in a title splits the route into an extra argument
        val route = ArticleRouter("https://t/a.json", title = "Cats & dogs").route()

        assertTrue(route.startsWith("article?title=Cats & dogs&url="))
    }

    @Test
    fun defaultRouter_hasEncodedPlaceholderForUrl() {
        // pins current output: the {url} placeholder is percent-encoded by the same builder
        assertEquals("article?title={title}&url=%7Burl%7D", ArticleDefaultRouter().route())
    }
}
