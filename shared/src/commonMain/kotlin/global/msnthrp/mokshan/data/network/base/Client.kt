package global.msnthrp.mokshan.data.network.base

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

class NetworkClient(
    private val client: HttpClient,
    private val cacheStorage: CacheStorage,
) {

    suspend fun getPlain(
        url: String
    ): String {
        val cachedResponse = cacheStorage.getResponse(url)
        if (cachedResponse != null) {
            return cachedResponse
        }

        val response: String = client.get(url).body()
        cacheStorage.saveResponse(url, response)
        return response
    }

    suspend inline fun <reified T> get(url: String): T {
        val plainResponse = getPlain(url)
        return when {
            T::class == String::class -> plainResponse as T
            else -> Json.decodeFromString(plainResponse)
        }
    }

    interface CacheStorage {
        suspend fun saveResponse(url: String, response: String)
        suspend fun getResponse(url: String): String?
    }
}

internal val client = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }
    install(HttpCache)
    install(HttpTimeout) {
        requestTimeoutMillis = 30.seconds.inWholeMilliseconds
    }
    install(Logging) {
        logger = Logger.SIMPLE
        level = LogLevel.HEADERS
    }
}