package global.msnthrp.mokshan.data.network.article

import global.msnthrp.mokshan.data.repository.article.ArticlesRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class ArticlesNetworkDataSource(
    private val client: HttpClient
) : ArticlesRepository.NetworkDataSource {

    override suspend fun loadArticle(url: String): String {
        val response: String = client.get(url).body()
        println(response)
        return response
    }
}