package global.msnthrp.mokshan.android.main

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import global.msnthrp.mokshan.android.R
import global.msnthrp.mokshan.android.core.designsystem.theme.Icons

enum class BottomItem(
    @StringRes
    val label: Int,
    val icon: ImageVector,
) {
    FIRST(label = R.string.bottom_item_phrasebook, icon = Icons.info),
    SECOND(label = R.string.bottom_item_lessons, icon = Icons.study),
    THIRD(label = R.string.bottom_item_articles, icon = Icons.article),
}