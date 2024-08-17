package global.msnthrp.mokshan.android.features.lessons.topic

import global.msnthrp.mokshan.android.core.navigation.Router
import global.msnthrp.mokshan.android.core.navigation.ScreenFactory
import global.msnthrp.mokshan.domain.lessons.TopicInfo

fun TopicsListScreenFactory(
    onTopicClicked: (TopicInfo, lessonNumber: Int) -> Unit,
    onArticleClicked: (title: String, url: String) -> Unit,
    onAppInfoClicked: () -> Unit,
): ScreenFactory {
    return ScreenFactory {
        TopicsListScreen(
            onTopicClicked = onTopicClicked,
            onArticleClicked = onArticleClicked,
            onAppInfoClicked = onAppInfoClicked,
        )
    }
}

object TopicsListRouter : Router {
    override fun route(): String = "topicsList"
}