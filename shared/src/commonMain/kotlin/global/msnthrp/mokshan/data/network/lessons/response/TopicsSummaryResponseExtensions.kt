package global.msnthrp.mokshan.data.network.lessons.response

import global.msnthrp.mokshan.domain.lessons.TopicInfo
import global.msnthrp.mokshan.domain.lessons.TopicsSummary

/**
 * Topics unlock one after another by their id, so an unusable entry must not silently disappear
 * from the list: the whole index is rejected instead. Duplicated ids are ambiguous and reject it too.
 */
internal fun TopicsSummaryResponse.toDomain(): TopicsSummary? {
    val infos = this.topicsInfo?.takeIf { it.isNotEmpty() }?.map { it.toDomain() ?: return null } ?: return null
    if (infos.map { it.id }.distinct().size != infos.size) return null
    return TopicsSummary(
        topicsCount = this.topicsCount ?: return null,
        topicsInfo = infos,
    )
}

private fun TopicInfoResponse.toDomain(): TopicInfo? {
    return TopicInfo(
        id = this.id ?: return null,
        lessonsCount = this.lessonsCount?.takeIf { it >= 1 } ?: return null,
        title = this.title ?: return null,
        description = this.description.orEmpty(),
        emoji = this.emoji,
        grammar = this.grammar,
    )
}