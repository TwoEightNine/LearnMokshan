package global.msnthrp.mokshan.usecase.lesson

import global.msnthrp.mokshan.data.repository.dictionary.DictionaryRepository
import global.msnthrp.mokshan.data.repository.lessons.TopicsRepository
import global.msnthrp.mokshan.domain.lessons.PreparedLesson
import global.msnthrp.mokshan.domain.lessons.Topic
import global.msnthrp.mokshan.testutil.FakeDictionaryDao
import global.msnthrp.mokshan.testutil.FakeLocaleProvider
import global.msnthrp.mokshan.testutil.FakeNow
import global.msnthrp.mokshan.testutil.FakeProgressStorage
import global.msnthrp.mokshan.testutil.FakeTopicsNetwork
import global.msnthrp.mokshan.testutil.assertSuccess
import kotlin.random.Random

/** Wires a real [LessonUseCase] over real repositories and in-memory fakes. */
internal class LessonHarness(
    val topic: Topic,
    seed: Int = 42,
) {
    val network = FakeTopicsNetwork().also { it.topics[topic.id] = topic }
    val storage = FakeProgressStorage()
    val dao = FakeDictionaryDao()
    val now = FakeNow(value = 5_000L)

    val topicsRepository = TopicsRepository(network, storage, FakeLocaleProvider())
    val dictionaryRepository = DictionaryRepository(dao, now)
    val useCase = LessonUseCase(
        lessonRepository = topicsRepository,
        dictionaryRepository = dictionaryRepository,
        topicsRepository = topicsRepository,
        random = Random(seed),
    )

    suspend fun prepare(lessonNumber: Int): PreparedLesson =
        useCase.prepareLesson(topic.id, lessonNumber).assertSuccess()
}
