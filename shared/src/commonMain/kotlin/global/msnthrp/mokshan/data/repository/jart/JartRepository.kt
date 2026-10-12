package global.msnthrp.mokshan.data.repository.jart

import global.msnthrp.mokshan.domain.jart.Jart
import global.msnthrp.mokshan.utils.runCatchingCancellable

class JartRepository(
    private val networkDs: NetworkDataSource,
) {

    suspend fun loadJart(url: String): Result<Jart> {
        val method = when {
            url.endsWith(".json") -> networkDs::loadJart
            else -> networkDs::loadPrelinar
        }
        return runCatchingCancellable { method(url) }
    }

    interface NetworkDataSource {
        suspend fun loadJart(url: String): Jart
        suspend fun loadPrelinar(url: String): Jart
    }
}