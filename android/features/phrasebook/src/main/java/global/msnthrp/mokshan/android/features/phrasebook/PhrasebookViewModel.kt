package global.msnthrp.mokshan.android.features.phrasebook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import global.msnthrp.mokshan.data.repository.phrasebook.PhrasebookRepositoryImpl
import global.msnthrp.mokshan.usecase.phrasebook.PhrasebookUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class PhrasebookViewModel : ViewModel() {

    private val phrasebookUc by inject<PhrasebookUseCase>(PhrasebookUseCase::class.java)

    private val _state = MutableStateFlow(PhrasebookState())
    val state: StateFlow<PhrasebookState>
        get() = _state.asStateFlow()

    fun load() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            val result = phrasebookUc.loadPhrasebook()
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