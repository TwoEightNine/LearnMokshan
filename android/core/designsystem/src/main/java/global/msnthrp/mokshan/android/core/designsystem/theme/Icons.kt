package global.msnthrp.mokshan.android.core.designsystem.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.rounded.FontDownload
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
//    val dictionary = Icons.Rounded.FormatColorText

    val study = Study
    val article = Icons.AutoMirrored.Outlined.Article
}

internal val Study by lazy {
    makeIconFromXMLPath("M4,11.5v4c0,0.83 0.67,1.5 1.5,1.5S7,16.33 7,15.5v-4c0,-0.83 -0.67,-1.5 -1.5,-1.5S4,10.67 4,11.5zM10,11.5v4c0,0.83 0.67,1.5 1.5,1.5s1.5,-0.67 1.5,-1.5v-4c0,-0.83 -0.67,-1.5 -1.5,-1.5s-1.5,0.67 -1.5,1.5zM3.5,22h16c0.83,0 1.5,-0.67 1.5,-1.5s-0.67,-1.5 -1.5,-1.5h-16c-0.83,0 -1.5,0.67 -1.5,1.5S2.67,22 3.5,22zM16,11.5v4c0,0.83 0.67,1.5 1.5,1.5s1.5,-0.67 1.5,-1.5v-4c0,-0.83 -0.67,-1.5 -1.5,-1.5s-1.5,0.67 -1.5,1.5zM10.57,1.49l-7.9,4.16c-0.41,0.21 -0.67,0.64 -0.67,1.1C2,7.44 2.56,8 3.25,8h16.51C20.44,8 21,7.44 21,6.75c0,-0.46 -0.26,-0.89 -0.67,-1.1l-7.9,-4.16c-0.58,-0.31 -1.28,-0.31 -1.86,0z")
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