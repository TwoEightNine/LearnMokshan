package global.msnthrp.mokshan.data.repository

import global.msnthrp.mokshan.data.repository.article.ArticlesRepository
import global.msnthrp.mokshan.data.repository.jart.JartRepository
import global.msnthrp.mokshan.data.repository.phrasebook.PhrasebookRepositoryImpl
import global.msnthrp.mokshan.domain.articles.ArticleInfo
import global.msnthrp.mokshan.domain.articles.Articles
import global.msnthrp.mokshan.domain.jart.Jart
import global.msnthrp.mokshan.domain.jart.JartMeta
import global.msnthrp.mokshan.domain.phrasebook.ForeignLanguage
import global.msnthrp.mokshan.domain.phrasebook.Phrasebook
import global.msnthrp.mokshan.testutil.FakeLocaleProvider
import global.msnthrp.mokshan.testutil.assertFailure
import global.msnthrp.mokshan.testutil.assertSuccess
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ArticlesRepositoryTest {

    private class FakeArticlesNetwork(var result: () -> Articles) : ArticlesRepository.NetworkDataSource {
        val languages = mutableListOf<ForeignLanguage>()
        override suspend fun loadArticles(language: ForeignLanguage): Articles {
            languages += language
            return result()
        }
    }

    private fun article(title: String, added: Long) =
        ArticleInfo(title, "d", listOf("Language"), added, "https://test/$title")

    @Test
    fun articles_areSortedByAddedDateDescending() = runTest {
        val network = FakeArticlesNetwork {
            Articles(listOf(article("old", 1), article("new", 3), article("mid", 2)), emptyList())
        }

        val result = ArticlesRepository(network, FakeLocaleProvider()).loadArticles().assertSuccess()

        assertEquals(listOf("new", "mid", "old"), result.articles.map { it.title })
    }

    @Test
    fun articlesWithEqualDate_keepOriginalOrder() = runTest {
        val network = FakeArticlesNetwork {
            Articles(listOf(article("b", 1), article("a", 1)), emptyList())
        }

        val result = ArticlesRepository(network, FakeLocaleProvider()).loadArticles().assertSuccess()

        assertEquals(listOf("b", "a"), result.articles.map { it.title })
    }

    @Test
    fun categories_areSortedAlphabetically() = runTest {
        val network = FakeArticlesNetwork { Articles(emptyList(), listOf("Language", "Grammar", "Alphabet")) }

        val result = ArticlesRepository(network, FakeLocaleProvider()).loadArticles().assertSuccess()

        assertEquals(listOf("Alphabet", "Grammar", "Language"), result.categories)
    }

    @Test
    fun articles_areRequestedForCurrentDeviceLanguage() = runTest {
        val network = FakeArticlesNetwork { Articles(emptyList(), emptyList()) }
        val locale = FakeLocaleProvider(ForeignLanguage.RUSSIAN)
        val repository = ArticlesRepository(network, locale)

        repository.loadArticles()
        locale.language = ForeignLanguage.ENGLISH
        repository.loadArticles()

        assertEquals(listOf(ForeignLanguage.RUSSIAN, ForeignLanguage.ENGLISH), network.languages)
    }

    @Test
    fun networkFailure_isWrappedInResult() = runTest {
        val boom = IllegalStateException("offline")
        val network = FakeArticlesNetwork { throw boom }

        assertEquals(boom, ArticlesRepository(network, FakeLocaleProvider()).loadArticles().assertFailure())
    }
}

class JartRepositoryTest {

    private class FakeJartNetwork : JartRepository.NetworkDataSource {
        val calls = mutableListOf<String>()
        var error: Throwable? = null

        private fun jart(url: String) = Jart(JartMeta(1, url), emptyList())

        override suspend fun loadJart(url: String): Jart {
            calls += "json:$url"
            error?.let { throw it }
            return jart(url)
        }

        override suspend fun loadPrelinar(url: String): Jart {
            calls += "prelinar:$url"
            error?.let { throw it }
            return jart(url)
        }
    }

    private val network = FakeJartNetwork()
    private val repository = JartRepository(network)

    @Test
    fun jsonUrl_isLoadedAsJart() = runTest {
        repository.loadJart("https://test/a.json").assertSuccess()

        assertEquals(listOf("json:https://test/a.json"), network.calls)
    }

    @Test
    fun otherExtensions_areLoadedAsPrelinar() = runTest {
        repository.loadJart("https://test/a.prelinar").assertSuccess()
        repository.loadJart("https://test/a.txt").assertSuccess()
        repository.loadJart("https://test/a").assertSuccess()

        assertEquals(
            listOf("prelinar:https://test/a.prelinar", "prelinar:https://test/a.txt", "prelinar:https://test/a"),
            network.calls,
        )
    }

    @Test
    fun extensionCheck_isCaseSensitiveAndIgnoresQueryString() = runTest {
        // pins current behaviour: these json URLs are parsed as Prelinar
        repository.loadJart("https://test/a.JSON").assertSuccess()
        repository.loadJart("https://test/a.json?raw=1").assertSuccess()

        assertEquals(
            listOf("prelinar:https://test/a.JSON", "prelinar:https://test/a.json?raw=1"),
            network.calls,
        )
    }

    @Test
    fun loadedJart_isReturned() = runTest {
        assertEquals(
            Jart(JartMeta(1, "https://test/a.json"), emptyList()),
            repository.loadJart("https://test/a.json").assertSuccess(),
        )
    }

    @Test
    fun failures_areWrappedInResult() = runTest {
        val boom = IllegalStateException("malformed")
        network.error = boom

        assertEquals(boom, repository.loadJart("https://test/a.json").assertFailure())
        assertEquals(boom, repository.loadJart("https://test/a.prelinar").assertFailure())
    }
}

class PhrasebookRepositoryImplTest {

    private class FakePhrasebookNetwork(var result: () -> Phrasebook) :
        PhrasebookRepositoryImpl.NetworkDataSource {
        override suspend fun loadPhrasebook(): Phrasebook = result()
    }

    @Test
    fun phrasebook_isReturnedFromNetwork() = runTest {
        val phrasebook = Phrasebook(1, emptyList(), emptyList())

        val result = PhrasebookRepositoryImpl(FakePhrasebookNetwork { phrasebook }).loadPhrasebook()

        assertEquals(phrasebook, result.assertSuccess())
    }

    @Test
    fun failure_isWrappedInResult() = runTest {
        val boom = IllegalStateException("offline")

        val result = PhrasebookRepositoryImpl(FakePhrasebookNetwork { throw boom }).loadPhrasebook()

        assertEquals(boom, result.assertFailure())
    }
}
