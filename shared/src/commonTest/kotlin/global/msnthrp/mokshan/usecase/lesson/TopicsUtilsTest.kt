package global.msnthrp.mokshan.usecase.lesson

import global.msnthrp.mokshan.domain.lessons.TopicInfo
import global.msnthrp.mokshan.testutil.lesson
import global.msnthrp.mokshan.testutil.pair
import global.msnthrp.mokshan.testutil.topic
import kotlin.test.Test
import kotlin.test.assertEquals

class TopicsUtilsTest {

    @Test
    fun topicLength_isTwoStepsPerLessonPlusThreeSummaries() {
        assertEquals(5, TopicsUtils.getTopicLength(1))
        assertEquals(7, TopicsUtils.getTopicLength(2))
        assertEquals(9, TopicsUtils.getTopicLength(3))
        assertEquals(11, TopicsUtils.getTopicLength(4))
    }

    @Test
    fun topicLength_withNoLessons_isStillThreeSummarySteps() {
        assertEquals(3, TopicsUtils.getTopicLength(0))
    }

    @Test
    fun topicLength_isUsedByTopicAndTopicInfo() {
        val topic = topic(lessons = List(4) { lesson(it + 1, listOf(pair("а", "a"))) })
        val info = TopicInfo(
            id = 1,
            lessonsCount = 4,
            title = "t",
            description = "d",
            emoji = null,
            grammar = null,
        )

        assertEquals(11, topic.topicLength)
        assertEquals(11, info.topicLength)
    }

    @Test
    fun topicLength_dependsOnActualLessonsOfTopic_notOnIndexCount() {
        // the index may promise 3 lessons while the topic file has 4 (see content tests)
        val info = TopicInfo(1, lessonsCount = 3, title = "t", description = "", emoji = null, grammar = null)
        val topic = topic(lessons = List(4) { lesson(it + 1, listOf(pair("а", "a"))) })

        assertEquals(9, info.topicLength)
        assertEquals(11, topic.topicLength)
    }
}
