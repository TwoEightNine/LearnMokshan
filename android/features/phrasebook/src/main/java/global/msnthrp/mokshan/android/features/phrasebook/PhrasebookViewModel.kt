package global.msnthrp.mokshan.android.features.phrasebook

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import global.msnthrp.mokshan.data.repository.lessons.TopicsRepository
import global.msnthrp.mokshan.domain.phrasebook.Category
import global.msnthrp.mokshan.usecase.phrasebook.PhrasebookUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class PhrasebookViewModel : ViewModel() {

    private val phrasebookUc by inject<PhrasebookUseCase>(PhrasebookUseCase::class.java)
    private val topicsRepository by inject<TopicsRepository>(TopicsRepository::class.java)

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

            val topic = topicsRepository.getTopic(1)
            Log.i("qwer", "${topic.getOrNull()}")
            Log.i("qwer", "${topic.exceptionOrNull()}")

            val topicSummary = topicsRepository.getTopicsSummary()
            Log.i("qwer", "${topicSummary.getOrNull()}")
            Log.i("qwer", "${topicSummary.exceptionOrNull()}")
        }
    }

    fun onCategoryClicked(category: Category) {
        val currentlyVisibleCategory = _state.value.visibleCategory
        val newlyVisibleCategory = when {
            currentlyVisibleCategory == category.id -> null
            else -> category.id
        }
        _state.update { it.copy(visibleCategory = newlyVisibleCategory) }
    }
}