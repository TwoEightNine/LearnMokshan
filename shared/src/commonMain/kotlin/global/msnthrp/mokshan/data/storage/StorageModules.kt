package global.msnthrp.mokshan.data.storage

import global.msnthrp.mokshan.data.repository.lessons.TopicsRepository
import global.msnthrp.mokshan.data.storage.base.Prefs
import global.msnthrp.mokshan.data.storage.lessons.LessonsStorageDataSource
import org.koin.dsl.module

val storageModules = module {
    single { Prefs(fileName = "default.preferences_pb", fileProducer = get()) }
    single<TopicsRepository.StorageDataSource> {
        LessonsStorageDataSource(
            prefs = get(),
        )
    }
}