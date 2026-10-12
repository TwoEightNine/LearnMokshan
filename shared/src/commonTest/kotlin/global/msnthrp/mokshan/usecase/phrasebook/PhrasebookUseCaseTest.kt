package global.msnthrp.mokshan.usecase.phrasebook

import global.msnthrp.mokshan.domain.phrasebook.Category
import global.msnthrp.mokshan.domain.phrasebook.ForeignLanguage
import global.msnthrp.mokshan.domain.phrasebook.ForeignLanguage.ENGLISH
import global.msnthrp.mokshan.domain.phrasebook.ForeignLanguage.RUSSIAN
import global.msnthrp.mokshan.domain.phrasebook.Phrase
import global.msnthrp.mokshan.domain.phrasebook.Phrasebook
import global.msnthrp.mokshan.domain.phrasebook.Translation
import global.msnthrp.mokshan.testutil.FakeLocaleProvider
import global.msnthrp.mokshan.testutil.assertFailure
import global.msnthrp.mokshan.testutil.assertSuccess
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PhrasebookUseCaseTest {

    private class FakeRepository(var result: Result<Phrasebook>) : PhrasebookRepository {
        override suspend fun loadPhrasebook(): Result<Phrasebook> = result
    }

    private fun tr(value: String, language: ForeignLanguage) = Translation(value, language)

    private fun category(id: String, vararg translations: Translation) = Category(id, translations.toList())

    private fun phrase(mokshan: String, category: Category, vararg translations: Translation) =
        Phrase(mokshan, translations.toList(), category)

    private val greetings = category("greetings", tr("Greetings", ENGLISH), tr("Приветствия", RUSSIAN))
    private val food = category("food", tr("Food", ENGLISH), tr("Еда", RUSSIAN))

    private fun useCase(
        phrasebook: Phrasebook,
        language: ForeignLanguage = ENGLISH,
    ) = PhrasebookUseCase(FakeLocaleProvider(language), FakeRepository(Result.success(phrasebook)))

    @Test
    fun phrases_areFilteredToDeviceLanguage() = runTest {
        val phrasebook = Phrasebook(
            1,
            listOf(greetings),
            listOf(phrase("Шумбрат!", greetings, tr("Hello", ENGLISH), tr("Привет", RUSSIAN))),
        )

        val result = useCase(phrasebook, RUSSIAN).loadPhrasebook().assertSuccess()

        val group = result.phrases.single()
        assertEquals(listOf(tr("Приветствия", RUSSIAN)), group.category.translations)
        assertEquals(listOf(tr("Привет", RUSSIAN)), group.phrases.single().translations)
    }

    @Test
    fun phrases_areGroupedByCategory_inOrderOfFirstPhrase() = runTest {
        val phrasebook = Phrasebook(
            1,
            listOf(greetings, food),
            listOf(
                phrase("а", food, tr("Bread", ENGLISH)),
                phrase("б", greetings, tr("Hello", ENGLISH)),
                phrase("в", food, tr("Water", ENGLISH)),
            ),
        )

        val result = useCase(phrasebook).loadPhrasebook().assertSuccess()

        assertEquals(listOf("food", "greetings"), result.phrases.map { it.category.id })
        assertEquals(listOf("а", "в"), result.phrases[0].phrases.map { it.mokshanPhrase })
    }

    @Test
    fun categoriesWithoutPhrases_areNotShown() = runTest {
        val phrasebook = Phrasebook(
            1,
            listOf(greetings, food),
            listOf(phrase("б", greetings, tr("Hello", ENGLISH))),
        )

        val result = useCase(phrasebook).loadPhrasebook().assertSuccess()

        assertEquals(listOf("greetings"), result.phrases.map { it.category.id })
    }

    @Test
    fun phraseWithoutTranslationInLanguage_isHidden() = runTest {
        val phrasebook = Phrasebook(
            1,
            listOf(greetings),
            listOf(
                phrase("а", greetings, tr("Hello", ENGLISH)),
                phrase("б", greetings, tr("Привет", RUSSIAN)),
            ),
        )

        val result = useCase(phrasebook, RUSSIAN).loadPhrasebook().assertSuccess()

        assertEquals(listOf("б"), result.phrases.single().phrases.map { it.mokshanPhrase })
    }

    @Test
    fun phraseWhoseCategoryLacksLanguage_isHiddenSilently() = runTest {
        val englishOnly = category("misc", tr("Misc", ENGLISH))
        val phrasebook = Phrasebook(
            1,
            listOf(greetings, englishOnly),
            listOf(
                phrase("а", englishOnly, tr("Thing", ENGLISH), tr("Вещь", RUSSIAN)),
                phrase("б", greetings, tr("Привет", RUSSIAN)),
            ),
        )

        val result = useCase(phrasebook, RUSSIAN).loadPhrasebook().assertSuccess()

        assertEquals(listOf("greetings"), result.phrases.map { it.category.id })
    }

    @Test
    fun whenNothingIsTranslated_fallsBackToEnglish() = runTest {
        val englishCategory = category("c", tr("Cat", ENGLISH))
        val phrasebook = Phrasebook(1, listOf(englishCategory), listOf(phrase("а", englishCategory, tr("Hi", ENGLISH))))

        val result = useCase(phrasebook, RUSSIAN).loadPhrasebook().assertSuccess()

        assertEquals(listOf(tr("Hi", ENGLISH)), result.phrases.single().phrases.single().translations)
    }

    @Test
    fun partiallyTranslatedPhrasebook_isNotMergedWithEnglish() = runTest {
        // pins current behaviour: English is used only when the device language yields nothing at all
        val phrasebook = Phrasebook(
            1,
            listOf(greetings),
            listOf(
                phrase("а", greetings, tr("Hello", ENGLISH)),
                phrase("б", greetings, tr("Привет", RUSSIAN)),
            ),
        )

        val result = useCase(phrasebook, RUSSIAN).loadPhrasebook().assertSuccess()

        assertEquals(1, result.phrases.single().phrases.size)
    }

    @Test
    fun emptyPhrasebook_isAFailure() = runTest {
        val error = useCase(Phrasebook(1, emptyList(), emptyList())).loadPhrasebook().assertFailure()

        assertIs<IllegalStateException>(error)
        assertEquals("Phrasebook is empty", error.message)
    }

    @Test
    fun phrasebookWithCategoriesButNoPhrases_isAFailure() = runTest {
        assertIs<IllegalStateException>(useCase(Phrasebook(1, listOf(greetings), emptyList())).loadPhrasebook().assertFailure())
    }

    @Test
    fun phrasesOfUnlistedCategory_areDropped() = runTest {
        val phrasebook = Phrasebook(1, listOf(greetings), listOf(phrase("а", food, tr("Bread", ENGLISH))))

        assertIs<IllegalStateException>(useCase(phrasebook).loadPhrasebook().assertFailure())
    }

    @Test
    fun repositoryFailure_isPropagated() = runTest {
        val boom = IllegalStateException("offline")
        val useCase = PhrasebookUseCase(FakeLocaleProvider(), FakeRepository(Result.failure(boom)))

        assertEquals(boom, useCase.loadPhrasebook().assertFailure())
    }

    @Test
    fun phraseCategory_keepsAllLanguages_whileGroupCategoryIsFiltered() = runTest {
        // pins current behaviour (B24): the category nested into a phrase is not filtered
        val phrasebook = Phrasebook(1, listOf(greetings), listOf(phrase("а", greetings, tr("Hello", ENGLISH))))

        val group = useCase(phrasebook).loadPhrasebook().assertSuccess().phrases.single()

        assertEquals(listOf(ENGLISH), group.category.translations.map { it.foreignLanguage })
        assertEquals(listOf(ENGLISH, RUSSIAN), group.phrases.single().category.translations.map { it.foreignLanguage })
    }
}
