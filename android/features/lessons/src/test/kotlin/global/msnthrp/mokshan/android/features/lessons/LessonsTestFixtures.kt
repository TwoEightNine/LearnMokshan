package global.msnthrp.mokshan.android.features.lessons

import global.msnthrp.mokshan.data.network.ServerConfig
import global.msnthrp.mokshan.data.repository.dictionary.DictionaryRepository
import global.msnthrp.mokshan.data.repository.lessons.TopicsRepository
import global.msnthrp.mokshan.data.storage.dictionary.DictionaryDao
import global.msnthrp.mokshan.data.storage.dictionary.DictionaryEntity
import global.msnthrp.mokshan.domain.lessons.Lesson
import global.msnthrp.mokshan.domain.lessons.LessonPair
import global.msnthrp.mokshan.domain.lessons.Topic
import global.msnthrp.mokshan.domain.lessons.TopicInfo
import global.msnthrp.mokshan.domain.lessons.TopicsProgress
import global.msnthrp.mokshan.domain.lessons.TopicsSummary
import global.msnthrp.mokshan.domain.phrasebook.ForeignLanguage
import global.msnthrp.mokshan.usecase.DeviceLocaleProvider
import global.msnthrp.mokshan.usecase.lesson.LessonUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import kotlin.random.Random

/** Replaces `Dispatchers.Main` so `viewModelScope` runs eagerly in unit tests. */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(UnconfinedTestDispatcher())
    override fun finished(description: Description) = Dispatchers.resetMain()
}

class FakeNetwork(
    var topic: Topic? = null,
    var summary: TopicsSummary? = null,
) : TopicsRepository.NetworkDataSource {
    var error: Throwable? = null
    var gate: CompletableDeferred<Unit>? = null

    override suspend fun getTopicsSummary(language: ForeignLanguage): TopicsSummary {
        gate?.await()
        error?.let { throw it }
        return summary ?: error("no summary")
    }

    override suspend fun getTopic(topicId: Int, language: ForeignLanguage): Topic {
        error?.let { throw it }
        return topic ?: error("no topic")
    }
}

class FakeStorage(
    var progress: TopicsProgress = TopicsProgress(emptyList(), 1, 0),
) : TopicsRepository.StorageDataSource {
    val writes = mutableListOf<Pair<Int, Int>>()
    var writeError: Throwable? = null
    var gate: CompletableDeferred<Unit>? = null

    override suspend fun getTopicsProgress(): TopicsProgress = progress

    override suspend fun setTopicsProgress(topicId: Int, completedLessonNumber: Int) {
        gate?.await()
        writeError?.let { throw it }
        writes += topicId to completedLessonNumber
        progress = TopicsProgress(IntRange(1, topicId.dec()).toList(), topicId, completedLessonNumber)
    }
}

class FakeDao : DictionaryDao {
    val rows = mutableListOf<DictionaryEntity>()
    var error: Throwable? = null

    override suspend fun insert(entity: DictionaryEntity) {
        error?.let { throw it }
        rows += entity
    }

    override suspend fun insertMany(entities: List<DictionaryEntity>) {
        error?.let { throw it }
        rows += entities
    }

    override suspend fun getAll(): List<DictionaryEntity> {
        error?.let { throw it }
        return rows.toList()
    }

    override suspend fun findByMokshan(mokshan: String): List<DictionaryEntity> {
        error?.let { throw it }
        return rows.filter { it.mokshan == mokshan }
    }

    override suspend fun updateByMokshan(mokshan: String, native: List<String>, addedTs: Long) {
        error?.let { throw it }
        rows.replaceAll { if (it.mokshan == mokshan) it.copy(native = native, addedTs = addedTs) else it }
    }
}

class EnglishLocale(var language: ForeignLanguage = ForeignLanguage.ENGLISH) : DeviceLocaleProvider {
    override fun getDeviceLocale() = language
}

class FakeServerConfig : ServerConfig {
    override fun getTopicsIndexUrl(langCode: String) = "https://test/lessons-$langCode/index.json"
    override fun getTopicUrl(langCode: String, topicId: Int) = "https://test/lessons-$langCode/$topicId/lessons.json"
    override fun getArticlesIndexUrl(langCode: String) = "https://test/articles/index-$langCode.json"
    override fun getArticlesDirUrl() = "https://test/articles/"
    override fun getPronunciationArticleUrl(langCode: String) = "https://test/articles/pron-$langCode.json"
    override fun getPhrasebookUrl() = "https://test/phrasebook.json"
}

/** Real use case and repositories wired over in-memory fakes. */
class LessonsEnvironment(
    topic: Topic = sampleTopic(),
    summary: TopicsSummary? = null,
    seed: Int = 1,
) {
    val network = FakeNetwork(topic, summary)
    val storage = FakeStorage()
    val dao = FakeDao()
    val topicsRepository = TopicsRepository(network, storage, EnglishLocale())
    val dictionaryRepository = DictionaryRepository(dao) { 1_000L }
    val useCase = LessonUseCase(topicsRepository, dictionaryRepository, topicsRepository, Random(seed))
}

fun topicInfo(id: Int = 1, lessonsCount: Int = 2) = TopicInfo(
    id = id,
    lessonsCount = lessonsCount,
    title = "Topic $id",
    description = "Description $id",
    emoji = null,
    grammar = null,
)

/** Two lessons: 1-2 word bank, 3 bank summary, 4-5 input, 6 input summary, 7 full review. */
fun sampleTopic(id: Int = 1) = Topic(
    id = id,
    title = "Topic $id",
    description = "Description $id",
    lessons = listOf(
        Lesson(
            order = 1,
            toNative = listOf(
                LessonPair("Шумбрат!", listOf("Hello!")),
                LessonPair("Мон эрян", listOf("I live")),
                LessonPair("Тон эрят", listOf("You live")),
            ),
            dictionary = listOf("Шумбрат!"),
        ),
        Lesson(
            order = 2,
            toNative = listOf(
                LessonPair("Кода тефне?", listOf("How are you?")),
                LessonPair("Лац", listOf("Fine")),
            ),
            dictionary = listOf("Лац"),
        ),
    ),
    translations = emptyList(),
)
