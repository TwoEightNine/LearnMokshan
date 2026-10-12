package global.msnthrp.mokshan.data.network

import global.msnthrp.mokshan.data.network.article.ArticlesNetworkDataSource
import global.msnthrp.mokshan.data.network.base.InvalidEntityException
import global.msnthrp.mokshan.data.network.jart.JartNetworkDataSource
import global.msnthrp.mokshan.data.network.lessons.LessonsNetworkDataSource
import global.msnthrp.mokshan.data.network.phrasebook.PhrasebookNetworkDataSource
import global.msnthrp.mokshan.domain.jart.JartEntry
import global.msnthrp.mokshan.domain.phrasebook.ForeignLanguage
import global.msnthrp.mokshan.testutil.FakeServerConfig
import global.msnthrp.mokshan.testutil.MockNetwork
import global.msnthrp.mokshan.testutil.MockNetwork.MockResponse
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class NetworkDataSourcesTest {

    private val config = FakeServerConfig()

    private fun mock(vararg responses: Pair<String, String>): MockNetwork {
        val map = responses.toMap()
        return MockNetwork { url ->
            map[url]?.let { MockResponse.Ok(it) } ?: MockResponse.Error(HttpStatusCode.NotFound)
        }
    }

    // region lessons

    private val summaryJson = """
        {"topicsCount": 1, "topicsInfo": [
          {"id": 1, "lessonsCount": 2, "title": "Hello", "description": "d", "emoji": "✋", "grammar": "g.prelinar"}
        ]}
    """.trimIndent()

    private val topicJson = """
        {"id": 7, "title": "Hello", "description": "d", "version": 1,
         "lessons": [{"order": 1, "toNative": [{"mokshan": "Лац", "native": ["Fine"]}], "dictionary": ["Лац"]}],
         "translations": [{"mokshan": "лац", "native": "fine"}]}
    """.trimIndent()

    @Test
    fun topicsSummary_isLoadedFromIndexUrlOfRequestedLanguage() = runTest {
        val network = mock("https://test/lessons-ru/index.json" to summaryJson)

        val summary = LessonsNetworkDataSource(config, network.client).getTopicsSummary(ForeignLanguage.RUSSIAN)

        assertEquals(listOf("https://test/lessons-ru/index.json"), network.requestedUrls)
        assertEquals("Hello", summary.topicsInfo.single().title)
        assertEquals(2, summary.topicsInfo.single().lessonsCount)
    }

    @Test
    fun invalidTopicsSummary_failsWithInvalidEntity() = runTest {
        val network = mock("https://test/lessons-en/index.json" to """{"topicsCount": 1, "topicsInfo": []}""")

        val error = assertFailsWith<InvalidEntityException> {
            LessonsNetworkDataSource(config, network.client).getTopicsSummary(ForeignLanguage.ENGLISH)
        }

        assertEquals("Invalid topics index received from https://test/lessons-en/index.json", error.message)
    }

    @Test
    fun topic_isLoadedFromTopicUrl() = runTest {
        val network = mock("https://test/lessons-en/7/lessons.json" to topicJson)

        val topic = LessonsNetworkDataSource(config, network.client).getTopic(7, ForeignLanguage.ENGLISH)

        assertEquals(listOf("https://test/lessons-en/7/lessons.json"), network.requestedUrls)
        assertEquals("Hello", topic.title)
        assertEquals("Лац", topic.lessons.single().toNative.single().mokshan)
    }

    @Test
    fun topicFileOfAnotherTopic_isRejectedAndNotCached() = runTest {
        val url = "https://test/lessons-en/8/lessons.json"
        val network = mock(url to topicJson) // the file says it is topic 7

        val error = assertFailsWith<InvalidEntityException> {
            LessonsNetworkDataSource(config, network.client).getTopic(8, ForeignLanguage.ENGLISH)
        }

        assertEquals("Invalid topic received from $url", error.message)
        assertEquals(null, network.cache.cached(url))
    }

    @Test
    fun invalidTopic_failsWithInvalidEntity() = runTest {
        val network = mock("https://test/lessons-en/7/lessons.json" to """{"id": 7, "title": "x", "lessons": []}""")

        val error = assertFailsWith<InvalidEntityException> {
            LessonsNetworkDataSource(config, network.client).getTopic(7, ForeignLanguage.ENGLISH)
        }

        assertEquals("Invalid topic received from https://test/lessons-en/7/lessons.json", error.message)
    }

    @Test
    fun missingTopic_propagatesHttpError() = runTest {
        val network = mock()

        assertFailsWith<ClientRequestException> {
            LessonsNetworkDataSource(config, network.client).getTopic(7, ForeignLanguage.ENGLISH)
        }
    }

    // endregion

    @Test
    fun invalidTopicsIndex_isNotCached() = runTest {
        val url = "https://test/lessons-en/index.json"
        val network = mock(url to """{"topicsCount": 1, "topicsInfo": []}""")

        assertFailsWith<InvalidEntityException> {
            LessonsNetworkDataSource(config, network.client).getTopicsSummary(ForeignLanguage.ENGLISH)
        }

        assertEquals(null, network.cache.cached(url))
    }

    @Test
    fun invalidTopic_doesNotReplaceGoodCachedTopic() = runTest {
        val url = "https://test/lessons-en/7/lessons.json"
        val network = mock(url to """{"id": 7, "title": "x", "lessons": []}""")
        network.cache.put(url, topicJson, isExpired = true)

        val topic = LessonsNetworkDataSource(config, network.client).getTopic(7, ForeignLanguage.ENGLISH)

        assertEquals("Hello", topic.title)
        assertEquals(topicJson, network.cache.cached(url))
    }

    // endregion

    // region articles

    @Test
    fun articles_areLoadedAndUrlsResolvedAgainstArticlesDir() = runTest {
        val json = """
            {"categories": ["Language"], "articles": [
              {"title": "A", "description": "d", "url": "a.json", "added": "2024-03-15", "categories": ["Language"]}
            ]}
        """.trimIndent()
        val network = mock("https://test/articles/index-ru.json" to json)

        val articles = ArticlesNetworkDataSource(config, network.client).loadArticles(ForeignLanguage.RUSSIAN)

        assertEquals("https://test/articles/a.json", articles.articles.single().url)
        assertEquals(listOf("Language"), articles.categories)
    }

    // endregion

    // region jart

    @Test
    fun malformedPrelinar_isNotCached() = runTest {
        val url = "https://test/a.prelinar"
        val network = mock(url to "no version here\nCONTENT\n<h1> Hi")

        assertFailsWith<IllegalStateException> { JartNetworkDataSource(network.client).loadPrelinar(url) }

        assertEquals(null, network.cache.cached(url))
    }

    @Test
    fun jartJsonWithoutMeta_isNotCached() = runTest {
        val url = "https://test/a.json"
        val network = mock(url to """{"content": []}""")

        assertFailsWith<IllegalStateException> { JartNetworkDataSource(network.client).loadJart(url) }

        assertEquals(null, network.cache.cached(url))
    }

    @Test
    fun jartJson_isLoadedAndKeepsArticleUrl() = runTest {
        val json = """{"meta": {"version": 2}, "content": [{"type": "h1", "value": "Hi"}]}"""
        val network = mock("https://test/a.json" to json)

        val jart = JartNetworkDataSource(network.client).loadJart("https://test/a.json")

        assertEquals(2, jart.meta.version)
        assertEquals("https://test/a.json", jart.meta.url)
        assertEquals(listOf<JartEntry>(JartEntry.Header1("Hi")), jart.content)
    }

    @Test
    fun jartJsonWithoutMeta_failsAsMalformed() = runTest {
        val network = mock("https://test/a.json" to """{"content": []}""")

        val error = assertFailsWith<IllegalStateException> {
            JartNetworkDataSource(network.client).loadJart("https://test/a.json")
        }

        assertEquals("Malformed jart", error.message)
    }

    @Test
    fun prelinar_isLoadedAsPlainTextAndParsed() = runTest {
        val network = mock("https://test/a.prelinar" to "version: 1\nCONTENT\n<h1> Hi")

        val jart = JartNetworkDataSource(network.client).loadPrelinar("https://test/a.prelinar")

        assertEquals(listOf<JartEntry>(JartEntry.Header1("Hi")), jart.content)
        assertEquals("https://test/a.prelinar", jart.meta.url)
    }

    // endregion

    // region phrasebook

    @Test
    fun phrasebook_isLoadedFromFixedProductionUrl() = runTest {
        val url = "https://raw.githubusercontent.com/TwoEightNine/LearnMokshan/master/content/phrasebook/phrasebook.json"
        val json = """
            {"version": 1,
             "categories": [{"category": "c", "translations": [{"value": "Cat", "language": "en"}]}],
             "phrasebook": [{"mokshan": "Лац", "category": "c", "translations": [{"value": "Fine", "language": "en"}]}]}
        """.trimIndent()
        val network = mock(url to json)

        val phrasebook = PhrasebookNetworkDataSource(network.client).loadPhrasebook()

        assertEquals(listOf(url), network.requestedUrls)
        assertEquals("Лац", phrasebook.phrases.single().mokshanPhrase)
    }

    @Test
    fun phrasebookWithoutPhrasesList_failsAsMalformed() = runTest {
        val network = MockNetwork { MockResponse.Ok("""{"version": 1}""") }

        val error = assertFailsWith<IllegalStateException> {
            PhrasebookNetworkDataSource(network.client).loadPhrasebook()
        }

        assertTrue(error.message!!.startsWith("Malformed"))
    }

    // endregion
}
