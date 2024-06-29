package global.msnthrp.mokshan.data.network.lessons.response

import kotlinx.serialization.Serializable

@Serializable
internal data class TopicResponse(
    val id: Int?,
    val title: String?,
    val version: Int?,
    val lessons: List<LessonResponse>?,
    val translations: List<TranslationResponse>?,
)

@Serializable
data class LessonResponse(
    val order: Int?,
    val toNative: List<LessonPairResponse>?,
    val dictionary: List<String>?,
)

@Serializable
data class LessonPairResponse(
    val mokshan: String?,
    val native: List<String>?,
)

@Serializable
data class TranslationResponse(
    val mokshan: String?,
    val native: String?,
)