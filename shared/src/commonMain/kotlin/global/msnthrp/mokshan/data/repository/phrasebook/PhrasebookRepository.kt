package global.msnthrp.mokshan.data.repository.phrasebook

import global.msnthrp.mokshan.domain.phrasebook.Phrasebook

class PhrasebookRepository(
    private val networkDs: NetworkDataSource,
) {

    suspend fun loadPhrasebook(): Result<Phrasebook> {
        return kotlin.runCatching { networkDs.loadPhrasebook() }
    }

    interface NetworkDataSource {
        suspend fun loadPhrasebook(): Phrasebook
    }
}