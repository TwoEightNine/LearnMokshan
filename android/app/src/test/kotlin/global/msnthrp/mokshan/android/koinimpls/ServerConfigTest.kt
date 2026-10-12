package global.msnthrp.mokshan.android.koinimpls

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

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
    fun prod_pronunciationArticleUrl_isPerLanguageInsideArticlesDir() {
        assertEquals("$content/articles/pronunciation-en.json", prod.getPronunciationArticleUrl("en"))
        assertEquals("$content/articles/pronunciation-ru.json", prod.getPronunciationArticleUrl("ru"))
        assertTrue(prod.getPronunciationArticleUrl("en").startsWith(prod.getArticlesDirUrl()))
    }

    @Test
    fun prod_phrasebookUrl_pointsToThePhrasebookFile() {
        assertEquals("$content/phrasebook/phrasebook.json", prod.getPhrasebookUrl())
    }

    @Test
    fun prod_urls_haveNoUnresolvedPlaceholders() {
        val urls = listOf(
            prod.getTopicsIndexUrl("en"),
            prod.getTopicUrl("ru", 3),
            prod.getArticlesIndexUrl("ru"),
            prod.getArticlesDirUrl(),
            prod.getPronunciationArticleUrl("ru"),
            prod.getPhrasebookUrl(),
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
    fun sandbox_keepsProductionPronunciationArticleAndPhrasebook() {
        assertEquals(prod.getPronunciationArticleUrl("ru"), sandbox.getPronunciationArticleUrl("ru"))
        assertEquals(prod.getPhrasebookUrl(), sandbox.getPhrasebookUrl())
    }

    @Test
    fun sandbox_indexUrlsAreAllSandbox() {
        assertTrue("index-sandbox" in sandbox.getTopicsIndexUrl("en"))
        assertTrue("index-sandbox" in sandbox.getArticlesIndexUrl("en"))
    }

    // endregion

    // region sandbox index conversion

    @Test
    fun sandboxIndex_convertsOnlyTheIndexFileName() {
        assertEquals("https://h/lessons-en/index-sandbox.json", "https://h/lessons-en/index.json".toSandboxIndexUrl())
        assertEquals("https://h/articles/index-sandbox-ru.json", "https://h/articles/index-ru.json".toSandboxIndexUrl())
    }

    @Test
    fun sandboxIndex_isIdempotent() {
        listOf("https://h/lessons-en/index.json", "https://h/articles/index-en.json").forEach { url ->
            val once = url.toSandboxIndexUrl()

            assertEquals(once, once.toSandboxIndexUrl())
            assertEquals(once, once.toSandboxIndexUrl().toSandboxIndexUrl())
        }
    }

    @Test
    fun sandboxIndex_leavesHostsDirectoriesAndOtherFilesAlone() {
        listOf(
            "https://index.example.com/content/lessons-en/1/lessons.json",
            "https://h/index/lessons-en/1/lessons.json",
            "https://h/indexes/a.json",
            "https://h/articles/",
            "https://h/articles/reindex.json",
            "https://h/articles/index.json/more/lessons.json",
            "https://h/lessons-en/1/lessons.json",
        ).forEach { url ->
            assertEquals(url, url.toSandboxIndexUrl())
        }
    }

    @Test
    fun sandboxIndex_worksForHostsThatContainIndex() {
        assertEquals(
            "https://index.example.com/index-sandbox.json",
            "https://index.example.com/index.json".toSandboxIndexUrl(),
        )
    }

    // endregion

    // region URLs match the repository content

    // the working directory of a module's unit tests is the module directory (android/app)
    private val contentDir = File("../../content")

    private fun fileOf(url: String): File {
        assertTrue(url, url.startsWith("$content/"))
        return File(contentDir, url.removePrefix("$content/"))
    }

    @Test
    fun contentDirectory_isFound() {
        assertTrue(contentDir.absolutePath, contentDir.isDirectory)
    }

    @Test
    fun everyUrlOfProdConfig_pointsToAnExistingContentFile() {
        for (lang in listOf("en", "ru")) {
            val urls = listOf(
                prod.getTopicsIndexUrl(lang),
                prod.getArticlesIndexUrl(lang),
                prod.getPronunciationArticleUrl(lang),
            ) + (1..10).map { prod.getTopicUrl(lang, it) }

            urls.forEach { assertTrue(it, fileOf(it).isFile) }
        }
        assertTrue(fileOf(prod.getPhrasebookUrl()).isFile)
        assertTrue(fileOf(prod.getArticlesDirUrl()).isDirectory)
    }

    @Test
    fun everyIndexUrlOfSandboxConfig_pointsToAnExistingContentFile() {
        for (lang in listOf("en", "ru")) {
            listOf(sandbox.getTopicsIndexUrl(lang), sandbox.getArticlesIndexUrl(lang)).forEach {
                assertTrue(it, fileOf(it).isFile)
            }
        }
    }

    // endregion
}
