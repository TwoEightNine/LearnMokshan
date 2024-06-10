package global.msnthrp.mokshan.android.core.designsystem.uikit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import asPxToDp
import global.msnthrp.mokshan.android.core.designsystem.theme.LeMokTheme
import global.msnthrp.mokshan.domain.jart.Jart
import global.msnthrp.mokshan.domain.jart.JartEntry
import global.msnthrp.mokshan.domain.jart.JartMeta

@Composable
fun JartView(
    jart: Jart,
    bottomPadding: Dp = 0.dp,
    showJartTitle: Boolean = true,
    onClicked: ((JartEntry) -> Unit)? = null,
) {
    LazyColumn {
        jart.content.forEach { entry ->
            when (entry) {
                is JartEntry.Title -> {
                    if (showJartTitle) {
                        item {
                            JartEntry(entry, onClicked)
                        }
                    }
                }

                is JartEntry.Table -> item {
                    JartTable(table = entry)
                }

                else -> item {
                    JartEntry(entry, onClicked)
                }
            }
        }
        if (bottomPadding != 0.dp) {
            item {
                Spacer(modifier = Modifier.height(bottomPadding))
            }
        }
    }
}

@Composable
fun JartEntry(
    entry: JartEntry,
    onClicked: ((JartEntry) -> Unit)? = null,
) {
    val textStyle = when (entry) {
        is JartEntry.Header1 -> MaterialTheme.typography.headlineSmall
        is JartEntry.Header2 -> MaterialTheme.typography.titleLarge
        is JartEntry.Header3 -> MaterialTheme.typography.titleMedium
        is JartEntry.Body -> MaterialTheme.typography.bodyLarge
        is JartEntry.Hint -> MaterialTheme.typography.bodyMedium
        is JartEntry.Title -> MaterialTheme.typography.headlineLarge
        else -> MaterialTheme.typography.bodyLarge
    }
    val textColor = when (entry) {
        is JartEntry.Hint -> MaterialTheme.colorScheme.secondary
        is JartEntry.Title,
        is JartEntry.Header1 -> MaterialTheme.colorScheme.primary

        else -> MaterialTheme.colorScheme.onBackground
    }
    val paddingVertical = when (entry) {
        is JartEntry.Header1,
        is JartEntry.Title -> 8.dp

        is JartEntry.Header2,
        is JartEntry.Header3 -> 6.dp

        else -> 4.dp
    }
    val textModifier = Modifier
        .run {
            if (onClicked != null) {
                clickable { onClicked(entry) }
            } else {
                this
            }
        }
        .padding(vertical = paddingVertical)
        .fillMaxWidth()
    val value = when (entry) {
        is JartEntry.ListItem -> " • ${entry.value}"
        is JartEntry.SubListItem -> "   ○ ${entry.value}"
        else -> entry.value
    }
    Text(
        modifier = textModifier,
        style = textStyle,
        color = textColor,
        text = value
    )
}

@Composable
fun JartTable(table: JartEntry.Table) {
    val cells = table.cells.chunked(table.size.first)
    val columnsCount = cells.first().size
    val itemWeight = 1f / columnsCount
    var isHeaderRequired = table.header
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        HorizontalDivider(
            thickness = 1.asPxToDp(),
            color = MaterialTheme.colorScheme.outline,
        )
        cells.forEach { row ->
            val background = when {
                isHeaderRequired -> MaterialTheme.colorScheme.surface
                else -> Color.Transparent
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .background(color = background)
            ) {
                VerticalDivider(
                    thickness = 1.asPxToDp(),
                    color = MaterialTheme.colorScheme.outline,
                )
                for (i in 0 until columnsCount) {
                    val value = row.getOrNull(i).orEmpty()
                    val style = when {
                        isHeaderRequired -> MaterialTheme.typography.titleMedium
                        else -> MaterialTheme.typography.bodyLarge
                    }
                    Text(
                        modifier = Modifier
                            .weight(itemWeight)
                            .padding(8.dp),
                        text = value,
                        style = style,
                    )
                    VerticalDivider(
                        thickness = 1.asPxToDp(),
                        color = MaterialTheme.colorScheme.outline,
                    )
                }

            }
            HorizontalDivider(
                thickness = 1.asPxToDp(),
                color = MaterialTheme.colorScheme.outline,
            )
            isHeaderRequired = false
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
                        JartEntry.Title(
                            value = "Long long long long title"
                        ),
                        JartEntry.Header1(
                            value = "Header 1"
                        ),
                        JartEntry.Header2(
                            value = "Header 2"
                        ),
                        JartEntry.Header3(
                            value = "Header 3"
                        ),
                        JartEntry.Table(
                            cells = "Some body once toooooooold me this world is gonnaroror roll me, i aint the sharpest tool of the sheeeed. But you looking kinda dumb with this".split(
                                ' '
                            ),
                            size = 4 to 20,
                            header = true,
                        ),
                        JartEntry.Body(
                            value = "Some body once told me this world is gonna roll me, i aint the sharpest tool of the sheeeed. But you looking kinda dumb with this"
                        ),
                        JartEntry.ListItem(
                            value = "Some body once told me"
                        ),
                        JartEntry.ListItem(
                            value = "this world is gonna roll me,"
                        ),
                        JartEntry.SubListItem(
                            value = "i aint the sharpest tool of the sheeeed."
                        ),
                        JartEntry.SubListItem(
                            value = "But you looking kinda dumb with this"
                        ),
                        JartEntry.Hint(
                            value = "Shreek 2025"
                        ),
                    )
                )
            )
        }
    }
}