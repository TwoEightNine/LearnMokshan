package global.msnthrp.mokshan.utils

import kotlin.test.Test
import kotlin.test.assertEquals
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
}
