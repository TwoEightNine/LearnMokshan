package global.msnthrp.mokshan.data.network.lessons.response

import global.msnthrp.mokshan.domain.lessons.Lesson
import global.msnthrp.mokshan.domain.lessons.LessonPair
import global.msnthrp.mokshan.domain.lessons.Topic
import global.msnthrp.mokshan.domain.lessons.Translation

/**
 * A lesson that cannot be used is not dropped: the lessons are numbered by position, so dropping
 * one would silently renumber the rest and break saved progress. The topic is rejected instead.
 * Lessons with the same `order` are ambiguous and reject the topic too.
 */
internal fun TopicResponse.toDomain(): Topic? {
    val responseLessons = this.lessons?.takeIf { it.isNotEmpty() } ?: return null
    val lessons = responseLessons.map { it.toDomain() ?: return null }
    if (lessons.map { it.order }.distinct().size != lessons.size) return null
    return Topic(
        id = this.id ?: return null,
        title = this.title ?: return null,
        description = this.description.orEmpty(),
        lessons = lessons.sortedBy { it.order },
        translations = this.translations?.mapNotNull { it.toDomain() } ?: emptyList(),
    )
}

private fun LessonResponse.toDomain(): Lesson? {
    val toNative =
        this.toNative?.mapNotNull { it.toDomain() }?.takeIf { it.isNotEmpty() } ?: return null
    return Lesson(
        order = this.order ?: return null,
        toNative = toNative,
        dictionary = this.dictionary.orEmpty(),
    )
}

private fun LessonPairResponse.toDomain(): LessonPair? {
    return LessonPair(
        mokshan = this.mokshan ?: return null,
        native = this.native ?: return null,
    )
}

private fun TranslationResponse.toDomain(): Translation? {
    return Translation(
        mokshan = this.mokshan ?: return null,
        native = this.native ?: return null,
    )
}