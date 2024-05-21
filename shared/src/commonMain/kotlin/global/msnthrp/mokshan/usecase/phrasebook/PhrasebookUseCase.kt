package global.msnthrp.mokshan.usecase.phrasebook

import global.msnthrp.mokshan.domain.phrasebook.Category
import global.msnthrp.mokshan.domain.phrasebook.ForeignLanguage
import global.msnthrp.mokshan.domain.phrasebook.Phrase
import global.msnthrp.mokshan.domain.phrasebook.Phrasebook
import global.msnthrp.mokshan.usecase.DeviceLocaleProvider

class PhrasebookUseCase(
    private val deviceLocaleProvider: DeviceLocaleProvider,
    private val phrasebookRepository: PhrasebookRepository,
) {

    suspend fun loadPhrasebook(): Result<CategorizedPhrasebook> {
        val phrasebookResult = phrasebookRepository.loadPhrasebook()
        val phrasebook = phrasebookResult.getOrNull()
            ?: return Result.failure(
                phrasebookResult.exceptionOrNull()
                    ?: IllegalStateException("Error without throwable")
            )

        val deviceLocale = deviceLocaleProvider.getDeviceLocale()
        var phrasebookWithOneLocale = phrasebook.getWithDeviceLocale(deviceLocale)
        if (phrasebookWithOneLocale == null) {
            phrasebookWithOneLocale = phrasebook.getWithDeviceLocale(ForeignLanguage.ENGLISH)
        }

        return when (phrasebookWithOneLocale) {
            null -> Result.failure(IllegalStateException("Phrasebook is empty"))
            else -> Result.success(phrasebookWithOneLocale)
        }
    }

    private fun Phrasebook.getWithDeviceLocale(deviceLocale: ForeignLanguage): CategorizedPhrasebook? {
        val categories = this.categories.mapNotNull { it.getWithDeviceLocale(deviceLocale) }
        if (categories.isEmpty()) return null

        val categoryIds = categories.map { it.id }
        val phrases = this.phrases.mapNotNull { it.getWithDeviceLocale(deviceLocale) }
            .filter { it.category.id in categoryIds }
        if (phrases.isEmpty()) return null

        val categoryIdToPhrasesMap = mutableMapOf<String, MutableList<Phrase>>()
        phrases.forEach { phrase ->
            if (phrase.category.id !in categoryIdToPhrasesMap) {
                categoryIdToPhrasesMap[phrase.category.id] = mutableListOf()
            }
            categoryIdToPhrasesMap[phrase.category.id]?.add(phrase)
        }

        return CategorizedPhrasebook(
            phrases = categoryIdToPhrasesMap.mapNotNull { (categoryId, phrases) ->
                PhrasesByCategories(
                    category = categories.find { it.id == categoryId } ?: return@mapNotNull null,
                    phrases = phrases,
                )
            }
        )
    }

    private fun Category.getWithDeviceLocale(deviceLocale: ForeignLanguage): Category? {
        val translations = this.translations.filter { it.foreignLanguage == deviceLocale }
        return translations.takeIf { it.isNotEmpty() }?.let { Category(this.id, it) }
    }

    private fun Phrase.getWithDeviceLocale(deviceLocale: ForeignLanguage): Phrase? {
        val translations = this.translations.filter { it.foreignLanguage == deviceLocale }
        return translations.takeIf { it.isNotEmpty() }?.let {
            Phrase(
                mokshanPhrase = this.mokshanPhrase,
                translations = translations,
                category = this.category,
            )
        }
    }

    data class CategorizedPhrasebook(
        val phrases: List<PhrasesByCategories>
    )

    data class PhrasesByCategories(
        val category: Category,
        val phrases: List<Phrase>
    )
}