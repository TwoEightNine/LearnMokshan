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
    fun groups_followTheOrderOfCategories_notTheOrderOfPhrases() = runTest {
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

        assertEquals(listOf("greetings", "food"), result.phrases.map { it.category.id })
        assertEquals(listOf("а", "в"), result.phrases[1].phrases.map { it.mokshanPhrase })
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
    fun phraseWithoutTranslationInLanguage_isShownInEnglish() = runTest {
        val phrasebook = Phrasebook(
            1,
            listOf(greetings),
            listOf(
                phrase("а", greetings, tr("Hello", ENGLISH)),
                phrase("б", greetings, tr("Привет", RUSSIAN)),
            ),
        )

        val phrases = useCase(phrasebook, RUSSIAN).loadPhrasebook().assertSuccess().phrases.single().phrases

        assertEquals(listOf("а", "б"), phrases.map { it.mokshanPhrase })
        assertEquals(listOf(tr("Hello", ENGLISH)), phrases[0].translations)
        assertEquals(listOf(tr("Привет", RUSSIAN)), phrases[1].translations)
    }

    @Test
    fun deviceLanguageWins_overEnglish_whenBothExist() = runTest {
        val phrasebook = Phrasebook(
            1,
            listOf(greetings),
            listOf(phrase("а", greetings, tr("Hello", ENGLISH), tr("Привет", RUSSIAN))),
        )

        val phrase = useCase(phrasebook, RUSSIAN).loadPhrasebook().assertSuccess().phrases.single().phrases.single()

        assertEquals(listOf(tr("Привет", RUSSIAN)), phrase.translations)
    }

    @Test
    fun englishDevice_neverFallsBackToRussian() = runTest {
        val phrasebook = Phrasebook(
            1,
            listOf(greetings),
            listOf(phrase("а", greetings, tr("Привет", RUSSIAN)), phrase("б", greetings, tr("Hello", ENGLISH))),
        )

        val phrases = useCase(phrasebook, ENGLISH).loadPhrasebook().assertSuccess().phrases.single().phrases

        assertEquals(listOf("б"), phrases.map { it.mokshanPhrase })
    }

    @Test
    fun categoryNotTranslatedIntoDeviceLanguage_isShownInEnglish() = runTest {
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

        assertEquals(listOf("greetings", "misc"), result.phrases.map { it.category.id })
        assertEquals(listOf(tr("Misc", ENGLISH)), result.phrases[1].category.translations)
        assertEquals(listOf(tr("Вещь", RUSSIAN)), result.phrases[1].phrases.single().translations)
    }

    @Test
    fun categoryWithoutAnyUsableTranslation_isLeftOutWithItsPhrases() = runTest {
        val untitled = Category("untitled", emptyList())
        val phrasebook = Phrasebook(
            1,
            listOf(untitled, greetings),
            listOf(
                phrase("а", untitled, tr("Thing", ENGLISH)),
                phrase("б", greetings, tr("Hello", ENGLISH)),
            ),
        )

        val result = useCase(phrasebook).loadPhrasebook().assertSuccess()

        assertEquals(listOf("greetings"), result.phrases.map { it.category.id })
    }

    @Test
    fun phraseWithoutAnyUsableTranslation_isLeftOut() = runTest {
        val phrasebook = Phrasebook(
            1,
            listOf(greetings),
            listOf(phrase("а", greetings), phrase("б", greetings, tr("Hello", ENGLISH))),
        )

        val phrases = useCase(phrasebook).loadPhrasebook().assertSuccess().phrases.single().phrases

        assertEquals(listOf("б"), phrases.map { it.mokshanPhrase })
    }

    @Test
    fun duplicatedCategoryIds_firstOneWins() = runTest {
        val first = category("c", tr("First", ENGLISH))
        val second = category("c", tr("Second", ENGLISH))
        val phrasebook = Phrasebook(1, listOf(first, second), listOf(phrase("а", first, tr("Hello", ENGLISH))))

        val result = useCase(phrasebook).loadPhrasebook().assertSuccess()

        assertEquals(1, result.phrases.size)
        assertEquals("First", result.phrases.single().category.translations.single().value)
    }

    @Test
    fun phrasesKeepTheirOrderInsideACategory() = runTest {
        val phrasebook = Phrasebook(
            1,
            listOf(greetings),
            listOf("в", "а", "б").map { phrase(it, greetings, tr("x $it", ENGLISH)) },
        )

        val phrases = useCase(phrasebook).loadPhrasebook().assertSuccess().phrases.single().phrases

        assertEquals(listOf("в", "а", "б"), phrases.map { it.mokshanPhrase })
    }

    @Test
    fun whenNothingIsTranslated_fallsBackToEnglish() = runTest {
        val englishCategory = category("c", tr("Cat", ENGLISH))
        val phrasebook = Phrasebook(1, listOf(englishCategory), listOf(phrase("а", englishCategory, tr("Hi", ENGLISH))))

        val result = useCase(phrasebook, RUSSIAN).loadPhrasebook().assertSuccess()

        assertEquals(listOf(tr("Hi", ENGLISH)), result.phrases.single().phrases.single().translations)
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
    fun phraseCategory_isTheSameLocalizedCategoryAsTheGroup() = runTest {
        val phrasebook = Phrasebook(1, listOf(greetings), listOf(phrase("а", greetings, tr("Hello", ENGLISH))))

        val group = useCase(phrasebook).loadPhrasebook().assertSuccess().phrases.single()

        assertEquals(listOf(ENGLISH), group.category.translations.map { it.foreignLanguage })
        assertEquals(group.category, group.phrases.single().category)
    }
}
