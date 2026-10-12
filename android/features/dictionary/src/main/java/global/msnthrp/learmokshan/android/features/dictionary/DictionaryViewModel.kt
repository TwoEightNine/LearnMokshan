package global.msnthrp.learmokshan.android.features.dictionary

import androidx.lifecycle.viewModelScope
import global.msnthrp.mokshan.android.core.arch.BaseViewModel
import global.msnthrp.mokshan.data.repository.dictionary.DictionaryRepository
import kotlinx.coroutines.launch

class DictionaryViewModel(
    private val dictionaryRepository: DictionaryRepository,
) : BaseViewModel<DictionaryViewState>() {

    override fun getInitialState() = DictionaryViewState()

    fun load() {
        updateState { copy(isLoading = true, isFailed = false) }
        viewModelScope.launch {
            val result = dictionaryRepository.getAllWords()
            result.getOrNull()?.also {
                println(it)
            }
            result.exceptionOrNull()?.also {
                it.printStackTrace()
            }
            updateState {
                // words that are already shown stay on screen when a refresh fails
                val dictionary = result.getOrNull() ?: dictionary
                copy(
                    isLoading = false,
                    dictionary = dictionary,
                    isFailed = result.isFailure && dictionary == null,
                )
            }
        }
    }
}
