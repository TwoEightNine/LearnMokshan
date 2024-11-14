package global.msnthrp.mokshan.android.features.articles.article

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import global.msnthrp.mokshan.android.core.designsystem.theme.Icons
import global.msnthrp.mokshan.android.core.designsystem.theme.LeMokTheme
import global.msnthrp.mokshan.android.core.designsystem.uikit.JartView
import global.msnthrp.mokshan.android.core.designsystem.uikit.LeMokScreen
import global.msnthrp.mokshan.domain.jart.JartEntry

@Composable
fun ArticleScreen(
    articleUrl: String,
    title: String = "",
    articleViewModel: ArticleViewModel = viewModel(),
    onBackClicked: () -> Unit
) {
    val state by articleViewModel.state.collectAsState()

    LifecycleResumeEffect(key1 = "huy") {
        articleViewModel.load(articleUrl)
        onPauseOrDispose {}
    }
    val actualTitle = when {
        title.isNotBlank() -> title
        else -> (state as? ArticleViewState.Loaded)?.jart?.content?.find { it is JartEntry.Title }?.value.orEmpty()
    }

    LeMokScreen(
        title = actualTitle,
        onNavigationClick = onBackClicked,
    ) { padding ->
        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Spacer(
                modifier = Modifier.height(
                    padding.calculateTopPadding()
                )
            )
            when (state) {
                ArticleViewState.Failed -> FailedView(
                    onRetryClicked = { articleViewModel.load(articleUrl) },
                )

                ArticleViewState.Loading -> {
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
                }

                is ArticleViewState.Loaded -> {
                    JartView(
                        jart = (state as ArticleViewState.Loaded).jart,
                        showJartTitle = false,
                        bottomPadding = padding.calculateBottomPadding(),
                    )
                }
            }
        }
    }
}

@Composable
private fun FailedView(
    onRetryClicked: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .width(240.dp)
        ) {
            Text(
                modifier = Modifier,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                text = "Failed to load the article.\nPlease check your internet connection and try again"
            )
            TextButton(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = onRetryClicked,
            ) {
                Text(text = "Retry",)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = Icons.retry,
                    contentDescription = null
                )
            }
        }
    }
}


@Preview
@Composable
private fun FailedPreview() {
    LeMokTheme {
        FailedView(
            onRetryClicked = {},
        )
    }
}

@Preview
@Composable
fun ArticlePreview() {
    LeMokTheme {
        ArticleScreen(
            articleUrl = "",
            onBackClicked = {},
        )
    }
}