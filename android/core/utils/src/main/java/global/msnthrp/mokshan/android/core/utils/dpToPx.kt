import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

@Composable
fun Dp.toPx() = with(LocalDensity.current) { this@toPx.toPx() }


@Composable
fun Int.asPxToDp() = with(LocalDensity.current) { this@asPxToDp.toDp() }