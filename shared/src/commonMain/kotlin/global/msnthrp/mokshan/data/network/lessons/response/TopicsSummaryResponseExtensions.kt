package global.msnthrp.mokshan.data.network.lessons.response

import global.msnthrp.mokshan.domain.lessons.TopicInfo
import global.msnthrp.mokshan.domain.lessons.TopicsSummary

internal fun TopicsSummaryResponse.toDomain(): TopicsSummary? {
    return TopicsSummary(
        topicsCount = this.topicsCount ?: return null,
        topicsInfo = this.topicsInfo?.mapNotNull { it.toDomain() }?.takeIf { it.isNotEmpty() } ?: return null,
    )
}

private fun TopicInfoResponse.toDomain(): TopicInfo? {
    return TopicInfo(
        id = this.id ?: return null,
        lessonsCount = this.lessonsCount ?: return null,
        title = this.title ?: return null,
    )
}