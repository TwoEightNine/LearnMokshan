package global.msnthrp.mokshan.android.features.lessons.lesson

import global.msnthrp.mokshan.android.features.lessons.topicInfo
import global.msnthrp.mokshan.domain.lessons.TopicInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonNavigationTest {

    @Test
    fun router_putsAllTopicArgumentsIntoRoute() {
        val route = LessonRouter(topicInfo(id = 4, lessonsCount = 3), lessonNumber = 5).route()

        assertEquals(
            "lesson?topic_id=4&lessons_count=3&lesson_number=5&topic_title=Topic 4&topic_description=Description 4",
            route,
        )
    }

    @Test
    fun router_supportsRepeatLessonNumber() {
        val route = LessonRouter(topicInfo(), lessonNumber = -3).route()

        assertTrue("lesson_number=-3" in route)
    }

    @Test
    fun defaultRouter_usesPlaceholdersForEveryArgument() {
        assertEquals(
            "lesson?topic_id={topic_id}&lessons_count={lessons_count}&lesson_number={lesson_number}" +
                "&topic_title={topic_title}&topic_description={topic_description}",
            LessonRouterDefault().route(),
        )
    }

    @Test
    fun router_doesNotEncodeSpecialCharactersOfTitle_knownBugB21() {
        // KNOWN BUG B21: '&' and '=' in a title would be read as extra route arguments
        val info = TopicInfo(1, 2, title = "Cats & dogs", description = "a=b", emoji = null, grammar = null)

        val route = LessonRouter(info, 1).route()

        assertTrue("topic_title=Cats & dogs&topic_description=a=b" in route)
    }

    @Test
    fun routeStart_isTheSameForRouterAndDefaultRouter() {
        assertEquals(
            LessonRouterDefault().route().substringBefore('?'),
            LessonRouter(topicInfo(), 1).route().substringBefore('?'),
        )
    }
}
