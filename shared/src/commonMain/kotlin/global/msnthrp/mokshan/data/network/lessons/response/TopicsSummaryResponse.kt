package global.msnthrp.mokshan.data.network.lessons.response

import kotlinx.serialization.Serializable

@Serializable
internal data class TopicsSummaryResponse(
    val topicsCount: Int?,
    val topicsInfo: List<TopicInfoResponse>?
)

@Serializable
internal data class TopicInfoResponse(
    val id: Int?,
    val lessonsCount: Int?,
    val title: String?,
    val description: String?,
    val emoji: String?,
    val grammar: String?,
)