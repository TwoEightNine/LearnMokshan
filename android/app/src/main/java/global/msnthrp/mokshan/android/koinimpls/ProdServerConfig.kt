package global.msnthrp.mokshan.android.koinimpls

import global.msnthrp.mokshan.data.network.ServerConfig

internal class ProdServerConfig : ServerConfig {

    override fun getTopicsIndexUrl(langCode: String): String {
        return TOPICS_INDEX_URL.replace(LANG_CODE_PLACEHOLDER, langCode)
    }

    override fun getTopicUrl(langCode: String, topicId: Int): String {
        return TOPIC_URL
            .replace(TOPIC_ID_PLACEHOLDER, topicId.toString())
            .replace(LANG_CODE_PLACEHOLDER, langCode)
    }

    override fun getArticlesIndexUrl(langCode: String): String {
        return ARTICLES_URL.replace(LANG_CODE_PLACEHOLDER, langCode)
    }

    override fun getArticlesDirUrl(): String = ARTICLES_BASE_URL

    companion object {
        private const val LANG_CODE_PLACEHOLDER = "{langCode}"

        private const val PROJECT_URL = "https://raw.githubusercontent.com/TwoEightNine/LearnMokshan"
        private const val CONTENT_URL = "$PROJECT_URL/master/content"

        private const val TOPICS_BASE_URL = "$CONTENT_URL/lessons-$LANG_CODE_PLACEHOLDER"

        private const val TOPICS_INDEX_URL = "$TOPICS_BASE_URL/index.json"
        private const val TOPIC_ID_PLACEHOLDER = "{topicId}"
        private const val TOPIC_URL = "$TOPICS_BASE_URL/$TOPIC_ID_PLACEHOLDER/lessons.json"

        private const val ARTICLES_BASE_URL = "$CONTENT_URL/articles/"
        private const val ARTICLES_URL = "${ARTICLES_BASE_URL}index-$LANG_CODE_PLACEHOLDER.json"
    }
}