package global.msnthrp.mokshan.android.features.lessons.topic

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import global.msnthrp.mokshan.android.core.designsystem.theme.LeMokTheme
import global.msnthrp.mokshan.android.core.designsystem.uikit.LeMokCard
import global.msnthrp.mokshan.android.core.designsystem.uikit.LeMokScreen
import global.msnthrp.mokshan.android.core.utils.stringResource
import global.msnthrp.mokshan.android.features.lessons.R
import global.msnthrp.mokshan.domain.lessons.TopicInfo

@Composable
internal fun TopicsListScreen(
    topicsListViewModel: TopicsListViewModel = viewModel(),
    onTopicClicked: (TopicInfo, lessonNumber: Int) -> Unit,
) {
    val state by topicsListViewModel.state.collectAsState()
    LifecycleResumeEffect(key1 = "load") {
        topicsListViewModel.load()
        onPauseOrDispose {}
    }
    LeMokScreen(title = stringResource(id = R.string.lessons_title)) { padding ->
        if (state.isLoading && state.topics == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width(40.dp),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(top = padding.calculateTopPadding())
            ) {
                val topicsWithProgress = state.topics
                topicsWithProgress?.summary?.topicsInfo?.forEach { topicInfo: TopicInfo ->
                    val progress = topicsWithProgress.progress
                    val lessonsCompletedCount = when {
                        topicInfo.id < progress.topicId -> topicInfo.topicLength
                        topicInfo.id == progress.topicId -> progress.lessonNumber
                        else -> 0
                    }
                    item {
                        TopicInfoCard(
                            topicInfo = topicInfo,
                            lessonsCompletedCount = lessonsCompletedCount,
                            onClicked = onTopicClicked,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopicInfoCard(
    topicInfo: TopicInfo,
    lessonsCompletedCount: Int,
    onClicked: (TopicInfo, lessonNumber: Int) -> Unit,
) {
    val progress = lessonsCompletedCount.toFloat() / topicInfo.topicLength
    val nextLessonNumber = when (lessonsCompletedCount) {
        topicInfo.topicLength -> lessonsCompletedCount
        else -> lessonsCompletedCount.inc()
    }
    LeMokCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClicked = { onClicked(topicInfo, nextLessonNumber) },
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(bottom = 5.dp, start = 16.dp, end = 16.dp)
                    .weight(1f),
                text = topicInfo.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(56.dp, 56.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                    progress = { progress },
                    trackColor = MaterialTheme.colorScheme.surfaceDim,
                    strokeCap = StrokeCap.Round,
                )
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = "$lessonsCompletedCount/${topicInfo.topicLength}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Image(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 16.dp, start = 8.dp),
                imageVector = global.msnthrp.mokshan.android.core.designsystem.theme.Icons.chevronRight,
                colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.tertiary),
                contentDescription = null
            )
        }
    }
}

@Preview
@Composable
private fun TopicsListScreenPreview() {
    LeMokTheme {
        TopicsListScreen(
            onTopicClicked = {_, _ -> },
        )
    }
}

@Preview
@Composable
private fun TopicInfoCardPreview() {
    LeMokTheme {
        TopicInfoCard(
            topicInfo = TopicInfo(
                id = 6,
                lessonsCount = 2,
                title = "Best topic ever ever ever"
            ),
            lessonsCompletedCount = 2,
            onClicked = {_, _ -> }
        )
    }
}
