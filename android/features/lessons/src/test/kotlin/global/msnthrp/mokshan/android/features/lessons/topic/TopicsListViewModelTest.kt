package global.msnthrp.mokshan.android.features.lessons.topic

import global.msnthrp.mokshan.android.features.lessons.EnglishLocale
import global.msnthrp.mokshan.android.features.lessons.FakeServerConfig
import global.msnthrp.mokshan.android.features.lessons.LessonsEnvironment
import global.msnthrp.mokshan.android.features.lessons.MainDispatcherRule
import global.msnthrp.mokshan.android.features.lessons.sampleTopic
import global.msnthrp.mokshan.data.network.ServerConfig
import global.msnthrp.mokshan.domain.lessons.TopicInfo
import global.msnthrp.mokshan.domain.lessons.TopicsProgress
import global.msnthrp.mokshan.domain.lessons.TopicsSummary
import global.msnthrp.mokshan.domain.phrasebook.ForeignLanguage
import global.msnthrp.mokshan.usecase.DeviceLocaleProvider
import kotlinx.coroutines.CompletableDeferred
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

class TopicsListViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val summary = TopicsSummary(
        topicsCount = 2,
        topicsInfo = listOf(
            TopicInfo(1, 2, "One", "d", null, null),
            TopicInfo(2, 2, "Two", "d", null, null),
        ),
    )

    private val environment = LessonsEnvironment(sampleTopic(), summary)

    private val locale = EnglishLocale()

    private fun createViewModel(): TopicsListViewModel {
        startKoin {
            modules(
                module {
                    single { environment.topicsRepository }
                    single<ServerConfig> { FakeServerConfig() }
                    single<DeviceLocaleProvider> { locale }
                }
            )
        }
        return TopicsListViewModel()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun initialState_isLoading() {
        assertEquals(TopicsListState.Loading, createViewModel().state.value)
    }

    @Test
    fun load_publishesSummaryWithProgress() {
        environment.storage.progress = TopicsProgress(listOf(1), 2, 3)
        val viewModel = createViewModel()

        viewModel.load()

        val state = viewModel.state.value as TopicsListState.Loaded
        assertEquals(summary, state.topics.summary)
        assertEquals(TopicsProgress(listOf(1), 2, 3), state.topics.progress)
    }

    @Test
    fun load_failure_publishesFailed() {
        environment.network.error = IllegalStateException("offline")
        val viewModel = createViewModel()

        viewModel.load()

        assertEquals(TopicsListState.Failed, viewModel.state.value)
    }

    @Test
    fun retryAfterFailure_canRecover() {
        environment.network.error = IllegalStateException("offline")
        val viewModel = createViewModel()
        viewModel.load()

        environment.network.error = null
        viewModel.load()

        assertTrue(viewModel.state.value is TopicsListState.Loaded)
    }

    @Test
    fun firstLoad_showsLoaderWhileRequestIsRunning() {
        environment.network.gate = CompletableDeferred()
        val viewModel = createViewModel()

        viewModel.load()

        assertEquals(TopicsListState.Loading, viewModel.state.value)
        environment.network.gate!!.complete(Unit)
        assertTrue(viewModel.state.value is TopicsListState.Loaded)
    }

    @Test
    fun reloadingLoadedContent_doesNotShowLoader() {
        val viewModel = createViewModel()
        viewModel.load()
        val loaded = viewModel.state.value
        environment.network.gate = CompletableDeferred()

        viewModel.load()

        assertEquals(loaded, viewModel.state.value)
        environment.network.gate!!.complete(Unit)
    }

    @Test
    fun reloadAfterFailure_showsLoaderAgain() {
        environment.network.error = IllegalStateException("offline")
        val viewModel = createViewModel()
        viewModel.load()
        environment.network.error = null
        environment.network.gate = CompletableDeferred()

        viewModel.load()

        assertEquals(TopicsListState.Loading, viewModel.state.value)
        environment.network.gate!!.complete(Unit)
    }

    @Test
    fun reload_picksUpNewProgress() {
        val viewModel = createViewModel()
        viewModel.load()

        environment.storage.progress = TopicsProgress(listOf(1), 2, 0)
        viewModel.load()

        val state = viewModel.state.value as TopicsListState.Loaded
        assertEquals(2, state.topics.progress.ongoingTopicId)
    }

    @Test
    fun grammarArticleUrl_isArticlesDirPlusFileNameFromIndex() {
        val viewModel = createViewModel()
        val info = TopicInfo(1, 2, "One", "d", null, grammar = "grammar-1-3-en.prelinar")

        assertEquals("https://test/articles/grammar-1-3-en.prelinar", viewModel.getGrammarArticleUrl(info))
    }

    @Test
    fun grammarArticleUrl_isNullWithoutGrammar() {
        val viewModel = createViewModel()

        assertEquals(null, viewModel.getGrammarArticleUrl(TopicInfo(1, 2, "One", "d", null, grammar = null)))
        assertEquals(null, viewModel.getGrammarArticleUrl(TopicInfo(1, 2, "One", "d", null, grammar = "  ")))
    }

    @Test
    fun pronunciationArticleUrl_followsDeviceLanguage() {
        val viewModel = createViewModel()

        assertEquals("https://test/articles/pron-en.json", viewModel.getPronunciationArticleUrl())

        locale.language = ForeignLanguage.RUSSIAN
        assertEquals("https://test/articles/pron-ru.json", viewModel.getPronunciationArticleUrl())
    }
}
