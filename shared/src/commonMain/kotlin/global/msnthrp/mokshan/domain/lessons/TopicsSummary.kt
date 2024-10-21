package global.msnthrp.mokshan.domain.lessons

import global.msnthrp.mokshan.usecase.lesson.TopicsUtils

data class TopicsSummary(
    val topicsCount: Int,
    val topicsInfo: List<TopicInfo>,
)

data class TopicInfo(
    val id: Int,
    val lessonsCount: Int,
    val title: String,
    val description: String,
    val emoji: String?,
    val grammar: String?,
) {
    val topicLength: Int by lazy { TopicsUtils.getTopicLength(lessonsCount) }
}

data class TopicsSummaryWithProgress(
    val summary: TopicsSummary,
    val progress: TopicsProgress,
)

data class TopicsProgress(
    val completedTopicIds: List<Int>,
    val ongoingTopicId: Int,
    val ongoingTopicLessonNumber: Int,
)