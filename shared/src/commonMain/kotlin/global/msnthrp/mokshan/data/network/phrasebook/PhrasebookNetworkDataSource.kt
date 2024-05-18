package global.msnthrp.mokshan.data.network.phrasebook

import global.msnthrp.mokshan.data.network.base.getFromJson
import global.msnthrp.mokshan.data.repository.phrasebook.PhrasebookRepository
import global.msnthrp.mokshan.domain.phrasebook.Phrasebook
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.json.Json

class PhrasebookNetworkDataSource(
    private val client: HttpClient
) : PhrasebookRepository.NetworkDataSource {


    override suspend fun loadPhrasebook(): Phrasebook {
        val response: PhrasebookResponse = client.get(PHRASEBOOK_URL).getFromJson()
        println(response)
        return response.toDomain() ?: throw IllegalStateException("Malformed json file!")
    }

    companion object {
        private const val PHRASEBOOK_URL =
            "https://raw.githubusercontent.com/TwoEightNine/LearnMokshan" +
                    "/master/content/phrasebook/phrasebook.json"
    }
}