package global.msnthrp.mokshan.data.network.lessons

import global.msnthrp.mokshan.data.network.base.InvalidEntityException
import global.msnthrp.mokshan.data.network.base.NetworkClient
import global.msnthrp.mokshan.data.repository.lessons.LessonsRepository
import global.msnthrp.mokshan.domain.lessons.Topic

internal class LessonsNetworkDataSource(
    private val client: NetworkClient,
) : LessonsRepository.NetworkDataSource {

    override suspend fun getTopic(lessonId: Int): Topic {
        val url = getLessonUrl(lessonId)
        val response: TopicResponse = client.get(url)
        return response.toDomain() ?: throw InvalidEntityException("lesson", url)
    }

    private fun getLessonUrl(lessonId: Int) = LESSON_URL.replace(LESSON_ID_PLACEHOLDER, lessonId.toString())

    companion object {
        private const val LESSON_ID_PLACEHOLDER = "{lessonId}"
        private const val LESSON_URL = "https://raw.githubusercontent.com/TwoEightNine/LearnMokshan/master/content" +
                "/lessons/$LESSON_ID_PLACEHOLDER/lessons.json"
    }
}