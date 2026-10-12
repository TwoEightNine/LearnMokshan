package global.msnthrp.mokshan.android.features.articles.list

import global.msnthrp.mokshan.android.features.articles.MainDispatcherRule
import global.msnthrp.mokshan.data.repository.article.ArticlesRepository
import global.msnthrp.mokshan.domain.articles.ArticleInfo
import global.msnthrp.mokshan.domain.articles.Articles
import global.msnthrp.mokshan.domain.phrasebook.ForeignLanguage
import global.msnthrp.mokshan.usecase.DeviceLocaleProvider
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ArticlesListViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private class FakeNetwork : ArticlesRepository.NetworkDataSource {
        var articles = Articles(emptyList(), emptyList())
        var error: Throwable? = null
        var gate: CompletableDeferred<Unit>? = null

        override suspend fun loadArticles(language: ForeignLanguage): Articles {
            gate?.await()
            error?.let { throw it }
            return articles
        }
    }

    private val network = FakeNetwork()
    private val repository = ArticlesRepository(network, object : DeviceLocaleProvider {
        override fun getDeviceLocale() = ForeignLanguage.ENGLISH
    })

    private fun article(title: String, added: Long) = ArticleInfo(title, "d", emptyList(), added, "https://t/$title")

    @Test
    fun initialState_isLoading() {
        assertEquals(ArticlesListState.Loading, ArticlesListViewModel(repository).state.value)
    }

    @Test
    fun load_publishesSortedArticles() {
        network.articles = Articles(listOf(article("old", 1), article("new", 2)), listOf("B", "A"))
        val viewModel = ArticlesListViewModel(repository)

        viewModel.load()

        val loaded = viewModel.state.value as ArticlesListState.Loaded
        assertEquals(listOf("new", "old"), loaded.articles.articles.map { it.title })
        assertEquals(listOf("A", "B"), loaded.articles.categories)
    }

    @Test
    fun load_failure_publishesFailed() {
        network.error = IllegalStateException("offline")
        val viewModel = ArticlesListViewModel(repository)

        viewModel.load()

        assertEquals(ArticlesListState.Failed, viewModel.state.value)
    }

    @Test
    fun retry_afterFailure_loadsContent() {
        network.error = IllegalStateException("offline")
        val viewModel = ArticlesListViewModel(repository)
        viewModel.load()

        network.error = null
        viewModel.load()

        assertTrue(viewModel.state.value is ArticlesListState.Loaded)
    }

    @Test
    fun reload_ofLoadedContent_doesNotShowLoader() {
        val viewModel = ArticlesListViewModel(repository)
        viewModel.load()
        val loaded = viewModel.state.value
        network.gate = CompletableDeferred()

        viewModel.load()

        assertEquals(loaded, viewModel.state.value)
        network.gate!!.complete(Unit)
    }

    @Test
    fun load_afterFailure_showsLoaderWhileRunning() {
        network.error = IllegalStateException("offline")
        val viewModel = ArticlesListViewModel(repository)
        viewModel.load()
        network.error = null
        network.gate = CompletableDeferred()

        viewModel.load()

        assertEquals(ArticlesListState.Loading, viewModel.state.value)
        network.gate!!.complete(Unit)
        assertTrue(viewModel.state.value is ArticlesListState.Loaded)
    }
}
