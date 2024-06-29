package global.msnthrp.mokshan.domain.lessons

data class TopicsSummary(
    val topicsCount: Int,
    val lessonsCountByTopicId: Map<Int, Int>,
)