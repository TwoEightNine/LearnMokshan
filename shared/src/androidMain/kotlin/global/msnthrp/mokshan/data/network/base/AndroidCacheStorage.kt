package global.msnthrp.mokshan.data.network.base

import android.content.Context
import android.content.SharedPreferences
import java.io.File
import java.security.MessageDigest

internal class AndroidCacheStorage(
    private val applicationContext: Context,
) : NetworkClient.CacheStorage {

    private val metaSharedPreferences =
        applicationContext.getSharedPreferences("ktor_cache_meta", Context.MODE_PRIVATE)
    private val cacheDir by lazy { File(applicationContext.cacheDir, "ktor_cache") }

    init {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }

    override suspend fun saveResponse(url: String, response: String) {
        val fileName = getCacheFileName(url)
        val result = kotlin.runCatching {
            File(cacheDir, fileName).writeText(response)
        }
        if (result.isSuccess) {
            metaSharedPreferences.edit().putLong(fileName, System.currentTimeMillis()).apply()
        }
        result.exceptionOrNull()?.printStackTrace()
    }

    override suspend fun getResponse(
        url: String,
        ttlMs: Long
    ): NetworkClient.CacheStorage.CachedResponse? {
        var isExpired = false
        val fileName = getCacheFileName(url)
        val createdAt = metaSharedPreferences.getLong(fileName, 0L)
        if (createdAt + ttlMs < System.currentTimeMillis()) {
            isExpired = true
        }

        val result = kotlin.runCatching {
            File(cacheDir, fileName).takeIf { it.exists() }?.readText()
        }
        if (result.isFailure) {
            metaSharedPreferences.edit().putLong(fileName, 0L).apply()
        }
        result.exceptionOrNull()?.printStackTrace()
        return result.getOrNull()?.let { response ->
            NetworkClient.CacheStorage.CachedResponse(response, isExpired)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun getCacheFileName(url: String): String {
        return MessageDigest
            .getInstance("SHA-512")
            .digest(url.toByteArray())
            .toHexString()
    }
}