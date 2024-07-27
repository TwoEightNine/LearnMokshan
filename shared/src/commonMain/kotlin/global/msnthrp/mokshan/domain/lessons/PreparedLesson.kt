package global.msnthrp.mokshan.domain.lessons

data class PreparedLesson(
    val topic: Topic,
    val lessonSteps: List<LessonStep>,
    val withHints: Boolean,
)

data class LessonStep(
    val type: LessonStepType,
    val direction: LessonStepDirection,
    val sentence: String,
    val answers: List<String>,
)

sealed interface LessonStepType {
    data class WordBank(
        val availableWords: List<BankWord>,
    ) : LessonStepType
    data object Input : LessonStepType
}

sealed interface UserInput {
    data class Input(val text: String = "") : UserInput
    data class Bank(val words: List<BankWord> = emptyList()): UserInput
}

data class BankWord(
    val word: String,
    val index: Int,
)

enum class LessonStepDirection {
    FROM_MOKSHAN,
    TO_MOKSHAN,
}