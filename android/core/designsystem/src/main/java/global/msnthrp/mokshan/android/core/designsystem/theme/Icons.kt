package global.msnthrp.mokshan.android.core.designsystem.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Lock
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

    val study = Study
    val article = Article
}

internal val Study by lazy {
    makeIconFromXMLPath("M4,11.5v4c0,0.83 0.67,1.5 1.5,1.5S7,16.33 7,15.5v-4c0,-0.83 -0.67,-1.5 -1.5,-1.5S4,10.67 4,11.5zM10,11.5v4c0,0.83 0.67,1.5 1.5,1.5s1.5,-0.67 1.5,-1.5v-4c0,-0.83 -0.67,-1.5 -1.5,-1.5s-1.5,0.67 -1.5,1.5zM3.5,22h16c0.83,0 1.5,-0.67 1.5,-1.5s-0.67,-1.5 -1.5,-1.5h-16c-0.83,0 -1.5,0.67 -1.5,1.5S2.67,22 3.5,22zM16,11.5v4c0,0.83 0.67,1.5 1.5,1.5s1.5,-0.67 1.5,-1.5v-4c0,-0.83 -0.67,-1.5 -1.5,-1.5s-1.5,0.67 -1.5,1.5zM10.57,1.49l-7.9,4.16c-0.41,0.21 -0.67,0.64 -0.67,1.1C2,7.44 2.56,8 3.25,8h16.51C20.44,8 21,7.44 21,6.75c0,-0.46 -0.26,-0.89 -0.67,-1.1l-7.9,-4.16c-0.58,-0.31 -1.28,-0.31 -1.86,0z")
}

internal val Article by lazy {
    makeIconFromXMLPath("M16,3H5C3.9,3 3,3.9 3,5v14c0,1.1 0.9,2 2,2h14c1.1,0 2,-0.9 2,-2V8L16,3zM8,7h3c0.55,0 1,0.45 1,1v0c0,0.55 -0.45,1 -1,1H8C7.45,9 7,8.55 7,8v0C7,7.45 7.45,7 8,7zM16,17H8c-0.55,0 -1,-0.45 -1,-1v0c0,-0.55 0.45,-1 1,-1h8c0.55,0 1,0.45 1,1v0C17,16.55 16.55,17 16,17zM16,13H8c-0.55,0 -1,-0.45 -1,-1v0c0,-0.55 0.45,-1 1,-1h8c0.55,0 1,0.45 1,1v0C17,12.55 16.55,13 16,13zM15,8V5l4,4h-3C15.45,9 15,8.55 15,8z")
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