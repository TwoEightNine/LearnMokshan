package global.msnthrp.mokshan.android.features.lessons.topic

import global.msnthrp.mokshan.domain.lessons.TopicInfo

/**
 * What the round topic badge shows: the emoji, or else the first letter of the title (a whole
 * code point, so an emoji or other supplementary character is not cut in half). Empty when the
 * topic has neither, so a broken index entry cannot crash the list.
 */
internal fun TopicInfo.badgeText(): String {
    emoji?.takeIf { it.isNotBlank() }?.let { return it }
    val trimmedTitle = title.trim()
    if (trimmedTitle.isEmpty()) return ""
    return trimmedTitle.substring(0, trimmedTitle.offsetByCodePoints(0, 1)).uppercase()
}
