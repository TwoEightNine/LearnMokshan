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
        "master/content/legal/pp-{locale}.json"
private const val TOS_URL = "https://raw.githubusercontent.com/TwoEightNine/LearnMokshan/" +
        "master/content/legal/tos-{locale}.json"

@Composable
fun AppInfoScreen(
    onBackClicked: () -> Unit,
    onArticleClicked: (url: String, title: String) -> Unit,
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
            val privacyPolicyTitle = stringResource(id = R.string.app_info_privacy_policy)
            LeMokCell(
                text = privacyPolicyTitle,
                icon = Icons.externalLink,
                onClicked = {
                    onArticleClicked(
                        getPrivacyPolicyUrl(),
                        privacyPolicyTitle
                    )
                },
            )

            val tosTitle = stringResource(id = R.string.app_info_tos)
            LeMokCell(
                text = tosTitle,
                icon = Icons.externalLink,
                onClicked = {
                    onArticleClicked(
                        getTermsOfServiceUrl(),
                        tosTitle
                    )
                },
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
            onArticleClicked = { _, _ -> },
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