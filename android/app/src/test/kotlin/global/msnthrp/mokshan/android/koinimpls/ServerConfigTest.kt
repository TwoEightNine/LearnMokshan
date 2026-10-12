package global.msnthrp.mokshan.android.koinimpls

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ServerConfigTest {

    private val content = "https://raw.githubusercontent.com/TwoEightNine/LearnMokshan/master/content"

    private val prod = ProdServerConfig()
    private val sandbox = SandboxServerConfig(prod)

    // region production

    @Test
    fun prod_topicsIndexUrl_isPerLanguage() {
        assertEquals("$content/lessons-en/index.json", prod.getTopicsIndexUrl("en"))
        assertEquals("$content/lessons-ru/index.json", prod.getTopicsIndexUrl("ru"))
    }

    @Test
    fun prod_topicUrl_containsLanguageAndTopicId() {
        assertEquals("$content/lessons-en/1/lessons.json", prod.getTopicUrl("en", 1))
        assertEquals("$content/lessons-ru/10/lessons.json", prod.getTopicUrl("ru", 10))
    }

    @Test
    fun prod_articlesIndexUrl_isPerLanguage() {
        assertEquals("$content/articles/index-en.json", prod.getArticlesIndexUrl("en"))
        assertEquals("$content/articles/index-ru.json", prod.getArticlesIndexUrl("ru"))
    }

    @Test
    fun prod_articlesDir_endsWithSlashSoRelativeUrlsCanBeAppended() {
        assertEquals("$content/articles/", prod.getArticlesDirUrl())
    }

    @Test
    fun prod_urls_haveNoUnresolvedPlaceholders() {
        val urls = listOf(
            prod.getTopicsIndexUrl("en"),
            prod.getTopicUrl("ru", 3),
            prod.getArticlesIndexUrl("ru"),
            prod.getArticlesDirUrl(),
        )

        urls.forEach { assertFalse(it, "{" in it || "}" in it) }
    }

    @Test
    fun prod_urls_neverPointToSandbox() {
        val urls = listOf(prod.getTopicsIndexUrl("en"), prod.getArticlesIndexUrl("en"), prod.getTopicUrl("en", 1))

        urls.forEach { assertFalse(it, "sandbox" in it) }
    }

    // endregion

    // region sandbox

    @Test
    fun sandbox_replacesIndexesWithSandboxIndexes() {
        assertEquals("$content/lessons-en/index-sandbox.json", sandbox.getTopicsIndexUrl("en"))
        assertEquals("$content/articles/index-sandbox-ru.json", sandbox.getArticlesIndexUrl("ru"))
    }

    @Test
    fun sandbox_keepsProductionTopicFiles() {
        for (topic in 1..12) for (lang in listOf("en", "ru")) {
            assertEquals(prod.getTopicUrl(lang, topic), sandbox.getTopicUrl(lang, topic))
        }
    }

    @Test
    fun sandbox_keepsArticlesDirectory() {
        assertEquals(prod.getArticlesDirUrl(), sandbox.getArticlesDirUrl())
    }

    @Test
    fun sandbox_indexUrlsAreAllSandbox() {
        assertTrue("index-sandbox" in sandbox.getTopicsIndexUrl("en"))
        assertTrue("index-sandbox" in sandbox.getArticlesIndexUrl("en"))
    }

    // endregion
}
