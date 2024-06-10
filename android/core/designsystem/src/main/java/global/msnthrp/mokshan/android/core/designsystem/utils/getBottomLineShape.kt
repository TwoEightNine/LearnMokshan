package global.msnthrp.mokshan.android.core.designsystem.utils

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape

@Composable
fun getBottomLineShape(): Shape {
    val lineThicknessPx = 1
    return GenericShape { size, _ ->
        // 1) Bottom-left corner
        moveTo(0f, size.height)
        // 2) Bottom-right corner
        lineTo(size.width, size.height)
        // 3) Top-right corner
        lineTo(size.width, size.height - lineThicknessPx)
        // 4) Top-left corner
        lineTo(0f, size.height - lineThicknessPx)
    }
}

@Composable
fun getLeftLineShape(): Shape {
    val lineThicknessPx = 1f
    return GenericShape { size, _ ->
        // 1) Bottom-left corner
        moveTo(0f, size.height)
        // 2) Bottom-right corner
        lineTo(0f, 0f)
        // 3) Top-right corner
        lineTo(lineThicknessPx, 0f)
        // 4) Top-left corner
        lineTo(lineThicknessPx, size.height)
    }
}