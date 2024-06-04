package global.msnthrp.mokshan.data.network.base

import android.content.Context
import java.io.File
import java.security.MessageDigest

internal class AndroidCacheStorage(
    private val applicationContext: Context,
) : NetworkClient.CacheStorage {

    private val cacheDir by lazy { File(applicationContext.cacheDir, "ktor_cache") }

    init {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }

    override suspend fun saveResponse(url: String, response: String) {
        val result = kotlin.runCatching {
            getCacheFile(url).writeText(response)
        }
        result.exceptionOrNull()?.printStackTrace()
    }

    override suspend fun getResponse(url: String): String? {
        val result = kotlin.runCatching {
            getCacheFile(url).takeIf { it.exists() }?.readText()
        }
        result.exceptionOrNull()?.printStackTrace()
        return result.getOrNull()
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun getCacheFile(url: String): File {
        val fileName = MessageDigest
            .getInstance("SHA-512")
            .digest(url.toByteArray())
            .toHexString()
        return File(cacheDir, fileName)
    }
}