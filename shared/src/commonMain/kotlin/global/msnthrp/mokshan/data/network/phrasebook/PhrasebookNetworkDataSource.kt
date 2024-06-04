package global.msnthrp.mokshan.data.network.phrasebook

import global.msnthrp.mokshan.data.network.base.NetworkClient
import global.msnthrp.mokshan.data.repository.phrasebook.PhrasebookRepositoryImpl
import global.msnthrp.mokshan.domain.phrasebook.Phrasebook

class PhrasebookNetworkDataSource(
    private val client: NetworkClient
) : PhrasebookRepositoryImpl.NetworkDataSource {

    override suspend fun loadPhrasebook(): Phrasebook {
        val response = client.get<PhrasebookResponse>(PHRASEBOOK_URL)
        return response.toDomain() ?: throw IllegalStateException("Malformed json file!")
    }

    companion object {
        private const val PHRASEBOOK_URL =
            "https://raw.githubusercontent.com/TwoEightNine/LearnMokshan" +
                    "/master/content/phrasebook/phrasebook.json"
    }
}