package global.msnthrp.mokshan.data.network.phrasebook

import global.msnthrp.mokshan.domain.phrasebook.Category
import global.msnthrp.mokshan.domain.phrasebook.ForeignLanguage
import global.msnthrp.mokshan.domain.phrasebook.Phrase
import global.msnthrp.mokshan.domain.phrasebook.Translation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PhrasebookMapperTest {

    private fun tr(value: String? = "Hello", language: String? = "en", note: String? = null) =
        PhrasebookResponse.Translation(value = value, note = note, language = language)

    private fun category(id: String? = "greetings", translations: List<PhrasebookResponse.Translation>? = listOf(tr())) =
        PhrasebookResponse.Category(id, translations)

    private fun phrase(
        mokshan: String? = "Шумбрат!",
        category: String? = "greetings",
        translations: List<PhrasebookResponse.Translation>? = listOf(tr()),
    ) = PhrasebookResponse.Phrase(mokshan, category, translations)

    private fun response(
        version: Int? = 1,
        categories: List<PhrasebookResponse.Category>? = listOf(category()),
        phrases: List<PhrasebookResponse.Phrase>? = listOf(phrase()),
    ) = PhrasebookResponse(version, categories, phrases)

    @Test
    fun phrasebook_isMapped() {
        val phrasebook = response().toDomain()!!

        val expectedCategory = Category("greetings", listOf(Translation("Hello", ForeignLanguage.ENGLISH)))
        assertEquals(1, phrasebook.version)
        assertEquals(listOf(expectedCategory), phrasebook.categories)
        assertEquals(
            listOf(Phrase("Шумбрат!", listOf(Translation("Hello", ForeignLanguage.ENGLISH)), expectedCategory)),
            phrasebook.phrases,
        )
    }

    @Test
    fun phrasebook_withoutPhrasesList_isInvalid() {
        assertNull(response(phrases = null).toDomain())
    }

    @Test
    fun phrasebook_withoutVersion_getsZero() {
        assertEquals(0, response(version = null).toDomain()!!.version)
    }

    @Test
    fun phrasebook_withoutCategories_hasNoPhrasesEither() {
        val phrasebook = response(categories = null).toDomain()!!

        assertEquals(emptyList(), phrasebook.categories)
        assertEquals(emptyList(), phrasebook.phrases)
    }

    @Test
    fun languages_areMappedAndUnknownOnesDropped() {
        val phrasebook = response(
            phrases = listOf(
                phrase(
                    translations = listOf(
                        tr("Hello", "en"), tr("Привет", "ru"), tr("Hallo", "de"), tr("?", null),
                    ),
                ),
            ),
        ).toDomain()!!

        assertEquals(
            listOf(ForeignLanguage.ENGLISH, ForeignLanguage.RUSSIAN),
            phrasebook.phrases.single().translations.map { it.foreignLanguage },
        )
    }

    @Test
    fun translation_withoutValue_isDropped() {
        val phrasebook = response(phrases = listOf(phrase(translations = listOf(tr(value = null), tr("ok"))))).toDomain()!!

        assertEquals(listOf("ok"), phrasebook.phrases.single().translations.map { it.value })
    }

    @Test
    fun blankNote_isDropped_andRealNoteKept() {
        val phrasebook = response(
            phrases = listOf(phrase(translations = listOf(tr("a", note = "  "), tr("b", note = "formal")))),
        ).toDomain()!!

        assertEquals(listOf(null, "formal"), phrasebook.phrases.single().translations.map { it.note })
    }

    @Test
    fun phrase_withUnknownCategory_isDropped() {
        val phrasebook = response(phrases = listOf(phrase(category = "missing"), phrase(category = null))).toDomain()!!

        assertEquals(emptyList(), phrasebook.phrases)
    }

    @Test
    fun phrase_withoutMokshanOrTranslationsList_isDropped() {
        val phrasebook = response(
            phrases = listOf(phrase(mokshan = null), phrase(translations = null), phrase(mokshan = "ok")),
        ).toDomain()!!

        assertEquals(listOf("ok"), phrasebook.phrases.map { it.mokshanPhrase })
    }

    @Test
    fun category_withoutIdOrTranslationsList_isDropped() {
        val phrasebook = response(
            categories = listOf(category(id = null), category(translations = null), category(id = "ok")),
            phrases = emptyList(),
        ).toDomain()!!

        assertEquals(listOf("ok"), phrasebook.categories.map { it.id })
    }

    @Test
    fun categoryWithOnlyUnknownLanguages_isKeptWithoutTranslations() {
        val phrasebook = response(categories = listOf(category(translations = listOf(tr(language = "de"))))).toDomain()!!

        assertEquals(emptyList(), phrasebook.categories.single().translations)
    }

    @Test
    fun duplicatedCategoryIds_lastOneWinsForPhrases() {
        val first = category("c", listOf(tr("first")))
        val second = category("c", listOf(tr("second")))

        val phrasebook = response(categories = listOf(first, second), phrases = listOf(phrase(category = "c"))).toDomain()!!

        assertEquals("second", phrasebook.phrases.single().category.translations.single().value)
    }
}
