package global.msnthrp.mokshan.android.features.articles.article

import androidx.compose.foundation.layout.Column
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
import global.msnthrp.mokshan.android.core.designsystem.uikit.JartView
import global.msnthrp.mokshan.android.core.designsystem.uikit.LeMokScreen

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

    LeMokScreen(
        title = title,
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
            state.jart?.also { jart ->
                JartView(
                    jart = jart,
                    showJartTitle = title.isBlank(),
                    bottomPadding = padding.calculateBottomPadding(),
                )
            }
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