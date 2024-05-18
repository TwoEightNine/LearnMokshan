package global.msnthrp.mokshan.data.network.phrasebook

import global.msnthrp.mokshan.domain.phrasebook.Category
import global.msnthrp.mokshan.domain.phrasebook.ForeignLanguage
import global.msnthrp.mokshan.domain.phrasebook.Phrase
import global.msnthrp.mokshan.domain.phrasebook.Phrasebook
import global.msnthrp.mokshan.domain.phrasebook.Translation


internal fun PhrasebookResponse.toDomain(): Phrasebook? {
    val categories = this.categories?.mapNotNull { it.toDomain() } ?: emptyList()
    val categoriesMap = categories.associateBy { it.id }
    return Phrasebook(
        version = this.version ?: 0,
        categories = this.categories?.mapNotNull { it.toDomain() } ?: emptyList(),
        phrases = this.phrasebook?.mapNotNull { it.toDomain(categoriesMap) } ?: return null,
    )
}

private fun PhrasebookResponse.Category.toDomain(): Category? {
    return Category(
        id = this.category ?: return null,
        translations = this.translations?.mapNotNull { it.toDomain() } ?: return null,
    )
}

private fun PhrasebookResponse.Phrase.toDomain(categoriesMap: Map<String, Category>): Phrase? {
    val category = categoriesMap[this.category.orEmpty()] ?: return null
    return Phrase(
        mokshanPhrase = this.mokshan ?: return null,
        category = category,
        translations = this.translations?.mapNotNull { it.toDomain() } ?: return null,
    )
}

private fun PhrasebookResponse.Translation.toDomain(): Translation? {
    return Translation(
        value = this.value ?: return null,
        foreignLanguage = when (this.language) {
            "en" -> ForeignLanguage.ENGLISH
            "ru" -> ForeignLanguage.RUSSIAN
            else -> return null
        },
        note = this.note?.takeIf { it.isNotBlank() }
    )
}