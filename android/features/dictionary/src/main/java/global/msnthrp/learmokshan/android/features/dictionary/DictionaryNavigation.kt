package global.msnthrp.learmokshan.android.features.dictionary

import global.msnthrp.mokshan.android.core.navigation.Router
import global.msnthrp.mokshan.android.core.navigation.ScreenFactory

fun DictionaryScreenFactory(
    onAppInfoClicked: () -> Unit,
): ScreenFactory {
    return ScreenFactory {
        DictionaryScreen(
            onAppInfoClicked = onAppInfoClicked,
        )
    }
}

object DictionaryRouter : Router {
    override fun route(): String = "dictionary"
}