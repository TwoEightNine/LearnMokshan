package global.msnthrp.mokshan.android.main

import global.msnthrp.mokshan.android.core.navigation.Router
import global.msnthrp.mokshan.android.core.navigation.ScreenFactory
import global.msnthrp.mokshan.domain.lessons.TopicInfo

fun MainScreenFactory(
    onInfoClicked: () -> Unit,
    onArticleClicked: (title: String, url: String) -> Unit,
    onTopicClicked: (TopicInfo, lessonNumber: Int) -> Unit,
): ScreenFactory {
    return ScreenFactory {
        MainScreen(
            onInfoClicked = onInfoClicked,
            onTopicClicked = onTopicClicked,
            onArticleClicked = onArticleClicked,
        )
    }
}

object MainRouter : Router {
    override fun route(): String = "main"
}