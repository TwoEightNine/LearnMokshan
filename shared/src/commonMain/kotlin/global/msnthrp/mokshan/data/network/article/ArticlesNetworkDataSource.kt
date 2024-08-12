package global.msnthrp.mokshan.data.network.article

import global.msnthrp.mokshan.data.network.base.NetworkClient
import global.msnthrp.mokshan.data.repository.article.ArticlesRepository
import global.msnthrp.mokshan.domain.articles.Articles

class ArticlesNetworkDataSource(
    private val client: NetworkClient
) : ArticlesRepository.NetworkDataSource {

    override suspend fun loadArticles(): Articles {
        val response = client.get<ArticlesResponse>(ARTICLES_URL)
        return response.toDomain()
    }

    companion object {
        private const val ARTICLES_URL = "https://raw.githubusercontent.com/TwoEightNine/" +
                "LearnMokshan/master/content/articles/index-en.json"
    }
}