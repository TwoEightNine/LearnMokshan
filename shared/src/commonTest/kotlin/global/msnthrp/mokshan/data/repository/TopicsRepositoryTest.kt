package global.msnthrp.mokshan.data.repository

import global.msnthrp.mokshan.data.repository.lessons.TopicsRepository
import global.msnthrp.mokshan.domain.lessons.TopicInfo
import global.msnthrp.mokshan.domain.lessons.TopicsProgress
import global.msnthrp.mokshan.domain.lessons.TopicsSummary
import global.msnthrp.mokshan.domain.phrasebook.ForeignLanguage
import global.msnthrp.mokshan.testutil.FakeLocaleProvider
import global.msnthrp.mokshan.testutil.FakeProgressStorage
import global.msnthrp.mokshan.testutil.FakeTopicsNetwork
import global.msnthrp.mokshan.testutil.assertFailure
import global.msnthrp.mokshan.testutil.assertSuccess
import global.msnthrp.mokshan.testutil.sampleTopic
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TopicsRepositoryTest {

    private val network = FakeTopicsNetwork()
    private val storage = FakeProgressStorage()
    private val locale = FakeLocaleProvider()
    private val repository = TopicsRepository(network, storage, locale)

    private val summary = TopicsSummary(
        topicsCount = 2,
        topicsInfo = listOf(
            TopicInfo(1, 2, "One", "d", null, null),
            TopicInfo(2, 3, "Two", "d", "✋", "grammar.prelinar"),
        ),
    )

    // region summary with progress

    @Test
    fun summaryWithProgress_combinesBothSources() = runTest {
        network.summary = summary
        storage.progress = TopicsProgress(listOf(1), 2, 4)

        val result = repository.getTopicsSummaryWithProgress().assertSuccess()

        assertEquals(summary, result.summary)
        assertEquals(TopicsProgress(listOf(1), 2, 4), result.progress)
    }

    @Test
    fun summaryWithProgress_failsWhenSummaryFails() = runTest {
        val boom = IllegalStateException("offline")
        network.error = boom

        assertEquals(boom, repository.getTopicsSummaryWithProgress().assertFailure())
    }

    @Test
    fun summaryWithProgress_failsWhenProgressFails() = runTest {
        network.summary = summary
        val boom = IllegalStateException("corrupt")
        storage.readError = boom

        assertEquals(boom, repository.getTopicsSummaryWithProgress().assertFailure())
    }

    @Test
    fun summaryWithProgress_doesNotReadProgressWhenSummaryFails() = runTest {
        network.error = IllegalStateException("offline")
        storage.readError = IllegalStateException("must not be read")

        assertTrue(repository.getTopicsSummaryWithProgress().exceptionOrNull()!!.message == "offline")
    }

    // endregion

    // region language

    @Test
    fun requests_useCurrentDeviceLanguage() = runTest {
        network.summary = summary
        network.topics[1] = sampleTopic(1)

        repository.getTopicsSummaryWithProgress()
        repository.getTopic(1)
        locale.language = ForeignLanguage.RUSSIAN
        repository.getTopicsSummaryWithProgress()
        repository.getTopic(1)

        assertEquals(listOf(ForeignLanguage.ENGLISH, ForeignLanguage.RUSSIAN), network.summaryLanguages)
        assertEquals(
            listOf(1 to ForeignLanguage.ENGLISH, 1 to ForeignLanguage.RUSSIAN),
            network.topicRequests,
        )
    }

    // endregion

    // region getTopic

    @Test
    fun getTopic_returnsTopicFromNetwork() = runTest {
        network.topics[3] = sampleTopic(3)

        assertEquals(sampleTopic(3), repository.getTopic(3).assertSuccess())
    }

    @Test
    fun getTopic_wrapsFailure() = runTest {
        val boom = IllegalStateException("404")
        network.error = boom

        assertEquals(boom, repository.getTopic(3).assertFailure())
    }

    @Test
    fun getTopic_doesNotVerifyReturnedTopicId() = runTest {
        // pins current behaviour (B12): the repository trusts the id inside the downloaded file
        network.topics[3] = sampleTopic(id = 9)

        assertEquals(9, repository.getTopic(3).assertSuccess().id)
    }

    // endregion

    // region isLessonCompleted

    @Test
    fun isLessonCompleted_isTrueOnlyForTopicsBelowOngoing() = runTest {
        storage.progress = TopicsProgress(listOf(1, 2), 3, 1)

        assertTrue(repository.isLessonCompleted(sampleTopic(1)).assertSuccess())
        assertTrue(repository.isLessonCompleted(sampleTopic(2)).assertSuccess())
        assertFalse(repository.isLessonCompleted(sampleTopic(3)).assertSuccess())
        assertFalse(repository.isLessonCompleted(sampleTopic(4)).assertSuccess())
    }

    @Test
    fun isLessonCompleted_propagatesProgressFailure() = runTest {
        val boom = IllegalStateException("corrupt")
        storage.readError = boom

        assertEquals(boom, repository.isLessonCompleted(sampleTopic(1)).assertFailure())
    }

    // endregion

    // region markLessonAsCompleted

    @Test
    fun markLessonAsCompleted_storesTopicAndLessonNumber() = runTest {
        repository.markLessonAsCompleted(sampleTopic(2), completedLessonNumber = 4).assertSuccess()

        assertEquals(listOf(2 to 4), storage.writes)
    }

    @Test
    fun markLessonAsCompleted_ofLastLesson_advancesToNextTopic() = runTest {
        val topic = sampleTopic(2) // topic length 7

        repository.markLessonAsCompleted(topic, completedLessonNumber = 7).assertSuccess()

        assertEquals(listOf(3 to 0), storage.writes)
    }

    @Test
    fun markLessonAsCompleted_ofLessonBeforeLast_staysInTopic() = runTest {
        repository.markLessonAsCompleted(sampleTopic(2), completedLessonNumber = 6).assertSuccess()

        assertEquals(listOf(2 to 6), storage.writes)
    }

    @Test
    fun markLessonAsCompleted_wrapsStorageFailure() = runTest {
        val boom = IllegalStateException("disk")
        storage.writeError = boom

        assertEquals(boom, repository.markLessonAsCompleted(sampleTopic(1), 1).assertFailure())
    }

    @Test
    fun markLessonAsCompleted_doesNotGuardAgainstMovingBackwards() = runTest {
        // pins current behaviour (B10)
        storage.progress = TopicsProgress(emptyList(), 1, 5)

        repository.markLessonAsCompleted(sampleTopic(1), 2).assertSuccess()

        assertEquals(2, storage.progress.ongoingTopicLessonNumber)
    }

    // endregion
}
