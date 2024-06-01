package global.msnthrp.mokshan.android.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun LeMokNavHost(
    navController: NavHostController,
    startDestinationRouter: Router,
    screens: Map<Router, ScreenFactory>,
) {
    NavHost(
        navController = navController,
        startDestination = startDestinationRouter.route(),
    ) {
        screens.forEach { screen ->
            composable(
                route = screen.key.route(),
                content = { entry ->
                    screen.value.Content(
                        arguments = entry.arguments,
                    )
                },
            )
        }
    }
}