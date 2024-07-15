package global.msnthrp.mokshan.utils

fun <T1, T2> Result.Companion.failureOf(otherResult: Result<T1>): Result<T2> {
    return failure(otherResult.exceptionOrNull() ?: IllegalArgumentException("original result doesn't have throwable"))
}