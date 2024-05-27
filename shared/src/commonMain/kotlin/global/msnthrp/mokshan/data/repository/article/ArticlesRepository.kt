package global.msnthrp.mokshan.data.repository.article

class ArticlesRepository(
    private val networkDs: NetworkDataSource,
) {

    suspend fun loadArticle(url: String): Result<String> {
        return kotlin.runCatching { networkDs.loadArticle(url) }
    }

    interface NetworkDataSource {
        suspend fun loadArticle(url: String): String
    }
}