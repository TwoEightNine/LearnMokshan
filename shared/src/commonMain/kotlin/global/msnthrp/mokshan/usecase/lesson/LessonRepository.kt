package global.msnthrp.mokshan.usecase.lesson

import global.msnthrp.mokshan.domain.lessons.Topic

interface LessonRepository {
    suspend fun getTopic(topicId: Int): Result<Topic>
}