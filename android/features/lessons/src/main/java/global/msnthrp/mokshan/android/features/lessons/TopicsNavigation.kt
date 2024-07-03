package global.msnthrp.mokshan.android.features.lessons

import global.msnthrp.mokshan.android.core.navigation.Router
import global.msnthrp.mokshan.android.core.navigation.ScreenFactory
import global.msnthrp.mokshan.domain.lessons.TopicInfo

fun TopicsListScreenFactory(
    onTopicClicked: (TopicInfo) -> Unit,
): ScreenFactory {
    return ScreenFactory {
        TopicsListScreen(
            onTopicClicked = onTopicClicked,
        )
    }
}

object TopicsListRouter : Router {
    override fun route(): String = "topicsList"
}