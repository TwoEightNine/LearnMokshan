package global.msnthrp.mokshan.data.repository.article

import global.msnthrp.mokshan.domain.articles.Articles
import global.msnthrp.mokshan.domain.phrasebook.ForeignLanguage
import global.msnthrp.mokshan.usecase.DeviceLocaleProvider

class ArticlesRepository(
    private val networkDs: NetworkDataSource,
    private val deviceLocaleProvider: DeviceLocaleProvider,
) {

    private val language: ForeignLanguage
        get() = deviceLocaleProvider.getDeviceLocale()

    suspend fun loadArticles(): Result<Articles> {
        return kotlin.runCatching { networkDs.loadArticles(language) }
    }

    interface NetworkDataSource {
        suspend fun loadArticles(language: ForeignLanguage): Articles
    }
}