package global.msnthrp.mokshan.data.repository.lessons

import global.msnthrp.mokshan.domain.lessons.Topic
import global.msnthrp.mokshan.domain.lessons.TopicsSummary
import global.msnthrp.mokshan.usecase.lesson.LessonRepository

class TopicsRepository(
    private val networkDs: NetworkDataSource,
) : LessonRepository {

    suspend fun getTopicsSummary(): Result<TopicsSummary> {
        return kotlin.runCatching { networkDs.getTopicsSummary() }
    }

    override suspend fun getTopic(topicId: Int): Result<Topic> {
        return kotlin.runCatching { networkDs.getTopic(topicId) }
    }

    interface NetworkDataSource {
        suspend fun getTopicsSummary(): TopicsSummary
        suspend fun getTopic(topicId: Int): Topic
    }
}