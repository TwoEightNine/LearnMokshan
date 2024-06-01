package global.msnthrp.mokshan.android.core.navigation

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

fun interface Router {
    fun route(): String
}

fun interface ScreenFactory {
    @Composable
    fun Content(arguments: Bundle?)
}

fun NavController.navigateWith(router: Router) = navigate(router.route())
