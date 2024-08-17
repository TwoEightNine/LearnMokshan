package global.msnthrp.mokshan.android.features.articles.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import asPxToDp
import global.msnthrp.mokshan.android.core.designsystem.theme.LeMokTheme
import global.msnthrp.mokshan.android.core.designsystem.uikit.LeMokScreen
import global.msnthrp.mokshan.android.core.utils.stringResource
import global.msnthrp.mokshan.android.features.articles.R
import global.msnthrp.mokshan.domain.articles.ArticleInfo
import global.msnthrp.mokshan.utils.DateFormatter
import org.koin.androidx.compose.koinViewModel

@Composable
fun ArticlesListScreen(
    onArticleClicked: (title: String, url: String) -> Unit,
    articlesListViewModel: ArticlesListViewModel = koinViewModel()
) {
    val state by articlesListViewModel.state.collectAsState()
    LifecycleResumeEffect(key1 = "load") {
        articlesListViewModel.load()
        onPauseOrDispose {}
    }
    LeMokScreen(title = stringResource(id = R.string.articles_title)) { padding ->
        if (state.isLoading && state.articles == null) {
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
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding())
            ) {
                state.articles?.articles?.forEach { article ->
                    item {
                        ArticleItem(
                            articleInfo = article,
                            onClicked = onArticleClicked,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArticleItem(
    articleInfo: ArticleInfo,
    onClicked: (title: String, url: String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClicked(articleInfo.title, articleInfo.url) }
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp)
    ) {
        Text(
            text = articleInfo.title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = articleInfo.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = articleInfo.categories.joinToString(separator = " • ").uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                modifier = Modifier.align(Alignment.CenterEnd),
                text = DateFormatter("dd MMM yyyy").format(articleInfo.added),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(
            thickness = 1.asPxToDp()
        )
    }
}

@Composable
@Preview
private fun ArticleItemPreview() {
    LeMokTheme {
        ArticleItem(
            articleInfo = ArticleInfo(
                title = "Article title",
                description = "Descrption de description description description description description",
                categories = listOf("News", "Grammar"),
                added = 1711123223232L,
                url = "",
            ),
            onClicked = { _, _ -> }
        )
    }
}