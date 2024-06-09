package global.msnthrp.mokshan.android.features.appinfo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import global.msnthrp.mokshan.android.core.designsystem.theme.Icons
import global.msnthrp.mokshan.android.core.designsystem.theme.LeMokTheme
import global.msnthrp.mokshan.android.core.designsystem.uikit.ArticleItem
import global.msnthrp.mokshan.android.core.designsystem.uikit.ArticleItemPreview
import global.msnthrp.mokshan.android.core.designsystem.uikit.LeMokCell
import global.msnthrp.mokshan.android.core.designsystem.uikit.LeMokScreen
import global.msnthrp.mokshan.android.core.utils.LeMokBuildConfig
import global.msnthrp.mokshan.android.core.utils.stringResource
import org.koin.java.KoinJavaComponent
import org.koin.java.KoinJavaComponent.getKoin
import java.util.Locale

private const val PP_URL = "https://raw.githubusercontent.com/TwoEightNine/LearnMokshan/" +
        "master/content/legal/pp-{locale}.json"
private const val TOS_URL = "https://raw.githubusercontent.com/TwoEightNine/LearnMokshan/" +
        "master/content/legal/tos-{locale}.json"
private const val DEV_URL = "https://raw.githubusercontent.com/TwoEightNine/LearnMokshan/" +
        "master/content/legal/thanks-{locale}.json"

@Composable
fun AppInfoScreen(
    onBackClicked: () -> Unit,
    onArticleClicked: (url: String, title: String) -> Unit,
    buildConfig: LeMokBuildConfig = getKoin().get(),
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

            ArticleItem(
                title = "A message from developer",
                showChevron = true,
                onClick = {
                    val url = DEV_URL.replace("{locale}", getLocaleForUrl())
                    onArticleClicked(url, "")
                }
            )

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

            Text(
                modifier = Modifier
                    .padding(start = 56.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
                text = stringResource(
                    id = R.string.app_info_app_version,
                    "version" to buildConfig.versionName,
                ),
                style = MaterialTheme.typography.bodyMedium,
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