package global.msnthrp.mokshan.android.core.designsystem.uikit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import global.msnthrp.mokshan.android.core.designsystem.theme.LeMokTheme
import global.msnthrp.mokshan.domain.jart.Jart
import global.msnthrp.mokshan.domain.jart.JartEntry
import global.msnthrp.mokshan.domain.jart.JartEntryType
import global.msnthrp.mokshan.domain.jart.JartMeta

@Composable
fun JartView(
    jart: Jart,
    onClicked: (JartEntry) -> Unit = {},
) {
    LazyColumn {
        jart.content.forEach { entry ->
            item {
                val textStyle = when (entry.type) {
                    JartEntryType.H1 -> MaterialTheme.typography.displaySmall
                    JartEntryType.H2 -> MaterialTheme.typography.headlineMedium
                    JartEntryType.H3 -> MaterialTheme.typography.headlineSmall
                    JartEntryType.BODY -> MaterialTheme.typography.bodyMedium
                    JartEntryType.HINT -> MaterialTheme.typography.labelMedium
                    JartEntryType.TITLE -> MaterialTheme.typography.displayMedium
                    else -> MaterialTheme.typography.bodyMedium
                }
                val textColor = when (entry.type) {
                    JartEntryType.HINT -> Color.DarkGray
                    else -> Color.Black
                }
                Text(
                    modifier = Modifier
                        .clickable { onClicked(entry) },
                    style = textStyle,
                    color = textColor,
                    text = entry.value
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
@Preview
fun JartPreview() {
    LeMokTheme {
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(all = 16.dp)
        ) {
            JartView(
                jart = Jart(
                    meta = JartMeta(version = 1),
                    content = listOf(
                        JartEntry(
                            type = JartEntryType.TITLE,
                            value = "Long long long long title"
                        ),
                        JartEntry(
                            type = JartEntryType.H1,
                            value = "Header 1"
                        ),
                        JartEntry(
                            type = JartEntryType.H2,
                            value = "Header 2"
                        ),
                        JartEntry(
                            type = JartEntryType.H3,
                            value = "Header 3"
                        ),
                        JartEntry(
                            type = JartEntryType.BODY,
                            value = "Some body once told me this world is gonna roll me, i aint the sharpest tool of the sheeeed. But you looking kinda dumb with this"
                        ),
                        JartEntry(
                            type = JartEntryType.HINT,
                            value = "Shreek 2025"
                        ),
                    )
                )
            )
        }
    }
}