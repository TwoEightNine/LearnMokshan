package global.msnthrp.mokshan.android.features.phrasebook

import global.msnthrp.mokshan.domain.phrasebook.Phrasebook

data class PhrasebookState(
    val isLoading: Boolean = false,
    val phrasebook: Phrasebook? = null,
)