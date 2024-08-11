package global.msnthrp.mokshan.domain.dictionary

typealias Dictionary = List<DictionaryEntry>

data class DictionaryEntry(
    val mokshan: String,
    val native: List<String>,
    val addedTs: Long = 0L,
)