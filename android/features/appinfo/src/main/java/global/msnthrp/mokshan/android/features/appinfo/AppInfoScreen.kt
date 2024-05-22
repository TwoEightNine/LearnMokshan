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
import java.util.Locale

private const val PP_URL = "https://raw.githubusercontent.com/TwoEightNine/LearnMokshan/" +
        "master/content/legal/pp-{locale}.txt"
private const val TOS_URL = "https://raw.githubusercontent.com/TwoEightNine/LearnMokshan/" +
        "master/content/legal/tos-{locale}.txt"

@Composable
fun AppInfoScreen(
    onBackClicked: () -> Unit,
    customTabsLauncher: (url: String) -> Unit,
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
                onClicked = { customTabsLauncher(getPrivacyPolicyUrl()) },
            )
            LeMokCell(
                text = stringResource(id = R.string.app_info_tos),
                icon = Icons.externalLink,
                onClicked = { customTabsLauncher(getTermsOfServiceUrl()) },
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
            customTabsLauncher = {},
        )
    }
}

private fun getPrivacyPolicyUrl(): String {
    return PP_URL.replace("{locale}", getLocaleForUrl())
}

private fun getTermsOfServiceUrl(): String {
    return TOS_URL.replace("{locale}", getLocaleForUrl())
}

private fun getLocaleForUrl(): String {
    val language = Locale.getDefault().language
    val supportedLanguages = setOf("en", "ru")
    return language.takeIf { it in supportedLanguages } ?: "en"
}