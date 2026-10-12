package global.msnthrp.mokshan.di

import global.msnthrp.mokshan.data.network.ServerConfig
import global.msnthrp.mokshan.data.network.base.NetworkClient
import global.msnthrp.mokshan.data.network.article.articlesNetworkModule
import global.msnthrp.mokshan.data.network.base.baseNetworkModule
import global.msnthrp.mokshan.data.network.jart.jartNetworkModule
import global.msnthrp.mokshan.data.network.lessons.lessonsNetworkModule
import global.msnthrp.mokshan.data.network.phrasebook.phrasebookNetworkModule
import global.msnthrp.mokshan.data.repository.article.ArticlesRepository
import global.msnthrp.mokshan.data.repository.dictionary.DictionaryRepository
import global.msnthrp.mokshan.data.repository.jart.JartRepository
import global.msnthrp.mokshan.data.repository.lessons.TopicsRepository
import global.msnthrp.mokshan.data.repository.repositoryModule
import global.msnthrp.mokshan.data.storage.base.AppDatabase
import global.msnthrp.mokshan.data.storage.base.FileProducer
import global.msnthrp.mokshan.data.storage.dictionary.DictionaryDao
import global.msnthrp.mokshan.data.storage.storageModules
import global.msnthrp.mokshan.domain.dictionary.DictionaryEntry
import global.msnthrp.mokshan.testutil.FakeCacheStorage
import global.msnthrp.mokshan.testutil.FakeDictionaryDao
import global.msnthrp.mokshan.testutil.FakeLocaleProvider
import global.msnthrp.mokshan.testutil.FakeNow
import global.msnthrp.mokshan.testutil.FakeServerConfig
import global.msnthrp.mokshan.testutil.assertSuccess
import global.msnthrp.mokshan.usecase.DeviceLocaleProvider
import global.msnthrp.mokshan.usecase.lesson.LessonRepository
import global.msnthrp.mokshan.usecase.lesson.LessonUseCase
import global.msnthrp.mokshan.usecase.phrasebook.PhrasebookRepository
import global.msnthrp.mokshan.usecase.phrasebook.PhrasebookUseCase
import global.msnthrp.mokshan.usecase.useCaseModule
import global.msnthrp.mokshan.utils.NowProvider
import okio.FileSystem
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlinx.coroutines.test.runTest

/**
 * Resolves the production Koin modules with only the platform bindings replaced, to protect the
 * wiring that is otherwise only exercised when the Android application starts.
 */
class KoinGraphTest {

    private val dao = FakeDictionaryDao()

    private val database: AppDatabase = object : AppDatabase() {
        override fun getDictionary(): DictionaryDao = dao

        override suspend fun clearAllTables() = Unit
    }

    private fun startGraph(): Koin = startKoin {
        // repositoryModule and networkModules call loadKoinModules while they are being created,
        // which only happens once per JVM, so the nested modules are listed explicitly
        modules(
            baseNetworkModule,
            phrasebookNetworkModule,
            articlesNetworkModule,
            jartNetworkModule,
            lessonsNetworkModule,
            storageModules,
            repositoryModule,
            useCaseModule,
            module {
                single<AppDatabase> { database }
                single<NetworkClient.CacheStorage> { FakeCacheStorage() }
                single<DeviceLocaleProvider> { FakeLocaleProvider() }
                single<NowProvider> { FakeNow(42L) }
                single<ServerConfig> { FakeServerConfig() }
                single<FileProducer> { FileProducer { name -> FileSystem.SYSTEM_TEMPORARY_DIRECTORY / name } }
            },
        )
    }.koin

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun repositories_areResolved() {
        val koin = startGraph()

        koin.get<ArticlesRepository>()
        koin.get<JartRepository>()
        koin.get<PhrasebookRepository>()
        koin.get<TopicsRepository>()
    }

    @Test
    fun topicsRepository_isTheLessonRepository() {
        val koin = startGraph()

        assertSame(koin.get<TopicsRepository>(), koin.get<LessonRepository>())
    }

    @Test
    fun useCases_areResolved() {
        val koin = startGraph()

        koin.get<LessonUseCase>()
        koin.get<PhrasebookUseCase>()
    }

    @Test
    fun repositoriesAndUseCases_areSingletons() {
        val koin = startGraph()

        assertSame(koin.get<LessonUseCase>(), koin.get<LessonUseCase>())
        assertSame(koin.get<DictionaryRepository>(), koin.get<DictionaryRepository>())
    }

    @Test
    fun dictionaryRepository_usesDaoOfTheDatabaseAndInjectedClock() = runTest {
        val koin = startGraph()

        koin.get<DictionaryRepository>()
            .addNewWords(listOf(DictionaryEntry("лац", listOf("fine"))))
            .assertSuccess()

        assertEquals(42L, dao.rows.first().addedTs)
    }
}
