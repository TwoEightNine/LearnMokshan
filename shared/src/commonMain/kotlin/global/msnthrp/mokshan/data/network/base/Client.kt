package global.msnthrp.mokshan.data.network.base

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalSerializationApi::class)
val defaultJson = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
    isLenient = true
    explicitNulls = false
}

class NetworkClient(
    private val client: HttpClient,
    private val cacheStorage: CacheStorage,
) {

    suspend fun getPlain(
        url: String
    ): String {
        val cachedResponse = cacheStorage.getResponse(url)
        if (cachedResponse != null && !cachedResponse.isExpired) {
            return cachedResponse.response
        }

        val result: Result<String> = kotlin.runCatching { client.get(url).body() }
        val response = result.getOrNull()
        if (response != null) {
            cacheStorage.saveResponse(url, response)
            return response
        } else if (cachedResponse != null) {
            return cachedResponse.response
        } else {
            throw (result.exceptionOrNull() ?: IllegalStateException("Result thrown but exception is null"))
        }
    }

    suspend inline fun <reified T> get(url: String): T {
        val plainResponse = getPlain(url)
        return when {
            T::class == String::class -> plainResponse as T
            else -> defaultJson.decodeFromString(plainResponse)
        }
    }

    interface CacheStorage {
        suspend fun saveResponse(url: String, response: String)
        suspend fun getResponse(url: String, ttlMs: Long = 1.hours.inWholeMilliseconds): CachedResponse?

        data class CachedResponse(
            val response: String,
            val isExpired: Boolean,
        )
    }
}

internal val client = HttpClient {
    install(ContentNegotiation) {
        json(defaultJson)
    }
    install(HttpCache)
    install(HttpTimeout) {
        requestTimeoutMillis = 30.seconds.inWholeMilliseconds
    }
    install(Logging) {
        logger = Logger.SIMPLE
        level = LogLevel.HEADERS
    }
    expectSuccess = true
}