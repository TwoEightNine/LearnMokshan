package global.msnthrp.mokshan.android.features.lessons.lesson

import global.msnthrp.mokshan.domain.lessons.PreparedLesson
import global.msnthrp.mokshan.domain.lessons.TopicInfo
import global.msnthrp.mokshan.domain.lessons.UserInput

data class LessonViewState(
    val topicInfo: TopicInfo? = null,
    val lessonNumber: Int = -1,
    val preparedLesson: PreparedLesson? = null,
    val currentStepIndex: Int = 0,
    val userInput: UserInput? = null,
    val showExitAlert: Boolean = false,
    val showCorrectCheck: Boolean = false,
    val showIncorrectCheck: Boolean = false,
    val exit: Boolean = false,
    val showCompleted: Boolean = false,
    // repeats currentStepIndex but updated a bit earlier to complete the progress after
    // correct answer but before increasing the currentStepIndex
    val completedStepsCount: Int = 0,
) {

    val progress: Float
        get() = completedStepsCount.toFloat() / (preparedLesson?.lessonSteps?.size ?: 1)
}



