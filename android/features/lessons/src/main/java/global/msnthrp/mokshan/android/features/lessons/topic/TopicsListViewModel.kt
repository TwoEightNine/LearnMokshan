package global.msnthrp.mokshan.android.features.lessons.topic

import androidx.lifecycle.viewModelScope
import global.msnthrp.mokshan.android.core.arch.BaseViewModel
import global.msnthrp.mokshan.data.repository.lessons.TopicsRepository
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

internal class TopicsListViewModel : BaseViewModel<TopicsListState>() {

    private val topicsRepository by inject<TopicsRepository>(TopicsRepository::class.java)

    override fun getInitialState(): TopicsListState = TopicsListState.Loading

    fun load() {
        // no need to make loader blink when content is present
        if (currentState !is TopicsListState.Loaded) {
            updateState { TopicsListState.Loading }
        }
        viewModelScope.launch {
            val summaryResult = topicsRepository.getTopicsSummaryWithProgress()
            val summary = summaryResult.getOrNull()
            summaryResult.getOrNull()?.also {
                println(it)
            }
            summaryResult.exceptionOrNull()?.also {
                it.printStackTrace()
            }
            if (summary == null) {
                updateState { TopicsListState.Failed }
            } else {
                updateState { TopicsListState.Loaded(summary) }
            }
        }
    }
}