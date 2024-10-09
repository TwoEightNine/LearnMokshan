package global.msnthrp.mokshan.android.features.lessons.topic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
import global.msnthrp.mokshan.android.core.utils.stringResource
import global.msnthrp.mokshan.android.features.lessons.R
import global.msnthrp.mokshan.domain.lessons.TopicInfo
import java.util.Locale

private const val ARTICLES_URL =
    "https://raw.githubusercontent.com/TwoEightNine/LearnMokshan/" +
            "master/content/articles/"
private const val PRONUNCIATION_ARTICLE_URL = "${ARTICLES_URL}pronunciation-{locale}.json"

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
                            isActive = isTopicActive,
                            onGrammarClicked = { onArticleClicked("", ARTICLES_URL + it.grammar) },
                            onStartLessonClicked = onTopicClicked,
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
    onGrammarClicked: (TopicInfo) -> Unit,
    onStartLessonClicked: (TopicInfo, lessonNumber: Int) -> Unit,
) {
    val isCompleted = lessonsCompletedCount == topicInfo.topicLength

    val nextLessonNumber = when (lessonsCompletedCount) {
        topicInfo.topicLength -> lessonsCompletedCount
        else -> lessonsCompletedCount.inc()
    }
    LeMokCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        isEnabled = isActive,
        onClicked = { onStartLessonClicked(topicInfo, nextLessonNumber) },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
        ) {

            // circle, emoji
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                        shape = CircleShape
                    ),
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = topicInfo.emoji ?: topicInfo.title.first().toString().uppercase(),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.headlineSmall,
                )
            }

            // title, description
            Column(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .align(Alignment.Top)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = topicInfo.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    val imageVector = when {
                        isCompleted -> Icons.check
                        !isActive -> Icons.lock
                        else -> null
                    }
                    val tint = when (imageVector) {
                        Icons.check -> SpecialColors.correctGreen
                        else -> LocalContentColor.current
                    }
                    if (imageVector != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            modifier = Modifier
                                .size(20.dp)
                                .padding(top = 4.dp)
                                .align(Alignment.CenterVertically),
                            imageVector = imageVector,
                            tint = tint,
                            contentDescription = ""
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = topicInfo.description,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }

        if (isCompleted || isActive) {
            val hasGrammar = topicInfo.grammar.isNullOrBlank().not()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .padding(start = 16.dp, end = 12.dp)
            ) {
                if (hasGrammar) {
                    TextButton(
                        modifier = Modifier
                            .align(Alignment.CenterStart),
                        onClick = { onGrammarClicked(topicInfo) }
                    ) {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            imageVector = Icons.study,
                            contentDescription = ""
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Grammar")

                    }
                }
                TextButton(
                    modifier = Modifier
                        .align(Alignment.CenterEnd),
                    onClick = { onStartLessonClicked(topicInfo, nextLessonNumber) }
                ) {
                    val text = when {
                        isCompleted -> "Review"
                        else -> "Start ${lessonsCompletedCount.inc()}/${topicInfo.topicLength}"
                    }
                    Text(text = text)
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = Icons.chevronRight,
                        contentDescription = ""
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview
@Composable
private fun TopicsListScreenPreview() {
    LeMokTheme {
        TopicsListScreen(
            onTopicClicked = { _, _ -> },
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
                emoji = "✋",
                grammar = null,
            ),
            lessonsCompletedCount = 10,
            isActive = true,
            onStartLessonClicked = { _, _ -> },
            onGrammarClicked = {},
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
                emoji = null,
                grammar = "something",
            ),
            lessonsCompletedCount = 2,
            isActive = true,
            onStartLessonClicked = { _, _ -> },
            onGrammarClicked = {},
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
                emoji = "✋",
                grammar = "somthing",
            ),
            lessonsCompletedCount = 0,
            isActive = false,
            onStartLessonClicked = { _, _ -> },
            onGrammarClicked = {},
        )
    }
}

private fun getLocaleForUrl(): String {
    val language = Locale.getDefault().language
    val supportedLanguages = setOf("en", "ru")
    return language.takeIf { it in supportedLanguages } ?: "en"
}
