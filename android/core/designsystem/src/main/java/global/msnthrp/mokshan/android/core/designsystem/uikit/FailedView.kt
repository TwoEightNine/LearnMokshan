package global.msnthrp.mokshan.android.core.designsystem.uikit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import global.msnthrp.mokshan.android.core.designsystem.R
import global.msnthrp.mokshan.android.core.designsystem.theme.Icons
import global.msnthrp.mokshan.android.core.designsystem.theme.LeMokTheme
import global.msnthrp.mokshan.android.core.utils.stringResource

@Composable
fun FailedView(
    errorMessage: String? = null,
    onRetryClicked: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .width(240.dp)
        ) {
            Text(
                modifier = Modifier,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                text = errorMessage ?: stringResource(R.string.failed_error_message)
            )
            TextButton(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = onRetryClicked,
            ) {
                Text(text = stringResource(R.string.failed_retry))
                Spacer(modifier = Modifier.width(6.dp))
                androidx.compose.material3.Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = Icons.retry,
                    contentDescription = null
                )
            }
        }
    }
}


@Preview
@Composable
private fun FailedPreview() {
    LeMokTheme {
        FailedView(
            onRetryClicked = {},
        )
    }
}