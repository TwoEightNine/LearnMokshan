package global.msnthrp.mokshan.data.network.jart

import global.msnthrp.mokshan.data.network.base.NetworkClient
import global.msnthrp.mokshan.data.repository.jart.JartRepository
import global.msnthrp.mokshan.domain.jart.Jart

class JartNetworkDataSource(
    private val client: NetworkClient,
) : JartRepository.NetworkDataSource {

    override suspend fun loadJart(url: String): Jart {
        val response = client.get<JartResponse>(url)
        return response.toDomain() ?: throw IllegalStateException("Malformed jart")
    }
}