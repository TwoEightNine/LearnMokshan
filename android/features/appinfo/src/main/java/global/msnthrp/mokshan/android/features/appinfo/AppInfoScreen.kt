package global.msnthrp.mokshan.android.features.appinfo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import global.msnthrp.mokshan.android.core.designsystem.theme.Icons
import global.msnthrp.mokshan.android.core.designsystem.theme.LeMokTheme
import global.msnthrp.mokshan.android.core.designsystem.uikit.LeMokCell
import global.msnthrp.mokshan.android.core.designsystem.uikit.LeMokScreen

@Composable
fun AppInfoScreen(
    onBackClicked: () -> Unit,
    onPrivacyPolicyClicked: () -> Unit,
    onTermsOfServiceClicked: () -> Unit,
) {
    LeMokScreen(
        onNavigationClick = onBackClicked,
        title = stringResource(id = R.string.app_info_title)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = padding.calculateTopPadding()),
        ) {
            LeMokCell(
                text = stringResource(id = R.string.app_info_privacy_policy),
                icon = Icons.externalLink,
                onClicked = onPrivacyPolicyClicked,
            )
            LeMokCell(
                text = stringResource(id = R.string.app_info_tos),
                icon = Icons.externalLink,
                onClicked = onTermsOfServiceClicked,
            )
        }
    }
}

@Preview
@Composable
fun AppInfoPreview() {
    LeMokTheme {
        AppInfoScreen(
            onBackClicked = {},
            onPrivacyPolicyClicked = {},
            onTermsOfServiceClicked = {}
        )
    }
}