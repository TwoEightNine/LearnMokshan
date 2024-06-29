package global.msnthrp.mokshan.data.network.lessons

import global.msnthrp.mokshan.data.repository.lessons.LessonsRepository
import org.koin.dsl.module

val lessonsNetworkModule = module {
    single<LessonsRepository.NetworkDataSource> {
        LessonsNetworkDataSource(
            client = get()
        )
    }
}