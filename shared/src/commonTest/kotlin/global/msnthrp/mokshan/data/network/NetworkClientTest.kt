package global.msnthrp.mokshan.data.network

import global.msnthrp.mokshan.data.network.base.defaultJson
import global.msnthrp.mokshan.testutil.FakeCacheStorage
import global.msnthrp.mokshan.testutil.MockNetwork
import global.msnthrp.mokshan.testutil.MockNetwork.MockResponse
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class NetworkClientTest {

    private val url = "https://test/file.json"

    @Serializable
    private data class Dto(val id: Int? = null, val name: String? = null)

    private fun network(
        cache: FakeCacheStorage = FakeCacheStorage(),
        handler: (String) -> MockResponse,
    ) = MockNetwork(cache, handler)

    // region caching strategy

    @Test
    fun freshCache_isReturnedWithoutNetworkCall() = runTest {
        val cache = FakeCacheStorage().apply { put(url, "cached", isExpired = false) }
        val network = network(cache) { error("network must not be called") }

        assertEquals("cached", network.client.getPlain(url))
        assertEquals(emptyList(), network.requestedUrls)
    }

    @Test
    fun withoutCache_responseIsLoadedAndSaved() = runTest {
        val network = network { MockResponse.Ok("fresh") }

        assertEquals("fresh", network.client.getPlain(url))
        assertEquals(listOf(url), network.requestedUrls)
        assertEquals("fresh", network.cache.cached(url))
    }

    @Test
    fun expiredCache_isRefreshedFromNetwork() = runTest {
        val cache = FakeCacheStorage().apply { put(url, "stale", isExpired = true) }
        val network = network(cache) { MockResponse.Ok("fresh") }

        assertEquals("fresh", network.client.getPlain(url))
        assertEquals(listOf(url to "fresh"), cache.saved)
    }

    @Test
    fun expiredCache_isReturnedWhenServerFails() = runTest {
        val cache = FakeCacheStorage().apply { put(url, "stale", isExpired = true) }
        val network = network(cache) { MockResponse.Error(HttpStatusCode.InternalServerError) }

        assertEquals("stale", network.client.getPlain(url))
        assertEquals(emptyList(), cache.saved)
    }

    @Test
    fun expiredCache_isReturnedWhenRequestCrashes() = runTest {
        val cache = FakeCacheStorage().apply { put(url, "stale", isExpired = true) }
        val network = network(cache) { MockResponse.Crash(IllegalStateException("offline")) }

        assertEquals("stale", network.client.getPlain(url))
    }

    @Test
    fun withoutCache_httpErrorIsThrown() = runTest {
        assertFailsWith<ClientRequestException> {
            network { MockResponse.Error(HttpStatusCode.NotFound) }.client.getPlain(url)
        }
        assertFailsWith<ServerResponseException> {
            network { MockResponse.Error(HttpStatusCode.BadGateway) }.client.getPlain(url)
        }
    }

    @Test
    fun withoutCache_requestFailureIsThrown() = runTest {
        val network = network { MockResponse.Crash(IllegalStateException("offline")) }

        val error = assertFailsWith<IllegalStateException> { network.client.getPlain(url) }

        assertEquals("offline", error.message)
    }

    @Test
    fun failedRequest_isNotCached() = runTest {
        val network = network { MockResponse.Error(HttpStatusCode.NotFound) }

        runCatching { network.client.getPlain(url) }

        assertNull(network.cache.cached(url))
    }

    @Test
    fun secondCall_isServedFromCache() = runTest {
        val network = network { MockResponse.Ok("fresh") }

        network.client.getPlain(url)
        network.client.getPlain(url)

        assertEquals(1, network.requestedUrls.size)
    }

    @Test
    fun cacheIsKeyedByUrl() = runTest {
        val network = network { requested -> MockResponse.Ok("body of $requested") }

        assertEquals("body of https://test/a", network.client.getPlain("https://test/a"))
        assertEquals("body of https://test/b", network.client.getPlain("https://test/b"))
    }

    @Test
    fun emptyBody_isCachedAndReturned() = runTest {
        val network = network { MockResponse.Ok("") }

        assertEquals("", network.client.getPlain(url))
        assertEquals("", network.cache.cached(url))
    }

    // endregion

    // region typed get

    @Test
    fun getString_returnsRawBody() = runTest {
        val network = network { MockResponse.Ok("""{"id": 1}""") }

        assertEquals("""{"id": 1}""", network.client.get<String>(url))
    }

    @Test
    fun getDto_decodesJson() = runTest {
        val network = network { MockResponse.Ok("""{"id": 1, "name": "лац"}""") }

        assertEquals(Dto(1, "лац"), network.client.get<Dto>(url))
    }

    @Test
    fun getDto_ignoresUnknownKeys() = runTest {
        val network = network { MockResponse.Ok("""{"id": 1, "unexpected": [1, 2, 3]}""") }

        assertEquals(Dto(id = 1), network.client.get<Dto>(url))
    }

    @Test
    fun getDto_acceptsLenientJsonAndExplicitNulls() = runTest {
        val network = network { MockResponse.Ok("""{id: 2, name: null}""") }

        assertEquals(Dto(id = 2, name = null), network.client.get<Dto>(url))
    }

    @Test
    fun getDto_ofInvalidJson_throwsSerializationException() = runTest {
        val network = network { MockResponse.Ok("<html>rate limited</html>") }

        assertFailsWith<SerializationException> { network.client.get<Dto>(url) }
    }

    @Test
    fun invalidBody_isCachedBeforeValidation_knownBugB4() = runTest {
        // KNOWN BUG B4: the body is saved to the cache before it is decoded
        val cache = FakeCacheStorage().apply { put(url, """{"id": 1}""", isExpired = true) }
        val network = network(cache) { MockResponse.Ok("<html>rate limited</html>") }

        assertFailsWith<SerializationException> { network.client.get<Dto>(url) }

        assertEquals("<html>rate limited</html>", cache.cached(url))
    }

    @Test
    fun invalidFreshCacheEntry_keepsFailingWithoutNetworkCall_knownBugB4() = runTest {
        val cache = FakeCacheStorage().apply { put(url, "garbage", isExpired = false) }
        val network = network(cache) { MockResponse.Ok("""{"id": 1}""") }

        assertFailsWith<SerializationException> { network.client.get<Dto>(url) }
        assertEquals(emptyList(), network.requestedUrls)
    }

    // endregion

    @Test
    fun defaultJson_isLenientAndSkipsUnknownKeys() {
        val dto = defaultJson.decodeFromString<Dto>("""{name: x, other: 5}""")

        assertEquals(Dto(name = "x"), dto)
    }
}
