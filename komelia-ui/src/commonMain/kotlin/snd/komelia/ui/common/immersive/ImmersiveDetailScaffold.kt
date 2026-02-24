package snd.komelia.ui.common.immersive

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import snd.komelia.ui.LocalRawStatusBarHeight
import snd.komelia.ui.common.images.ThumbnailImage
import kotlin.math.roundToInt

private enum class CardDragValue { COLLAPSED, EXPANDED }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImmersiveDetailScaffold(
    coverData: Any,
    coverKey: String,
    cardColor: Color?,
    modifier: Modifier = Modifier,
    immersive: Boolean = false,
    topBarContent: @Composable () -> Unit,
    fabContent: @Composable () -> Unit,
    cardContent: @Composable ColumnScope.(expandFraction: Float) -> Unit,
) {
    val density = LocalDensity.current
    val backgroundColor = cardColor ?: MaterialTheme.colorScheme.surfaceVariant

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenHeight = maxHeight
        val collapsedOffset = screenHeight * 0.65f
        val collapsedOffsetPx = with(density) { collapsedOffset.toPx() }

        val state = remember(collapsedOffsetPx) {
            AnchoredDraggableState(
                initialValue = CardDragValue.COLLAPSED,
                anchors = DraggableAnchors {
                    CardDragValue.COLLAPSED at collapsedOffsetPx
                    CardDragValue.EXPANDED at 0f
                },
                positionalThreshold = { d -> d * 0.5f },
                velocityThreshold = { with(density) { 100.dp.toPx() } },
                // M3 Standard easing (0.2, 0, 0, 1) + Long2 (500ms) — spatial movement within screen
                snapAnimationSpec = tween(durationMillis = 500, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)),
                decayAnimationSpec = exponentialDecay(),
            )
        }

        val cardOffsetPx = if (state.offset.isNaN()) collapsedOffsetPx else state.offset
        val expandFraction = (1f - cardOffsetPx / collapsedOffsetPx).coerceIn(0f, 1f)

        val nestedScrollConnection = remember(state) {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    val delta = available.y
                    return if (delta < 0 && cardOffsetPx > 0f)
                        Offset(0f, state.dispatchRawDelta(delta))
                    else Offset.Zero
                }
                override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                    val delta = available.y
                    return if (delta > 0 && source == NestedScrollSource.UserInput)
                        Offset(0f, state.dispatchRawDelta(delta))
                    else Offset.Zero
                }
            }
        }

        val topCornerRadiusDp = lerp(28f, 0f, expandFraction).dp
        val statusBarDp = LocalRawStatusBarHeight.current
        val statusBarPx = with(density) { statusBarDp.toPx() }

        Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {

            // Layer 1: Cover image — fades out as card expands
            // Extends by the card corner radius so it fills behind the rounded corners
            // When immersive=true, shifts up behind the status bar
            ThumbnailImage(
                data = coverData,
                cacheKey = coverKey,
                contentScale = ContentScale.Crop,
                modifier = if (immersive)
                    Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(0, -statusBarPx.roundToInt()) }
                        .height(collapsedOffset + topCornerRadiusDp + statusBarDp)
                        .graphicsLayer { alpha = 1f - expandFraction }
                else
                    Modifier
                        .fillMaxWidth()
                        .height(collapsedOffset + topCornerRadiusDp)
                        .graphicsLayer { alpha = 1f - expandFraction }
            )

            // Layer 2: Card
            Column(
                modifier = Modifier
                    .offset { IntOffset(0, cardOffsetPx.roundToInt()) }
                    .fillMaxWidth()
                    .height(screenHeight)
                    .nestedScroll(nestedScrollConnection)
                    .anchoredDraggable(state, Orientation.Vertical)
                    .clip(RoundedCornerShape(topStart = topCornerRadiusDp, topEnd = topCornerRadiusDp))
                    .background(backgroundColor)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 32.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    )
                }
                Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    cardContent(expandFraction)
                }
            }

            // Layer 3: Thumbnail — fades in as card expands, moves with the card
            // Positioned at card top + drag handle (28dp) + small gap (8dp), left-aligned with 16dp margin
            val thumbAlpha = (expandFraction * 2f - 1f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = with(density) { 16.dp.toPx() }.roundToInt(),
                            y = (cardOffsetPx + with(density) { (28.dp + 20.dp).toPx() }).roundToInt()
                        )
                    }
                    .graphicsLayer { alpha = thumbAlpha }
            ) {
                ThumbnailImage(
                    data = coverData,
                    cacheKey = coverKey,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(width = 110.dp, height = (110.dp / 0.703f))
                        .clip(RoundedCornerShape(8.dp))
                )
            }

            // Layer 4: FAB
            val fabAlpha = (1f - expandFraction * 3f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .offset { IntOffset(0, (cardOffsetPx - with(density) { 72.dp.toPx() }).roundToInt()) }
                    .fillMaxWidth()
                    .graphicsLayer { alpha = fabAlpha }
            ) {
                fabContent()
            }

            // Layer 5: Top bar
            Box(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                topBarContent()
            }
        }
    }
}
