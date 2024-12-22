package global.msnthrp.mokshan.android.koinimpls

import global.msnthrp.mokshan.data.network.ServerConfig

internal class SandboxServerConfig(
    private val prodServerConfig: ProdServerConfig,
) : ServerConfig {

    override fun getTopicsIndexUrl(langCode: String): String {
        return prodServerConfig.getTopicsIndexUrl(langCode).replaceIndex()
    }

    override fun getTopicUrl(langCode: String, topicId: Int): String {
        return prodServerConfig.getTopicUrl(langCode, topicId).replaceIndex()
    }

    override fun getArticlesIndexUrl(langCode: String): String {
        return prodServerConfig.getArticlesIndexUrl(langCode).replaceIndex()
    }

    override fun getArticlesDirUrl(): String {
        return prodServerConfig.getArticlesDirUrl().replaceIndex()
    }

    private fun String.replaceIndex(): String {
        return replace("index", "index-sandbox")
    }
}