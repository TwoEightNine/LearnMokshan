package global.msnthrp.mokshan.android.features.lessons.topic

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import global.msnthrp.mokshan.android.core.designsystem.theme.Icons
import global.msnthrp.mokshan.android.core.designsystem.theme.LeMokTheme
import global.msnthrp.mokshan.android.core.designsystem.theme.SpecialColors
import global.msnthrp.mokshan.android.core.designsystem.uikit.ArticleCard
import global.msnthrp.mokshan.android.core.designsystem.uikit.LeMokCard
import global.msnthrp.mokshan.android.core.designsystem.uikit.LeMokScreen
import global.msnthrp.mokshan.android.core.utils.setVisibleElseInvisible
import global.msnthrp.mokshan.android.core.utils.stringResource
import global.msnthrp.mokshan.android.features.lessons.R
import global.msnthrp.mokshan.domain.lessons.TopicInfo
import java.util.Locale

private const val PRONUNCIATION_ARTICLE_URL =
    "https://raw.githubusercontent.com/TwoEightNine/LearnMokshan/" +
            "master/content/articles/pronunciation-{locale}.json"

@Composable
internal fun TopicsListScreen(
    topicsListViewModel: TopicsListViewModel = viewModel(),
    onTopicClicked: (TopicInfo, lessonNumber: Int) -> Unit,
    onArticleClicked: (title: String, url: String) -> Unit,
    onAppInfoClicked: () -> Unit,
) {
    val state by topicsListViewModel.state.collectAsState()
    LifecycleResumeEffect(key1 = "load") {
        topicsListViewModel.load()
        onPauseOrDispose {}
    }
    LeMokScreen(
        title = stringResource(id = R.string.lessons_title),
        actions = {
            IconButton(onClick = onAppInfoClicked) {
                Icon(
                    imageVector = Icons.info,
                    contentDescription = "App info",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    ) { padding ->
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
                item {
                    val articleTitle =
                        androidx.compose.ui.res.stringResource(id = R.string.pronunciation_article_title)
                    ArticleCard(
                        title = articleTitle,
                        description = androidx.compose.ui.res.stringResource(id = R.string.pronunciation_article_description),
                        showChevron = true,
                        onClick = {
                            onArticleClicked(
                                articleTitle,
                                PRONUNCIATION_ARTICLE_URL.replace("{locale}", getLocaleForUrl()),
                            )
                        },
                    )
                }

                val topicsWithProgress = state.topics
                topicsWithProgress?.summary?.topicsInfo?.forEach { topicInfo: TopicInfo ->
                    val progress = topicsWithProgress.progress

                    val isCompleted = topicInfo.id in progress.completedTopicIds
                    val isOngoing = topicInfo.id == progress.ongoingTopicId
                    val isTopicActive = isCompleted || isOngoing

                    val lessonsCompletedCount = when {
                        isCompleted -> topicInfo.topicLength
                        isOngoing -> progress.ongoingTopicLessonNumber
                        else -> 0
                    }
                    item {
                        TopicInfoCard(
                            topicInfo = topicInfo,
                            lessonsCompletedCount = lessonsCompletedCount,
                            onClicked = onTopicClicked,
                            isActive = isTopicActive,
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
    isActive: Boolean,
    onClicked: (TopicInfo, lessonNumber: Int) -> Unit,
) {
    val progress = lessonsCompletedCount.toFloat() / topicInfo.topicLength
    val isCompleted = lessonsCompletedCount == topicInfo.topicLength

    val nextLessonNumber = when (lessonsCompletedCount) {
        topicInfo.topicLength -> lessonsCompletedCount
        else -> lessonsCompletedCount.inc()
    }
    LeMokCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        isEnabled = isActive,
        onClicked = { onClicked(topicInfo, nextLessonNumber) },
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(bottom = 5.dp, start = 16.dp, end = 16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = topicInfo.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = topicInfo.description,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(56.dp, 56.dp)
            ) {
                val trackColor = when {
                    isActive -> MaterialTheme.colorScheme.secondaryContainer
                    else -> Color.Transparent
                }
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                    trackColor = trackColor,
                    progress = { progress },
                    strokeCap = StrokeCap.Round,
                )
                when {
                    isCompleted -> {
                        Image(
                            modifier = Modifier.align(Alignment.Center),
                            imageVector = Icons.check,
                            colorFilter = ColorFilter.tint(color = SpecialColors.correctGreen),
                            contentDescription = null
                        )
                    }
                    isActive -> {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = "$lessonsCompletedCount/${topicInfo.topicLength}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    else -> {
                        Image(
                            modifier = Modifier.align(Alignment.Center),
                            imageVector = Icons.lock,
                            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.tertiary),
                            contentDescription = null
                        )
                    }
                }
            }
            Image(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 16.dp, start = 8.dp)
                    .setVisibleElseInvisible(isVisible = isActive),
                imageVector = Icons.chevronRight,
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
            onArticleClicked = { _, _ -> },
            onAppInfoClicked = {},
        )
    }
}

@Preview
@Composable
private fun TopicInfoCardCompletedPreview() {
    LeMokTheme {
        TopicInfoCard(
            topicInfo = TopicInfo(
                id = 6,
                lessonsCount = 2,
                title = "Best topic ever ever ever",
                description = "Common phrases and genitive case",
            ),
            lessonsCompletedCount = 10,
            isActive = true,
            onClicked = { _, _ -> }
        )
    }
}

@Preview
@Composable
private fun TopicInfoCardOngoingPreview() {
    LeMokTheme {
        TopicInfoCard(
            topicInfo = TopicInfo(
                id = 6,
                lessonsCount = 2,
                title = "Best topic ever ever ever",
                description = "Common phrases and genitive case",
            ),
            lessonsCompletedCount = 2,
            isActive = true,
            onClicked = { _, _ -> }
        )
    }
}

@Preview
@Composable
private fun TopicInfoCardInactivePreview() {
    LeMokTheme {
        TopicInfoCard(
            topicInfo = TopicInfo(
                id = 6,
                lessonsCount = 2,
                title = "Best topic ever ever ever",
                description = "Common phrases and genitive case",
            ),
            lessonsCompletedCount = 0,
            isActive = false,
            onClicked = { _, _ -> }
        )
    }
}

private fun getLocaleForUrl(): String {
    val language = Locale.getDefault().language
    val supportedLanguages = setOf("en", "ru")
    return language.takeIf { it in supportedLanguages } ?: "en"
}
