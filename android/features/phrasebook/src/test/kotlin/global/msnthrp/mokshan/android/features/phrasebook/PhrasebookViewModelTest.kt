package global.msnthrp.mokshan.android.features.phrasebook

import global.msnthrp.mokshan.domain.phrasebook.Category
import global.msnthrp.mokshan.domain.phrasebook.ForeignLanguage
import global.msnthrp.mokshan.domain.phrasebook.Phrase
import global.msnthrp.mokshan.domain.phrasebook.Phrasebook
import global.msnthrp.mokshan.domain.phrasebook.Translation
import global.msnthrp.mokshan.usecase.DeviceLocaleProvider
import global.msnthrp.mokshan.usecase.phrasebook.PhrasebookRepository
import global.msnthrp.mokshan.usecase.phrasebook.PhrasebookUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

class PhrasebookViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val english = ForeignLanguage.ENGLISH
    private val greetings = Category("greetings", listOf(Translation("Greetings", english)))
    private val food = Category("food", listOf(Translation("Food", english)))

    private var result: Result<Phrasebook> = Result.success(
        Phrasebook(
            version = 1,
            categories = listOf(greetings, food),
            phrases = listOf(
                Phrase("Шумбрат!", listOf(Translation("Hello", english)), greetings),
                Phrase("Кши", listOf(Translation("Bread", english)), food),
            ),
        )
    )

    private fun createViewModel(): PhrasebookViewModel {
        val useCase = PhrasebookUseCase(
            deviceLocaleProvider = object : DeviceLocaleProvider {
                override fun getDeviceLocale() = english
            },
            phrasebookRepository = object : PhrasebookRepository {
                override suspend fun loadPhrasebook() = result
            },
        )
        startKoin { modules(module { single { useCase } }) }
        return PhrasebookViewModel()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun initialState_isIdleAndEmpty() {
        assertEquals(PhrasebookState(), createViewModel().state.value)
    }

    @Test
    fun load_publishesCategorizedPhrasebook() {
        val viewModel = createViewModel()

        viewModel.load()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(listOf("greetings", "food"), state.phrasebook!!.phrases.map { it.category.id })
    }

    @Test
    fun load_failure_leavesNoPhrasebook() {
        result = Result.failure(IllegalStateException("offline"))
        val viewModel = createViewModel()

        viewModel.load()

        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.phrasebook)
    }

    @Test
    fun load_ofEmptyPhrasebook_leavesNoPhrasebook() {
        result = Result.success(Phrasebook(1, emptyList(), emptyList()))
        val viewModel = createViewModel()

        viewModel.load()

        assertNull(viewModel.state.value.phrasebook)
    }

    @Test
    fun categoryClick_expandsCategory() {
        val viewModel = createViewModel()

        viewModel.onCategoryClicked(greetings)

        assertEquals("greetings", viewModel.state.value.visibleCategory)
    }

    @Test
    fun secondClickOnSameCategory_collapsesIt() {
        val viewModel = createViewModel()
        viewModel.onCategoryClicked(greetings)

        viewModel.onCategoryClicked(greetings)

        assertNull(viewModel.state.value.visibleCategory)
    }

    @Test
    fun clickOnAnotherCategory_switchesVisibleCategory() {
        val viewModel = createViewModel()
        viewModel.onCategoryClicked(greetings)

        viewModel.onCategoryClicked(food)

        assertEquals("food", viewModel.state.value.visibleCategory)
    }

    @Test
    fun reload_keepsVisibleCategory() {
        val viewModel = createViewModel()
        viewModel.load()
        viewModel.onCategoryClicked(food)

        viewModel.load()

        assertEquals("food", viewModel.state.value.visibleCategory)
        assertTrue(viewModel.state.value.phrasebook != null)
    }
}
