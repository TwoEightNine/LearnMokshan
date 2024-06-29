package global.msnthrp.mokshan.data.network.lessons.response

import kotlinx.serialization.Serializable

@Serializable
internal data class TopicsSummaryResponse(
    val topicsCount: Int?,
    val lessonsCountByTopics: List<TopicCountEntry>?
)

@Serializable
internal data class TopicCountEntry(
    val topicId: Int?,
    val count: Int?,
)