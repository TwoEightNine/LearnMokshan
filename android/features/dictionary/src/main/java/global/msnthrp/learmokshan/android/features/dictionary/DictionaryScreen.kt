package global.msnthrp.learmokshan.android.features.dictionary

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import asPxToDp
import global.msnthrp.mokshan.android.core.designsystem.theme.LeMokTheme
import global.msnthrp.mokshan.android.core.designsystem.uikit.LeMokScreen
import global.msnthrp.mokshan.android.core.utils.stringResource
import global.msnthrp.mokshan.android.features.dictionary.R
import global.msnthrp.mokshan.domain.dictionary.DictionaryEntry
import org.koin.androidx.compose.koinViewModel


@Composable
fun DictionaryScreen(
    dictionaryViewModel: DictionaryViewModel = koinViewModel(),
) {
    val state by dictionaryViewModel.state.collectAsState()
    LifecycleResumeEffect(key1 = "load") {
        dictionaryViewModel.load()
        onPauseOrDispose {}
    }
    LeMokScreen(title = stringResource(id = R.string.dictionary_title)) { padding ->
        val dictionary = state.dictionary
        if (state.isLoading && dictionary == null) {
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
        } else if (dictionary.isNullOrEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(padding)
            ) {
                Text(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 64.dp),
                    text = "You do not have words yet.\nComplete a lesson to start collecting your vocabulary.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(top = padding.calculateTopPadding())
            ) {
                dictionary.forEach { entry ->
                    item {
                        EntryItem(
                            entry = entry,
                            showDivider = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EntryItem(
    entry: DictionaryEntry,
    showDivider: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Text(
            text = entry.mokshan,
            modifier = Modifier
                .padding(start = 16.dp, top = 12.dp, end = 16.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = entry.native.joinToString(),
            modifier = Modifier
                .padding(start = 16.dp, bottom = 12.dp, end = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
        if (showDivider) {
            HorizontalDivider(
                thickness = 1.asPxToDp(),
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@Composable
@Preview
private fun EntryItemPreview() {
    LeMokTheme {
        EntryItem(
            entry = DictionaryEntry(
                mokshan = "Mokshan",
                native = listOf("Native1", "Native2", "Native3", "Native4", "Native5", "123456789773"),
            ),
            showDivider = true,
        )
    }
}

@Composable
@Preview
private fun DictionaryScreenPreview() {
    LeMokTheme {
        DictionaryScreen()
    }
}