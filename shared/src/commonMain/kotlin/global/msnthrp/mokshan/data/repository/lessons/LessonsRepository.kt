package global.msnthrp.mokshan.data.repository.lessons

import global.msnthrp.mokshan.domain.lessons.Topic

class LessonsRepository(
    private val networkDs: NetworkDataSource,
) {

    suspend fun getTopic(lessonId: Int): Result<Topic> {
        return kotlin.runCatching { networkDs.getTopic(lessonId) }
    }

    interface NetworkDataSource {
        suspend fun getTopic(lessonId: Int): Topic
    }
}