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
import kotlinx.coroutines.CancellationException
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

    suspend fun getPlain(url: String): String = getParsed(url) { it }

    /**
     * Loads [url] and converts the body with [parse]. Only a body that [parse] accepts is cached
     * or returned, so an error page or a malformed file can neither replace a good cached copy nor
     * be served from the cache later. Fresh cache is used without a request; when the request
     * fails (or returns a body that [parse] rejects) an expired cached copy that [parse] accepts
     * is returned. If nothing is usable, the request error (or the parse error) is thrown.
     */
    suspend fun <T> getParsed(url: String, parse: (String) -> T): T {
        val cachedResponse = cacheStorage.getResponse(url)
        val cachedParsed = cachedResponse?.let { kotlin.runCatching { parse(it.response) } }
        if (cachedResponse != null && !cachedResponse.isExpired && cachedParsed?.isSuccess == true) {
            return cachedParsed.getOrThrow()
        }

        val requestResult = request(url)
        val response = requestResult.getOrNull()
        var parseError: Throwable? = null
        if (response != null) {
            val parsed = kotlin.runCatching { parse(response) }
            if (parsed.isSuccess) {
                cacheStorage.saveResponse(url, response)
                return parsed.getOrThrow()
            }
            parseError = parsed.exceptionOrNull()
        }

        if (cachedParsed?.isSuccess == true) {
            return cachedParsed.getOrThrow()
        }
        throw parseError
            ?: requestResult.exceptionOrNull()
            ?: IllegalStateException("Result thrown but exception is null")
    }

    suspend inline fun <reified T> get(url: String): T {
        return when {
            T::class == String::class -> getPlain(url) as T
            else -> getParsed(url) { defaultJson.decodeFromString<T>(it) }
        }
    }

    /** Decodes the body as [R] and maps it; a mapping that throws keeps the body out of the cache. */
    suspend inline fun <reified R, T> getMapped(url: String, crossinline map: (R) -> T): T {
        return getParsed(url) { map(defaultJson.decodeFromString<R>(it)) }
    }

    private suspend fun request(url: String): Result<String> {
        return try {
            Result.success(client.get(url).body())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Result.failure(e)
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