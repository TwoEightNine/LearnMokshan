package global.msnthrp.mokshan.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import global.msnthrp.mokshan.android.core.designsystem.theme.LeMokTheme
import global.msnthrp.mokshan.android.core.navigation.LeMokNavHost
import global.msnthrp.mokshan.android.core.navigation.navigateWith
import global.msnthrp.mokshan.android.features.appinfo.AppInfoRouter
import global.msnthrp.mokshan.android.features.appinfo.AppInfoScreenFactory
import global.msnthrp.mokshan.android.features.articles.ArticleDefaultRouter
import global.msnthrp.mokshan.android.features.articles.ArticleRouter
import global.msnthrp.mokshan.android.features.articles.ArticleScreenFactory
import global.msnthrp.mokshan.android.features.lessons.TopicsListRouter
import global.msnthrp.mokshan.android.features.lessons.TopicsListScreenFactory
import global.msnthrp.mokshan.android.features.phrasebook.PhrasebookRouter
import global.msnthrp.mokshan.android.features.phrasebook.PhrasebookScreenFactory

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
                    LeMokNavHost(
                        navController = navController,
                        startDestinationRouter = PhrasebookRouter,
                        screens = mapOf(
                            PhrasebookRouter to PhrasebookScreenFactory(
                                onInfoClicked = { navController.navigateWith(AppInfoRouter) },
                                onPronunciationArticleClicked = { url, title ->
                                    navController.navigateWith(ArticleRouter(url, title))
                                }
                            ),
                            AppInfoRouter to AppInfoScreenFactory(
                                onBackClicked = { navController.popBackStack() },
                                onArticleClicked = { url, title ->
                                    navController.navigateWith(ArticleRouter(url, title))
                                },
                            ),
                            ArticleDefaultRouter() to ArticleScreenFactory(
                                onBackClicked = { navController.popBackStack() },
                            ),
                            TopicsListRouter to TopicsListScreenFactory(
                                onTopicClicked = {  }
                            )
                        ),
                    )
                }
            }
        }
    }
}


