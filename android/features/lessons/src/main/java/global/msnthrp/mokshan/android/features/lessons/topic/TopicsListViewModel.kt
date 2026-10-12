package global.msnthrp.mokshan.android.features.lessons.topic

import androidx.lifecycle.viewModelScope
import global.msnthrp.mokshan.android.core.arch.BaseViewModel
import global.msnthrp.mokshan.data.network.ServerConfig
import global.msnthrp.mokshan.data.repository.lessons.TopicsRepository
import global.msnthrp.mokshan.domain.lessons.TopicInfo
import global.msnthrp.mokshan.usecase.DeviceLocaleProvider
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

internal class TopicsListViewModel : BaseViewModel<TopicsListState>() {

    private val topicsRepository by inject<TopicsRepository>(TopicsRepository::class.java)
    private val serverConfig by inject<ServerConfig>(ServerConfig::class.java)
    private val deviceLocaleProvider by inject<DeviceLocaleProvider>(DeviceLocaleProvider::class.java)

    override fun getInitialState(): TopicsListState = TopicsListState.Loading

    /** The article file a topic's grammar button opens (`grammar` is the file name from the index). */
    fun getGrammarArticleUrl(topicInfo: TopicInfo): String? {
        val fileName = topicInfo.grammar?.takeIf { it.isNotBlank() } ?: return null
        return serverConfig.getArticlesDirUrl() + fileName
    }

    fun getPronunciationArticleUrl(): String {
        return serverConfig.getPronunciationArticleUrl(deviceLocaleProvider.getDeviceLocale().code)
    }

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