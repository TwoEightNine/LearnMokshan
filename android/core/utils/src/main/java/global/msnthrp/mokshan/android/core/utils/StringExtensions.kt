package global.msnthrp.mokshan.android.core.utils

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable

@Composable
fun stringResource(@StringRes id: Int, vararg args: Pair<String, String>): String {
    var string = androidx.compose.ui.res.stringResource(id = id)
    args.forEach { (key, value) ->
        string = string.replace("{$key}", value)
    }
    return string
}