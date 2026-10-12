package global.msnthrp.mokshan.utils

import kotlin.coroutines.cancellation.CancellationException

fun <T1, T2> Result.Companion.failureOf(otherResult: Result<T1>): Result<T2> {
    return failure(otherResult.exceptionOrNull() ?: IllegalArgumentException("original result doesn't have throwable"))
}

/**
 * Like [runCatching], but a [CancellationException] is rethrown: swallowing it into a failed
 * [Result] would let a cancelled coroutine carry on as if it had merely failed.
 */
inline fun <T> runCatchingCancellable(block: () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }
}
