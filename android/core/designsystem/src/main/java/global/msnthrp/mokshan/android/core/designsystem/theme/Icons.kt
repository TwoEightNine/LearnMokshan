package global.msnthrp.mokshan.android.core.designsystem.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FontDownload
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Icons {

    val chevronDown = Icons.Outlined.KeyboardArrowDown
    val chevronRight = Icons.AutoMirrored.Filled.KeyboardArrowRight
    val info = Icons.Outlined.Info
    val externalLink = Icons.AutoMirrored.Outlined.ExitToApp
    val arrowBack = Icons.AutoMirrored.Outlined.ArrowBack
    val close = Icons.Outlined.Close
    val lock = Icons.Outlined.Lock
    val dictionary = Icons.Rounded.FontDownload
    val check = Icons.Rounded.CheckCircle
    val contribute = Icons.Rounded.Handshake
    val question = Icons.AutoMirrored.Rounded.HelpOutline
//    val dictionary = Icons.Rounded.FormatColorText

    val study = Icons.Rounded.AccountBalance
    val article = Icons.AutoMirrored.Rounded.Article
}


internal fun makeIconFromXMLPath(
    pathStr: String,
    viewportWidth: Float = 24f,
    viewportHeight: Float = 24f,
    defaultWidth: Dp = 24.dp,
    defaultHeight: Dp = 24.dp,
    fillColor: Color = Color.White,
): ImageVector {
    val fillBrush = SolidColor(fillColor)
    val strokeBrush = SolidColor(fillColor)

    return ImageVector.Builder(
        defaultWidth = defaultWidth,
        defaultHeight = defaultHeight,
        viewportWidth = viewportWidth,
        viewportHeight = viewportHeight,
    ).run {
        addPath(
            pathData = addPathNodes(pathStr),
            name = "",
            fill = fillBrush,
            stroke = strokeBrush,
        )
        build()
    }
}