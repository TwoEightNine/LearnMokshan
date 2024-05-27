package global.msnthrp.mokshan.data.network.jart

import global.msnthrp.mokshan.data.network.base.getFromJson
import global.msnthrp.mokshan.data.repository.jart.JartRepository
import global.msnthrp.mokshan.domain.jart.Jart
import io.ktor.client.HttpClient
import io.ktor.client.request.get

class JartNetworkDataSource(
    private val client: HttpClient,
) : JartRepository.NetworkDataSource {

    override suspend fun loadJart(url: String): Jart {
        val response: JartResponse = client.get(url).getFromJson()
        println(response)
        return response.toDomain() ?: throw IllegalStateException("Malformed jart")
    }
}