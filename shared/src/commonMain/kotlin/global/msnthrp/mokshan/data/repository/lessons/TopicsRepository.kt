package global.msnthrp.mokshan.data.repository.lessons

import global.msnthrp.mokshan.domain.lessons.Topic
import global.msnthrp.mokshan.domain.lessons.TopicsProgress
import global.msnthrp.mokshan.domain.lessons.TopicsSummary
import global.msnthrp.mokshan.domain.lessons.TopicsSummaryWithProgress
import global.msnthrp.mokshan.domain.phrasebook.ForeignLanguage
import global.msnthrp.mokshan.usecase.DeviceLocaleProvider
import global.msnthrp.mokshan.usecase.lesson.LessonRepository
import global.msnthrp.mokshan.utils.failureOf

class TopicsRepository(
    private val networkDs: NetworkDataSource,
    private val storageDs: StorageDataSource,
    private val deviceLocaleProvider: DeviceLocaleProvider,
) : LessonRepository {

    private val language: ForeignLanguage
        get() = deviceLocaleProvider.getDeviceLocale()

    suspend fun getTopicsSummaryWithProgress(): Result<TopicsSummaryWithProgress> {
        val summaryResult = getTopicsSummary()
        val summary = summaryResult.getOrNull() ?: return Result.failureOf(summaryResult)

        val progressResult = getTopicsProgress()
        val progress = progressResult.getOrNull() ?: return Result.failureOf(progressResult)
        return Result.success(
            TopicsSummaryWithProgress(summary, progress)
        )
    }

    suspend fun isLessonCompleted(topic: Topic): Result<Boolean> {
        val progressResult = getTopicsProgress()
        val isCompleted = progressResult.getOrNull()?.run { topic.id in completedTopicIds }
        return isCompleted?.let { Result.success(it) } ?: Result.failureOf(progressResult)
    }

    override suspend fun markLessonAsCompleted(topic: Topic, completedLessonNumber: Int): Result<Unit> {
        val isLastLessonInTopic = completedLessonNumber == topic.topicLength
        val topicId = when {
            isLastLessonInTopic -> topic.id.inc()
            else -> topic.id
        }
        val lessonNumber = when {
            isLastLessonInTopic -> 0
            else -> completedLessonNumber
        }
        return kotlin.runCatching {
            storageDs.setTopicsProgress(topicId, lessonNumber)
        }
    }

    private suspend fun getTopicsSummary(): Result<TopicsSummary> {
        return kotlin.runCatching { networkDs.getTopicsSummary(language) }
    }

    private suspend fun getTopicsProgress(): Result<TopicsProgress> {
        return kotlin.runCatching { storageDs.getTopicsProgress() }
    }

    override suspend fun getTopic(topicId: Int): Result<Topic> {
        return kotlin.runCatching { networkDs.getTopic(topicId, language) }
    }

    interface NetworkDataSource {
        suspend fun getTopicsSummary(language: ForeignLanguage): TopicsSummary
        suspend fun getTopic(topicId: Int, language: ForeignLanguage): Topic
    }

    interface StorageDataSource {
        suspend fun getTopicsProgress(): TopicsProgress
        suspend fun setTopicsProgress(topicId: Int, completedLessonNumber: Int)
    }
}