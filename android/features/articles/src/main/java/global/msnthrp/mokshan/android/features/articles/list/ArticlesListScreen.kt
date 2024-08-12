package global.msnthrp.mokshan.android.features.articles.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import global.msnthrp.mokshan.android.core.designsystem.uikit.ArticleItem
import global.msnthrp.mokshan.android.core.designsystem.uikit.LeMokScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun ArticlesListScreen(
    articlesListViewModel: ArticlesListViewModel = koinViewModel()
) {
    val state by articlesListViewModel.state.collectAsState()
    LifecycleResumeEffect(key1 = "load") {
        articlesListViewModel.load()
        onPauseOrDispose {}
    }
    LeMokScreen(title = "Articles") { padding ->
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
                            title = article.title,
                            description = article.description,
                            onClick = {}
                        )
                    }
                }
            }
        }
    }
}