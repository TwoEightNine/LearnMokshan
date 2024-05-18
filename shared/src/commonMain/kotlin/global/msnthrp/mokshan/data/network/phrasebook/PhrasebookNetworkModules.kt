package global.msnthrp.mokshan.data.network.phrasebook

import global.msnthrp.mokshan.data.repository.phrasebook.PhrasebookRepository
import org.koin.dsl.module

val phrasebookNetworkModule = module {
    single<PhrasebookRepository.NetworkDataSource> {
        PhrasebookNetworkDataSource(
            client = get()
        )
    }
}