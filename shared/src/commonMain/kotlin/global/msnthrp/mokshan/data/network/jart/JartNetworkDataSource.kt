package global.msnthrp.mokshan.data.network.jart

import global.msnthrp.mokshan.data.network.base.NetworkClient
import global.msnthrp.mokshan.data.repository.jart.JartRepository
import global.msnthrp.mokshan.domain.jart.Jart

class JartNetworkDataSource(
    private val client: NetworkClient,
) : JartRepository.NetworkDataSource {

    override suspend fun loadJart(url: String): Jart {
        return client.getMapped<JartResponse, Jart>(url) {
            it.toDomain(url) ?: throw IllegalStateException("Malformed jart")
        }
    }

    override suspend fun loadPrelinar(url: String): Jart {
        return client.getParsed(url) { it.asPrelinarToJart(url) }
    }
}