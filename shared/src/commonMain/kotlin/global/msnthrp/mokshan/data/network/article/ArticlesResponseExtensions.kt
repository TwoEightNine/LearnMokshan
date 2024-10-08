package global.msnthrp.mokshan.data.network.article

import global.msnthrp.mokshan.domain.articles.ArticleInfo
import global.msnthrp.mokshan.domain.articles.Articles
import global.msnthrp.mokshan.utils.DateFormatter

private val dateFormatter = DateFormatter("yyyy-MM-dd")

internal fun ArticlesResponse.toDomain(urlBase: String): Articles {
    return Articles(
        articles = articles?.mapNotNull { it.toDomain(urlBase) } ?: emptyList(),
        categories = categories ?: emptyList(),
    )
}

internal fun ArticleInfoResponse.toDomain(urlBase: String): ArticleInfo? {
    return ArticleInfo(
        title = title ?: return null,
        description = description.orEmpty(),
        categories = categories ?: emptyList(),
        added = added?.let { dateFormatter.parse(it) } ?: 0L,
        url = urlBase + (url ?: return null),
    )
}