package global.msnthrp.mokshan.data.network.phrasebook

import global.msnthrp.mokshan.data.repository.phrasebook.PhrasebookRepositoryImpl
import org.koin.dsl.module

val phrasebookNetworkModule = module {
    single<PhrasebookRepositoryImpl.NetworkDataSource> {
        PhrasebookNetworkDataSource(
            client = get()
        )
    }
}