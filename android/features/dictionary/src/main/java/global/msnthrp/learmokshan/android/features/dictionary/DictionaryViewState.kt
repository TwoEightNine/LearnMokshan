package global.msnthrp.learmokshan.android.features.dictionary

import global.msnthrp.mokshan.domain.dictionary.Dictionary

data class DictionaryViewState(
    val isLoading: Boolean = false,
    val dictionary: Dictionary? = null,
    // loading failed and there is nothing loaded earlier to show instead
    val isFailed: Boolean = false,
)