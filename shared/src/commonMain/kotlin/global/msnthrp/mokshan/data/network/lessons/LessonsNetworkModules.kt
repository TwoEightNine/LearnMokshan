package global.msnthrp.mokshan.data.network.lessons

import global.msnthrp.mokshan.data.repository.lessons.TopicsRepository
import org.koin.dsl.module

val lessonsNetworkModule = module {
    single<TopicsRepository.NetworkDataSource> {
        LessonsNetworkDataSource(
            serverConfig = get(),
            client = get()
        )
    }
}