package global.msnthrp.mokshan.domain.phrasebook

typealias Phrasebook = List<Phrase>

data class Phrase(
    val mokshanPhrase: String,
    val translations: List<Translation>,
    val tags: List<Any> = emptyList(),
)

data class Translation(
    val value: String,
    val foreignLanguage: ForeignLanguage,
)

enum class ForeignLanguage {
    ENGLISH,
    RUSSIAN,
}