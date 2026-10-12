package global.msnthrp.mokshan.data.storage.lessons

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import global.msnthrp.mokshan.data.repository.lessons.TopicsRepository
import global.msnthrp.mokshan.data.storage.base.Prefs
import global.msnthrp.mokshan.domain.lessons.TopicsProgress
import kotlinx.coroutines.flow.first

class LessonsStorageDataSource(
    private val prefs: Prefs,
) : TopicsRepository.StorageDataSource {

    private val progressKey = stringPreferencesKey("progress")

    override suspend fun getTopicsProgress(): TopicsProgress {
        val preferences = prefs.data.first()
        // an unreadable value is treated as "no progress": failing here would lock the user out of
        // the lessons, while the next completed lesson overwrites the broken value
        return preferences[progressKey]?.toProgress() ?: DEFAULT_PROGRESS
    }

    override suspend fun setTopicsProgress(topicId: Int, completedLessonNumber: Int) {
        val string = "${topicId}_$completedLessonNumber"
        prefs.edit { it[progressKey] = string }
    }

    private fun String.toProgress(): TopicsProgress? {
        val parts = split("_")
        val ongoingTopicId = parts.getOrNull(0)?.toIntOrNull()?.takeIf { it >= 1 } ?: return null
        val ongoingTopicLessonNumber = parts.getOrNull(1)?.toIntOrNull()?.takeIf { it >= 0 } ?: return null

        val completedTopicIds = IntRange(1, ongoingTopicId.dec()).toList()
        return TopicsProgress(
            completedTopicIds = completedTopicIds,
            ongoingTopicId = ongoingTopicId,
            ongoingTopicLessonNumber = ongoingTopicLessonNumber
        )
    }

    private companion object {
        val DEFAULT_PROGRESS = TopicsProgress(
            completedTopicIds = emptyList(),
            ongoingTopicId = 1,
            ongoingTopicLessonNumber = 0,
        )
    }
}
