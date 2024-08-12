package global.msnthrp.learmokshan.android.features.dictionary

import global.msnthrp.mokshan.domain.dictionary.Dictionary

data class DictionaryViewState(
    val isLoading: Boolean = false,
    val dictionary: Dictionary? = null,
)