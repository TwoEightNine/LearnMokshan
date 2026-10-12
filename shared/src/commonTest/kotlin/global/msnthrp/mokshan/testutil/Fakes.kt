package global.msnthrp.mokshan.testutil

import global.msnthrp.mokshan.data.network.ServerConfig
import global.msnthrp.mokshan.data.network.base.NetworkClient
import global.msnthrp.mokshan.data.repository.lessons.TopicsRepository
import global.msnthrp.mokshan.data.storage.dictionary.DictionaryDao
import global.msnthrp.mokshan.data.storage.dictionary.DictionaryEntity
import global.msnthrp.mokshan.domain.lessons.Topic
import global.msnthrp.mokshan.domain.lessons.TopicsProgress
import global.msnthrp.mokshan.domain.lessons.TopicsSummary
import global.msnthrp.mokshan.domain.phrasebook.ForeignLanguage
import global.msnthrp.mokshan.usecase.DeviceLocaleProvider
import global.msnthrp.mokshan.utils.NowProvider

class FakeLocaleProvider(var language: ForeignLanguage = ForeignLanguage.ENGLISH) :
    DeviceLocaleProvider {
    override fun getDeviceLocale(): ForeignLanguage = language
}

class FakeNow(var value: Long = 1_000L) : NowProvider {
    override fun getNow(): Long = value
}

class FakeTopicsNetwork : TopicsRepository.NetworkDataSource {
    var summary: TopicsSummary? = null
    val topics = mutableMapOf<Int, Topic>()
    var error: Throwable? = null

    val summaryLanguages = mutableListOf<ForeignLanguage>()
    val topicRequests = mutableListOf<Pair<Int, ForeignLanguage>>()

    override suspend fun getTopicsSummary(language: ForeignLanguage): TopicsSummary {
        summaryLanguages += language
        error?.let { throw it }
        return summary ?: error("summary is not configured")
    }

    override suspend fun getTopic(topicId: Int, language: ForeignLanguage): Topic {
        topicRequests += topicId to language
        error?.let { throw it }
        return topics[topicId] ?: error("topic $topicId is not configured")
    }
}

/** Mimics `LessonsStorageDataSource`: every topic below the ongoing one is considered complete. */
class FakeProgressStorage(
    var progress: TopicsProgress = TopicsProgress(
        completedTopicIds = emptyList(),
        ongoingTopicId = 1,
        ongoingTopicLessonNumber = 0,
    ),
) : TopicsRepository.StorageDataSource {

    var readError: Throwable? = null
    var writeError: Throwable? = null
    val writes = mutableListOf<Pair<Int, Int>>()

    override suspend fun getTopicsProgress(): TopicsProgress {
        readError?.let { throw it }
        return progress
    }

    override suspend fun setTopicsProgress(topicId: Int, completedLessonNumber: Int) {
        writeError?.let { throw it }
        writes += topicId to completedLessonNumber
        progress = TopicsProgress(
            completedTopicIds = IntRange(1, topicId.dec()).toList(),
            ongoingTopicId = topicId,
            ongoingTopicLessonNumber = completedLessonNumber,
        )
    }
}

/** In-memory DAO with the same semantics as the Room one (no unique index on `mokshan`). */
class FakeDictionaryDao : DictionaryDao {

    val rows = mutableListOf<DictionaryEntity>()
    var error: Throwable? = null
    private var nextId = 1

    override suspend fun insert(entity: DictionaryEntity) {
        error?.let { throw it }
        rows += entity.copy(id = nextId++)
    }

    override suspend fun insertMany(entities: List<DictionaryEntity>) {
        error?.let { throw it }
        entities.forEach { rows += it.copy(id = nextId++) }
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
        rows.replaceAll {
            if (it.mokshan == mokshan) it.copy(native = native, addedTs = addedTs) else it
        }
    }
}

class FakeCacheStorage : NetworkClient.CacheStorage {

    private val entries = mutableMapOf<String, String>()
    private val expired = mutableSetOf<String>()
    val saved = mutableListOf<Pair<String, String>>()

    fun put(url: String, response: String, isExpired: Boolean = false) {
        entries[url] = response
        if (isExpired) expired += url else expired -= url
    }

    fun cached(url: String): String? = entries[url]

    override suspend fun saveResponse(url: String, response: String) {
        saved += url to response
        entries[url] = response
        expired -= url
    }

    override suspend fun getResponse(
        url: String,
        ttlMs: Long,
    ): NetworkClient.CacheStorage.CachedResponse? {
        val response = entries[url] ?: return null
        return NetworkClient.CacheStorage.CachedResponse(response, isExpired = url in expired)
    }
}

class FakeServerConfig : ServerConfig {
    override fun getTopicsIndexUrl(langCode: String) = "https://test/lessons-$langCode/index.json"
    override fun getTopicUrl(langCode: String, topicId: Int) =
        "https://test/lessons-$langCode/$topicId/lessons.json"

    override fun getArticlesIndexUrl(langCode: String) = "https://test/articles/index-$langCode.json"
    override fun getArticlesDirUrl() = "https://test/articles/"
    override fun getPronunciationArticleUrl(langCode: String) = "https://test/articles/pronunciation-$langCode.json"
    override fun getPhrasebookUrl() = "https://test/phrasebook.json"
}
