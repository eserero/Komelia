package snd.komelia.ui.reader.image.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import snd.komelia.image.EdgeSampling

@Composable
fun AdaptiveBackground(
    edgeSampling: EdgeSampling?,
    modifier: Modifier = Modifier,
    imageSize: IntSize? = null,
    content: @Composable () -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val topColor = remember(edgeSampling) {
        if (edgeSampling?.vertical == true) Color(edgeSampling.first.averageColor) else Color.Transparent
    }
    val bottomColor = remember(edgeSampling) {
        if (edgeSampling?.vertical == true) Color(edgeSampling.second.averageColor) else Color.Transparent
    }
    val leftColor = remember(edgeSampling) {
        if (edgeSampling?.vertical == false) Color(edgeSampling.first.averageColor) else Color.Transparent
    }
    val rightColor = remember(edgeSampling) {
        if (edgeSampling?.vertical == false) Color(edgeSampling.second.averageColor) else Color.Transparent
    }

    val animatedTop by animateColorAsState(targetValue = topColor, animationSpec = tween(durationMillis = 500))
    val animatedBottom by animateColorAsState(targetValue = bottomColor, animationSpec = tween(durationMillis = 500))
    val animatedLeft by animateColorAsState(targetValue = leftColor, animationSpec = tween(durationMillis = 500))
    val animatedRight by animateColorAsState(targetValue = rightColor, animationSpec = tween(durationMillis = 500))

        Box(
            modifier = modifier
                .fillMaxSize()
                .drawBehind {
                    if (edgeSampling != null) {
                        if (edgeSampling.vertical) {
                            val containerHeight = size.height.toInt()
                            val imageTop = if (imageSize != null) ((containerHeight - imageSize.height) / 2).coerceAtLeast(0).toFloat()
                            else size.height / 2
                            val imageBottom = if (imageSize != null) (imageTop + imageSize.height).coerceAtMost(size.height)
                            else size.height / 2
    
                            // Top bloom
                            drawRect(
                                brush = Brush.verticalGradient(
                                    0f to backgroundColor,
                                    1f to animatedTop,
                                    startY = 0f,
                                    endY = imageTop + 1f // Overlap by 1px
                                ),
                                topLeft = Offset.Zero,
                                size = Size(size.width, imageTop + 1f)
                            )
    
                            // Bottom bloom
                            drawRect(
                                brush = Brush.verticalGradient(
                                    0f to animatedBottom,
                                    1f to backgroundColor,
                                    startY = imageBottom - 1f, // Overlap by 1px
                                    endY = size.height
                                ),
                                topLeft = Offset(0f, imageBottom - 1f),
                                size = Size(size.width, size.height - imageBottom + 1f)
                            )
                        } else {
                            val containerWidth = size.width.toInt()
                            val imageLeft = if (imageSize != null) ((containerWidth - imageSize.width) / 2).coerceAtLeast(0).toFloat()
                            else size.width / 2
                            val imageRight = if (imageSize != null) (imageLeft + imageSize.width).coerceAtMost(size.width)
                            else size.width / 2
    
                            // Left bloom
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    0f to backgroundColor,
                                    1f to animatedLeft,
                                    startX = 0f,
                                    endX = imageLeft + 1f // Overlap by 1px
                                ),
                                topLeft = Offset.Zero,
                                size = Size(imageLeft + 1f, size.height)
                            )
    
                            // Right bloom
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    0f to animatedRight,
                                    1f to backgroundColor,
                                    startX = imageRight - 1f, // Overlap by 1px
                                    endX = size.width
                                ),
                                topLeft = Offset(imageRight - 1f, 0f),
                                size = Size(size.width - imageRight + 1f, size.height)
                            )
                        }
                    }
                }
        )
     {
        content()
    }
}
