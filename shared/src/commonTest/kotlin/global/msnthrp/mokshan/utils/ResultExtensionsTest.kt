package global.msnthrp.mokshan.utils

import kotlinx.coroutines.test.runTest
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ResultExtensionsTest {

    @Test
    fun failureOf_carriesTheOriginalThrowable() {
        val boom = IllegalStateException("boom")

        val result: Result<String> = Result.failureOf(Result.failure<Int>(boom))

        assertEquals(boom, result.exceptionOrNull())
    }

    @Test
    fun failureOf_successfulResult_becomesDescriptiveFailure() {
        val result: Result<String> = Result.failureOf(Result.success(1))

        assertTrue(result.isFailure)
        assertIs<IllegalArgumentException>(result.exceptionOrNull())
    }

    @Test
    fun runCatchingCancellable_wrapsRegularFailures() {
        val boom = IllegalStateException("boom")

        assertEquals(boom, runCatchingCancellable { throw boom }.exceptionOrNull())
        assertEquals(5, runCatchingCancellable { 5 }.getOrNull())
    }

    @Test
    fun runCatchingCancellable_wrapsErrorsToo() {
        val error = runCatchingCancellable { throw StackOverflowError() }.exceptionOrNull()

        assertIs<StackOverflowError>(error)
    }

    @Test
    fun runCatchingCancellable_rethrowsCancellation() {
        assertFailsWith<CancellationException> { runCatchingCancellable<Int> { throw CancellationException("cancelled") } }
    }

    @Test
    fun runCatchingCancellable_rethrowsSubclassesOfCancellation() {
        assertFailsWith<TimeoutCancellationLike> { runCatchingCancellable<Int> { throw TimeoutCancellationLike() } }
    }

    private class TimeoutCancellationLike : CancellationException("timeout")

    @Test
    fun runCatchingCancellable_canWrapSuspendCalls() = runTest {
        suspend fun answer(): Int = 42

        assertEquals(42, runCatchingCancellable { answer() }.getOrNull())
    }
}
