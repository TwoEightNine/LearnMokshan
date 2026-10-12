package global.msnthrp.mokshan.data.network.lessons

import global.msnthrp.mokshan.data.network.lessons.response.LessonPairResponse
import global.msnthrp.mokshan.data.network.lessons.response.LessonResponse
import global.msnthrp.mokshan.data.network.lessons.response.TopicInfoResponse
import global.msnthrp.mokshan.data.network.lessons.response.TopicResponse
import global.msnthrp.mokshan.data.network.lessons.response.TopicsSummaryResponse
import global.msnthrp.mokshan.data.network.lessons.response.TranslationResponse
import global.msnthrp.mokshan.data.network.lessons.response.toDomain
import global.msnthrp.mokshan.domain.lessons.Lesson
import global.msnthrp.mokshan.domain.lessons.LessonPair
import global.msnthrp.mokshan.domain.lessons.Topic
import global.msnthrp.mokshan.domain.lessons.TopicInfo
import global.msnthrp.mokshan.domain.lessons.TopicsSummary
import global.msnthrp.mokshan.domain.lessons.Translation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class LessonsMappersTest {

    private fun pairResponse(mokshan: String? = "Лац", native: List<String>? = listOf("Fine")) =
        LessonPairResponse(mokshan, native)

    private fun lessonResponse(
        order: Int? = 1,
        toNative: List<LessonPairResponse>? = listOf(pairResponse()),
        dictionary: List<String>? = listOf("Лац"),
    ) = LessonResponse(order, toNative, dictionary)

    private fun topicResponse(
        id: Int? = 1,
        title: String? = "Title",
        description: String? = "Description",
        lessons: List<LessonResponse>? = listOf(lessonResponse()),
        translations: List<TranslationResponse>? = listOf(TranslationResponse("лац", "fine")),
    ) = TopicResponse(id, title, description, version = 1, lessons = lessons, translations = translations)

    // region topic

    @Test
    fun topic_isMappedWithAllFields() {
        val expected = Topic(
            id = 1,
            title = "Title",
            description = "Description",
            lessons = listOf(Lesson(1, listOf(LessonPair("Лац", listOf("Fine"))), listOf("Лац"))),
            translations = listOf(Translation("лац", "fine")),
        )

        assertEquals(expected, topicResponse().toDomain())
    }

    @Test
    fun topic_withoutIdOrTitle_isInvalid() {
        assertNull(topicResponse(id = null).toDomain())
        assertNull(topicResponse(title = null).toDomain())
    }

    @Test
    fun topic_withoutDescription_hasEmptyDescription() {
        assertEquals("", topicResponse(description = null).toDomain()!!.description)
    }

    @Test
    fun topic_withoutLessons_isInvalid() {
        assertNull(topicResponse(lessons = null).toDomain())
        assertNull(topicResponse(lessons = emptyList()).toDomain())
    }

    @Test
    fun topic_withoutTranslations_hasEmptyTranslations() {
        assertEquals(emptyList(), topicResponse(translations = null).toDomain()!!.translations)
    }

    @Test
    fun topic_dropsInvalidTranslations() {
        val topic = topicResponse(
            translations = listOf(
                TranslationResponse("лац", "fine"),
                TranslationResponse(null, "x"),
                TranslationResponse("y", null),
            ),
        ).toDomain()!!

        assertEquals(listOf(Translation("лац", "fine")), topic.translations)
    }

    @Test
    fun topic_sortsLessonsByOrder() {
        val topic = topicResponse(
            lessons = listOf(lessonResponse(order = 3), lessonResponse(order = 1), lessonResponse(order = 2)),
        ).toDomain()!!

        assertEquals(listOf(1, 2, 3), topic.lessons.map { it.order })
    }

    @Test
    fun topic_withDuplicatedLessonOrder_isRejected() {
        assertNull(topicResponse(lessons = listOf(lessonResponse(order = 1), lessonResponse(order = 1))).toDomain())
    }

    @Test
    fun topic_withUnusableLesson_isRejectedInsteadOfRenumberingLessons() {
        // dropping a lesson would silently shift the numbers of the following ones
        val invalidLessons = listOf(
            lessonResponse(order = null),
            lessonResponse(order = 2, toNative = null),
            lessonResponse(order = 2, toNative = emptyList()),
            lessonResponse(order = 2, toNative = listOf(pairResponse(mokshan = null))),
        )

        invalidLessons.forEach { invalid ->
            val topic = topicResponse(lessons = listOf(lessonResponse(order = 1), invalid, lessonResponse(order = 3)))

            assertNull(topic.toDomain(), "invalid=$invalid")
        }
    }

    @Test
    fun lessonWithoutDictionary_isUsableWithEmptyDictionary() {
        val topic = topicResponse(lessons = listOf(lessonResponse(dictionary = null))).toDomain()!!

        assertEquals(emptyList(), topic.lessons.single().dictionary)
    }

    @Test
    fun lesson_dropsInvalidPairsButKeepsValidOnes() {
        val topic = topicResponse(
            lessons = listOf(
                lessonResponse(
                    toNative = listOf(
                        pairResponse(),
                        pairResponse(mokshan = null),
                        pairResponse(native = null),
                        pairResponse("Пара", listOf("Good")),
                    ),
                ),
            ),
        ).toDomain()!!

        assertEquals(listOf("Лац", "Пара"), topic.lessons.single().toNative.map { it.mokshan })
    }

    @Test
    fun lesson_withOnlyInvalidPairs_isDropped() {
        assertNull(topicResponse(lessons = listOf(lessonResponse(toNative = listOf(pairResponse(null))))).toDomain())
    }

    @Test
    fun pair_withEmptyNativeList_isAccepted() {
        // pins current behaviour (see B7): nothing prevents a pair without translations
        val topic = topicResponse(lessons = listOf(lessonResponse(toNative = listOf(pairResponse(native = emptyList()))))).toDomain()

        assertEquals(emptyList(), topic!!.lessons.single().toNative.single().native)
    }

    @Test
    fun lesson_withEmptyDictionary_isAccepted() {
        assertNotNull(topicResponse(lessons = listOf(lessonResponse(dictionary = emptyList()))).toDomain())
    }

    // endregion

    // region summary

    private fun infoResponse(
        id: Int? = 1,
        lessonsCount: Int? = 3,
        title: String? = "Title",
        description: String? = "Description",
        emoji: String? = "✋",
        grammar: String? = "grammar.prelinar",
    ) = TopicInfoResponse(id, lessonsCount, title, description, emoji, grammar)

    @Test
    fun summary_isMapped() {
        val summary = TopicsSummaryResponse(1, listOf(infoResponse())).toDomain()

        assertEquals(
            TopicsSummary(1, listOf(TopicInfo(1, 3, "Title", "Description", "✋", "grammar.prelinar"))),
            summary,
        )
    }

    @Test
    fun summary_withoutCountOrTopics_isInvalid() {
        assertNull(TopicsSummaryResponse(null, listOf(infoResponse())).toDomain())
        assertNull(TopicsSummaryResponse(1, null).toDomain())
        assertNull(TopicsSummaryResponse(1, emptyList()).toDomain())
    }

    @Test
    fun summary_withUnusableTopicInfo_isRejectedInsteadOfHidingTheTopic() {
        // topics unlock by id, so a missing entry could lock the user out of the next ones
        val invalidInfos = listOf(
            infoResponse(id = null),
            infoResponse(id = 3, lessonsCount = null),
            infoResponse(id = 3, lessonsCount = 0),
            infoResponse(id = 3, lessonsCount = -2),
            infoResponse(id = 3, title = null),
        )

        invalidInfos.forEach { invalid ->
            val summary = TopicsSummaryResponse(3, listOf(infoResponse(id = 1), invalid, infoResponse(id = 5)))

            assertNull(summary.toDomain(), "invalid=$invalid")
        }
    }

    @Test
    fun summary_withDuplicatedTopicIds_isRejected() {
        assertNull(TopicsSummaryResponse(2, listOf(infoResponse(id = 1), infoResponse(id = 1))).toDomain())
    }

    @Test
    fun summary_keepsTopicsInFileOrder() {
        val summary = TopicsSummaryResponse(3, listOf(infoResponse(id = 2), infoResponse(id = 1), infoResponse(id = 3))).toDomain()!!

        assertEquals(listOf(2, 1, 3), summary.topicsInfo.map { it.id })
    }

    @Test
    fun summary_doesNotCompareTopicsCountWithActualTopics() {
        val summary = TopicsSummaryResponse(99, listOf(infoResponse())).toDomain()!!

        assertEquals(99, summary.topicsCount)
        assertEquals(1, summary.topicsInfo.size)
    }

    @Test
    fun topicInfo_optionalFieldsMayBeMissing() {
        val info = TopicsSummaryResponse(1, listOf(infoResponse(description = null, emoji = null, grammar = null)))
            .toDomain()!!.topicsInfo.single()

        assertEquals("", info.description)
        assertNull(info.emoji)
        assertNull(info.grammar)
    }

    @Test
    fun topicInfo_lengthIsDerivedFromLessonsCount() {
        val info = TopicsSummaryResponse(1, listOf(infoResponse(lessonsCount = 3))).toDomain()!!.topicsInfo.single()

        assertEquals(9, info.topicLength)
    }

    // endregion
}
