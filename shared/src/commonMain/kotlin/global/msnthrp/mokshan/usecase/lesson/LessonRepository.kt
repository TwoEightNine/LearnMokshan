package global.msnthrp.mokshan.usecase.lesson

import global.msnthrp.mokshan.domain.lessons.Topic

interface LessonRepository {
    suspend fun getTopic(topicId: Int): Result<Topic>
    suspend fun markLessonAsCompleted(topic: Topic, completedLessonNumber: Int): Result<Unit>
}