package global.msnthrp.mokshan.android.features.lessons.topic

import global.msnthrp.mokshan.domain.lessons.TopicsSummaryWithProgress

sealed interface TopicsListState {
    data class Loaded(val topics: TopicsSummaryWithProgress) : TopicsListState
    data object Loading : TopicsListState
    data object Failed : TopicsListState
}