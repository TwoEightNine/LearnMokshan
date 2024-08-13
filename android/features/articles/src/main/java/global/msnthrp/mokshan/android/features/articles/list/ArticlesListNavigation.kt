package global.msnthrp.mokshan.android.features.articles.list

import global.msnthrp.mokshan.android.core.navigation.ScreenFactory

fun ArticlesListFactory(
    onArticleClicked: (title: String, url: String) -> Unit,
): ScreenFactory {
    return ScreenFactory {
        ArticlesListScreen(
            onArticleClicked = onArticleClicked,
        )
    }
}