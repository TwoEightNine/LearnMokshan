package global.msnthrp.mokshan.testutil

import global.msnthrp.mokshan.data.network.base.NetworkClient
import global.msnthrp.mokshan.domain.lessons.Lesson
import global.msnthrp.mokshan.domain.lessons.LessonPair
import global.msnthrp.mokshan.domain.lessons.Topic
import global.msnthrp.mokshan.domain.lessons.Translation
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.job
import kotlin.test.assertTrue

fun pair(mokshan: String, vararg native: String) = LessonPair(mokshan, native.toList())

fun lesson(
    order: Int,
    pairs: List<LessonPair>,
    dictionary: List<String> = emptyList(),
) = Lesson(order = order, toNative = pairs, dictionary = dictionary)

fun topic(
    id: Int = 1,
    lessons: List<Lesson>,
    translations: List<Translation> = emptyList(),
) = Topic(
    id = id,
    title = "Topic $id",
    description = "Description $id",
    lessons = lessons,
    translations = translations,
)

fun translation(mokshan: String, native: String) = Translation(mokshan, native)

/** Two lessons, four pairs each. The vocabulary of both lessons together has 12 native words. */
fun sampleTopic(id: Int = 1): Topic = topic(
    id = id,
    lessons = listOf(
        lesson(
            order = 1,
            pairs = listOf(
                pair("Шумбрат!", "Hello!"),
                pair("Мон эрян", "I live"),
                pair("Тон эрят", "You live"),
                pair("Сон эрясы", "He lives", "She lives"),
            ),
            dictionary = listOf("Шумбрат!", "Мон эрян"),
        ),
        lesson(
            order = 2,
            pairs = listOf(
                pair("Кода тефне?", "How are you?"),
                pair("Пара шобдава!", "Good morning!"),
                pair("Цебярьста", "Good"),
                pair("Лац", "Fine"),
            ),
            dictionary = listOf("Лац"),
        ),
    ),
)

/** Builds a [NetworkClient] over a Ktor [MockEngine] that mimics `expectSuccess = true`. */
class MockNetwork(
    val cache: FakeCacheStorage = FakeCacheStorage(),
    staleFallbackTimeoutMs: Long = NetworkClient.STALE_FALLBACK_TIMEOUT_MS,
    private val handler: suspend (url: String) -> MockResponse,
) {
    val requestedUrls = mutableListOf<String>()

    /** Where refreshes outliving their caller run; see [awaitRefreshes]. */
    private val refreshScope = CoroutineScope(SupervisorJob())

    private val engine = MockEngine { request ->
        val url = request.url.toString()
        requestedUrls += url
        when (val response = handler(url)) {
            is MockResponse.Ok -> respond(content = response.body, status = HttpStatusCode.OK)
            is MockResponse.Error -> respondError(status = response.status)
            is MockResponse.Crash -> throw response.error
        }
    }

    val client = NetworkClient(
        client = HttpClient(engine) { expectSuccess = true },
        cacheStorage = cache,
        refreshScope = refreshScope,
        staleFallbackTimeoutMs = staleFallbackTimeoutMs,
    )

    suspend fun awaitRefreshes() {
        refreshScope.coroutineContext.job.children.forEach { it.join() }
    }

    sealed interface MockResponse {
        data class Ok(val body: String) : MockResponse
        data class Error(val status: HttpStatusCode) : MockResponse
        data class Crash(val error: Throwable) : MockResponse
    }
}

fun <T> Result<T>.assertSuccess(): T {
    assertTrue(isSuccess, "expected success but was $this")
    return getOrThrow()
}

fun <T> Result<T>.assertFailure(): Throwable {
    assertTrue(isFailure, "expected failure but was $this")
    return exceptionOrNull()!!
}
