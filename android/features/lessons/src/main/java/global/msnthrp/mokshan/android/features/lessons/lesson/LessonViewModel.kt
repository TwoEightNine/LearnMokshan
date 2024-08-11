package global.msnthrp.mokshan.android.features.lessons.lesson

import androidx.lifecycle.viewModelScope
import global.msnthrp.mokshan.android.core.arch.BaseViewModel
import global.msnthrp.mokshan.domain.lessons.BankWord
import global.msnthrp.mokshan.domain.lessons.LessonStepType
import global.msnthrp.mokshan.domain.lessons.PreparedLesson
import global.msnthrp.mokshan.domain.lessons.TopicInfo
import global.msnthrp.mokshan.domain.lessons.UserInput
import global.msnthrp.mokshan.usecase.lesson.LessonUseCase
import kotlinx.coroutines.launch

class LessonViewModel(
    topicInfo: TopicInfo,
    lessonNumber: Int,
    private val lessonUc: LessonUseCase
) : BaseViewModel<LessonViewState>() {

    override fun getInitialState(): LessonViewState = LessonViewState()

    init {
        updateState { copy(topicInfo = topicInfo, lessonNumber = lessonNumber) }
        viewModelScope.launch {
            val preparedLesson = lessonUc.prepareLesson(topicId = topicInfo.id, lessonNumber = lessonNumber).getOrNull()
            println(preparedLesson)
            preparedLesson ?: return@launch

            val initialUserInput = preparedLesson.getEmptyUserInputForIndex(index = 0)
            updateState {
                copy(
                    preparedLesson = preparedLesson,
                    userInput = initialUserInput,
                )
            }
        }
    }

    fun onWordAdded(word: BankWord) {
        val currentInput = currentState.userInput as? UserInput.Bank ?: return
        val updatedInput = currentInput.copy(words = currentInput.words.plus(word))
        updateState { copy(userInput = updatedInput) }
    }

    fun onWordRemoved(word: BankWord) {
        val currentInput = currentState.userInput as? UserInput.Bank ?: return
        val updatedInput = currentInput.copy(words = currentInput.words.minus(word))
        updateState { copy(userInput = updatedInput) }
    }

    fun onInputUpdated(text: String) {
        val currentInput = currentState.userInput as? UserInput.Input ?: return
        val updatedInput = currentInput.copy(text = text)
        updateState { copy(userInput = updatedInput) }
    }

    fun onCloseClicked() {
        updateState { copy(showExitAlert = true) }
    }

    fun onCloseDismissed() {
        updateState { copy(showExitAlert = false) }
    }

    fun onCloseConfirmed() {
        updateState { copy(exit = true, showExitAlert = false) }
    }

    fun onCheckSheetClosed() {
        if (currentState.showIncorrectCheck) {
            updateLessonAfterIncorrectCheck()
        } else if (currentState.showCorrectCheck) {
            updateLessonAfterCorrectCheck()
        }
    }

    fun onCompletedClosed() {
        updateState { copy(exit = true) }
    }

    fun onCheckClicked() {
        val currentInput = currentState.userInput ?: return
        val currentLesson = currentState.preparedLesson ?: return
        val currentStep = currentLesson.lessonSteps.getOrNull(currentState.currentStepIndex) ?: return

        val isCorrect = lessonUc.isCorrect(currentInput, currentStep)
        if (isCorrect) {
            updateState { copy(showCorrectCheck = true, completedStepsCount = currentState.completedStepsCount.inc()) }
        } else {
            updateState { copy(showIncorrectCheck = true) }
        }
    }

    fun onCloseInvoked() {
        updateState { copy(exit = false) }
    }

    fun onWordInSentenceClicked(word: String, pos: Int) {
        val lesson = currentState.preparedLesson ?: return
        val step = lesson.lessonSteps.getOrNull(currentState.currentStepIndex) ?: return
        val direction = step.direction
        val topic = lesson.topic

        val wordTranslations = lessonUc.getTranslationsForWord(word, topic, direction)
        if (wordTranslations.isNotEmpty()) {
            val translationHint = TranslationHint(word, pos, wordTranslations)
            updateState { copy(translationHint = translationHint) }
        } else if (currentState.translationHint != null) {
            onHintDismissed()
        }
    }

    fun onHintDismissed() {
        updateState { copy(translationHint = null) }
    }

    private fun updateLessonAfterCorrectCheck() {
        val topic = currentState.preparedLesson?.topic ?: return
        val nextLessonIndex = currentState.currentStepIndex.inc()
        val currentLesson = currentState.preparedLesson ?: return
        if (nextLessonIndex == currentLesson.lessonSteps.size) {
            viewModelScope.launch {
                lessonUc.completeLesson(
                    topic = topic,
                    lessonNumber = currentState.lessonNumber,
                )
            }
            updateState { copy(showCompleted = true) }
            return
        }

        updateState {
            copy(
                currentStepIndex = nextLessonIndex,
                userInput = currentLesson.getEmptyUserInputForIndex(nextLessonIndex),
                showCorrectCheck = false,
                showIncorrectCheck = false,
            )
        }
    }

    private fun updateLessonAfterIncorrectCheck() {
        val currentLesson = currentState.preparedLesson ?: return
        val currentStep = currentLesson.lessonSteps.getOrNull(currentState.currentStepIndex) ?: return
        val updatedSteps = currentLesson.lessonSteps.minus(currentStep).plus(currentStep)
        val updatedLesson = currentLesson.copy(lessonSteps = updatedSteps)
        updateState {
            copy(
                preparedLesson = updatedLesson,
                userInput = currentLesson.getEmptyUserInputForIndex(currentState.currentStepIndex),
                showCorrectCheck = false,
                showIncorrectCheck = false,
            )
        }
    }

    private fun PreparedLesson.getEmptyUserInputForIndex(index: Int): UserInput? {
        val type = lessonSteps.getOrNull(index)?.type ?: return null
        return when (type) {
            LessonStepType.Input -> UserInput.Input()
            is LessonStepType.WordBank -> UserInput.Bank()
        }
    }
}