package global.msnthrp.mokshan.android.koinimpls

import global.msnthrp.mokshan.data.network.ServerConfig

internal class SandboxServerConfig(
    private val prodServerConfig: ProdServerConfig,
) : ServerConfig {

    override fun getTopicsIndexUrl(langCode: String): String {
        return prodServerConfig.getTopicsIndexUrl(langCode).replaceIndex()
    }

    override fun getTopicUrl(langCode: String, topicId: Int): String {
        // the sandbox only swaps the indexes: lessons are still served from the production files
        return prodServerConfig.getTopicUrl(langCode, topicId)
    }

    override fun getArticlesIndexUrl(langCode: String): String {
        return prodServerConfig.getArticlesIndexUrl(langCode).replaceIndex()
    }

    override fun getArticlesDirUrl(): String {
        return prodServerConfig.getArticlesDirUrl()
    }

    private fun String.replaceIndex(): String = toSandboxIndexUrl()
}

/** The last path segment when it is an index file: `index.json`, `index-en.json`, ... */
private val indexFileName = Regex("""/index(?!-sandbox)(?=[.-][^/]*$)""")

/**
 * Points an index file URL to its sandbox variant (`index.json` -> `index-sandbox.json`,
 * `index-en.json` -> `index-sandbox-en.json`). Only the file name is touched, so the host,
 * directories and other files are left alone, and an already converted URL stays as it is.
 */
internal fun String.toSandboxIndexUrl(): String = replace(indexFileName, "/index-sandbox")
