package global.msnthrp.mokshan.usecase.phrasebook

import global.msnthrp.mokshan.domain.phrasebook.Phrasebook

interface PhrasebookRepository {
    suspend fun loadPhrasebook(): Result<Phrasebook>
}