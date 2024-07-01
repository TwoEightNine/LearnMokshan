package global.msnthrp.mokshan.domain.lessons

data class TopicsSummary(
    val topicsCount: Int,
    val topicsInfo: List<TopicInfo>,
)

data class TopicInfo(
    val id: Int,
    val lessonsCount: Int,
    val title: String,
)