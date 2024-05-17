package global.msnthrp.mokshan.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import asPxToDp
import global.msnthrp.mokshan.Greeting
import global.msnthrp.mokshan.android.core.designsystem.theme.LeMokTheme
import global.msnthrp.mokshan.domain.phrasebook.PhrasebookCollection
import toPx

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
                    NavHost(
                        navController = rememberNavController(),
                        startDestination = "screen",
                    ) {
                        composable(route = "screen") {
                            ScreenView()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScreenView(text: String = Greeting().greet()) {
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Mokshan Phrasebook",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(top = padding.calculateTopPadding())
        ) {
            PhrasebookCollection.phrases.forEach { phrase ->
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 12.dp)
                    ) {
                        Text(
                            text = phrase.mokshanPhrase,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        phrase.translations.forEach { translation ->
                            Text(
                                modifier = Modifier.padding(top = 2.dp),
                                text = translation.value,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(thickness = 1.asPxToDp())
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(padding.calculateBottomPadding())) }
        }
    }
}

@Preview
@Composable
fun ScreenPreview() {
    LeMokTheme {
        ScreenView()
    }
}
