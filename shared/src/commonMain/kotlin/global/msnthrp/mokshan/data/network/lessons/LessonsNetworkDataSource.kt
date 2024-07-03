package global.msnthrp.mokshan.data.network.lessons

import global.msnthrp.mokshan.data.network.base.InvalidEntityException
import global.msnthrp.mokshan.data.network.base.NetworkClient
import global.msnthrp.mokshan.data.network.lessons.response.TopicResponse
import global.msnthrp.mokshan.data.network.lessons.response.TopicsSummaryResponse
import global.msnthrp.mokshan.data.network.lessons.response.toDomain
import global.msnthrp.mokshan.data.repository.lessons.TopicsRepository
import global.msnthrp.mokshan.domain.lessons.Topic
import global.msnthrp.mokshan.domain.lessons.TopicsSummary

internal class LessonsNetworkDataSource(
    private val client: NetworkClient,
) : TopicsRepository.NetworkDataSource {

    override suspend fun getTopicsSummary(): TopicsSummary {
        val response: TopicsSummaryResponse = client.get(TOPICS_INDEX_URL)
        return response.toDomain() ?: throw InvalidEntityException("topics index", TOPICS_INDEX_URL)
    }

    override suspend fun getTopic(topicId: Int): Topic {
        val url = getTopicUrl(topicId)
        val response: TopicResponse = client.get(url)
        return response.toDomain() ?: throw InvalidEntityException("topic", url)
    }

    private fun getTopicUrl(topicId: Int) = TOPIC_URL.replace(TOPIC_ID_PLACEHOLDER, topicId.toString())

    companion object {

        private const val TOPICS_BASE_URL = "https://raw.githubusercontent.com/TwoEightNine/" +
                "LearnMokshan/master/content/lessons"

        private const val TOPICS_INDEX_URL = "$TOPICS_BASE_URL/index.json"
        private const val TOPIC_ID_PLACEHOLDER = "{topicId}"
        private const val TOPIC_URL = "$TOPICS_BASE_URL/$TOPIC_ID_PLACEHOLDER/lessons.json"
    }
}