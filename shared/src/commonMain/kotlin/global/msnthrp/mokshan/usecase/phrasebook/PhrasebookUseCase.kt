package global.msnthrp.mokshan.usecase.phrasebook

import global.msnthrp.mokshan.domain.phrasebook.Category
import global.msnthrp.mokshan.domain.phrasebook.ForeignLanguage
import global.msnthrp.mokshan.domain.phrasebook.Phrase
import global.msnthrp.mokshan.domain.phrasebook.Translation
import global.msnthrp.mokshan.usecase.DeviceLocaleProvider

class PhrasebookUseCase(
    private val deviceLocaleProvider: DeviceLocaleProvider,
    private val phrasebookRepository: PhrasebookRepository,
) {

    /**
     * Phrases grouped by category, in the order of the categories. Texts are in the device
     * language; a category or a phrase that is not translated into it is shown in English.
     * A category without any usable phrase, and a phrase whose category cannot be shown, are left out.
     */
    suspend fun loadPhrasebook(): Result<CategorizedPhrasebook> {
        val phrasebookResult = phrasebookRepository.loadPhrasebook()
        val phrasebook = phrasebookResult.getOrNull()
            ?: return Result.failure(
                phrasebookResult.exceptionOrNull()
                    ?: IllegalStateException("Error without throwable")
            )

        val language = deviceLocaleProvider.getDeviceLocale()
        val groups = phrasebook.categories
            .distinctBy { it.id }
            .mapNotNull { category ->
                val translations = category.translations.inLanguage(language)
                if (translations.isEmpty()) return@mapNotNull null
                val localizedCategory = Category(category.id, translations)
                val phrases = phrasebook.phrases
                    .filter { it.category.id == category.id }
                    .mapNotNull { it.localized(localizedCategory, language) }
                phrases.takeIf { it.isNotEmpty() }?.let { PhrasesByCategories(localizedCategory, it) }
            }

        return when {
            groups.isEmpty() -> Result.failure(IllegalStateException("Phrasebook is empty"))
            else -> Result.success(CategorizedPhrasebook(groups))
        }
    }

    private fun Phrase.localized(category: Category, language: ForeignLanguage): Phrase? {
        val translations = this.translations.inLanguage(language)
        return translations.takeIf { it.isNotEmpty() }?.let {
            Phrase(mokshanPhrase = mokshanPhrase, translations = it, category = category)
        }
    }

    private fun List<Translation>.inLanguage(language: ForeignLanguage): List<Translation> {
        return filter { it.foreignLanguage == language }
            .ifEmpty { filter { it.foreignLanguage == ForeignLanguage.ENGLISH } }
    }

    data class CategorizedPhrasebook(
        val phrases: List<PhrasesByCategories>
    )

    data class PhrasesByCategories(
        val category: Category,
        val phrases: List<Phrase>
    )
}