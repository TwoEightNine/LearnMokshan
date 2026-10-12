package global.msnthrp.mokshan.data.network.article

import global.msnthrp.mokshan.domain.articles.ArticleInfo
import global.msnthrp.mokshan.domain.articles.Articles
import global.msnthrp.mokshan.utils.DateFormatter

private val dateFormatter = DateFormatter("yyyy-MM-dd")

/**
 * An index without an `articles` list, or one whose articles are all unusable, is malformed and
 * is rejected (so it is not cached and the last good copy is used) instead of showing an empty
 * list. A well-formed index may legitimately have no articles; single unusable articles and a
 * missing `categories` list are tolerated.
 */
internal fun ArticlesResponse.toDomain(urlBase: String): Articles? {
    val responseArticles = articles ?: return null
    val articles = responseArticles.mapNotNull { it.toDomain(urlBase) }
    if (responseArticles.isNotEmpty() && articles.isEmpty()) return null
    return Articles(
        articles = articles,
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