package global.msnthrp.mokshan.data.network.article

import global.msnthrp.mokshan.data.network.base.NetworkClient
import global.msnthrp.mokshan.data.repository.article.ArticlesRepository

class ArticlesNetworkDataSource(
    private val client: NetworkClient
) : ArticlesRepository.NetworkDataSource {

    override suspend fun loadArticle(url: String): String {
        return client.get(url)
    }
}