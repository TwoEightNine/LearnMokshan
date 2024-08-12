package global.msnthrp.mokshan.data.repository.article

import global.msnthrp.mokshan.domain.articles.Articles

class ArticlesRepository(
    private val networkDs: NetworkDataSource,
) {

    suspend fun loadArticles(): Result<Articles> {
        return kotlin.runCatching { networkDs.loadArticles() }
    }

    interface NetworkDataSource {
        suspend fun loadArticles(): Articles
    }
}