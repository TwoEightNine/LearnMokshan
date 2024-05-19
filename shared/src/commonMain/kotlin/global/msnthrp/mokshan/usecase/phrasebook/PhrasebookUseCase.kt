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

    suspend fun loadPhrasebook(): Result<Phrasebook> {
        val phrasebookResult = phrasebookRepository.loadPhrasebook()
        val phrasebook = phrasebookResult.getOrNull() ?: return phrasebookResult

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

    private fun Phrasebook.getWithDeviceLocale(deviceLocale: ForeignLanguage): Phrasebook? {
        val categories = this.categories.mapNotNull { it.getWithDeviceLocale(deviceLocale) }
        if (categories.isEmpty()) return null

        val categoryIds = categories.map { it.id }
        val phrases = this.phrases.mapNotNull { it.getWithDeviceLocale(deviceLocale) }
            .filter { it.category.id in categoryIds }
        if (phrases.isEmpty()) return null

        return Phrasebook(version, categories, phrases)
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
}