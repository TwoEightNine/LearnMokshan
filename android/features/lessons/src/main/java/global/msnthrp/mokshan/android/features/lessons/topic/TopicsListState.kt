package global.msnthrp.mokshan.android.features.lessons.topic

import global.msnthrp.mokshan.domain.lessons.TopicsSummaryWithProgress

internal data class TopicsListState(
    val isLoading: Boolean = false,
    val topics: TopicsSummaryWithProgress? = null,
)