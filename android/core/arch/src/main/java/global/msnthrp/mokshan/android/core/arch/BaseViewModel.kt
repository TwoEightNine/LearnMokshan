package global.msnthrp.mokshan.android.core.arch

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseViewModel<State> : ViewModel() {

    private val _state = MutableStateFlow(getInitialState())
    val state: StateFlow<State>
        get() = _state.asStateFlow()

//    private val _command = MutableStateFlow<Command?>(value = null)
//    val command: Flow<Command>
//        get() = _command.asSharedFlow().mapNotNull { it }

    protected val currentState: State
        get() = _state.value

    abstract fun getInitialState(): State

    protected fun updateState(copier: State.() -> State) {
        _state.value = _state.value.copier()
    }

//    protected fun sendCommand(command: Command) {
//        viewModelScope.launch {
//            _command.emit(command)
//        }
//    }
}