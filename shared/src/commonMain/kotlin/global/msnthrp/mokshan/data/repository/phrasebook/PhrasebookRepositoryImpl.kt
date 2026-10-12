package global.msnthrp.mokshan.data.repository.phrasebook

import global.msnthrp.mokshan.domain.phrasebook.Phrasebook
import global.msnthrp.mokshan.usecase.phrasebook.PhrasebookRepository
import global.msnthrp.mokshan.utils.runCatchingCancellable

class PhrasebookRepositoryImpl(
    private val networkDs: NetworkDataSource,
) : PhrasebookRepository {

    override suspend fun loadPhrasebook(): Result<Phrasebook> {
        return runCatchingCancellable { networkDs.loadPhrasebook() }
    }

    interface NetworkDataSource {
        suspend fun loadPhrasebook(): Phrasebook
    }
}