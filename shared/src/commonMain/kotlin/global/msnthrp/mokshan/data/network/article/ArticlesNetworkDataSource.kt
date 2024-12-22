package global.msnthrp.mokshan.data.network.article

import global.msnthrp.mokshan.data.network.ServerConfig
import global.msnthrp.mokshan.data.network.base.NetworkClient
import global.msnthrp.mokshan.data.repository.article.ArticlesRepository
import global.msnthrp.mokshan.domain.articles.Articles
import global.msnthrp.mokshan.domain.phrasebook.ForeignLanguage

class ArticlesNetworkDataSource(
    private val serverConfig: ServerConfig,
    private val client: NetworkClient
) : ArticlesRepository.NetworkDataSource {

    override suspend fun loadArticles(language: ForeignLanguage): Articles {
        val url = serverConfig.getArticlesIndexUrl(language.code)
        val response = client.get<ArticlesResponse>(url)
        return response.toDomain(urlBase = serverConfig.getArticlesDirUrl())
    }
}