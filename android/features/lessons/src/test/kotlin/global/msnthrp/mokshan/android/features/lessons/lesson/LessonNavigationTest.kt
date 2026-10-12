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
            "lesson?topic_id=4&lessons_count=3&lesson_number=5&topic_title=Topic%204&topic_description=Description%204",
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
    fun router_encodesSpecialCharactersOfTitleAndDescription() {
        val info = TopicInfo(1, 2, title = "Cats & dogs", description = "a=b #1 100%", emoji = null, grammar = null)

        val route = LessonRouter(info, 1).route()

        assertEquals(
            "lesson?topic_id=1&lessons_count=2&lesson_number=1" +
                "&topic_title=Cats%20%26%20dogs&topic_description=a%3Db%20%231%20100%25",
            route,
        )
    }

    @Test
    fun routeArguments_staySeparateWhateverTheTitleContains() {
        val titles = listOf("Where? Where to? Where from?", "a&b=c&d", "{topic_id}", "#fragment", "x/y", "Кто это?")

        titles.forEach { title ->
            val info = TopicInfo(7, 3, title = title, description = title, emoji = null, grammar = null)

            val route = LessonRouter(info, 4).route()

            val arguments = route.substringAfter('?').split('&').associate { it.substringBefore('=') to it.substringAfter('=') }
            assertEquals(title, listOf("topic_id", "lessons_count", "lesson_number", "topic_title", "topic_description"), arguments.keys.toList())
            assertEquals("7", arguments["topic_id"])
            assertEquals("4", arguments["lesson_number"])
            assertEquals(title, java.net.URLDecoder.decode(arguments["topic_title"]!!.replace("+", "%2B"), "UTF-8"))
            assertEquals(title, java.net.URLDecoder.decode(arguments["topic_description"]!!.replace("+", "%2B"), "UTF-8"))
        }
    }

    @Test
    fun defaultRouter_placeholdersAreNotEncoded() {
        assertTrue("{topic_title}" in LessonRouterDefault().route())
    }

    @Test
    fun routeStart_isTheSameForRouterAndDefaultRouter() {
        assertEquals(
            LessonRouterDefault().route().substringBefore('?'),
            LessonRouter(topicInfo(), 1).route().substringBefore('?'),
        )
    }
}
