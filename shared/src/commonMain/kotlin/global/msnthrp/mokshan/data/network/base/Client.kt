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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
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
    /** Runs cache access and parsing, so callers on the main thread are never blocked. */
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    /** Keeps a refresh running after its caller has been answered from the cache. */
    private val refreshScope: CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher),
    private val staleFallbackTimeoutMs: Long = STALE_FALLBACK_TIMEOUT_MS,
) {

    suspend fun getPlain(url: String): String = getParsed(url) { it }

    /**
     * Loads [url] and converts the body with [parse]. Only a body that [parse] accepts is cached
     * or returned, so an error page or a malformed file can neither replace a good cached copy nor
     * be served from the cache later. Fresh cache is used without a request. When the cache has
     * expired, the request gets [staleFallbackTimeoutMs] to answer; after that, or when it fails
     * (or returns a body that [parse] rejects), an expired cached copy that [parse] accepts is
     * returned while a slow request keeps running in [refreshScope] to update the cache. If
     * nothing is usable, the request error (or the parse error) is thrown.
     */
    suspend fun <T> getParsed(url: String, parse: (String) -> T): T = withContext(dispatcher) {
        val cachedResponse = cacheStorage.getResponse(url)
            ?: return@withContext fetch(url, parse).getOrThrow()
        // an expired copy is parsed only when it is needed as a fallback
        val cachedParsed by lazy(LazyThreadSafetyMode.NONE) {
            kotlin.runCatching { parse(cachedResponse.response) }
        }
        if (!cachedResponse.isExpired && cachedParsed.isSuccess) {
            return@withContext cachedParsed.getOrThrow()
        }

        val refresh = refreshScope.async { fetch(url, parse) }
        val refreshed = withTimeoutOrNull(staleFallbackTimeoutMs) { refresh.await() }
        when {
            refreshed?.isSuccess == true -> refreshed.getOrThrow()
            cachedParsed.isSuccess -> cachedParsed.getOrThrow()
            else -> (refreshed ?: refresh.await()).getOrThrow()
        }
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

    /** Requests [url] and caches the body if [parse] accepts it. */
    private suspend fun <T> fetch(url: String, parse: (String) -> T): Result<T> {
        val response = request(url).getOrElse { return Result.failure(it) }
        val parsed = kotlin.runCatching { parse(response) }
        if (parsed.isSuccess) {
            cacheStorage.saveResponse(url, response)
        }
        return parsed
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

    companion object {
        const val STALE_FALLBACK_TIMEOUT_MS = 3_000L
    }
}

/** [logHeaders] prints request and response headers; it is meant for debug builds only. */
internal fun createHttpClient(logHeaders: Boolean) = HttpClient {
    install(ContentNegotiation) {
        json(defaultJson)
    }
    install(HttpCache)
    install(HttpTimeout) {
        requestTimeoutMillis = 30.seconds.inWholeMilliseconds
    }
    if (logHeaders) {
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.HEADERS
        }
    }
    expectSuccess = true
}
