package global.msnthrp.mokshan.data.repository

import global.msnthrp.mokshan.data.network.networkModules
import global.msnthrp.mokshan.data.repository.article.ArticlesRepository
import global.msnthrp.mokshan.data.repository.jart.JartRepository
import global.msnthrp.mokshan.data.repository.lessons.TopicsRepository
import global.msnthrp.mokshan.data.repository.phrasebook.PhrasebookRepositoryImpl
import global.msnthrp.mokshan.usecase.lesson.LessonRepository
import global.msnthrp.mokshan.usecase.phrasebook.PhrasebookRepository
import org.koin.core.context.loadKoinModules
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoryModule = module {
    loadKoinModules(
        listOf(
            networkModules
        )
    )
    single<PhrasebookRepository> {
        PhrasebookRepositoryImpl(
            networkDs = get()
        )
    }
    single {
        ArticlesRepository(
            networkDs = get()
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
        )
    } bind LessonRepository::class
}