package global.msnthrp.mokshan.data.network.lessons

import global.msnthrp.mokshan.data.network.ServerConfig
import global.msnthrp.mokshan.data.network.base.InvalidEntityException
import global.msnthrp.mokshan.data.network.base.NetworkClient
import global.msnthrp.mokshan.data.network.lessons.response.TopicResponse
import global.msnthrp.mokshan.data.network.lessons.response.TopicsSummaryResponse
import global.msnthrp.mokshan.data.network.lessons.response.toDomain
import global.msnthrp.mokshan.data.repository.lessons.TopicsRepository
import global.msnthrp.mokshan.domain.lessons.Topic
import global.msnthrp.mokshan.domain.lessons.TopicsSummary
import global.msnthrp.mokshan.domain.phrasebook.ForeignLanguage

internal class LessonsNetworkDataSource(
    private val serverConfig: ServerConfig,
    private val client: NetworkClient,
) : TopicsRepository.NetworkDataSource {

    override suspend fun getTopicsSummary(language: ForeignLanguage): TopicsSummary {
        val url = serverConfig.getTopicsIndexUrl(language.code)
        val response: TopicsSummaryResponse = client.get(url)
        return response.toDomain() ?: throw InvalidEntityException("topics index", url)
    }

    override suspend fun getTopic(topicId: Int, language: ForeignLanguage): Topic {
        val url = serverConfig.getTopicUrl(language.code, topicId)
        val response: TopicResponse = client.get(url)
        return response.toDomain() ?: throw InvalidEntityException("topic", url)
    }
}