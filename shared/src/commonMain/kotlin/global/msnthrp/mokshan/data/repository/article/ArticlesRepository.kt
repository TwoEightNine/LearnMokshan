package global.msnthrp.mokshan.data.repository.article

import global.msnthrp.mokshan.domain.articles.Articles
import global.msnthrp.mokshan.domain.phrasebook.ForeignLanguage
import global.msnthrp.mokshan.usecase.DeviceLocaleProvider
import global.msnthrp.mokshan.utils.runCatchingCancellable

class ArticlesRepository(
    private val networkDs: NetworkDataSource,
    private val deviceLocaleProvider: DeviceLocaleProvider,
) {

    private val language: ForeignLanguage
        get() = deviceLocaleProvider.getDeviceLocale()

    suspend fun loadArticles(): Result<Articles> {
        return runCatchingCancellable {
            val articles = networkDs.loadArticles(language)

            Articles(
                articles = articles.articles.sortedByDescending { it.added },
                categories = articles.categories.sorted(),
            )
        }
    }

    interface NetworkDataSource {
        suspend fun loadArticles(language: ForeignLanguage): Articles
    }
}