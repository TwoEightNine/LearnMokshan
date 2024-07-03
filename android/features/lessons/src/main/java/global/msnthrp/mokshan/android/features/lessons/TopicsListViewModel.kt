package global.msnthrp.mokshan.android.features.lessons

import androidx.lifecycle.viewModelScope
import global.msnthrp.mokshan.android.core.arch.BaseViewModel
import global.msnthrp.mokshan.data.repository.lessons.TopicsRepository
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

internal class TopicsListViewModel : BaseViewModel<TopicsListState>() {

    private val topicsRepository by inject<TopicsRepository>(TopicsRepository::class.java)

    override fun getInitialState(): TopicsListState = TopicsListState()

    fun load() {
        updateState { copy(isLoading = true) }
        viewModelScope.launch {
            val summaryResult = topicsRepository.getTopicsSummary()
            summaryResult.getOrNull()?.also {
                println(it)
            }
            summaryResult.exceptionOrNull()?.also {
                it.printStackTrace()
            }
            updateState { copy(isLoading = false, summary = summaryResult.getOrNull()) }
        }
    }
}