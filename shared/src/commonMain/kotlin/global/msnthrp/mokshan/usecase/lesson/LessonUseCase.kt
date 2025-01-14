package global.msnthrp.mokshan.usecase.lesson

import global.msnthrp.mokshan.data.repository.dictionary.DictionaryRepository
import global.msnthrp.mokshan.data.repository.lessons.TopicsRepository
import global.msnthrp.mokshan.domain.dictionary.DictionaryEntry
import global.msnthrp.mokshan.domain.lessons.BankWord
import global.msnthrp.mokshan.domain.lessons.LessonPair
import global.msnthrp.mokshan.domain.lessons.LessonStep
import global.msnthrp.mokshan.domain.lessons.LessonStepDirection
import global.msnthrp.mokshan.domain.lessons.LessonStepType
import global.msnthrp.mokshan.domain.lessons.PreparedLesson
import global.msnthrp.mokshan.domain.lessons.Topic
import global.msnthrp.mokshan.domain.lessons.UserInput
import global.msnthrp.mokshan.utils.failureOf
import kotlin.reflect.KClass

class LessonUseCase(
    private val lessonRepository: LessonRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val topicsRepository: TopicsRepository,
) {

    suspend fun prepareLesson(topicId: Int, lessonNumber: Int): Result<PreparedLesson> {
        println("prepareLesson(topicId=$topicId, lessonNumber=$lessonNumber)")
        val topicResult = lessonRepository.getTopic(topicId)
        val topic = topicResult.getOrNull() ?: return Result.failureOf(topicResult)
        if (lessonNumber !in 0..topic.topicLength && lessonNumber != LESSON_NUMBER_REPEAT) {
            return Result.failure(
                IllegalArgumentException(
                    "Lesson number $lessonNumber received while topic ${topic.id} offers " +
                            "only ${topic.topicLength} lessons"
                )
            )
        }
        println("LessonUC: topic = $topic")

        val lessonsCount = topic.lessons.size
        val lessonByNumber = getLessonByNumber(lessonsCount, lessonNumber)
        val exactLesson = getExactLesson(topic, lessonNumber)
        println("LessonUC: lessonsCount = $lessonsCount, lessonByNumber = $lessonByNumber, exactLesson = $exactLesson")

        val lessonPairsFromMokshan = when (exactLesson) {
            null -> topic.lessons.flatMap { it.toNative }
            else -> topic.lessons.find { it.order == exactLesson }?.toNative ?: emptyList()
        }
        println("LessonUC: lessonPairs(${lessonPairsFromMokshan.size}) = $lessonPairsFromMokshan")
        if (lessonPairsFromMokshan.isEmpty()) {
            return Result.failure(IllegalStateException("Lesson pairs not found"))
        }

        val lessonPairsFromNativeMap = mutableMapOf<String, MutableList<String>>()
        for (pair in lessonPairsFromMokshan) {
            pair.native.forEach { native ->
                if (native !in lessonPairsFromNativeMap) {
                    lessonPairsFromNativeMap[native] = mutableListOf()
                }
                lessonPairsFromNativeMap[native]?.add(pair.mokshan)
            }
        }

        val allAvailableWordsMokshan =
            lessonPairsFromMokshan.flatMap { it.getMokshanWords() }.toSet()
        val allAvailableWordsNative = lessonPairsFromMokshan.flatMap { it.getNativeWords() }.toSet()

        val pairsCount = lessonPairsFromMokshan.size
        val lessonSteps = lessonPairsFromMokshan.shuffled()
            .mapIndexed { index, pair ->
                val stepRatio = index.inc().toFloat() / pairsCount
                val direction = getDirectionByNumber(stepRatio, lessonByNumber)
                val correctWords = when (direction) {
                    LessonStepDirection.FROM_MOKSHAN -> pair.getNativeWords()
                    LessonStepDirection.TO_MOKSHAN -> pair.getMokshanWords()
                }
                val extendFrom = when (direction) {
                    LessonStepDirection.FROM_MOKSHAN -> allAvailableWordsNative
                    LessonStepDirection.TO_MOKSHAN -> allAvailableWordsMokshan
                }
                val sentence = when (direction) {
                    LessonStepDirection.FROM_MOKSHAN -> pair.mokshan
                    LessonStepDirection.TO_MOKSHAN -> pair.native.first()
                }
                val answers = when (direction) {
                    LessonStepDirection.FROM_MOKSHAN -> pair.native
                    LessonStepDirection.TO_MOKSHAN -> lessonPairsFromNativeMap[sentence] ?: listOf(
                        pair.mokshan
                    )
                }
                val suggestedWords = getListExtendedFrom(correctWords, extendFrom, desiredSize = 10)
                    .mapIndexed { i, word -> BankWord(word, i) }

                val type = when (getTypeByLessonNumber(lessonByNumber)) {
                    LessonStepType.WordBank::class -> LessonStepType.WordBank(suggestedWords)
                    LessonStepType.Input::class -> LessonStepType.Input
                    else -> throw NotImplementedError("what else do you want?")
                }
                LessonStep(
                    type = type,
                    direction = direction,
                    sentence = sentence,
                    answers = answers,
                )
            }
        val withHints = lessonByNumber != LessonByNumber.FULL_REVIEW
        return Result.success(PreparedLesson(topic, lessonSteps, withHints))
    }

    fun isCorrect(userInput: UserInput, lesson: LessonStep): Boolean {
        val cleanInput = when (userInput) {
            is UserInput.Bank -> userInput.words.joinToString(separator = " ") { it.word }
            is UserInput.Input -> userInput.text
        }.cleanUp().trim()
        val cleanAnswers = lesson.answers.map { it.cleanUp() }
        return cleanInput in cleanAnswers
    }

    suspend fun completeLesson(topic: Topic, lessonNumber: Int): Result<Unit> {
        if (lessonNumber == LESSON_NUMBER_REPEAT) {
            return Result.success(Unit)
        }
        val isAlreadyCompleted = topicsRepository.isLessonCompleted(topic).getOrDefault(false)
        if (isAlreadyCompleted) {
            return Result.success(Unit)
        }

        return kotlin.runCatching {
            lessonRepository.markLessonAsCompleted(topic, lessonNumber)

            getExactLesson(topic, lessonNumber)
                ?.dec()
                ?.takeIf { it < topic.lessons.size }
                ?.let { topic.lessons.getOrNull(it) }
                ?.also { lesson ->
                    val newWords = lesson.dictionary
                    val lessonPairs = topic.lessons.flatMap { it.toNative }
                    val entries = newWords.mapNotNull { mokshan ->
                        lessonPairs
                            .find { it.mokshan == mokshan }
                            ?.native
                            ?.let { native ->
                                DictionaryEntry(
                                    mokshan = mokshan.lowercase(),
                                    native = native.map { it.lowercase() },
                                )
                            }
                    }
                    dictionaryRepository.addNewWords(entries)
                }
        }
    }

    fun getTranslationsForWord(
        word: String,
        topic: Topic,
        direction: LessonStepDirection
    ): List<String> {
        val cleanWord = word.cleanUp()
        val translations = topic.translations
        val wordTranslations = when (direction) {
            LessonStepDirection.FROM_MOKSHAN -> translations.filter { it.mokshan == cleanWord }
                .map { it.native }

            LessonStepDirection.TO_MOKSHAN -> translations.filter { it.native == cleanWord }
                .map { it.mokshan }
        }

        val cleanSentences = topic.lessons.flatMap { it.toNative }.map {
            LessonPair(mokshan = it.mokshan.cleanUp(), native = it.native.map { it.cleanUp() })
        }
        val wordTranslationsFromSentences = when (direction) {
            LessonStepDirection.FROM_MOKSHAN -> cleanSentences.filter { it.mokshan == cleanWord }
                .flatMap { it.native }

            LessonStepDirection.TO_MOKSHAN -> cleanSentences.filter { cleanWord in it.native }
                .map { it.mokshan }
        }
        val cleanTranslationsFromSentences = wordTranslationsFromSentences.map { it.cleanUp() }
        return (wordTranslations + cleanTranslationsFromSentences).distinct()
    }

    private fun getListExtendedFrom(
        source: List<String>,
        extendFrom: Collection<String>,
        desiredSize: Int
    ): List<String> {
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

    private fun getDirectionByNumber(
        stepRatio: Float,
        lessonByNumber: LessonByNumber
    ): LessonStepDirection {
        return when (lessonByNumber) {
            LessonByNumber.BANK_ONLY,
            LessonByNumber.BANK_SUMMARY -> LessonStepDirection.FROM_MOKSHAN
            else -> if (stepRatio in 0f..0.25f || stepRatio in 0.501f..0.75f) {
                LessonStepDirection.FROM_MOKSHAN
            } else {
                LessonStepDirection.TO_MOKSHAN
            }
        }
    }

    private fun getTypeByLessonNumber(
        lessonByNumber: LessonByNumber
    ): KClass<out LessonStepType> {
        return when (lessonByNumber) {
            LessonByNumber.BANK_ONLY,
            LessonByNumber.BANK_SUMMARY -> LessonStepType.WordBank::class
            LessonByNumber.INPUT_ONLY,
            LessonByNumber.INPUT_SUMMARY,
            LessonByNumber.FULL_REVIEW,
            LessonByNumber.REPEAT -> LessonStepType.Input::class
        }
    }

    private fun getLessonByNumber(lessonsCount: Int, lessonNumber: Int): LessonByNumber {
        val bankSummary = lessonsCount + 1
        val inputSummary = 2 * lessonsCount + 2
        val fullReview = TopicsUtils.getTopicLength(lessonsCount)
        return when {
            lessonNumber == LESSON_NUMBER_REPEAT -> LessonByNumber.REPEAT
            lessonNumber < bankSummary -> LessonByNumber.BANK_ONLY
            lessonNumber == bankSummary -> LessonByNumber.BANK_SUMMARY
            lessonNumber < inputSummary -> LessonByNumber.INPUT_ONLY
            lessonNumber == inputSummary -> LessonByNumber.INPUT_SUMMARY
            lessonNumber == fullReview -> LessonByNumber.FULL_REVIEW
            else -> LessonByNumber.REPEAT
        }
    }

    private fun getExactLesson(topic: Topic, lessonNumber: Int): Int? {
        val lessonsCount = topic.lessons.size
        return (lessonNumber % (lessonsCount + 1))
            .takeIf { it in 1..lessonsCount }
            ?.takeIf { topic.topicLength != lessonNumber }
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
            .replace("–", "")
            .replace("  ", " ")
            .lowercase()
    }

    internal enum class LessonByNumber {
        BANK_ONLY,
        BANK_SUMMARY,
        INPUT_ONLY,
        INPUT_SUMMARY,
        FULL_REVIEW,
        REPEAT,
    }

    companion object {
        const val LESSON_NUMBER_REPEAT = -3
    }
}