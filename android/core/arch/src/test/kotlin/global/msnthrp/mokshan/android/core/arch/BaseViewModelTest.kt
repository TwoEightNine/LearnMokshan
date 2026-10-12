package global.msnthrp.mokshan.android.core.arch

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Test

class BaseViewModelTest {

    private data class State(val counter: Int = 0, val label: String = "")

    private class TestViewModel : BaseViewModel<State>() {
        override fun getInitialState() = State()

        val current: State get() = currentState

        fun increment() = updateState { copy(counter = counter + 1) }
        fun label(value: String) = updateState { copy(label = value) }
    }

    @Test
    fun initialState_isTakenFromGetInitialState() {
        assertEquals(State(), TestViewModel().state.value)
    }

    @Test
    fun updateState_replacesStateWithCopy() {
        val viewModel = TestViewModel()

        viewModel.increment()
        viewModel.increment()

        assertEquals(State(counter = 2), viewModel.state.value)
    }

    @Test
    fun updateState_keepsFieldsNotTouchedByTheCopier() {
        val viewModel = TestViewModel()

        viewModel.label("a")
        viewModel.increment()

        assertEquals(State(counter = 1, label = "a"), viewModel.state.value)
    }

    @Test
    fun currentState_alwaysEqualsLatestStateFlowValue() {
        val viewModel = TestViewModel()

        viewModel.increment()

        assertEquals(viewModel.state.value, viewModel.current)
    }

    @Test
    fun stateValuesAreImmutableSnapshots() {
        val viewModel = TestViewModel()
        val before = viewModel.state.value

        viewModel.increment()

        assertEquals(State(), before)
        assertNotSame(before, viewModel.state.value)
    }
}
