package global.msnthrp.mokshan.usecase.lesson

import global.msnthrp.mokshan.domain.lessons.BankWord
import global.msnthrp.mokshan.domain.lessons.LessonStep
import global.msnthrp.mokshan.domain.lessons.LessonStepDirection.FROM_MOKSHAN
import global.msnthrp.mokshan.domain.lessons.LessonStepDirection.TO_MOKSHAN
import global.msnthrp.mokshan.domain.lessons.LessonStepType
import global.msnthrp.mokshan.domain.lessons.Topic
import global.msnthrp.mokshan.domain.lessons.UserInput
import global.msnthrp.mokshan.testutil.lesson
import global.msnthrp.mokshan.testutil.pair
import global.msnthrp.mokshan.testutil.sampleTopic
import global.msnthrp.mokshan.testutil.topic
import global.msnthrp.mokshan.testutil.translation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LessonUseCaseCheckTest {

    private val useCase = LessonHarness(sampleTopic()).useCase

    private fun inputStep(vararg answers: String) = LessonStep(
        type = LessonStepType.Input,
        direction = TO_MOKSHAN,
        sentence = "irrelevant",
        answers = answers.toList(),
    )

    private fun typed(text: String) = UserInput.Input(text)

    private fun bank(vararg words: String) =
        UserInput.Bank(words.mapIndexed { index, word -> BankWord(word, index) })

    // region isCorrect: typed input

    @Test
    fun exactAnswer_isCorrect() {
        assertTrue(useCase.isCorrect(typed("Шумбрат!"), inputStep("Шумбрат!")))
    }

    @Test
    fun wrongAnswer_isIncorrect() {
        assertFalse(useCase.isCorrect(typed("Шумбратада"), inputStep("Шумбрат!")))
    }

    @Test
    fun answerComparison_ignoresCase() {
        assertTrue(useCase.isCorrect(typed("ШУМБРАТ"), inputStep("шумбрат")))
        assertTrue(useCase.isCorrect(typed("hello"), inputStep("Hello")))
    }

    @Test
    fun answerComparison_ignoresSupportedPunctuation() {
        val step = inputStep("Кода тефне? – Аф пяк!")

        assertTrue(useCase.isCorrect(typed("кода тефне аф пяк"), step))
        assertTrue(useCase.isCorrect(typed("Кода, тефне. Аф пяк"), step))
    }

    @Test
    fun answerComparison_trimsUserInput() {
        assertTrue(useCase.isCorrect(typed("  hello  "), inputStep("Hello")))
        assertTrue(useCase.isCorrect(typed("hello ?"), inputStep("Hello")))
    }

    @Test
    fun anyOfSeveralAnswers_isCorrect() {
        val step = inputStep("He lives", "She lives")

        assertTrue(useCase.isCorrect(typed("she lives"), step))
        assertTrue(useCase.isCorrect(typed("He lives"), step))
        assertFalse(useCase.isCorrect(typed("it lives"), step))
    }

    @Test
    fun doubleSpaceInsideAnswer_isCollapsedOnce() {
        assertTrue(useCase.isCorrect(typed("how  are you"), inputStep("how are you")))
    }

    @Test
    fun tripleSpaceInsideAnswer_isNotCollapsed_knownBugB11() {
        // KNOWN BUG B11: only one pass of "  " -> " " is performed
        assertFalse(useCase.isCorrect(typed("how   are you"), inputStep("how are you")))
    }

    @Test
    fun answersWithOuterWhitespace_areNotTrimmed_knownBugB11() {
        // KNOWN BUG B11: the user input is trimmed, the stored answers are not
        assertFalse(useCase.isCorrect(typed("hello"), inputStep("hello ")))
        assertFalse(useCase.isCorrect(typed("hello"), inputStep(" hello")))
    }

    @Test
    fun punctuationOutsideSupportedSet_isSignificant_knownBugB11() {
        // KNOWN BUG B11: hyphen, apostrophe, ellipsis are not normalised
        assertFalse(useCase.isCorrect(typed("so so"), inputStep("so-so")))
        assertFalse(useCase.isCorrect(typed("im here"), inputStep("I'm here")))
        assertFalse(useCase.isCorrect(typed("wait"), inputStep("wait…")))
    }

    @Test
    fun emptyInput_isIncorrectForRegularAnswer() {
        assertFalse(useCase.isCorrect(typed(""), inputStep("hello")))
        assertFalse(useCase.isCorrect(typed("   "), inputStep("hello")))
    }

    @Test
    fun emptyInput_matchesAnswerConsistingOnlyOfPunctuation() {
        assertTrue(useCase.isCorrect(typed(""), inputStep("?!")))
    }

    @Test
    fun noAnswers_meansEverythingIsIncorrect() {
        assertFalse(useCase.isCorrect(typed("anything"), inputStep()))
    }

    // endregion

    // region isCorrect: word bank

    @Test
    fun bankWords_areJoinedBySpaceInSelectedOrder() {
        val step = inputStep("how are you?")

        assertTrue(useCase.isCorrect(bank("how", "are", "you"), step))
        assertFalse(useCase.isCorrect(bank("are", "how", "you"), step))
    }

    @Test
    fun bank_acceptsAnyOfSeveralAnswers() {
        val step = inputStep("He lives", "She lives")

        assertTrue(useCase.isCorrect(bank("she", "lives"), step))
        assertFalse(useCase.isCorrect(bank("she", "he"), step))
    }

    @Test
    fun emptyBank_isIncorrect() {
        assertFalse(useCase.isCorrect(UserInput.Bank(), inputStep("hello")))
    }

    @Test
    fun partialBank_isIncorrect() {
        assertFalse(useCase.isCorrect(bank("how", "are"), inputStep("how are you")))
    }

    @Test
    fun bankWithDuplicatedWordInAnswer_isCorrect() {
        assertTrue(useCase.isCorrect(bank("he", "lives", "he"), inputStep("He lives he")))
    }

    @Test
    fun cyrillicAnswers_areComparedCaseInsensitively() {
        assertTrue(useCase.isCorrect(bank("кода", "тефне"), inputStep("Кода тефне?")))
    }

    // endregion

    // region getTranslationsForWord

    private val hintsTopic: Topic = topic(
        lessons = listOf(
            lesson(
                1,
                listOf(
                    pair("Лац", "Fine", "OK!"),
                    pair("Кода тефне?", "How are you?"),
                    pair("Кода", "How"),
                ),
            ),
        ),
        translations = listOf(
            translation("кода", "how"),
            translation("кода", "in what way"),
            translation("тефне", "your deal"),
            translation("Пара", "good"),
        ),
    )

    @Test
    fun translationsFromMokshan_comeFromExplicitTranslations() {
        val result = useCase.getTranslationsForWord("тефне", hintsTopic, FROM_MOKSHAN)

        assertEquals(listOf("your deal"), result)
    }

    @Test
    fun translationsFromMokshan_mergeExplicitAndInferredFromSentencesWithoutDuplicates() {
        val result = useCase.getTranslationsForWord("Кода?", hintsTopic, FROM_MOKSHAN)

        // explicit: how, in what way; inferred from the pair "Кода" -> "How": how (duplicate)
        assertEquals(listOf("how", "in what way"), result)
    }

    @Test
    fun translationsFromMokshan_inferredFromSingleWordPair() {
        val result = useCase.getTranslationsForWord("лац", hintsTopic, FROM_MOKSHAN)

        assertEquals(listOf("fine", "ok"), result)
    }

    @Test
    fun translationsFromMokshan_areCaseInsensitiveForTheClickedWord() {
        assertEquals(
            useCase.getTranslationsForWord("лац", hintsTopic, FROM_MOKSHAN),
            useCase.getTranslationsForWord("ЛАЦ!", hintsTopic, FROM_MOKSHAN),
        )
    }

    @Test
    fun translationsToMokshan_findMokshanForNativeWord() {
        val result = useCase.getTranslationsForWord("How?", hintsTopic, TO_MOKSHAN)

        // explicit translation "кода"->"how" and the pair "Кода" -> "How"
        assertEquals(listOf("кода"), result)
    }

    @Test
    fun translationsToMokshan_matchWholeNativeVariantOnly() {
        // "fine" is a whole variant of the pair, "are" is only a part of a sentence
        assertEquals(listOf("лац"), useCase.getTranslationsForWord("fine", hintsTopic, TO_MOKSHAN))
        assertEquals(emptyList(), useCase.getTranslationsForWord("are", hintsTopic, TO_MOKSHAN))
    }

    @Test
    fun translations_forUnknownWord_areEmpty() {
        assertEquals(emptyList(), useCase.getTranslationsForWord("неведомо", hintsTopic, FROM_MOKSHAN))
        assertEquals(emptyList(), useCase.getTranslationsForWord("unknown", hintsTopic, TO_MOKSHAN))
    }

    @Test
    fun explicitTranslationKeys_areComparedWithoutNormalisation() {
        // pins current behaviour: a translation stored as "Пара" is not found by "пара"
        assertEquals(emptyList(), useCase.getTranslationsForWord("Пара", hintsTopic, FROM_MOKSHAN))
    }

    @Test
    fun translationsForSentenceWithPunctuation_areFoundByWholeCleanedSentence() {
        val result = useCase.getTranslationsForWord("Кода тефне?", hintsTopic, FROM_MOKSHAN)

        assertEquals(listOf("how are you"), result)
    }

    // endregion
}
