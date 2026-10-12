package global.msnthrp.mokshan.data.network.article

import global.msnthrp.mokshan.domain.articles.Articles
import global.msnthrp.mokshan.utils.DateFormatter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ArticlesMapperTest {

    private val base = "https://test/articles/"

    private fun response(
        title: String? = "Title",
        description: String? = "Description",
        url: String? = "a.json",
        added: String? = "2024-03-15",
        categories: List<String>? = listOf("Language"),
    ) = ArticleInfoResponse(title, description, url, added, categories)

    @Test
    fun article_isMapped_withUrlResolvedAgainstBase() {
        val article = response().toDomain(base)!!

        assertEquals("Title", article.title)
        assertEquals("Description", article.description)
        assertEquals("https://test/articles/a.json", article.url)
        assertEquals(listOf("Language"), article.categories)
        assertEquals(DateFormatter("yyyy-MM-dd").parse("2024-03-15"), article.added)
    }

    @Test
    fun article_withoutTitleOrUrl_isDropped() {
        assertNull(response(title = null).toDomain(base))
        assertNull(response(url = null).toDomain(base))
    }

    @Test
    fun article_optionalFieldsHaveDefaults() {
        val article = response(description = null, categories = null, added = null).toDomain(base)!!

        assertEquals("", article.description)
        assertEquals(emptyList(), article.categories)
        assertEquals(0L, article.added)
    }

    @Test
    fun article_withUnparsableDate_hasZeroTimestamp() {
        assertEquals(0L, response(added = "yesterday").toDomain(base)!!.added)
    }

    @Test
    fun url_isSimpleConcatenation_soBaseMustEndWithSlash() {
        assertEquals("https://test/articlesa.json", response().toDomain("https://test/articles")!!.url)
    }

    @Test
    fun laterDate_producesLargerTimestamp() {
        val older = response(added = "2024-03-15").toDomain(base)!!
        val newer = response(added = "2024-03-16").toDomain(base)!!

        assertTrue(newer.added > older.added)
    }

    @Test
    fun articles_dropInvalidItemsAndKeepOrder() {
        val articles = ArticlesResponse(
            categories = listOf("Language", "Grammar"),
            articles = listOf(response(title = "a"), response(title = null), response(title = "b")),
        ).toDomain(base)

        assertEquals(listOf("a", "b"), articles.articles.map { it.title })
        assertEquals(listOf("Language", "Grammar"), articles.categories)
    }

    @Test
    fun articles_withMissingLists_areEmptyNotFailed() {
        assertEquals(Articles(emptyList(), emptyList()), ArticlesResponse(null, null).toDomain(base))
    }
}
