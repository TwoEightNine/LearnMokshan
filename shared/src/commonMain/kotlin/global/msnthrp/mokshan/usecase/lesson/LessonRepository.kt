package global.msnthrp.mokshan.usecase.lesson

import global.msnthrp.mokshan.domain.lessons.Topic
import global.msnthrp.mokshan.domain.lessons.TopicInfo

interface LessonRepository {
    suspend fun getTopic(topicId: Int): Result<Topic>
    suspend fun markLessonAsCompleted(topic: TopicInfo, completedLessonNumber: Int): Result<Unit>
}