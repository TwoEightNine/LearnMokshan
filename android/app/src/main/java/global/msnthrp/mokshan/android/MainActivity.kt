package global.msnthrp.mokshan.android

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import global.msnthrp.mokshan.android.core.designsystem.theme.LeMokTheme
import global.msnthrp.mokshan.android.features.appinfo.AppInfoScreen
import global.msnthrp.mokshan.android.features.articles.ArticleScreen
import global.msnthrp.mokshan.android.features.phrasebook.PhrasebookScreen
import java.net.URLEncoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LeMokTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "phrasebook",
                    ) {
                        composable(route = "phrasebook") {
                            PhrasebookScreen(
                                onInfoClicked = { navController.navigate("app_info") },
                            )
                        }
                        composable(route = "app_info") {
                            AppInfoScreen(
                                onBackClicked = { navController.popBackStack() },
                                onArticleClicked = { url, title ->
                                    navController.navigate(
                                        "article?" +
                                                "title=${URLEncoder.encode(title)}" +
                                                "&url=${URLEncoder.encode(url)}")
                                }
                            )
                        }
                        composable(
                            route = "article?title={title}&url={url}",
                        ) { backStackEntry ->
                            val title = backStackEntry.arguments?.getString("title")
                            val url = backStackEntry.arguments?.getString("url") ?: return@composable

                            ArticleScreen(
                                articleUrl = url,
                                title = title.orEmpty(),
                                onBackClicked = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}


