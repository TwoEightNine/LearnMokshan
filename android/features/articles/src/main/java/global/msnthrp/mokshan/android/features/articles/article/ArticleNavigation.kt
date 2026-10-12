package global.msnthrp.mokshan.android.features.articles.article

import global.msnthrp.mokshan.android.core.navigation.Router
import global.msnthrp.mokshan.android.core.navigation.ScreenFactory
import global.msnthrp.mokshan.android.core.navigation.encodeRouteArgument

private const val ARG_URL = "url"
private const val ARG_TITLE = "title"

fun ArticleScreenFactory(
    onBackClicked: () -> Unit,
): ScreenFactory {
    return ScreenFactory { arguments ->
        ArticleScreen(
            articleUrl = arguments?.getString(ARG_URL) ?: return@ScreenFactory,
            onBackClicked = onBackClicked,
            title = arguments.getString(ARG_TITLE).orEmpty(),
        )
    }
}

fun ArticleDefaultRouter() = articleRoute(
    title = "{$ARG_TITLE}",
    url = encodeRouteArgument("{$ARG_URL}"),
)

fun ArticleRouter(
    url: String,
    title: String = "",
) = articleRoute(
    title = encodeRouteArgument(title),
    url = encodeRouteArgument(url),
)

/** Both arguments must already be safe to put into a route. */
private fun articleRoute(title: String, url: String) = Router { "article?title=$title&url=$url" }
