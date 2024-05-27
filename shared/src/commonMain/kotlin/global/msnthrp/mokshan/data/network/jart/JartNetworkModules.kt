package global.msnthrp.mokshan.data.network.jart

import global.msnthrp.mokshan.data.repository.jart.JartRepository
import org.koin.dsl.module

val jartNetworkModule = module {
    single<JartRepository.NetworkDataSource> {
        JartNetworkDataSource(
            client = get()
        )
    }
}