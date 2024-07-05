package global.msnthrp.mokshan.android.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import global.msnthrp.mokshan.android.core.designsystem.theme.LeMokTheme
import global.msnthrp.mokshan.android.features.lessons.TopicsListScreenFactory
import global.msnthrp.mokshan.android.features.phrasebook.PhrasebookScreenFactory
import global.msnthrp.mokshan.domain.lessons.TopicInfo

@Composable
fun MainScreen(
    onInfoClicked: () -> Unit,
    onPronunciationArticleClicked: (String, String) -> Unit,
    onTopicClicked: (TopicInfo) -> Unit,
) {
    var state by remember { mutableStateOf(BottomItem.FIRST) }
    Scaffold(
        bottomBar = {
            NavBar(
                selectedItem = state,
                onItemSelected = { state = it }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding())
        ) {

            val factory = when (state) {
                BottomItem.FIRST -> {
                    PhrasebookScreenFactory(
                        onInfoClicked = onInfoClicked,
                        onPronunciationArticleClicked = onPronunciationArticleClicked,
                    )
                }
                BottomItem.SECOND -> {
                    TopicsListScreenFactory(
                        onTopicClicked = onTopicClicked,
                    )
                }
                BottomItem.THIRD -> {
                    TopicsListScreenFactory(
                        onTopicClicked = onTopicClicked,
                    )
                }
            }
            factory.Content(arguments = null)
        }
    }
}

@Composable
private fun NavBar(
    selectedItem: BottomItem,
    onItemSelected: (BottomItem) -> Unit,
) {
    NavigationBar {
        BottomItem.entries.forEach { item ->
            NavigationBarItem(
                selected = item == selectedItem,
                label = { Text(text = stringResource(id = item.label)) },
                onClick = { onItemSelected(item) },
                icon = { Icon(imageVector = item.icon, contentDescription = null) }
            )
        }
    }
}

@Preview
@Composable
private fun MainScreenPreview() {
    LeMokTheme {
        MainScreen(
            onInfoClicked = {},
            onPronunciationArticleClicked = { _, _ -> },
            onTopicClicked = {}
        )
    }
}