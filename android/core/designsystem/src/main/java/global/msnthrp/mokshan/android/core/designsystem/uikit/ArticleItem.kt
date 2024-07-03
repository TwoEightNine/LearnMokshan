package global.msnthrp.mokshan.android.core.designsystem.uikit

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import global.msnthrp.mokshan.android.core.designsystem.theme.Icons
import global.msnthrp.mokshan.android.core.designsystem.theme.LeMokTheme

@Composable
fun ArticleItem(
    title: String,
    description: String = "",
    categories: List<String> = emptyList(),
    showChevron: Boolean = false,
    onClick: () -> Unit
) {
    LeMokCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClicked = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            val paddingEnd = when {
                showChevron -> 36.dp
                else -> 0.dp
            }
            ArticleTexts(
                modifier = Modifier
                    .padding(end = paddingEnd),
                title = title,
                description = description,
                categories = categories
            )
            if (showChevron) {
                Image(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(all = 16.dp),
                    imageVector = Icons.chevronRight,
                    colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.tertiary),
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun ArticleTexts(
    modifier: Modifier = Modifier,
    title: String,
    description: String = "",
    categories: List<String> = emptyList(),
) {
    Column(modifier = modifier) {
        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
                .fillMaxWidth(),
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        if (description.isNotBlank()) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        if (categories.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp, top = 8.dp)
            ) {
                categories.forEach { category ->
                    item {
                        Text(
                            modifier = Modifier
                                .padding(end = 8.dp),
                            text = category,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
@Preview
fun ArticleItemPreview() {
    LeMokTheme {
        ArticleItem(
            title = "Writing system and pronunciation rules",
            description = "Get familiar with h h h h h h h h h h h h h h h y y y y",
//            categories = listOf("for beginners", "education"),
            showChevron = true,
            onClick = {}
        )
    }
}