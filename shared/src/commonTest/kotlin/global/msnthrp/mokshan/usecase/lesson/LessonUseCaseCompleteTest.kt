package global.msnthrp.mokshan.usecase.lesson

import global.msnthrp.mokshan.domain.dictionary.DictionaryEntry
import global.msnthrp.mokshan.domain.lessons.TopicsProgress
import global.msnthrp.mokshan.testutil.assertSuccess
import global.msnthrp.mokshan.testutil.lesson
import global.msnthrp.mokshan.testutil.pair
import global.msnthrp.mokshan.testutil.sampleTopic
import global.msnthrp.mokshan.testutil.topic
import global.msnthrp.mokshan.testutil.translation
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** `sampleTopic()` has 2 lessons: 1-2 bank, 3 bank summary, 4-5 input, 6 input summary, 7 review. */
class LessonUseCaseCompleteTest {

    private suspend fun LessonHarness.words(): List<DictionaryEntry> =
        dictionaryRepository.getAllWords().assertSuccess()

    // region progress

    @Test
    fun completingRegularLesson_savesItAsCompletedInTheTopic() = runTest {
        val harness = LessonHarness(sampleTopic(id = 3))

        harness.useCase.completeLesson(harness.topic, 1).assertSuccess()

        assertEquals(listOf(3 to 1), harness.storage.writes)
    }

    @Test
    fun completingFinalLesson_movesProgressToNextTopicStart() = runTest {
        val harness = LessonHarness(sampleTopic(id = 3))
        harness.storage.progress = TopicsProgress(listOf(1, 2), 3, 6)

        harness.useCase.completeLesson(harness.topic, 7).assertSuccess()

        assertEquals(listOf(4 to 0), harness.storage.writes)
    }

    @Test
    fun completingSummaryLessons_savesProgressWithoutDictionaryWords() = runTest {
        val harness = LessonHarness(sampleTopic())

        listOf(3, 6, 7).forEach { number ->
            harness.useCase.completeLesson(harness.topic, number).assertSuccess()
        }

        assertEquals(listOf(1 to 3, 1 to 6, 2 to 0), harness.storage.writes)
        assertEquals(emptyList(), harness.words())
    }

    @Test
    fun repeat_doesNotTouchProgressNorDictionary() = runTest {
        val harness = LessonHarness(sampleTopic())

        harness.useCase.completeLesson(harness.topic, LessonUseCase.LESSON_NUMBER_REPEAT).assertSuccess()

        assertEquals(emptyList(), harness.storage.writes)
        assertEquals(emptyList(), harness.dao.rows)
    }

    @Test
    fun repeat_doesNotEvenReadProgress() = runTest {
        val harness = LessonHarness(sampleTopic())
        harness.storage.readError = IllegalStateException("must not be read")

        harness.useCase.completeLesson(harness.topic, LessonUseCase.LESSON_NUMBER_REPEAT).assertSuccess()
    }

    @Test
    fun alreadyCompletedTopic_isNotTouched() = runTest {
        val harness = LessonHarness(sampleTopic(id = 1))
        harness.storage.progress = TopicsProgress(listOf(1), 2, 0)

        harness.useCase.completeLesson(harness.topic, 1).assertSuccess()

        assertEquals(emptyList(), harness.storage.writes)
        assertEquals(emptyList(), harness.dao.rows)
    }

    @Test
    fun ongoingTopic_isNotConsideredCompleted() = runTest {
        val harness = LessonHarness(sampleTopic(id = 2))
        harness.storage.progress = TopicsProgress(listOf(1), 2, 1)

        harness.useCase.completeLesson(harness.topic, 2).assertSuccess()

        assertEquals(listOf(2 to 2), harness.storage.writes)
    }

    @Test
    fun unreadableProgress_failsCompletionWithoutWritingProgress() = runTest {
        val harness = LessonHarness(sampleTopic())
        val boom = IllegalStateException("broken")
        harness.storage.readError = boom

        val result = harness.useCase.completeLesson(harness.topic, 1)

        assertEquals(boom, result.exceptionOrNull())
        assertEquals(emptyList(), harness.storage.writes)
    }

    @Test
    fun completingEarlierLesson_doesNotMoveProgressBack() = runTest {
        val harness = LessonHarness(sampleTopic())
        harness.storage.progress = TopicsProgress(emptyList(), 1, 5)

        harness.useCase.completeLesson(harness.topic, 2).assertSuccess()

        assertEquals(5, harness.storage.progress.ongoingTopicLessonNumber)
        assertEquals(emptyList(), harness.storage.writes)
    }

    // endregion

    // region dictionary

    @Test
    fun bankLesson_addsLessonDictionaryWordsLowercasedWithNativeTranslations() = runTest {
        val harness = LessonHarness(sampleTopic())

        harness.useCase.completeLesson(harness.topic, 1).assertSuccess()

        assertEquals(
            listOf(
                DictionaryEntry("мон эрян", listOf("i live"), 5_000L),
                DictionaryEntry("шумбрат!", listOf("hello!"), 5_000L),
            ),
            harness.words(),
        )
    }

    @Test
    fun secondLesson_addsItsOwnDictionaryWords() = runTest {
        val harness = LessonHarness(sampleTopic())

        harness.useCase.completeLesson(harness.topic, 2).assertSuccess()

        assertEquals(listOf("лац"), harness.words().map { it.mokshan })
    }

    @Test
    fun inputLesson_addsWordsOfTheCorrespondingLesson() = runTest {
        val harness = LessonHarness(sampleTopic())

        harness.useCase.completeLesson(harness.topic, 5).assertSuccess() // input lesson 2

        assertEquals(listOf("лац"), harness.words().map { it.mokshan })
        assertEquals(listOf(1 to 5), harness.storage.writes)
    }

    @Test
    fun bankAndInputPhaseOfSameLesson_doNotDuplicateWordsInDictionaryView() = runTest {
        val harness = LessonHarness(sampleTopic())

        harness.useCase.completeLesson(harness.topic, 1).assertSuccess()
        harness.useCase.completeLesson(harness.topic, 4).assertSuccess()

        assertEquals(listOf("мон эрян", "шумбрат!"), harness.words().map { it.mokshan })
    }

    @Test
    fun nativeTranslation_isTakenFromFirstMatchingPair() = runTest {
        val topic = topic(
            lessons = listOf(
                lesson(
                    1,
                    listOf(pair("Лац", "Fine"), pair("Лац", "OK")),
                    dictionary = listOf("Лац"),
                ),
            ),
        )
        val harness = LessonHarness(topic)

        harness.useCase.completeLesson(topic, 1).assertSuccess()

        assertEquals(listOf("fine"), harness.words().single().native)
    }

    @Test
    fun dictionaryWordFromAnotherLessonPair_isResolvedAcrossTheWholeTopic() = runTest {
        val topic = topic(
            lessons = listOf(
                lesson(1, listOf(pair("Лац", "Fine"))),
                lesson(2, listOf(pair("Пара", "Good")), dictionary = listOf("Лац")),
            ),
        )
        val harness = LessonHarness(topic)

        harness.useCase.completeLesson(topic, 2).assertSuccess()

        assertEquals(listOf("лац"), harness.words().map { it.mokshan })
    }

    @Test
    fun singleWords_areTranslatedByTopicTranslations() = runTest {
        val topic = topic(
            lessons = listOf(
                lesson(
                    1,
                    listOf(pair("Кода тефне?", "How are you?")),
                    dictionary = listOf("кода", "тефне"),
                ),
            ),
            translations = listOf(translation("кода", "how"), translation("тефне", "your deal")),
        )
        val harness = LessonHarness(topic)

        harness.useCase.completeLesson(topic, 1).assertSuccess()

        assertEquals(
            listOf(
                DictionaryEntry("кода", listOf("how"), 5_000L),
                DictionaryEntry("тефне", listOf("your deal"), 5_000L),
            ),
            harness.words(),
        )
    }

    @Test
    fun entriesAreExactlyTheLessonDictionary_notEveryTranslation() = runTest {
        val topic = topic(
            lessons = listOf(lesson(1, listOf(pair("Лац", "Fine")), dictionary = listOf("кода"))),
            translations = listOf(translation("кода", "how"), translation("лац", "fine")),
        )
        val harness = LessonHarness(topic)

        harness.useCase.completeLesson(topic, 1).assertSuccess()

        assertEquals(listOf("кода"), harness.words().map { it.mokshan })
    }

    @Test
    fun topicTranslations_takePriorityOverLessonPairs() = runTest {
        val topic = topic(
            lessons = listOf(lesson(1, listOf(pair("Лац", "Fine")), dictionary = listOf("Лац"))),
            translations = listOf(translation("лац", "okay")),
        )
        val harness = LessonHarness(topic)

        harness.useCase.completeLesson(topic, 1).assertSuccess()

        assertEquals(listOf("okay"), harness.words().single().native)
    }

    @Test
    fun severalTranslationsOfOneWord_areAllKept() = runTest {
        val topic = topic(
            lessons = listOf(lesson(1, listOf(pair("а", "a")), dictionary = listOf("Кода"))),
            translations = listOf(translation("кода", "how"), translation("кода", "in what way")),
        )
        val harness = LessonHarness(topic)

        harness.useCase.completeLesson(topic, 1).assertSuccess()

        assertEquals(listOf("how", "in what way"), harness.words().single().native)
    }

    @Test
    fun phraseWithoutTranslationEntry_fallsBackToLessonPair() = runTest {
        val topic = topic(
            lessons = listOf(
                lesson(1, listOf(pair("Кода тефне?", "How are you?", "How is it going?")), dictionary = listOf("Кода тефне?")),
            ),
            translations = listOf(translation("кода", "how")),
        )
        val harness = LessonHarness(topic)

        harness.useCase.completeLesson(topic, 1).assertSuccess()

        assertEquals(
            listOf(DictionaryEntry("кода тефне?", listOf("how are you?", "how is it going?"), 5_000L)),
            harness.words(),
        )
    }

    @Test
    fun wordsAreMatchedIgnoringCaseAndPunctuation() = runTest {
        val topic = topic(
            lessons = listOf(
                lesson(1, listOf(pair("Лац!", "Fine")), dictionary = listOf("лац", "ПАРА.")),
            ),
            translations = listOf(translation("Пара", "good")),
        )
        val harness = LessonHarness(topic)

        harness.useCase.completeLesson(topic, 1).assertSuccess()

        assertEquals(
            mapOf("лац" to listOf("fine"), "пара." to listOf("good")),
            harness.words().associate { it.mokshan to it.native },
        )
    }

    @Test
    fun wordWithoutAnyTranslation_isSkipped() = runTest {
        val topic = topic(
            lessons = listOf(lesson(1, listOf(pair("Лац", "Fine")), dictionary = listOf("Лац", "неведомо"))),
        )
        val harness = LessonHarness(topic)

        harness.useCase.completeLesson(topic, 1).assertSuccess()

        assertEquals(listOf("лац"), harness.words().map { it.mokshan })
    }

    @Test
    fun translationsAreLowercasedAndDeduplicated() = runTest {
        val topic = topic(
            lessons = listOf(lesson(1, listOf(pair("а", "a")), dictionary = listOf("Кода"))),
            translations = listOf(translation("кода", "How"), translation("кода", "how")),
        )
        val harness = LessonHarness(topic)

        harness.useCase.completeLesson(topic, 1).assertSuccess()

        assertEquals(listOf("how"), harness.words().single().native)
    }

    @Test
    fun lessonWithoutDictionary_savesNothing() = runTest {
        val topic = topic(lessons = listOf(lesson(1, listOf(pair("Лац", "Fine")))))
        val harness = LessonHarness(topic)

        harness.useCase.completeLesson(topic, 1).assertSuccess()

        assertEquals(emptyList(), harness.dao.rows)
        assertEquals(listOf(1 to 1), harness.storage.writes)
    }

    // endregion

    // region failures are reported

    @Test
    fun failureToSaveProgress_isReported() = runTest {
        val harness = LessonHarness(sampleTopic())
        val boom = IllegalStateException("disk is full")
        harness.storage.writeError = boom

        val result = harness.useCase.completeLesson(harness.topic, 1)

        assertEquals(boom, result.exceptionOrNull())
        assertEquals(emptyList(), harness.storage.writes)
    }

    @Test
    fun failureToSaveDictionary_isReported_andProgressIsNotAdvanced() = runTest {
        val harness = LessonHarness(sampleTopic())
        val boom = IllegalStateException("db is locked")
        harness.dao.error = boom

        val result = harness.useCase.completeLesson(harness.topic, 1)

        assertEquals(boom, result.exceptionOrNull())
        assertEquals(emptyList(), harness.storage.writes)
    }

    @Test
    fun wordsAreSavedBeforeProgress_soFailedProgressCanBeRetriedWithoutLosingWords() = runTest {
        val harness = LessonHarness(sampleTopic())
        harness.storage.writeError = IllegalStateException("disk is full")
        assertTrue(harness.useCase.completeLesson(harness.topic, 1).isFailure)
        assertEquals(listOf("мон эрян", "шумбрат!"), harness.words().map { it.mokshan })

        harness.storage.writeError = null
        harness.useCase.completeLesson(harness.topic, 1).assertSuccess()

        assertEquals(listOf(1 to 1), harness.storage.writes)
        assertEquals(2, harness.dao.rows.size)
    }

    @Test
    fun retryAfterDictionaryFailure_completesTheLesson() = runTest {
        val harness = LessonHarness(sampleTopic())
        harness.dao.error = IllegalStateException("db is locked")
        assertTrue(harness.useCase.completeLesson(harness.topic, 1).isFailure)

        harness.dao.error = null
        harness.useCase.completeLesson(harness.topic, 1).assertSuccess()

        assertEquals(listOf(1 to 1), harness.storage.writes)
        assertEquals(listOf("мон эрян", "шумбрат!"), harness.words().map { it.mokshan })
    }

    @Test
    fun lessonWithoutWords_failsOnlyWhenProgressFails() = runTest {
        val topic = topic(lessons = listOf(lesson(1, listOf(pair("Лац", "Fine")))))
        val harness = LessonHarness(topic)
        harness.dao.error = IllegalStateException("db is locked")

        harness.useCase.completeLesson(topic, 1).assertSuccess()

        assertEquals(listOf(1 to 1), harness.storage.writes)
    }

    // endregion
}
