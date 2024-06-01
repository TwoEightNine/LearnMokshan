package global.msnthrp.mokshan.android.features.articles

import global.msnthrp.mokshan.android.core.navigation.Router
import global.msnthrp.mokshan.android.core.navigation.ScreenFactory
import java.net.URLEncoder

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

fun ArticleDefaultRouter() = ArticleRouter("{$ARG_URL}", "{$ARG_TITLE}")

fun ArticleRouter(
    url: String,
    title: String = "",
) = Router { "article?title=${URLEncoder.encode(title)}&url=${URLEncoder.encode(url)}" }