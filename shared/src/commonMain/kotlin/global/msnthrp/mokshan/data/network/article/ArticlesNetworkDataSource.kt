package global.msnthrp.mokshan.data.network.article

import global.msnthrp.mokshan.data.network.base.NetworkClient
import global.msnthrp.mokshan.data.repository.article.ArticlesRepository
import global.msnthrp.mokshan.domain.articles.Articles
import global.msnthrp.mokshan.domain.phrasebook.ForeignLanguage

class ArticlesNetworkDataSource(
    private val client: NetworkClient
) : ArticlesRepository.NetworkDataSource {

    override suspend fun loadArticles(language: ForeignLanguage): Articles {
        val response = client.get<ArticlesResponse>(getArticlesUrl(language))
        return response.toDomain(ARTICLES_BASE_URL)
    }

    private fun getArticlesUrl(language: ForeignLanguage): String {
        return ARTICLES_URL.replace(LANG_CODE_PLACEHOLDER, language.code)
    }

    companion object {
        private const val LANG_CODE_PLACEHOLDER = "{lang}"
        private const val ARTICLES_BASE_URL = "https://raw.githubusercontent.com/TwoEightNine/" +
                "LearnMokshan/master/content/articles/"
        private const val ARTICLES_URL = "${ARTICLES_BASE_URL}index-$LANG_CODE_PLACEHOLDER.json"
    }
}