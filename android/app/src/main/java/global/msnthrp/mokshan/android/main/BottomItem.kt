package global.msnthrp.mokshan.android.main

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import global.msnthrp.learnmokshan.R
import global.msnthrp.mokshan.android.core.designsystem.theme.Icons

enum class BottomItem(
    @StringRes
    val label: Int,
    val icon: ImageVector,
) {
    LESSONS(label = R.string.bottom_item_lessons, icon = Icons.study),
    DICTIONARY(label = R.string.bottom_item_dictionary, icon = Icons.dictionary),
//    PHRASEBOOK(label = R.string.bottom_item_phrasebook, icon = Icons.info),
    ARTICLES(label = R.string.bottom_item_articles, icon = Icons.article),
}