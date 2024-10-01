package global.msnthrp.mokshan.android.features.appinfo

import global.msnthrp.mokshan.android.core.navigation.Router
import global.msnthrp.mokshan.android.core.navigation.ScreenFactory

fun AppInfoScreenFactory(
    onBackClicked: () -> Unit,
    onArticleClicked: (url: String, title: String) -> Unit,
    onMailClicked: (mailTo: String, subject: String, message: String) -> Unit,
): ScreenFactory {
    return ScreenFactory {
        AppInfoScreen(
            onBackClicked = onBackClicked,
            onArticleClicked = onArticleClicked,
            onMailClicked = onMailClicked,
        )
    }
}

object AppInfoRouter : Router {
    override fun route(): String = "app_info"
}