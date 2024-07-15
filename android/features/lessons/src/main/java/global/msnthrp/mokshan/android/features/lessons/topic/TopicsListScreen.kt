package global.msnthrp.mokshan.android.features.lessons.topic

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
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
    onTopicClicked: (TopicInfo) -> Unit,
) {
    val state by topicsListViewModel.state.collectAsState()
    LifecycleResumeEffect(key1 = "load") {
        topicsListViewModel.load()
        onPauseOrDispose {}
    }
    LeMokScreen(title = stringResource(id = R.string.lessons_title)) { padding ->
        if (state.isLoading && state.summary == null) {
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
                state.summary?.topicsInfo?.forEach { topicInfo: TopicInfo ->
                    item {
                        TopicInfoCard(
                            topicInfo = topicInfo,
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
    onClicked: (TopicInfo) -> Unit,
) {
    LeMokCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClicked = { onClicked(topicInfo) },
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = topicInfo.title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
            )
            Image(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(all = 16.dp),
                imageVector = global.msnthrp.mokshan.android.core.designsystem.theme.Icons.chevronRight,
                colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.tertiary),
                contentDescription = null
            )
        }
        LinearProgressIndicator(
            progress = { 50f },
            strokeCap = StrokeCap.Round,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Preview
@Composable
private fun TopicsListScreenPreview() {
    LeMokTheme {
        TopicsListScreen(
            onTopicClicked = {},
        )
    }
}

@Preview
@Composable
private fun TopicInfoCardPreview() {
    LeMokTheme {
        TopicInfoCard(
            topicInfo = TopicInfo(id = 6, lessonsCount = 4, title = "Best topic ever"),
            onClicked = {}
        )
    }
}
