package global.msnthrp.mokshan.android.features.lessons.lesson

import androidx.lifecycle.viewModelScope
import global.msnthrp.mokshan.android.core.arch.BaseViewModel
import global.msnthrp.mokshan.domain.lessons.BankWord
import global.msnthrp.mokshan.domain.lessons.LessonStepType
import global.msnthrp.mokshan.domain.lessons.PreparedLesson
import global.msnthrp.mokshan.domain.lessons.UserInput
import global.msnthrp.mokshan.usecase.lesson.LessonUseCase
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class LessonViewModel : BaseViewModel<LessonViewState>() {

    private val lessonUseCase by inject<LessonUseCase>(LessonUseCase::class.java)

    override fun getInitialState(): LessonViewState = LessonViewState()

    init {
        viewModelScope.launch {
            val preparedLesson = lessonUseCase.prepareLesson(topicId = 1, lessonNumber = 1).getOrNull()
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

    }

    fun onCloseClicked() {
        updateState { copy(showExitAlert = true) }
    }

    fun onCloseDismissed() {
        updateState { copy(showExitAlert = false) }
    }

    fun onCloseConfirmed() {
        updateState { copy(exit = true) }
    }

    fun onCheckSheetClosed() {
        if (currentState.showIncorrectCheck) {
            updateLessonAfterIncorrectCheck()
        } else if (currentState.showCorrectCheck) {
            updateLessonAfterCorrectCheck()
        }
    }

    fun onCheckClicked() {
        val currentInput = currentState.userInput ?: return
        val currentLesson = currentState.preparedLesson ?: return
        val currentStep = currentLesson.lessonSteps.getOrNull(currentState.currentStepIndex) ?: return

        val isCorrect = lessonUseCase.isCorrect(currentInput, currentStep)
        if (isCorrect) {
            updateState { copy(showCorrectCheck = true) }
        } else {
            updateState { copy(showIncorrectCheck = true) }
        }
    }

    fun onCloseInvoked() {
        updateState { copy(exit = false) }
    }

    private fun updateLessonAfterCorrectCheck() {
        val nextLessonIndex = currentState.currentStepIndex.inc()
        val currentLesson = currentState.preparedLesson ?: return
        if (nextLessonIndex == currentLesson.lessonSteps.size) {
            updateState { copy(exit = true) }
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