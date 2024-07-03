package global.msnthrp.mokshan.android.features.lessons

import global.msnthrp.mokshan.domain.lessons.TopicsSummary

internal data class TopicsListState(
    val isLoading: Boolean = false,
    val summary: TopicsSummary? = null,
)