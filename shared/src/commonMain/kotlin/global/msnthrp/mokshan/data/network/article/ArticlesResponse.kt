package global.msnthrp.mokshan.data.network.article

import kotlinx.serialization.Serializable

@Serializable
data class ArticlesResponse(
    val categories: List<String>?,
    val articles: List<ArticleInfoResponse>?
)

@Serializable
data class ArticleInfoResponse(
    val title: String?,
    val description: String?,
    val url: String?,
    val added: String?,
    val categories: List<String>?,
)