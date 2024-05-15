package global.msnthrp.mokshan.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import global.msnthrp.mokshan.Greeting
import global.msnthrp.mokshan.android.core.designsystem.theme.LeMokTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LeMokTheme {
                ContentView(text = Greeting().greet())
            }
        }
    }
}

@Composable
fun ContentView(text: String) {
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            CenterAlignedTopAppBar(
                title = { Text(text = text) }
            )
        },
    ) { padding ->
        NavHost(
            navController = rememberNavController(),
            startDestination = "screen",
        ) {
            composable(
                route = "screen"
            ) {
                Text(text = "AHAHAHHA")
            }
        }
    }
}

@Preview
@Composable
fun DefaultPreview() {
    LeMokTheme {
        ContentView("Hello, Android!")
    }
}
