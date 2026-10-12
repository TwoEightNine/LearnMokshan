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
    fun lessonNumberZero_isRejected() = runTest {
        // progress starts at "0 lessons completed", so the first lesson to start is number 1
        val error = LessonHarness(sampleTopic()).useCase.prepareLesson(1, 0).assertFailure()

        assertIs<IllegalArgumentException>(error)
    }

    @Test
    fun lessonsAreSelectedByPositionInOrder_evenWhenOrderValuesHaveGaps() = runTest {
        val gappedTopic = topic(
            lessons = listOf(
                lesson(1, listOf(pair("а", "a"))),
                lesson(3, listOf(pair("б", "b"))),
            ),
        )

        val first = LessonHarness(gappedTopic).prepare(1)
        val second = LessonHarness(gappedTopic).prepare(2)

        assertEquals(listOf("а"), first.lessonSteps.map { it.sentence })
        assertEquals(listOf("б"), second.lessonSteps.map { it.sentence })
    }

    @Test
    fun lessonsGivenOutOfOrder_areStillNumberedByOrder() = runTest {
        val shuffledTopic = topic(
            lessons = listOf(
                lesson(2, listOf(pair("б", "b"))),
                lesson(1, listOf(pair("а", "a"))),
            ),
        )

        assertEquals(listOf("а"), LessonHarness(shuffledTopic).prepare(1).lessonSteps.map { it.sentence })
        assertEquals(listOf("б"), LessonHarness(shuffledTopic).prepare(2).lessonSteps.map { it.sentence })
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
    fun pairWithoutTranslation_isSkipped_insteadOfCrashing() = runTest {
        val topic = topic(
            lessons = listOf(lesson(1, listOf(pair("Шумбрат!"), pair("Лац", "Fine")))),
        )

        listOf(1, 3).forEach { number -> // word bank and typed input
            val steps = LessonHarness(topic).prepare(number).lessonSteps

            assertEquals(1, steps.size, "lesson=$number")
            assertTrue(steps.single().sentence == "Лац" || steps.single().sentence == "Fine")
        }
    }

    @Test
    fun lessonWithoutAnyTranslatedPair_failsWithoutCrashing() = runTest {
        val topic = topic(lessons = listOf(lesson(1, listOf(pair("Шумбрат!")))))

        listOf(1, 3).forEach { number ->
            assertIs<IllegalStateException>(LessonHarness(topic).useCase.prepareLesson(1, number).assertFailure())
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
    fun wordBank_withTenOrMoreCorrectWords_isShuffledToo() = runTest {
        val sentence = "one two three four five six seven eight nine ten"
        val topic = topic(lessons = listOf(lesson(1, listOf(pair("слово", sentence)))))

        val banks = (0 until 20).map { seed ->
            LessonHarness(topic, seed).prepare(1).lessonSteps.single().bankWords()
        }

        assertTrue(banks.all { it.sorted() == sentence.split(" ").sorted() })
        assertTrue(banks.any { it != sentence.split(" ") }, "the correct order must not be shown")
        assertTrue(banks.toSet().size > 1)
    }

    @Test
    fun wordBank_ofAnswerWithMoreThanTenWords_keepsEveryWord() = runTest {
        val sentence = "a b c d e f g h i j k l"
        val topic = topic(lessons = listOf(lesson(1, listOf(pair("слово", sentence)))))

        val bank = LessonHarness(topic).prepare(1).lessonSteps.single().bankWords()

        assertEquals(sentence.split(" ").sorted(), bank.sorted())
    }

    @Test
    fun wordBank_ignoresIrregularWhitespace_andNeverHasEmptyWords() = runTest {
        val topic = topic(lessons = listOf(lesson(1, listOf(pair("слово", "  how   are\u00A0you  ?  ")))))

        val bank = LessonHarness(topic).prepare(1).lessonSteps.single().bankWords()

        assertEquals(listOf("are", "how", "you"), bank.sorted())
    }

    @Test
    fun wordBank_ofPunctuationOnlyAnswer_isEmpty() = runTest {
        val topic = topic(lessons = listOf(lesson(1, listOf(pair("слово", "?!")))))

        assertEquals(emptyList(), LessonHarness(topic).prepare(1).lessonSteps.single().bankWords())
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
