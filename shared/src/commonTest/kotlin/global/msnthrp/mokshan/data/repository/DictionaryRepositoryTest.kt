package global.msnthrp.mokshan.data.repository

import global.msnthrp.mokshan.data.repository.dictionary.DictionaryRepository
import global.msnthrp.mokshan.data.storage.dictionary.DictionaryEntity
import global.msnthrp.mokshan.domain.dictionary.DictionaryEntry
import global.msnthrp.mokshan.testutil.FakeDictionaryDao
import global.msnthrp.mokshan.testutil.FakeNow
import global.msnthrp.mokshan.testutil.assertFailure
import global.msnthrp.mokshan.testutil.assertSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DictionaryRepositoryTest {

    private val dao = FakeDictionaryDao()
    private val now = FakeNow(value = 100L)
    private val repository = DictionaryRepository(dao, now)

    private fun row(mokshan: String, vararg native: String, ts: Long = 1L) =
        DictionaryEntity(mokshan = mokshan, native = native.toList(), addedTs = ts)

    // region getAllWords

    @Test
    fun getAllWords_ofEmptyStorage_isEmpty() = runTest {
        assertEquals(emptyList(), repository.getAllWords().assertSuccess())
    }

    @Test
    fun getAllWords_isSortedByMokshan() = runTest {
        dao.insert(row("шумбрат", "hello"))
        dao.insert(row("аф", "not"))
        dao.insert(row("лац", "fine"))

        assertEquals(listOf("аф", "лац", "шумбрат"), repository.getAllWords().assertSuccess().map { it.mokshan })
    }

    @Test
    fun getAllWords_keepsFirstRowOfDuplicatedMokshan() = runTest {
        dao.insert(row("лац", "fine", ts = 1))
        dao.insert(row("лац", "ok", ts = 2))

        val words = repository.getAllWords().assertSuccess()

        assertEquals(listOf(DictionaryEntry("лац", listOf("fine"), 1)), words)
    }

    @Test
    fun getAllWords_mapsAllFieldsToDomain() = runTest {
        dao.insert(row("лац", "fine", "ok", ts = 77))

        assertEquals(
            DictionaryEntry(mokshan = "лац", native = listOf("fine", "ok"), addedTs = 77),
            repository.getAllWords().assertSuccess().single(),
        )
    }

    @Test
    fun getAllWords_sortingIsCaseSensitive() = runTest {
        dao.insert(row("б", "b"))
        dao.insert(row("Б", "B"))
        dao.insert(row("а", "a"))

        // uppercase Cyrillic sorts before lowercase: pins current (code point) ordering
        assertEquals(listOf("Б", "а", "б"), repository.getAllWords().assertSuccess().map { it.mokshan })
    }

    @Test
    fun getAllWords_wrapsStorageFailure() = runTest {
        val boom = IllegalStateException("db")
        dao.error = boom

        assertEquals(boom, repository.getAllWords().assertFailure())
    }

    // endregion

    // region addNewWords

    @Test
    fun addNewWords_stampsEntriesWithCurrentTime() = runTest {
        repository.addNewWords(listOf(DictionaryEntry("лац", listOf("fine"), addedTs = 0))).assertSuccess()

        assertEquals(listOf(100L), repository.getAllWords().assertSuccess().map { it.addedTs })
    }

    @Test
    fun addNewWords_ofSeveralEntries_makesAllOfThemVisible() = runTest {
        repository.addNewWords(
            listOf(
                DictionaryEntry("лац", listOf("fine")),
                DictionaryEntry("аф", listOf("not")),
            )
        ).assertSuccess()

        assertEquals(listOf("аф", "лац"), repository.getAllWords().assertSuccess().map { it.mokshan })
    }

    @Test
    fun addNewWords_ofEmptyList_isSuccessAndStoresNothing() = runTest {
        repository.addNewWords(emptyList()).assertSuccess()

        assertEquals(emptyList(), dao.rows)
    }

    @Test
    fun addNewWords_mergesNativesOfExistingWordWithoutDuplicates() = runTest {
        dao.insert(row("лац", "fine", "good", ts = 1))

        repository.addNewWords(listOf(DictionaryEntry("лац", listOf("good", "ok")))).assertSuccess()

        val word = repository.getAllWords().assertSuccess().single()
        assertEquals(listOf("fine", "good", "ok"), word.native)
        assertEquals(100L, word.addedTs)
    }

    @Test
    fun addNewWords_withSameWordTwiceInOneBatch_mergesIntoOneVisibleWord() = runTest {
        repository.addNewWords(
            listOf(
                DictionaryEntry("лац", listOf("fine")),
                DictionaryEntry("лац", listOf("ok")),
            )
        ).assertSuccess()

        assertEquals(listOf("fine", "ok"), repository.getAllWords().assertSuccess().single().native)
    }

    @Test
    fun addNewWords_updatesTimestampOfAlreadyKnownWord() = runTest {
        repository.addNewWords(listOf(DictionaryEntry("лац", listOf("fine")))).assertSuccess()
        now.value = 200L

        repository.addNewWords(listOf(DictionaryEntry("лац", listOf("fine")))).assertSuccess()

        assertEquals(200L, repository.getAllWords().assertSuccess().single().addedTs)
    }

    @Test
    fun addNewWords_storesOneRowPerWord() = runTest {
        repository.addNewWords(
            listOf(DictionaryEntry("лац", listOf("fine")), DictionaryEntry("аф", listOf("not")))
        ).assertSuccess()

        assertEquals(2, dao.rows.size)
    }

    @Test
    fun addNewWords_repeatedWithSameEntries_doesNotGrowStorage() = runTest {
        val entries = listOf(DictionaryEntry("лац", listOf("fine")))
        repository.addNewWords(entries).assertSuccess()

        repository.addNewWords(entries).assertSuccess()
        repository.addNewWords(entries).assertSuccess()

        assertEquals(1, dao.rows.size)
        assertEquals(listOf("fine"), dao.rows.single().native)
    }

    @Test
    fun addNewWords_ofKnownWord_updatesItsRowInsteadOfAddingOne() = runTest {
        dao.insert(row("лац", "fine", ts = 1))

        repository.addNewWords(listOf(DictionaryEntry("лац", listOf("ok")))).assertSuccess()

        assertEquals(1, dao.rows.size)
        assertEquals(listOf("fine", "ok"), dao.rows.single().native)
    }

    @Test
    fun addNewWords_withSameWordTwiceInOneBatch_storesOneMergedRow() = runTest {
        repository.addNewWords(
            listOf(
                DictionaryEntry("лац", listOf("fine", "ok")),
                DictionaryEntry("лац", listOf("ok", "good")),
            )
        ).assertSuccess()

        assertEquals(1, dao.rows.size)
        assertEquals(listOf("fine", "ok", "good"), dao.rows.single().native)
    }

    @Test
    fun getAllWords_stillCopesWithDuplicateRowsWrittenByOlderVersions() = runTest {
        dao.insert(row("лац", "fine", ts = 1))
        dao.insert(row("лац", "fine", ts = 1))

        assertEquals(1, repository.getAllWords().assertSuccess().size)
    }

    @Test
    fun addNewWords_wrapsStorageFailure() = runTest {
        val boom = IllegalStateException("db")
        dao.error = boom

        assertEquals(boom, repository.addNewWords(listOf(DictionaryEntry("лац", listOf("fine")))).assertFailure())
    }

    @Test
    fun addNewWords_doesNotNormaliseCase() = runTest {
        repository.addNewWords(
            listOf(DictionaryEntry("Лац", listOf("fine")), DictionaryEntry("лац", listOf("ok")))
        ).assertSuccess()

        assertEquals(2, repository.getAllWords().assertSuccess().size)
    }

    // endregion

    @Test
    fun cancellation_isRethrown_notWrappedInResult() = runTest {
        dao.error = CancellationException("scope is cancelled")

        assertFailsWith<CancellationException> { repository.getAllWords() }
        assertFailsWith<CancellationException> {
            repository.addNewWords(listOf(DictionaryEntry("лац", listOf("fine"))))
        }
    }
}
