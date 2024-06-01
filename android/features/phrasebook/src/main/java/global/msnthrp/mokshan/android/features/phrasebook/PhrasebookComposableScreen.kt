package global.msnthrp.mokshan.android.features.phrasebook

import global.msnthrp.mokshan.android.core.navigation.Router
import global.msnthrp.mokshan.android.core.navigation.ScreenFactory

fun PhrasebookScreenFactory(onInfoClicked: () -> Unit): ScreenFactory {
    return ScreenFactory {
        PhrasebookScreen(
            onInfoClicked = onInfoClicked
        )
    }
}

object PhrasebookRouter : Router {
    override fun route(): String = "phrasebook"
}