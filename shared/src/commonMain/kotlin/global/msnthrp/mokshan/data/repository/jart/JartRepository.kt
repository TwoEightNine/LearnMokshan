package global.msnthrp.mokshan.data.repository.jart

import global.msnthrp.mokshan.domain.jart.Jart

class JartRepository(
    private val networkDs: NetworkDataSource,
) {

    suspend fun loadJart(url: String): Result<Jart> {
        return kotlin.runCatching { networkDs.loadJart(url) }
    }

    interface NetworkDataSource {
        suspend fun loadJart(url: String): Jart
    }
}