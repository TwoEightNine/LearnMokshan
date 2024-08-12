package global.msnthrp.mokshan.domain.articles

data class Articles(
    val articles: List<ArticleInfo>,
    val categories: List<String>,
)

data class ArticleInfo(
    val title: String,
    val description: String,
    val categories: List<String>,
    val added: Long,
    val url: String,
)