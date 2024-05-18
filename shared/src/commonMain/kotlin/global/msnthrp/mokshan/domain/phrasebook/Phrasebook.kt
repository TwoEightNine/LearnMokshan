package global.msnthrp.mokshan.domain.phrasebook

data class Phrasebook(
    val version: Int,
    val categories: List<Category>,
    val phrases: List<Phrase>,
)


data class Phrase(
    val mokshanPhrase: String,
    val translations: List<Translation>,
    val category: Category,
)

data class Translation(
    val value: String,
    val foreignLanguage: ForeignLanguage,
    val note: String? = null,
)

data class Category(
    val id: String,
    val translations: List<Translation>,
)

enum class ForeignLanguage {
    ENGLISH,
    RUSSIAN,
}