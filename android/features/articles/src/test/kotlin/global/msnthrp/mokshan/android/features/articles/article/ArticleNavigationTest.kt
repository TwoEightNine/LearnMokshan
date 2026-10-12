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
    fun routeUrl_roundTripsThroughDecoding() {
        val url = "https://t/статья вторая.json"

        val encoded = ArticleRouter(url).route().substringAfter("&url=")

        assertEquals(url, decode(encoded))
    }

    @Test
    fun title_isEncoded() {
        val route = ArticleRouter("https://t/a.json", title = "Cats & dogs = 100%").route()

        assertEquals("article?title=Cats%20%26%20dogs%20%3D%20100%25&url=https%3A%2F%2Ft%2Fa.json", route)
    }

    @Test
    fun routeArguments_staySeparateWhateverTitleAndUrlContain() {
        val titles = listOf("a&url=b", "Where? Where to?", "{title}", "#x", "Грамматика: падежи")
        val urls = listOf("https://t/a.json?x=1&title=2#z", "https://t/статья вторая+третья.json")

        titles.forEach { title ->
            urls.forEach { url ->
                val route = ArticleRouter(url, title).route()

                val arguments = route.substringAfter('?').split('&').associate { it.substringBefore('=') to it.substringAfter('=') }
                assertEquals(listOf("title", "url"), arguments.keys.toList())
                assertEquals(title, decode(arguments["title"]!!))
                assertEquals(url, decode(arguments["url"]!!))
            }
        }
    }

    private fun decode(value: String) = java.net.URLDecoder.decode(value.replace("+", "%2B"), "UTF-8")

    @Test
    fun defaultRouter_hasEncodedPlaceholderForUrl() {
        // unchanged route pattern: the {url} placeholder has always been percent-encoded
        assertEquals("article?title={title}&url=%7Burl%7D", ArticleDefaultRouter().route())
    }
}
