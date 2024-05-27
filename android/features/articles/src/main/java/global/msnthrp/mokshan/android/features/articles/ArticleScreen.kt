package global.msnthrp.mokshan.android.features.articles

import android.graphics.Typeface
import android.text.Html
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
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
        Column {
            Spacer(
                modifier = Modifier.height(
                    padding.calculateTopPadding()
                )
            )
            state.jart?.also { jart ->
                JartView(
                    jart = jart,
                    onClicked = {},
                )
            }
            Spacer(
                modifier = Modifier.height(
                    padding.calculateBottomPadding()
                )
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