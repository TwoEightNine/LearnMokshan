package global.msnthrp.mokshan.data.network.phrasebook

import global.msnthrp.mokshan.data.network.base.getFromJson
import global.msnthrp.mokshan.data.repository.phrasebook.PhrasebookRepositoryImpl
import global.msnthrp.mokshan.domain.phrasebook.Phrasebook
import io.ktor.client.HttpClient
import io.ktor.client.request.get

class PhrasebookNetworkDataSource(
    private val client: HttpClient
) : PhrasebookRepositoryImpl.NetworkDataSource {

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