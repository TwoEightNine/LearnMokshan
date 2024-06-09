package global.msnthrp.mokshan.android.features.phrasebook

import global.msnthrp.mokshan.android.core.navigation.Router
import global.msnthrp.mokshan.android.core.navigation.ScreenFactory

fun PhrasebookScreenFactory(
    onInfoClicked: () -> Unit,
    onPronunciationArticleClicked: (url: String, title: String) -> Unit,
): ScreenFactory {
    return ScreenFactory {
        PhrasebookScreen(
            onInfoClicked = onInfoClicked,
            onPronunciationArticleClicked = onPronunciationArticleClicked
        )
    }
}

object PhrasebookRouter : Router {
    override fun route(): String = "phrasebook"
}