package global.msnthrp.mokshan.domain.lessons

data class TopicsSummary(
    val topicsCount: Int,
    val topicsInfo: List<TopicInfo>,
)

data class TopicInfo(
    val id: Int,
    val lessonsCount: Int,
    val title: String,
    val description: String,
) {
    val topicLength: Int by lazy { lessonsCount * 3 + 4 }
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