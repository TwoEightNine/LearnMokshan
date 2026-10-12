package global.msnthrp.mokshan.android.features.lessons.lesson

import global.msnthrp.mokshan.android.features.lessons.LessonsEnvironment
import global.msnthrp.mokshan.android.features.lessons.MainDispatcherRule
import global.msnthrp.mokshan.android.features.lessons.topicInfo
import global.msnthrp.mokshan.domain.lessons.BankWord
import global.msnthrp.mokshan.domain.lessons.LessonStep
import global.msnthrp.mokshan.domain.lessons.LessonStepType
import global.msnthrp.mokshan.domain.lessons.UserInput
import global.msnthrp.mokshan.usecase.lesson.LessonUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LessonViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private fun viewModel(
        lessonNumber: Int,
        environment: LessonsEnvironment = LessonsEnvironment(),
    ) = LessonViewModel(topicInfo(), lessonNumber, environment.useCase)

    private val LessonViewModel.step: LessonStep
        get() = state.value.run { preparedLesson!!.lessonSteps[currentStepIndex] }

    private fun cleanWords(sentence: String): List<String> =
        sentence.replace("?", "").replace("!", "").replace(".", "").replace(",", "")
            .lowercase().split(" ")

    private fun LessonViewModel.typeCorrectAnswer() {
        val step = step
        when (val type = step.type) {
            LessonStepType.Input -> onInputUpdated(step.answers.first())
            is LessonStepType.WordBank -> {
                val used = mutableSetOf<Int>()
                cleanWords(step.answers.first()).forEach { word ->
                    val bankWord = type.availableWords.first { it.word == word && it.index !in used }
                    used += bankWord.index
                    onWordAdded(bankWord)
                }
            }
        }
    }

    private fun LessonViewModel.answerCorrectly() {
        typeCorrectAnswer()
        onCheckClicked()
    }

    private fun LessonViewModel.answerWrongly() {
        when (step.type) {
            LessonStepType.Input -> onInputUpdated("definitely wrong")
            is LessonStepType.WordBank -> Unit // empty bank is never a correct answer
        }
        onCheckClicked()
    }

    // region initialisation

    @Test
    fun init_preparesLessonAndFirstEmptyInput() {
        val viewModel = viewModel(lessonNumber = 1)

        val state = viewModel.state.value
        assertEquals(topicInfo(), state.topicInfo)
        assertEquals(1, state.lessonNumber)
        assertEquals(3, state.preparedLesson!!.lessonSteps.size)
        assertEquals(0, state.currentStepIndex)
        assertEquals(UserInput.Bank(), state.userInput)
    }

    @Test
    fun init_ofTypedLesson_startsWithEmptyTextInput() {
        val viewModel = viewModel(lessonNumber = 4)

        assertEquals(UserInput.Input(), viewModel.state.value.userInput)
    }

    @Test
    fun init_whenPreparationFails_publishesFailedState() {
        val viewModel = viewModel(lessonNumber = 99)

        val state = viewModel.state.value
        assertTrue(state.isLoadFailed)
        assertNull(state.preparedLesson)
        assertNull(state.userInput)
        assertEquals(99, state.lessonNumber)
    }

    @Test
    fun init_whenNetworkFails_publishesFailedState() {
        val environment = LessonsEnvironment()
        environment.network.error = IllegalStateException("offline")

        assertTrue(viewModel(1, environment).state.value.isLoadFailed)
    }

    @Test
    fun successfulInit_isNotFailed() {
        assertFalse(viewModel(1).state.value.isLoadFailed)
    }

    @Test
    fun retry_afterFailure_loadsTheLesson() {
        val environment = LessonsEnvironment()
        environment.network.error = IllegalStateException("offline")
        val viewModel = viewModel(1, environment)

        environment.network.error = null
        viewModel.onRetryClicked()

        val state = viewModel.state.value
        assertFalse(state.isLoadFailed)
        assertEquals(3, state.preparedLesson!!.lessonSteps.size)
        assertEquals(UserInput.Bank(), state.userInput)
    }

    @Test
    fun retry_whileStillFailing_staysFailed() {
        val viewModel = viewModel(lessonNumber = 99)

        viewModel.onRetryClicked()

        assertTrue(viewModel.state.value.isLoadFailed)
        assertNull(viewModel.state.value.preparedLesson)
    }

    @Test
    fun initialProgress_isZero() {
        assertEquals(0f, viewModel(1).state.value.progress, 0f)
    }

    @Test
    fun progressWithoutLesson_isZeroDividedByOne() {
        assertEquals(0f, viewModel(99).state.value.progress, 0f)
    }

    // endregion

    // region user input

    @Test
    fun wordsAreAddedToBankInTapOrder() {
        val viewModel = viewModel(1)
        val words = (viewModel.step.type as LessonStepType.WordBank).availableWords

        viewModel.onWordAdded(words[2])
        viewModel.onWordAdded(words[0])

        assertEquals(UserInput.Bank(listOf(words[2], words[0])), viewModel.state.value.userInput)
    }

    @Test
    fun removingWord_removesOnlyThatWord() {
        val viewModel = viewModel(1)
        val words = (viewModel.step.type as LessonStepType.WordBank).availableWords
        viewModel.onWordAdded(words[0])
        viewModel.onWordAdded(words[1])

        viewModel.onWordRemoved(words[0])

        assertEquals(UserInput.Bank(listOf(words[1])), viewModel.state.value.userInput)
    }

    @Test
    fun removingWordThatWasNotSelected_changesNothing() {
        val viewModel = viewModel(1)
        val words = (viewModel.step.type as LessonStepType.WordBank).availableWords
        viewModel.onWordAdded(words[0])

        viewModel.onWordRemoved(words[1])

        assertEquals(UserInput.Bank(listOf(words[0])), viewModel.state.value.userInput)
    }

    @Test
    fun sameBankWordIsNotAddedTwice() {
        val viewModel = viewModel(1)
        val word = (viewModel.step.type as LessonStepType.WordBank).availableWords.first()

        viewModel.onWordAdded(word)
        viewModel.onWordAdded(word)

        assertEquals(UserInput.Bank(listOf(word)), viewModel.state.value.userInput)
    }

    @Test
    fun equalWordsWithDifferentIndices_canBothBeAdded() {
        val viewModel = viewModel(1)

        viewModel.onWordAdded(BankWord("he", 0))
        viewModel.onWordAdded(BankWord("he", 1))

        assertEquals(2, (viewModel.state.value.userInput as UserInput.Bank).words.size)
    }

    @Test
    fun removedWord_canBeAddedAgain() {
        val viewModel = viewModel(1)
        val word = (viewModel.step.type as LessonStepType.WordBank).availableWords.first()
        viewModel.onWordAdded(word)
        viewModel.onWordRemoved(word)

        viewModel.onWordAdded(word)

        assertEquals(UserInput.Bank(listOf(word)), viewModel.state.value.userInput)
    }

    @Test
    fun bankEvents_areIgnoredForTypedInput() {
        val viewModel = viewModel(4)

        viewModel.onWordAdded(BankWord("x", 0))
        viewModel.onWordRemoved(BankWord("x", 0))

        assertEquals(UserInput.Input(), viewModel.state.value.userInput)
    }

    @Test
    fun typedText_isStored_andIgnoredForWordBank() {
        val typed = viewModel(4)
        typed.onInputUpdated("hello")
        assertEquals(UserInput.Input("hello"), typed.state.value.userInput)

        val bank = viewModel(1)
        bank.onInputUpdated("hello")
        assertEquals(UserInput.Bank(), bank.state.value.userInput)
    }

    // endregion

    // region checking and stepping

    @Test
    fun correctAnswer_showsCorrectSheetAndAdvancesProgress() {
        val viewModel = viewModel(4)

        viewModel.answerCorrectly()

        val state = viewModel.state.value
        assertTrue(state.showCorrectCheck)
        assertFalse(state.showIncorrectCheck)
        assertEquals(1, state.completedStepsCount)
        assertEquals(1f / 3f, state.progress, 0.0001f)
        assertEquals(0, state.currentStepIndex)
    }

    @Test
    fun wrongAnswer_showsIncorrectSheetWithoutProgress() {
        val viewModel = viewModel(4)

        viewModel.answerWrongly()

        val state = viewModel.state.value
        assertTrue(state.showIncorrectCheck)
        assertFalse(state.showCorrectCheck)
        assertEquals(0, state.completedStepsCount)
    }

    @Test
    fun wordBankAnswer_isCheckedInSelectedOrder() {
        val viewModel = viewModel(1)

        viewModel.answerCorrectly()

        assertTrue(viewModel.state.value.showCorrectCheck)
    }

    @Test
    fun emptyAnswer_isCheckedAndRejected() {
        val viewModel = viewModel(1)

        viewModel.onCheckClicked()

        assertTrue(viewModel.state.value.showIncorrectCheck)
    }

    @Test
    fun checkWithoutLesson_doesNothing() {
        val viewModel = viewModel(99)

        viewModel.onCheckClicked()

        assertFalse(viewModel.state.value.showCorrectCheck)
        assertFalse(viewModel.state.value.showIncorrectCheck)
    }

    @Test
    fun closingCorrectSheet_movesToNextStepWithEmptyInput() {
        val viewModel = viewModel(4)
        viewModel.answerCorrectly()

        viewModel.onCheckSheetClosed()

        val state = viewModel.state.value
        assertEquals(1, state.currentStepIndex)
        assertEquals(UserInput.Input(), state.userInput)
        assertFalse(state.showCorrectCheck)
        assertFalse(state.showIncorrectCheck)
    }

    @Test
    fun closingIncorrectSheet_movesFailedStepToTheEndAndKeepsIndex() {
        val viewModel = viewModel(4)
        val failed = viewModel.step
        val originalOrder = viewModel.state.value.preparedLesson!!.lessonSteps
        viewModel.answerWrongly()

        viewModel.onCheckSheetClosed()

        val state = viewModel.state.value
        assertEquals(0, state.currentStepIndex)
        assertEquals(originalOrder.drop(1) + failed, state.preparedLesson!!.lessonSteps)
        assertEquals(UserInput.Input(), state.userInput)
        assertFalse(state.showIncorrectCheck)
        assertEquals(0, state.completedStepsCount)
    }

    @Test
    fun closingSheetWhenNothingIsShown_changesNothing() {
        val viewModel = viewModel(4)
        val before = viewModel.state.value

        viewModel.onCheckSheetClosed()

        assertEquals(before, viewModel.state.value)
    }

    @Test
    fun wordBankStep_afterWrongAnswer_startsWithEmptyBank() {
        val viewModel = viewModel(1)
        viewModel.onWordAdded((viewModel.step.type as LessonStepType.WordBank).availableWords.first())
        viewModel.onCheckClicked()
        viewModel.onCheckSheetClosed()

        assertEquals(UserInput.Bank(), viewModel.state.value.userInput)
    }

    @Test
    fun checkingTwiceBeforeClosingSheet_countsStepOnce() {
        val viewModel = viewModel(4)
        viewModel.typeCorrectAnswer()

        viewModel.onCheckClicked()
        viewModel.onCheckClicked()

        assertEquals(1, viewModel.state.value.completedStepsCount)
    }

    @Test
    fun checkingTwiceAfterWrongAnswer_keepsSingleIncorrectSheet() {
        val viewModel = viewModel(4)
        viewModel.answerWrongly()

        viewModel.onInputUpdated(viewModel.step.answers.first())
        viewModel.onCheckClicked()

        val state = viewModel.state.value
        assertTrue(state.showIncorrectCheck)
        assertFalse(state.showCorrectCheck)
        assertEquals(0, state.completedStepsCount)
    }

    @Test
    fun checkingIsPossibleAgain_afterSheetIsClosed() {
        val viewModel = viewModel(4)
        viewModel.answerCorrectly()
        viewModel.onCheckSheetClosed()

        viewModel.answerCorrectly()

        assertEquals(2, viewModel.state.value.completedStepsCount)
    }

    // endregion

    // region completion

    @Test
    fun solvingAllSteps_completesLessonAndSavesProgress() {
        val environment = LessonsEnvironment()
        val viewModel = viewModel(4, environment)

        repeat(3) {
            viewModel.answerCorrectly()
            viewModel.onCheckSheetClosed()
        }

        val state = viewModel.state.value
        assertTrue(state.showCompleted)
        assertEquals(1f, state.progress, 0f)
        assertEquals(listOf(1 to 4), environment.storage.writes)
    }

    @Test
    fun wordBankLesson_whenCompleted_alsoSavesDictionaryWords() {
        val environment = LessonsEnvironment()
        val viewModel = viewModel(1, environment)

        repeat(3) {
            viewModel.answerCorrectly()
            viewModel.onCheckSheetClosed()
        }

        assertTrue(viewModel.state.value.showCompleted)
        assertEquals(listOf(1 to 1), environment.storage.writes)
        assertTrue(environment.dao.rows.any { it.mokshan == "шумбрат!" })
    }

    @Test
    fun mistakesDoNotChangeStepCount_butRequireRepeatingTheStep() {
        val viewModel = viewModel(4)

        viewModel.answerWrongly()
        viewModel.onCheckSheetClosed()
        repeat(3) {
            viewModel.answerCorrectly()
            viewModel.onCheckSheetClosed()
        }

        val state = viewModel.state.value
        assertTrue(state.showCompleted)
        assertEquals(3, state.preparedLesson!!.lessonSteps.size)
        assertEquals(3, state.completedStepsCount)
    }

    @Test
    fun lessonIsNotCompleted_untilLastStepIsConfirmed() {
        val environment = LessonsEnvironment()
        val viewModel = viewModel(4, environment)
        repeat(2) {
            viewModel.answerCorrectly()
            viewModel.onCheckSheetClosed()
        }

        viewModel.answerCorrectly()

        assertFalse(viewModel.state.value.showCompleted)
        assertTrue(environment.storage.writes.isEmpty())
    }

    @Test
    fun repeatedLesson_whenCompleted_doesNotChangeProgress() {
        val environment = LessonsEnvironment()
        val viewModel = viewModel(LessonUseCase.LESSON_NUMBER_REPEAT, environment)

        repeat(viewModel.state.value.preparedLesson!!.lessonSteps.size) {
            viewModel.answerCorrectly()
            viewModel.onCheckSheetClosed()
        }

        assertTrue(viewModel.state.value.showCompleted)
        assertTrue(environment.storage.writes.isEmpty())
    }

    @Test
    fun completionIsShown_evenWhenSavingProgressFails_knownBugB3() {
        // KNOWN BUG B3: the result of completeLesson is ignored by the view model
        val environment = LessonsEnvironment()
        environment.storage.writeError = IllegalStateException("disk is full")
        val viewModel = viewModel(4, environment)

        repeat(3) {
            viewModel.answerCorrectly()
            viewModel.onCheckSheetClosed()
        }

        assertTrue(viewModel.state.value.showCompleted)
        assertTrue(environment.storage.writes.isEmpty())
    }

    // endregion

    // region exit flow

    @Test
    fun closeFlow_showsAlertThenExitsOnConfirm() {
        val viewModel = viewModel(1)

        viewModel.onCloseClicked()
        assertTrue(viewModel.state.value.showExitAlert)

        viewModel.onCloseDismissed()
        assertFalse(viewModel.state.value.showExitAlert)
        assertFalse(viewModel.state.value.exit)

        viewModel.onCloseClicked()
        viewModel.onCloseConfirmed()
        assertTrue(viewModel.state.value.exit)
        assertFalse(viewModel.state.value.showExitAlert)
    }

    @Test
    fun exitFlag_isResetByCloseInvoked() {
        val viewModel = viewModel(1)
        viewModel.onCloseConfirmed()

        viewModel.onCloseInvoked()

        assertFalse(viewModel.state.value.exit)
    }

    @Test
    fun closingCompletedScreen_requestsExit() {
        val viewModel = viewModel(1)

        viewModel.onCompletedClosed()

        assertTrue(viewModel.state.value.exit)
    }

    // endregion

    // region translation hints

    @Test
    fun clickingKnownWord_showsHintAtPosition() {
        val viewModel = viewModel(1)

        viewModel.onWordInSentenceClicked("Шумбрат!", pos = 3)

        assertEquals(TranslationHint("Шумбрат!", 3, listOf("hello")), viewModel.state.value.translationHint)
    }

    @Test
    fun clickingUnknownWord_keepsNoHint() {
        val viewModel = viewModel(1)

        viewModel.onWordInSentenceClicked("неведомо", pos = 0)

        assertNull(viewModel.state.value.translationHint)
    }

    @Test
    fun clickingUnknownWord_dismissesVisibleHint() {
        val viewModel = viewModel(1)
        viewModel.onWordInSentenceClicked("Шумбрат!", pos = 0)
        assertNotNull(viewModel.state.value.translationHint)

        viewModel.onWordInSentenceClicked("неведомо", pos = 1)

        assertNull(viewModel.state.value.translationHint)
    }

    @Test
    fun hint_canBeDismissedExplicitly() {
        val viewModel = viewModel(1)
        viewModel.onWordInSentenceClicked("Шумбрат!", pos = 0)

        viewModel.onHintDismissed()

        assertNull(viewModel.state.value.translationHint)
    }

    @Test
    fun hintWithoutLesson_isIgnored() {
        val viewModel = viewModel(99)

        viewModel.onWordInSentenceClicked("Шумбрат!", pos = 0)

        assertNull(viewModel.state.value.translationHint)
    }

    // endregion
}
