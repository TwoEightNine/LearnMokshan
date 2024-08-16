package global.msnthrp.mokshan.android.core.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha

fun Modifier.setVisibleElseInvisible(isVisible: Boolean) = alpha(if (isVisible) 1f else 0f)