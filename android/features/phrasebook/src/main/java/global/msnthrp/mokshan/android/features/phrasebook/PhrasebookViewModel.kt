package global.msnthrp.mokshan.android.features.phrasebook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import global.msnthrp.mokshan.data.repository.phrasebook.PhrasebookRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class PhrasebookViewModel : ViewModel() {

    private val repository by inject<PhrasebookRepository>(PhrasebookRepository::class.java)

    private val _state = MutableStateFlow(PhrasebookState())
    val state: StateFlow<PhrasebookState>
        get() = _state.asStateFlow()

    init {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            val result = repository.loadPhrasebook()
            result.getOrNull()?.also {
                println(it)
            }
            result.exceptionOrNull()?.also {
                it.printStackTrace()
            }
            _state.update {
                it.copy(
                    isLoading = false,
                    phrasebook = result.getOrNull()
                )
            }
        }
    }
}