package global.msnthrp.mokshan.android.core.designsystem.uikit

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LeMokCard(
    modifier: Modifier = Modifier,
    onClicked: () -> Unit,
    isEnabled: Boolean = true,
    isElevatedStyle: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = when {
        isElevatedStyle -> {
            CardDefaults.elevatedCardColors(
                disabledContainerColor = MaterialTheme.colorScheme.surfaceDim,
            )
        }
        else -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
    Card(
        modifier = modifier,
        elevation = if (isElevatedStyle && isEnabled) CardDefaults.elevatedCardElevation() else CardDefaults.cardElevation(),
        colors = colors,
        shape = if (isElevatedStyle && isEnabled) CardDefaults.elevatedShape else CardDefaults.shape,
        enabled = isEnabled,
        onClick = onClicked,
        content = content,
    )
}