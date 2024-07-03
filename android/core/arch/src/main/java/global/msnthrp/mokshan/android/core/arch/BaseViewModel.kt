package global.msnthrp.mokshan.android.core.arch

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseViewModel<State> : ViewModel() {

    private val _state = MutableStateFlow(getInitialState())
    val state: StateFlow<State>
        get() = _state.asStateFlow()

    abstract fun getInitialState(): State

    protected fun updateState(copier: State.() -> State) {
        _state.value = _state.value.copier()
    }
}