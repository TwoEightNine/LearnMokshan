package global.msnthrp.mokshan.data.repository

import global.msnthrp.mokshan.data.network.networkModules
import global.msnthrp.mokshan.data.repository.article.ArticlesRepository
import global.msnthrp.mokshan.data.repository.dictionary.DictionaryRepository
import global.msnthrp.mokshan.data.repository.jart.JartRepository
import global.msnthrp.mokshan.data.repository.lessons.TopicsRepository
import global.msnthrp.mokshan.data.repository.phrasebook.PhrasebookRepositoryImpl
import global.msnthrp.mokshan.data.storage.storageModules
import global.msnthrp.mokshan.usecase.lesson.LessonRepository
import global.msnthrp.mokshan.usecase.phrasebook.PhrasebookRepository
import org.koin.core.context.loadKoinModules
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoryModule = module {
    loadKoinModules(
        listOf(
            networkModules,
            storageModules,
        )
    )
    single<PhrasebookRepository> {
        PhrasebookRepositoryImpl(
            networkDs = get()
        )
    }
    single {
        ArticlesRepository(
            networkDs = get(),
            deviceLocaleProvider = get(),
        )
    }
    single {
        JartRepository(
            networkDs = get(),
        )
    }
    single {
        TopicsRepository(
            networkDs = get(),
            storageDs = get(),
            deviceLocaleProvider = get(),
        )
    } bind LessonRepository::class

    singleOf(::DictionaryRepository)
}