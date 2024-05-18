package global.msnthrp.mokshan.android.features.phrasebook

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import asPxToDp
import global.msnthrp.mokshan.android.core.designsystem.theme.LeMokTheme
import global.msnthrp.mokshan.domain.phrasebook.Phrase

@Composable
fun PhrasebookScreen(
    appName: String,
    phrasebookViewModel: PhrasebookViewModel = viewModel()
) {
    val state by phrasebookViewModel.state.collectAsState()

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            (CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = appName,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
            ))
        },
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width(40.dp),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(top = padding.calculateTopPadding())
            ) {
                state.phrasebook?.phrases?.forEach { phrase ->
                    item {
                        PhraseView(phrase = phrase)
                    }
                }
                item {
                    Spacer(
                        modifier = Modifier.height(
                            padding.calculateBottomPadding()
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun PhraseView(phrase: Phrase) {
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

@Preview
@Composable
fun ScreenPreview() {
    LeMokTheme {
        PhrasebookScreen(appName = "App name")
    }
}