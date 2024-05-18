package global.msnthrp.mokshan.android.features.ispeak

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import global.msnthrp.mokshan.android.core.designsystem.theme.LeMokTheme
import global.msnthrp.mokshan.domain.phrasebook.ForeignLanguage
import global.msnthrp.mokshan.domain.phrasebook.PhrasebookCollection

@Composable
fun ISpeakScreen() {
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            (CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "I speak ...",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
            ))
        },
    ) { padding ->
        val languages = ForeignLanguage.entries
        val (selected, setSelected) = remember { mutableStateOf(ForeignLanguage.ENGLISH) }
        Column(
            modifier = Modifier
                .padding(top = padding.calculateTopPadding())
        ) {
            languages.forEach { language ->
                LanguageItem(language, selected, setSelected)
            }
            Text(text = "")
            Spacer(
                modifier = Modifier.height(
                    padding.calculateBottomPadding()
                )
            )
        }
    }
}

@Composable
private fun LanguageItem(
    language: ForeignLanguage,
    selected: ForeignLanguage,
    setSelected: (ForeignLanguage) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { setSelected(language) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        RadioButton(selected = selected == language, onClick = null)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = language.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
@Preview
fun ISpeakPreview() {
    LeMokTheme {
        ISpeakScreen()
    }
}