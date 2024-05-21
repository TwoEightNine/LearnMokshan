package global.msnthrp.mokshan.android.features.phrasebook

import global.msnthrp.mokshan.usecase.phrasebook.PhrasebookUseCase

data class PhrasebookState(
    val isLoading: Boolean = false,
    val phrasebook: PhrasebookUseCase.CategorizedPhrasebook? = null,
    val visibleCategory: String? = null,
)