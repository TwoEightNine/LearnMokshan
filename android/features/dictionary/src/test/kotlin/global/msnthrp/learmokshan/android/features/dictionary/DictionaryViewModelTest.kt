package global.msnthrp.learmokshan.android.features.dictionary

import global.msnthrp.mokshan.data.repository.dictionary.DictionaryRepository
import global.msnthrp.mokshan.data.storage.dictionary.DictionaryDao
import global.msnthrp.mokshan.data.storage.dictionary.DictionaryEntity
import global.msnthrp.mokshan.domain.dictionary.DictionaryEntry
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DictionaryViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private class FakeDao : DictionaryDao {
        val rows = mutableListOf<DictionaryEntity>()
        var error: Throwable? = null
        var gate: CompletableDeferred<Unit>? = null

        override suspend fun insert(entity: DictionaryEntity) {
            rows += entity
        }

        override suspend fun insertMany(entities: List<DictionaryEntity>) {
            rows += entities
        }

        override suspend fun getAll(): List<DictionaryEntity> {
            gate?.await()
            error?.let { throw it }
            return rows.toList()
        }

        override suspend fun findByMokshan(mokshan: String) = rows.filter { it.mokshan == mokshan }

        override suspend fun updateByMokshan(mokshan: String, native: List<String>, addedTs: Long) = Unit
    }

    private val dao = FakeDao()
    private val viewModel = DictionaryViewModel(DictionaryRepository(dao) { 1L })

    private fun row(mokshan: String, vararg native: String) = DictionaryEntity(0, mokshan, native.toList(), 1L)

    @Test
    fun initialState_isIdleWithoutDictionary() {
        assertEquals(DictionaryViewState(isLoading = false, dictionary = null), viewModel.state.value)
    }

    @Test
    fun load_publishesSortedDictionary() {
        dao.rows += row("шумбрат", "hello")
        dao.rows += row("аф", "not")

        viewModel.load()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(
            listOf(DictionaryEntry("аф", listOf("not"), 1L), DictionaryEntry("шумбрат", listOf("hello"), 1L)),
            state.dictionary,
        )
    }

    @Test
    fun load_isLoadingWhileRequestRunning() {
        dao.gate = CompletableDeferred()

        viewModel.load()

        assertTrue(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.dictionary)
        dao.gate!!.complete(Unit)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun load_ofEmptyStorage_givesEmptyDictionary() {
        viewModel.load()

        assertEquals(emptyList<DictionaryEntry>(), viewModel.state.value.dictionary)
    }

    @Test
    fun load_failure_publishesFailedStateInsteadOfEmptyDictionary() {
        dao.error = IllegalStateException("db")

        viewModel.load()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.isFailed)
        assertNull(state.dictionary)
    }

    @Test
    fun successfulLoad_isNotFailed_evenWhenEmpty() {
        viewModel.load()

        assertFalse(viewModel.state.value.isFailed)
        assertEquals(emptyList<DictionaryEntry>(), viewModel.state.value.dictionary)
    }

    @Test
    fun retry_afterFailure_loadsTheDictionary() {
        dao.rows += row("лац", "fine")
        dao.error = IllegalStateException("db")
        viewModel.load()

        dao.error = null
        viewModel.load()

        val state = viewModel.state.value
        assertFalse(state.isFailed)
        assertEquals(listOf("лац"), state.dictionary!!.map { it.mokshan })
    }

    @Test
    fun retry_clearsFailedStateWhileLoading() {
        dao.error = IllegalStateException("db")
        viewModel.load()
        dao.error = null
        dao.gate = CompletableDeferred()

        viewModel.load()

        assertTrue(viewModel.state.value.isLoading)
        assertFalse(viewModel.state.value.isFailed)
        dao.gate!!.complete(Unit)
    }

    @Test
    fun reload_keepsOldDictionaryWhileLoading() {
        dao.rows += row("лац", "fine")
        viewModel.load()
        dao.gate = CompletableDeferred()

        viewModel.load()

        assertTrue(viewModel.state.value.isLoading)
        assertEquals(1, viewModel.state.value.dictionary!!.size)
        dao.gate!!.complete(Unit)
    }

    @Test
    fun failedRefresh_keepsWordsThatAreAlreadyShown() {
        dao.rows += row("лац", "fine")
        viewModel.load()
        dao.error = IllegalStateException("db")

        viewModel.load()

        val state = viewModel.state.value
        assertFalse(state.isFailed)
        assertFalse(state.isLoading)
        assertEquals(listOf("лац"), state.dictionary!!.map { it.mokshan })
    }
}
