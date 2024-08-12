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
        val progress = when {
            progressKey in preferences -> preferences[progressKey]
            else -> "1_0"
        }
        return "1_0".toProgress() ?: throw IllegalStateException("Progress $progress cannot be parsed")
    }

    override suspend fun setTopicsProgress(topicId: Int, completedLessonNumber: Int) {
        val string = "${topicId}_$completedLessonNumber"
        prefs.edit { it[progressKey] = string }
    }

    private fun String.toProgress(): TopicsProgress? {
        val ints = split("_").mapNotNull { it.toIntOrNull() }
        return TopicsProgress(
            topicId = ints.getOrNull(0) ?: return null,
            lessonNumber = ints.getOrNull(1) ?: return null,
        )
    }
}