package global.msnthrp.mokshan.data.network.article

import global.msnthrp.mokshan.data.repository.article.ArticlesRepository
import org.koin.dsl.module

val articlesNetworkModule = module {
    single<ArticlesRepository.NetworkDataSource> {
        ArticlesNetworkDataSource(
            serverConfig = get(),
            client = get()
        )
    }
}