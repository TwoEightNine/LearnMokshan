package global.msnthrp.mokshan.usecase.lesson

import global.msnthrp.mokshan.domain.lessons.BankWord
import global.msnthrp.mokshan.domain.lessons.LessonPair
import global.msnthrp.mokshan.domain.lessons.LessonStep
import global.msnthrp.mokshan.domain.lessons.LessonStepDirection
import global.msnthrp.mokshan.domain.lessons.LessonStepType
import global.msnthrp.mokshan.domain.lessons.PreparedLesson
import global.msnthrp.mokshan.domain.lessons.UserInput
import global.msnthrp.mokshan.utils.failureOf

class LessonUseCase(
    private val repository: LessonRepository,
) {

    suspend fun prepareLesson(topicId: Int, lessonNumber: Int): Result<PreparedLesson> {
        val topicResult = repository.getTopic(topicId)
        val topic = topicResult.getOrNull() ?: return Result.failureOf(topicResult)
        println("LessonUC: topic = $topic")

        val lessonsCount = topic.lessons.size
        val lessonByNumber = getLessonByNumber(lessonsCount, lessonNumber)
        val exactLesson = (lessonNumber % (lessonsCount + 1)).takeIf { lessonNumber <= 3 * (lessonsCount + 1) }
        println("LessonUC: lessonsCount = $lessonsCount, lessonByNumber = $lessonByNumber, exactLesson = $exactLesson")

        val lessonPairs = when (exactLesson) {
            null -> topic.lessons.flatMap { it.toNative }
            else -> topic.lessons.find { it.order == exactLesson }?.toNative ?: emptyList()
        }
        println("LessonUC: lessonPairs = $lessonPairs")
        if (lessonPairs.isEmpty()) {
            return Result.failure(IllegalStateException("Lesson pairs not found"))
        }

        val allAvailableWordsMokshan = lessonPairs.flatMap { it.getMokshanWords() }.toSet()
        val allAvailableWordsNative = lessonPairs.flatMap { it.getNativeWords() }.toSet()

        val pairsCount = lessonPairs.size
        val lessonSteps = lessonPairs.shuffled()
            .mapIndexed { index, pair ->
                val direction = getDirectionByNumber(index, lessonByNumber)
                val correctWords = when (direction) {
                    LessonStepDirection.FROM_MOKSHAN -> pair.getNativeWords()
                    LessonStepDirection.TO_MOKSHAN -> pair.getMokshanWords()
                }
                val extendFrom = when(direction) {
                    LessonStepDirection.FROM_MOKSHAN -> allAvailableWordsNative
                    LessonStepDirection.TO_MOKSHAN -> allAvailableWordsMokshan
                }
                val suggestedWords = getListExtendedFrom(correctWords, extendFrom, desiredSize = 10)
                    .mapIndexed { i, word -> BankWord(word, i) }

                LessonStep(
                    type = when (lessonByNumber) {
                        LessonByNumber.BANK_ONLY, LessonByNumber.BANK_SUMMARY -> LessonStepType.WordBank(suggestedWords)
                        LessonByNumber.INPUT_ONLY, LessonByNumber.INPUT_SUMMARY, LessonByNumber.FULL_REVIEW -> LessonStepType.Input
                        else -> if (index.toFloat() / pairsCount <= 0.5f) LessonStepType.WordBank(suggestedWords) else LessonStepType.Input
                    },
                    direction = direction,
                    sentence = pair.mokshan,
                    answers = pair.native,
                )
            }
        return Result.success(PreparedLesson(topic, lessonSteps))
    }

    fun isCorrect(userInput: UserInput, lesson: LessonStep): Boolean {
        val cleanInput = when (userInput) {
            is UserInput.Bank -> userInput.words.joinToString(separator = " ") { it.word }
            is UserInput.Input -> userInput.text
        }.cleanUp()
        val cleanAnswers = lesson.answers.map { it.cleanUp() }
        return cleanInput in cleanAnswers
    }

    private fun getListExtendedFrom(source: List<String>, extendFrom: Collection<String>, desiredSize: Int): List<String> {
        if (source.size >= desiredSize) return source

        val result = source.toMutableList()
        val newWords = extendFrom.minus(result.toSet()).shuffled().take(desiredSize - source.size)
        return result.plus(newWords).shuffled()
    }

    private fun LessonPair.getMokshanWords(): List<String> {
        return mokshan.splitToWords()
    }

    private fun LessonPair.getNativeWords(): List<String> {
        return native.flatMap { it.splitToWords() }
    }

    private fun getDirectionByNumber(stepIndex: Int, lessonByNumber: LessonByNumber): LessonStepDirection {
        return when (lessonByNumber) {
            LessonByNumber.BANK_ONLY, LessonByNumber.BANK_SUMMARY -> LessonStepDirection.FROM_MOKSHAN
            else -> if (stepIndex % 2 == 0) LessonStepDirection.FROM_MOKSHAN else LessonStepDirection.TO_MOKSHAN
        }
    }

    private fun getLessonByNumber(lessonsCount: Int, lessonNumber: Int): LessonByNumber {
        return when {
            lessonNumber <= lessonsCount -> LessonByNumber.BANK_ONLY
            lessonNumber == lessonsCount + 1 -> LessonByNumber.BANK_SUMMARY
            lessonNumber <= 2 * lessonsCount + 1 -> LessonByNumber.HALF_BANK_HALF_INPUT
            lessonNumber == 2 * lessonsCount + 2 -> LessonByNumber.HALF_SUMMARY
            lessonNumber <= 3 * lessonsCount + 2 -> LessonByNumber.INPUT_ONLY
            lessonNumber == 3 * lessonsCount + 3 -> LessonByNumber.INPUT_SUMMARY
            else -> LessonByNumber.FULL_REVIEW
        }
    }

    private fun String.splitToWords(): List<String> {
        return cleanUp()
            .split(' ')
    }

    private fun String.cleanUp(): String {
        return replace("?", "")
            .replace("!", "")
            .replace(".", "")
            .replace(",", "")
            .lowercase()
    }

    internal enum class LessonByNumber {
        BANK_ONLY,
        BANK_SUMMARY,
        HALF_BANK_HALF_INPUT,
        HALF_SUMMARY,
        INPUT_ONLY,
        INPUT_SUMMARY,
        FULL_REVIEW,
    }
}