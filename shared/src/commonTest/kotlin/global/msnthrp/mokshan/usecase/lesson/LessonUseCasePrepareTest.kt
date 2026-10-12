package global.msnthrp.mokshan.usecase.lesson

import global.msnthrp.mokshan.domain.lessons.LessonStep
import global.msnthrp.mokshan.domain.lessons.LessonStepDirection.FROM_MOKSHAN
import global.msnthrp.mokshan.domain.lessons.LessonStepDirection.TO_MOKSHAN
import global.msnthrp.mokshan.domain.lessons.LessonStepType
import global.msnthrp.mokshan.testutil.assertFailure
import global.msnthrp.mokshan.testutil.lesson
import global.msnthrp.mokshan.testutil.pair
import global.msnthrp.mokshan.testutil.sampleTopic
import global.msnthrp.mokshan.testutil.topic
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * `sampleTopic()` has two lessons, so it consists of seven steps:
 * 1-2 word bank, 3 bank summary, 4-5 typed input, 6 input summary, 7 full review.
 */
class LessonUseCasePrepareTest {

    private val lesson1Sentences = setOf("Шумбрат!", "Мон эрян", "Тон эрят", "Сон эрясы")
    private val lesson2Sentences = setOf("Кода тефне?", "Пара шобдава!", "Цебярьста", "Лац")

    // region lesson number mapping

    @Test
    fun bankLesson_usesOnlyPairsOfThatLesson_fromMokshan() = runTest {
        val prepared = LessonHarness(sampleTopic()).prepare(1)

        assertEquals(lesson1Sentences, prepared.lessonSteps.map { it.sentence }.toSet())
        assertEquals(4, prepared.lessonSteps.size)
        assertTrue(prepared.lessonSteps.all { it.direction == FROM_MOKSHAN })
        assertTrue(prepared.lessonSteps.all { it.type is LessonStepType.WordBank })
        assertTrue(prepared.withHints)
    }

    @Test
    fun secondBankLesson_usesPairsOfSecondLesson() = runTest {
        val prepared = LessonHarness(sampleTopic()).prepare(2)

        assertEquals(lesson2Sentences, prepared.lessonSteps.map { it.sentence }.toSet())
    }

    @Test
    fun bankSummary_usesAllPairsAsWordBankFromMokshan() = runTest {
        val prepared = LessonHarness(sampleTopic()).prepare(3)

        assertEquals(
            lesson1Sentences + lesson2Sentences,
            prepared.lessonSteps.map { it.sentence }.toSet(),
        )
        assertTrue(prepared.lessonSteps.all { it.direction == FROM_MOKSHAN })
        assertTrue(prepared.lessonSteps.all { it.type is LessonStepType.WordBank })
        assertTrue(prepared.withHints)
    }

    @Test
    fun inputLessons_reuseTheLessonPairsAsTypedInput() = runTest {
        val harness = LessonHarness(sampleTopic())

        val first = harness.prepare(4)
        val second = harness.prepare(5)

        assertTrue(first.lessonSteps.all { it.type == LessonStepType.Input })
        assertTrue(second.lessonSteps.all { it.type == LessonStepType.Input })
        assertEquals(4, first.lessonSteps.size)
        assertEquals(4, second.lessonSteps.size)
        first.lessonSteps.forEach { step ->
            when (step.direction) {
                FROM_MOKSHAN -> assertTrue(step.sentence in lesson1Sentences)
                TO_MOKSHAN -> assertTrue(lesson1Sentences.containsAll(step.answers))
            }
        }
        second.lessonSteps.forEach { step ->
            when (step.direction) {
                FROM_MOKSHAN -> assertTrue(step.sentence in lesson2Sentences)
                TO_MOKSHAN -> assertTrue(lesson2Sentences.containsAll(step.answers))
            }
        }
        assertTrue(first.withHints)
    }

    @Test
    fun inputSummary_usesAllPairsAsTypedInput() = runTest {
        val prepared = LessonHarness(sampleTopic()).prepare(6)

        assertEquals(8, prepared.lessonSteps.size)
        assertTrue(prepared.lessonSteps.all { it.type == LessonStepType.Input })
        assertTrue(prepared.withHints)
    }

    @Test
    fun fullReview_usesAllPairsAsTypedInputWithoutHints() = runTest {
        val prepared = LessonHarness(sampleTopic()).prepare(7)

        assertEquals(8, prepared.lessonSteps.size)
        assertTrue(prepared.lessonSteps.all { it.type == LessonStepType.Input })
        assertEquals(false, prepared.withHints)
    }

    @Test
    fun repeat_usesAllPairsAsTypedInputWithHints() = runTest {
        val prepared = LessonHarness(sampleTopic()).prepare(LessonUseCase.LESSON_NUMBER_REPEAT)

        assertEquals(8, prepared.lessonSteps.size)
        assertTrue(prepared.lessonSteps.all { it.type == LessonStepType.Input })
        assertTrue(prepared.withHints)
    }

    @Test
    fun preparedLesson_carriesTheTopic() = runTest {
        val topic = sampleTopic(id = 5)

        assertEquals(topic, LessonHarness(topic).prepare(1).topic)
    }

    @Test
    fun lessonNumberBeyondTopicLength_fails() = runTest {
        val result = LessonHarness(sampleTopic()).useCase.prepareLesson(1, lessonNumber = 8)

        assertIs<IllegalArgumentException>(result.assertFailure())
    }

    @Test
    fun negativeLessonNumber_otherThanRepeat_fails() = runTest {
        val harness = LessonHarness(sampleTopic())

        assertIs<IllegalArgumentException>(harness.useCase.prepareLesson(1, -1).assertFailure())
        assertIs<IllegalArgumentException>(harness.useCase.prepareLesson(1, -2).assertFailure())
    }

    @Test
    fun lastValidLessonNumber_isAccepted() = runTest {
        assertEquals(8, LessonHarness(sampleTopic()).prepare(7).lessonSteps.size)
    }

    @Test
    fun repositoryFailure_isPropagatedAsIs() = runTest {
        val harness = LessonHarness(sampleTopic())
        val boom = IllegalStateException("network is down")
        harness.network.error = boom

        assertEquals(boom, harness.useCase.prepareLesson(1, 1).assertFailure())
    }

    @Test
    fun topicWithoutLessons_failsBecauseThereAreNoPairs() = runTest {
        val harness = LessonHarness(topic(lessons = emptyList()))

        val error = harness.useCase.prepareLesson(1, 1).assertFailure()

        assertIs<IllegalStateException>(error)
    }

    @Test
    fun lessonNumberZero_isAcceptedAndBehavesAsWholeTopicWordBank() = runTest {
        // pins current behaviour: UI never starts lesson 0 (progress starts at 0 completed lessons)
        val prepared = LessonHarness(sampleTopic()).prepare(0)

        assertEquals(8, prepared.lessonSteps.size)
        assertTrue(prepared.lessonSteps.all { it.type is LessonStepType.WordBank })
    }

    @Test
    fun lessonsAreSelectedByOrder_notByPosition_knownBugB9() = runTest {
        // KNOWN BUG B9: prepareLesson looks a lesson up by `order`, completeLesson by list index.
        // With non-contiguous orders [1, 3] lesson number 2 has no matching `order`.
        val gappedTopic = topic(
            lessons = listOf(
                lesson(1, listOf(pair("а", "a"))),
                lesson(3, listOf(pair("б", "b"))),
            ),
        )

        val error = LessonHarness(gappedTopic).useCase.prepareLesson(1, 2).assertFailure()

        assertIs<IllegalStateException>(error)
    }

    // endregion

    // region direction schedule

    @Test
    fun wordBankPhases_alwaysGoFromMokshan() = runTest {
        val harness = LessonHarness(sampleTopic())

        listOf(1, 2, 3).forEach { number ->
            assertTrue(harness.prepare(number).lessonSteps.all { it.direction == FROM_MOKSHAN })
        }
    }

    @Test
    fun typedInputDirections_followStepRatioSchedule() = runTest {
        // pairs -> expected directions of consecutive steps in the input lesson (lesson number 3
        // for a one-lesson topic: 1 bank, 2 bank summary, 3 input)
        val expected = mapOf(
            1 to listOf(TO_MOKSHAN),
            2 to listOf(TO_MOKSHAN, TO_MOKSHAN),
            3 to listOf(TO_MOKSHAN, FROM_MOKSHAN, TO_MOKSHAN),
            4 to listOf(FROM_MOKSHAN, TO_MOKSHAN, FROM_MOKSHAN, TO_MOKSHAN),
            5 to listOf(FROM_MOKSHAN, TO_MOKSHAN, FROM_MOKSHAN, TO_MOKSHAN, TO_MOKSHAN),
            8 to listOf(
                FROM_MOKSHAN, FROM_MOKSHAN, TO_MOKSHAN, TO_MOKSHAN,
                FROM_MOKSHAN, FROM_MOKSHAN, TO_MOKSHAN, TO_MOKSHAN,
            ),
        )

        expected.forEach { (pairsCount, directions) ->
            val pairs = List(pairsCount) { pair("слово$it", "word$it") }
            val prepared = LessonHarness(topic(lessons = listOf(lesson(1, pairs)))).prepare(3)

            assertEquals(directions, prepared.lessonSteps.map { it.direction }, "pairs=$pairsCount")
        }
    }

    // endregion

    // region steps content

    @Test
    fun fromMokshanStep_asksMokshanSentenceAndAcceptsAllNativeVariants() = runTest {
        val step = LessonHarness(sampleTopic()).prepare(1)
            .lessonSteps.single { it.sentence == "Сон эрясы" }

        assertEquals(listOf("He lives", "She lives"), step.answers)
    }

    @Test
    fun toMokshanStep_asksFirstNativeAndAcceptsEveryMokshanWithSameNative() = runTest {
        val topic = topic(
            lessons = listOf(
                lesson(1, listOf(pair("Шумбрат!", "Hello!"), pair("Шумбратада!", "Hello!"))),
            ),
        )

        val steps = LessonHarness(topic).prepare(3).lessonSteps // 3 -> typed input

        assertTrue(steps.all { it.direction == TO_MOKSHAN })
        steps.forEach { step ->
            assertEquals("Hello!", step.sentence)
            assertEquals(listOf("Шумбрат!", "Шумбратада!"), step.answers)
        }
    }

    @Test
    fun toMokshanStep_usesFirstNativeVariantAsSentence() = runTest {
        val topic = topic(lessons = listOf(lesson(1, listOf(pair("Сон эрясы", "He lives", "She lives")))))

        val step = LessonHarness(topic).prepare(3).lessonSteps.single()

        assertEquals(TO_MOKSHAN, step.direction)
        assertEquals("He lives", step.sentence)
        assertEquals(listOf("Сон эрясы"), step.answers)
    }

    @Test
    fun toMokshanStep_whenNativeVariantsDiffer_secondVariantIsNotOfferedAsSentence() = runTest {
        val topic = topic(lessons = listOf(lesson(1, listOf(pair("Сон эрясы", "He lives", "She lives")))))

        val sentences = LessonHarness(topic).prepare(3).lessonSteps.map { it.sentence }

        assertEquals(listOf("He lives"), sentences)
    }

    @Test
    fun pairWithEmptyNative_crashesTypedInputPreparation_knownBugB7() = runTest {
        // KNOWN BUG B7: `native.first()` throws instead of returning Result.failure
        val topic = topic(lessons = listOf(lesson(1, listOf(pair("Шумбрат!")))))

        assertFailsWith<NoSuchElementException> {
            LessonHarness(topic).useCase.prepareLesson(1, 3)
        }
    }

    // endregion

    // region word bank

    @Test
    fun wordBank_containsEveryCorrectWordOfTheAnswer() = runTest {
        val prepared = LessonHarness(sampleTopic()).prepare(1)

        prepared.lessonSteps.forEach { step ->
            val bank = step.bankWords()
            val correct = step.answers.flatMap { cleanWords(it) }
            correct.distinct().forEach { word -> assertTrue(word in bank, "$word missing in $bank") }
            // duplicates of correct words are preserved
            correct.groupBy { it }.forEach { (word, occurrences) ->
                assertTrue(bank.count { it == word } >= occurrences.size)
            }
        }
    }

    @Test
    fun wordBank_ofMultiVariantAnswer_containsWordsOfAllVariants() = runTest {
        val step = LessonHarness(sampleTopic()).prepare(1).lessonSteps.single { it.sentence == "Сон эрясы" }

        val bank = step.bankWords()

        // he lives + she lives (4 words), plus 4 distractors out of the lesson vocabulary
        assertEquals(
            listOf("he", "hello", "i", "live", "lives", "lives", "she", "you"),
            bank.sorted(),
        )
    }

    @Test
    fun wordBank_isPaddedWithDistractorsUpToTenWords() = runTest {
        val prepared = LessonHarness(sampleTopic()).prepare(3) // vocabulary of both lessons: 12 words

        prepared.lessonSteps.forEach { step ->
            assertEquals(10, step.bankWords().size, step.sentence)
        }
    }

    @Test
    fun wordBank_isLimitedByAvailableVocabulary() = runTest {
        val step = LessonHarness(sampleTopic()).prepare(1).lessonSteps.single { it.sentence == "Шумбрат!" }

        // "hello" + the other six words of the first lesson vocabulary
        assertEquals(7, step.bankWords().size)
        assertEquals(listOf("he", "hello", "i", "live", "lives", "she", "you"), step.bankWords().sorted())
    }

    @Test
    fun wordBank_distractorsComeOnlyFromLessonVocabulary() = runTest {
        val vocabulary = setOf("hello", "i", "live", "you", "he", "lives", "she")

        LessonHarness(sampleTopic()).prepare(1).lessonSteps.forEach { step ->
            assertTrue(vocabulary.containsAll(step.bankWords()), step.bankWords().toString())
        }
    }

    @Test
    fun wordBank_hasUniqueConsecutiveIndices() = runTest {
        LessonHarness(sampleTopic()).prepare(3).lessonSteps.forEach { step ->
            val indices = (step.type as LessonStepType.WordBank).availableWords.map { it.index }
            assertEquals(indices.indices.toList(), indices)
        }
    }

    @Test
    fun wordBank_stripsPunctuationAndLowercasesWords() = runTest {
        val topic = topic(lessons = listOf(lesson(1, listOf(pair("Кода тефне?", "How, are – you?")))))

        val step = LessonHarness(topic).prepare(1).lessonSteps.single()

        assertEquals(listOf("are", "how", "you"), step.bankWords().sorted())
    }

    @Test
    fun wordBank_withTenOrMoreCorrectWords_isNotShuffled_knownBugB6() = runTest {
        // KNOWN BUG B6: when the answer has >= 10 words the bank is returned in the correct order
        val sentence = "one two three four five six seven eight nine ten"
        val topic = topic(lessons = listOf(lesson(1, listOf(pair("слово", sentence)))))

        repeat(10) { seed ->
            val step = LessonHarness(topic, seed).prepare(1).lessonSteps.single()
            assertEquals(sentence.split(" "), step.bankWords(), "seed=$seed")
        }
    }

    @Test
    fun wordBank_withNineCorrectWordsAndNoDistractors_isShuffledEventually() = runTest {
        val sentence = "one two three four five six seven eight nine"
        val topic = topic(lessons = listOf(lesson(1, listOf(pair("слово", sentence)))))

        val orders = (0 until 20).map { seed ->
            LessonHarness(topic, seed).prepare(1).lessonSteps.single().bankWords()
        }

        assertTrue(orders.all { it.sorted() == sentence.split(" ").sorted() })
        assertTrue(orders.toSet().size > 1)
    }

    // endregion

    // region invariants / determinism

    @Test
    fun everyLessonNumber_producesStepsForExpectedPairs_acrossSeeds() = runTest {
        val expectedStepCounts = mapOf(1 to 4, 2 to 4, 3 to 8, 4 to 4, 5 to 4, 6 to 8, 7 to 8, -3 to 8)

        repeat(30) { seed ->
            val harness = LessonHarness(sampleTopic(), seed)
            expectedStepCounts.forEach { (number, count) ->
                val steps = harness.prepare(number).lessonSteps
                assertEquals(count, steps.size, "seed=$seed lesson=$number")
                steps.forEach { step -> assertTrue(step.answers.isNotEmpty(), "seed=$seed lesson=$number") }
            }
        }
    }

    @Test
    fun everyStep_hasItsOwnSentenceAsOneOfTheAnswersAfterRoundTrip() = runTest {
        val topic = sampleTopic()
        val allPairs = topic.lessons.flatMap { it.toNative }

        repeat(20) { seed ->
            LessonHarness(topic, seed).prepare(6).lessonSteps.forEach { step ->
                when (step.direction) {
                    FROM_MOKSHAN -> assertTrue(allPairs.any { it.mokshan == step.sentence && it.native == step.answers })
                    TO_MOKSHAN -> assertTrue(allPairs.any { step.sentence in it.native && it.mokshan in step.answers })
                }
            }
        }
    }

    @Test
    fun sameSeed_producesSameLesson() = runTest {
        val first = LessonHarness(sampleTopic(), seed = 7).prepare(3)
        val second = LessonHarness(sampleTopic(), seed = 7).prepare(3)

        assertEquals(first, second)
    }

    // endregion

    private fun LessonStep.bankWords(): List<String> =
        (type as LessonStepType.WordBank).availableWords.map { it.word }

    private fun cleanWords(sentence: String): List<String> =
        sentence.replace("?", "").replace("!", "").replace(".", "").replace(",", "")
            .replace("–", "").replace("  ", " ").lowercase().split(" ")
}
