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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import global.msnthrp.learmokshan.android.features.dictionary.DictionaryScreenFactory
import global.msnthrp.mokshan.android.core.designsystem.theme.LeMokTheme
import global.msnthrp.mokshan.android.features.articles.list.ArticlesListFactory
import global.msnthrp.mokshan.android.features.lessons.topic.TopicsListScreenFactory
import global.msnthrp.mokshan.domain.lessons.TopicInfo

@Composable
fun MainScreen(
    onInfoClicked: () -> Unit,
    onPronunciationArticleClicked: (String, String) -> Unit,
    onTopicClicked: (TopicInfo, lessonNumber: Int) -> Unit,
) {
    var state by rememberSaveable { mutableStateOf(BottomItem.LESSONS) }
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
                BottomItem.LESSONS -> {
                    TopicsListScreenFactory(
                        onTopicClicked = onTopicClicked,
                    )
                }
                BottomItem.DICTIONARY -> {
                    DictionaryScreenFactory()
                }
//                BottomItem.PHRASEBOOK -> {
//                    PhrasebookScreenFactory(
//                        onInfoClicked = onInfoClicked,
//                        onPronunciationArticleClicked = onPronunciationArticleClicked,
//                    )
//                }
                BottomItem.ARTICLES -> {
                    ArticlesListFactory()
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
            onTopicClicked = { _, _ -> }
        )
    }
}