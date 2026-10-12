package global.msnthrp.mokshan.android.features.articles.article

import global.msnthrp.mokshan.android.features.articles.MainDispatcherRule
import global.msnthrp.mokshan.data.repository.jart.JartRepository
import global.msnthrp.mokshan.domain.jart.Jart
import global.msnthrp.mokshan.domain.jart.JartEntry
import global.msnthrp.mokshan.domain.jart.JartMeta
import kotlinx.coroutines.CompletableDeferred
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

class ArticleViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private class FakeNetwork : JartRepository.NetworkDataSource {
        val requested = mutableListOf<String>()
        var error: Throwable? = null
        var gate: CompletableDeferred<Unit>? = null

        private fun jart(url: String) = Jart(JartMeta(1, url), listOf(JartEntry.Title("Title of $url")))

        override suspend fun loadJart(url: String): Jart {
            requested += "json:$url"
            gate?.await()
            error?.let { throw it }
            return jart(url)
        }

        override suspend fun loadPrelinar(url: String): Jart {
            requested += "prelinar:$url"
            gate?.await()
            error?.let { throw it }
            return jart(url)
        }
    }

    private val network = FakeNetwork()

    private fun createViewModel(): ArticleViewModel {
        startKoin { modules(module { single { JartRepository(network) } }) }
        return ArticleViewModel()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun initialState_isLoading() {
        assertEquals(ArticleViewState.Loading, createViewModel().state.value)
    }

    @Test
    fun load_publishesParsedArticle() {
        val viewModel = createViewModel()

        viewModel.load("https://t/a.json")

        assertEquals(
            ArticleViewState.Loaded(Jart(JartMeta(1, "https://t/a.json"), listOf(JartEntry.Title("Title of https://t/a.json")))),
            viewModel.state.value,
        )
    }

    @Test
    fun load_choosesParserByUrlExtension() {
        val viewModel = createViewModel()

        viewModel.load("https://t/a.json")
        viewModel.load("https://t/b.prelinar")

        assertEquals(listOf("json:https://t/a.json", "prelinar:https://t/b.prelinar"), network.requested)
    }

    @Test
    fun load_failure_publishesFailed() {
        network.error = IllegalStateException("malformed")
        val viewModel = createViewModel()

        viewModel.load("https://t/a.json")

        assertEquals(ArticleViewState.Failed, viewModel.state.value)
    }

    @Test
    fun retry_afterFailure_showsLoaderThenContent() {
        network.error = IllegalStateException("malformed")
        val viewModel = createViewModel()
        viewModel.load("https://t/a.json")
        network.error = null
        network.gate = CompletableDeferred()

        viewModel.load("https://t/a.json")
        assertEquals(ArticleViewState.Loading, viewModel.state.value)

        network.gate!!.complete(Unit)
        assertTrue(viewModel.state.value is ArticleViewState.Loaded)
    }

    @Test
    fun reload_ofLoadedArticle_showsLoaderAgain() {
        // unlike the lists, the article screen always resets to the loader
        val viewModel = createViewModel()
        viewModel.load("https://t/a.json")
        network.gate = CompletableDeferred()

        viewModel.load("https://t/a.json")

        assertEquals(ArticleViewState.Loading, viewModel.state.value)
        network.gate!!.complete(Unit)
    }
}
