package global.msnthrp.mokshan.android.features.articles.article

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import global.msnthrp.mokshan.android.core.designsystem.theme.LeMokTheme
import global.msnthrp.mokshan.android.core.designsystem.uikit.FailedView
import global.msnthrp.mokshan.android.core.designsystem.uikit.JartView
import global.msnthrp.mokshan.android.core.designsystem.uikit.LeMokScreen
import global.msnthrp.mokshan.android.core.designsystem.uikit.Loader
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
            Article(
                state = state,
                padding = padding,
                onRetryClicked = { articleViewModel.load(articleUrl) },
            )
        }
    }
}

@Composable
private fun Article(
    state: ArticleViewState,
    padding: PaddingValues,
    onRetryClicked: () -> Unit,
) {
    when (state) {
        ArticleViewState.Loading -> Loader()
        ArticleViewState.Failed -> FailedView(onRetryClicked = onRetryClicked)
        is ArticleViewState.Loaded -> {
            JartView(
                jart = state.jart,
                showJartTitle = false,
                bottomPadding = padding.calculateBottomPadding(),
            )
        }
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