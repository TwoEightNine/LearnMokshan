package global.msnthrp.mokshan.data.network

interface ServerConfig {
    fun getTopicsIndexUrl(langCode: String): String
    fun getTopicUrl(langCode: String, topicId: Int): String

    fun getArticlesIndexUrl(langCode: String): String

    /** Directory of the article files; ends with `/`, so a file name can be appended. */
    fun getArticlesDirUrl(): String

    /** The article about pronunciation that the lessons list links to. */
    fun getPronunciationArticleUrl(langCode: String): String

    fun getPhrasebookUrl(): String
}
