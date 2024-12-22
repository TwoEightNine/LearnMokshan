package global.msnthrp.mokshan.data.network

interface ServerConfig {
    fun getTopicsIndexUrl(langCode: String): String
    fun getTopicUrl(langCode: String, topicId: Int): String

    fun getArticlesIndexUrl(langCode: String): String
    fun getArticlesDirUrl(): String
}