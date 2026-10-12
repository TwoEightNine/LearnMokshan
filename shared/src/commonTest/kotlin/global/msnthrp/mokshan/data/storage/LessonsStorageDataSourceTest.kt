package global.msnthrp.mokshan.data.storage

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import global.msnthrp.mokshan.data.storage.base.FileProducer
import global.msnthrp.mokshan.data.storage.base.Prefs
import global.msnthrp.mokshan.data.storage.lessons.LessonsStorageDataSource
import global.msnthrp.mokshan.domain.lessons.TopicsProgress
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/** Uses a real DataStore backed by a temporary file. */
class LessonsStorageDataSourceTest {

    private val fs = FileSystem.SYSTEM
    private val dir = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "lemok-storage-test-${Random.nextLong().toULong()}"
    private val progressKey = stringPreferencesKey("progress")

    private val prefs: Prefs by lazy {
        fs.createDirectories(dir)
        Prefs(fileName = "default.preferences_pb", fileProducer = FileProducer { name -> dir / name })
    }
    private val storage by lazy { LessonsStorageDataSource(prefs) }

    @AfterTest
    fun cleanUp() {
        fs.deleteRecursively(dir)
    }

    private suspend fun writeRaw(value: String) {
        prefs.edit { it[progressKey] = value }
    }

    private suspend fun readRaw(): String? = prefs.data.first()[progressKey]

    @Test
    fun missingProgress_defaultsToFirstTopicWithoutCompletedLessons() = runTest {
        assertEquals(
            TopicsProgress(completedTopicIds = emptyList(), ongoingTopicId = 1, ongoingTopicLessonNumber = 0),
            storage.getTopicsProgress(),
        )
    }

    @Test
    fun readingDefaultProgress_doesNotWriteIt() = runTest {
        storage.getTopicsProgress()

        assertNull(readRaw())
    }

    @Test
    fun progress_isStoredAsTopicUnderscoreLesson() = runTest {
        storage.setTopicsProgress(topicId = 4, completedLessonNumber = 9)

        assertEquals("4_9", readRaw())
    }

    @Test
    fun savedProgress_isReadBack_withAllPreviousTopicsCompleted() = runTest {
        storage.setTopicsProgress(topicId = 4, completedLessonNumber = 9)

        assertEquals(TopicsProgress(listOf(1, 2, 3), 4, 9), storage.getTopicsProgress())
    }

    @Test
    fun firstTopic_hasNoCompletedTopics() = runTest {
        storage.setTopicsProgress(1, 3)

        assertEquals(TopicsProgress(emptyList(), 1, 3), storage.getTopicsProgress())
    }

    @Test
    fun laterWrite_overridesEarlierOne() = runTest {
        storage.setTopicsProgress(1, 3)
        storage.setTopicsProgress(2, 0)

        assertEquals(TopicsProgress(listOf(1), 2, 0), storage.getTopicsProgress())
    }

    @Test
    fun progress_isSharedBetweenDataSourcesOverTheSameStore() = runTest {
        storage.setTopicsProgress(3, 2)

        assertEquals(TopicsProgress(listOf(1, 2), 3, 2), LessonsStorageDataSource(prefs).getTopicsProgress())
    }

    @Test
    fun unparsableProgress_failsWithIllegalState() = runTest {
        listOf("garbage", "", "5", "1_", "_5", "a_b").forEach { raw ->
            writeRaw(raw)

            assertFailsWith<IllegalStateException>(raw) { storage.getTopicsProgress() }
        }
    }

    @Test
    fun nonNumericPartsAreSkipped_soSegmentsShift_knownBugB5() = runTest {
        // KNOWN BUG B5: mapNotNull drops bad segments, so the remaining ones move forward
        writeRaw("1_x_3")
        assertEquals(TopicsProgress(emptyList(), 1, 3), storage.getTopicsProgress())

        writeRaw("abc_2_7")
        assertEquals(TopicsProgress(listOf(1), 2, 7), storage.getTopicsProgress())
    }

    @Test
    fun extraSegments_areIgnored() = runTest {
        writeRaw("2_1_99")

        assertEquals(TopicsProgress(listOf(1), 2, 1), storage.getTopicsProgress())
    }

    @Test
    fun nonPositiveTopic_hasNoCompletedTopics() = runTest {
        writeRaw("0_0")
        assertEquals(TopicsProgress(emptyList(), 0, 0), storage.getTopicsProgress())

        writeRaw("-1_2")
        assertEquals(TopicsProgress(emptyList(), -1, 2), storage.getTopicsProgress())
    }
}
