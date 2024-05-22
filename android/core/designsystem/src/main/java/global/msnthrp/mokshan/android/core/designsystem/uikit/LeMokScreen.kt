package global.msnthrp.mokshan.android.core.designsystem.uikit

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import global.msnthrp.mokshan.android.core.designsystem.theme.Icons

@Composable
fun LeMokScreen(
    title: String,
    onNavigationClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            (CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                },
                navigationIcon = {
                    onNavigationClick?.let { click ->
                        IconButton(onClick = click) {
                            Icon(
                                imageVector = Icons.arrowBack,
                                contentDescription = "Go back",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                },
                actions = actions,
            ))
        },
        content = content,
    )
}