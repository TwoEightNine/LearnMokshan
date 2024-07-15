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
) {

    val progress: Float
        get() = currentStepIndex.toFloat() / (preparedLesson?.lessonSteps?.size ?: 1)
}



