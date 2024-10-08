package global.msnthrp.mokshan.data.network.jart

import global.msnthrp.mokshan.data.network.base.NetworkClient
import global.msnthrp.mokshan.data.repository.jart.JartRepository
import global.msnthrp.mokshan.domain.jart.Jart

class JartNetworkDataSource(
    private val client: NetworkClient,
) : JartRepository.NetworkDataSource {

    override suspend fun loadJart(url: String): Jart {
        val response = client.get<JartResponse>(url)
        return response.toDomain(url) ?: throw IllegalStateException("Malformed jart")
    }

    override suspend fun loadPrelinar(url: String): Jart {
        val response = client.getPlain(url)
        return response.asPrelinarToJart(url)
    }
}