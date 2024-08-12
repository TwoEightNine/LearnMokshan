package global.msnthrp.mokshan.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import global.msnthrp.mokshan.android.core.designsystem.theme.LeMokTheme
import global.msnthrp.mokshan.android.core.navigation.LeMokNavHost
import global.msnthrp.mokshan.android.core.navigation.navigateWith
import global.msnthrp.mokshan.android.features.appinfo.AppInfoRouter
import global.msnthrp.mokshan.android.features.appinfo.AppInfoScreenFactory
import global.msnthrp.mokshan.android.features.articles.article.ArticleDefaultRouter
import global.msnthrp.mokshan.android.features.articles.article.ArticleRouter
import global.msnthrp.mokshan.android.features.articles.article.ArticleScreenFactory
import global.msnthrp.mokshan.android.features.lessons.lesson.LessonRouter
import global.msnthrp.mokshan.android.features.lessons.lesson.LessonRouterDefault
import global.msnthrp.mokshan.android.features.lessons.lesson.LessonScreenFactory
import global.msnthrp.mokshan.android.main.MainRouter
import global.msnthrp.mokshan.android.main.MainScreenFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LeMokTheme {
                MainContent()
            }
        }
    }
}

@Composable
private fun MainContent() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val navController = rememberNavController()
        LeMokNavHost(
            navController = navController,
            startDestinationRouter = MainRouter,
            screens = mapOf(
                MainRouter to MainScreenFactory(
                    onInfoClicked = { navController.navigateWith(AppInfoRouter) },
                    onPronunciationArticleClicked = { url, title ->
                        navController.navigateWith(ArticleRouter(url, title))
                    },
                    onTopicClicked = { topicInfo, lessonNumber ->
                        navController.navigateWith(LessonRouter(topicInfo, lessonNumber))
                    },
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
                LessonRouterDefault() to LessonScreenFactory(
                    onBackPressed = { navController.popBackStack() },
                )
            ),
        )
    }
}

@Preview
@Composable
private fun MainScreenPreview() {
    LeMokTheme {
        MainContent()
    }
}


