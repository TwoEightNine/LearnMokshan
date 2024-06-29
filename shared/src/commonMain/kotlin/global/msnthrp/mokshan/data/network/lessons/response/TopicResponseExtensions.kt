package global.msnthrp.mokshan.data.network.lessons.response

import global.msnthrp.mokshan.domain.lessons.Lesson
import global.msnthrp.mokshan.domain.lessons.LessonPair
import global.msnthrp.mokshan.domain.lessons.Topic
import global.msnthrp.mokshan.domain.lessons.Translation

internal fun TopicResponse.toDomain(): Topic? {
    val lessons = this.lessons?.mapNotNull { it.toDomain() }?.takeIf { it.isNotEmpty() } ?: return null
    return Topic(
        id = this.id ?: return null,
        title = this.title ?: return null,
        lessons = lessons,
        translations = this.translations?.mapNotNull { it.toDomain() } ?: emptyList(),
    )
}

private fun LessonResponse.toDomain(): Lesson? {
    val toNative =
        this.toNative?.mapNotNull { it.toDomain() }?.takeIf { it.isNotEmpty() } ?: return null
    return Lesson(
        order = this.order ?: return null,
        toNative = toNative,
        dictionary = this.dictionary ?: return null,
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