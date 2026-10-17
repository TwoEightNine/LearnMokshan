package global.msnthrp.mokshan.data.network.base

import android.content.Context
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

internal class AndroidCacheStorage(
    private val applicationContext: Context,
) : NetworkClient.CacheStorage {

    private val metaSharedPreferences by lazy {
        applicationContext.getSharedPreferences("ktor_cache_meta", Context.MODE_PRIVATE)
    }
    private val cacheDir by lazy {
        File(applicationContext.cacheDir, "ktor_cache").apply { mkdirs() }
    }

    /** Bodies already read or written in this process, so screens that reload on resume skip the disk. */
    private val memoryCache = object : LruCache<String, String>(MEMORY_CACHE_MAX_CHARS) {
        override fun sizeOf(key: String, value: String): Int = value.length
    }

    override suspend fun saveResponse(url: String, response: String) = withContext(Dispatchers.IO) {
        val fileName = getCacheFileName(url)
        val result = kotlin.runCatching {
            File(cacheDir, fileName).writeText(response)
        }
        if (result.isSuccess) {
            memoryCache.put(fileName, response)
            metaSharedPreferences.edit().putLong(fileName, System.currentTimeMillis()).apply()
        }
    }

    override suspend fun getResponse(
        url: String,
        ttlMs: Long
    ): NetworkClient.CacheStorage.CachedResponse? = withContext(Dispatchers.IO) {
        val fileName = getCacheFileName(url)
        val createdAt = metaSharedPreferences.getLong(fileName, 0L)
        val isExpired = createdAt + ttlMs < System.currentTimeMillis()

        val result = kotlin.runCatching {
            memoryCache.get(fileName)
                ?: File(cacheDir, fileName).takeIf { it.exists() }?.readText()
                    ?.also { memoryCache.put(fileName, it) }
        }
        if (result.isFailure) {
            metaSharedPreferences.edit().putLong(fileName, 0L).apply()
        }
        result.getOrNull()?.let { response ->
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

    private companion object {
        // all lesson, article and phrasebook files together are far below this
        const val MEMORY_CACHE_MAX_CHARS = 4 * 1024 * 1024
    }
}
