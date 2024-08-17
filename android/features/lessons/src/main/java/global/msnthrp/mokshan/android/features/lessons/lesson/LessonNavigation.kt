package global.msnthrp.mokshan.android.features.lessons.lesson

import android.os.Bundle
import global.msnthrp.mokshan.android.core.navigation.Router
import global.msnthrp.mokshan.android.core.navigation.ScreenFactory
import global.msnthrp.mokshan.domain.lessons.TopicInfo

private const val ARG_LESSON_NUMBER = "lesson_number"
private const val ARG_TOPIC_ID = "topic_id"
private const val ARG_TOPIC_TITLE = "topic_title"
private const val ARG_TOPIC_DESCRIPTION = "topic_description"
private const val ARG_TOPIC_LESSONS_COUNT = "lessons_count"

fun LessonScreenFactory(
    onBackPressed: () -> Unit
): ScreenFactory {
    return ScreenFactory { bundle ->
        LessonScreen(
            topicInfo = bundle.getTopicInfo() ?: return@ScreenFactory,
            lessonNumber = bundle?.getString(ARG_LESSON_NUMBER)?.toIntOrNull() ?: return@ScreenFactory,
            onBackPressed = onBackPressed
        )
    }
}

fun LessonRouterDefault() = LessonRouterInternal(
    topicId = "{$ARG_TOPIC_ID}",
    lessonsCount = "{$ARG_TOPIC_LESSONS_COUNT}",
    lessonNumber = "{$ARG_LESSON_NUMBER}",
    topicTitle = "{$ARG_TOPIC_TITLE}",
    topicDescription = "{$ARG_TOPIC_DESCRIPTION}"
)

fun LessonRouter(topicInfo: TopicInfo, lessonNumber: Int) = LessonRouterInternal(
    topicId = topicInfo.id.toString(),
    lessonsCount = topicInfo.lessonsCount.toString(),
    topicTitle = topicInfo.title,
    lessonNumber = lessonNumber.toString(),
    topicDescription = topicInfo.description,
)

private fun LessonRouterInternal(
    topicId: String,
    lessonsCount: String,
    lessonNumber: String,
    topicTitle: String,
    topicDescription: String,
) = Router {
    "lesson?" +
            "$ARG_TOPIC_ID=$topicId&" +
            "$ARG_TOPIC_LESSONS_COUNT=$lessonsCount&" +
            "$ARG_LESSON_NUMBER=$lessonNumber&" +
            "$ARG_TOPIC_TITLE=$topicTitle&"
            "$ARG_TOPIC_DESCRIPTION=$topicDescription"
}

private fun Bundle?.getTopicInfo(): TopicInfo? {
    val bundle = this ?: return null
    return TopicInfo(
        id = bundle.getString(ARG_TOPIC_ID)?.toIntOrNull() ?: return null,
        title = bundle.getString(ARG_TOPIC_TITLE) ?: return null,
        lessonsCount = bundle.getString(ARG_TOPIC_LESSONS_COUNT)?.toIntOrNull() ?: return null,
        description = bundle.getString(ARG_TOPIC_DESCRIPTION).orEmpty(),
    )
}