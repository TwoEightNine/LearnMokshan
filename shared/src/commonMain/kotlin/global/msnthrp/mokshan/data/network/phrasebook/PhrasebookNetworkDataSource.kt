package global.msnthrp.mokshan.data.network.phrasebook

import global.msnthrp.mokshan.data.network.ServerConfig
import global.msnthrp.mokshan.data.network.base.NetworkClient
import global.msnthrp.mokshan.data.repository.phrasebook.PhrasebookRepositoryImpl
import global.msnthrp.mokshan.domain.phrasebook.Phrasebook

class PhrasebookNetworkDataSource(
    private val serverConfig: ServerConfig,
    private val client: NetworkClient,
) : PhrasebookRepositoryImpl.NetworkDataSource {

    override suspend fun loadPhrasebook(): Phrasebook {
        return client.getMapped<PhrasebookResponse, Phrasebook>(serverConfig.getPhrasebookUrl()) {
            it.toDomain() ?: throw IllegalStateException("Malformed json file!")
        }
    }
}
