package global.msnthrp.learmokshan.android.features.dictionary

import global.msnthrp.mokshan.android.core.navigation.Router
import global.msnthrp.mokshan.android.core.navigation.ScreenFactory

fun DictionaryScreenFactory(): ScreenFactory {
    return ScreenFactory {
        DictionaryScreen()
    }
}

object DictionaryRouter : Router {
    override fun route(): String = "dictionary"
}