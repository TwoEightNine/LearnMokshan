package global.msnthrp.mokshan.data.network.lessons.response

import global.msnthrp.mokshan.domain.lessons.TopicsSummary

internal fun TopicsSummaryResponse.toDomain(): TopicsSummary? {
    val lessonsCountByTopicId = mutableMapOf<Int, Int>()
    this.lessonsCountByTopics?.forEach { (topicId, count) ->
        if (topicId != null && count != null) {
            lessonsCountByTopicId[topicId] = count
        }
    }
    return TopicsSummary(
        topicsCount = this.topicsCount ?: return null,
        lessonsCountByTopicId = lessonsCountByTopicId,
    )
}