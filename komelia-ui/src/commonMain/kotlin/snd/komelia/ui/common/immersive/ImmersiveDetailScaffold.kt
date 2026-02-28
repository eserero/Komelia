package snd.komelia.ui.common.immersive

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.VectorizedAnimationSpec
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import snd.komelia.ui.LocalAnimatedVisibilityScope
import snd.komelia.ui.LocalRawStatusBarHeight
import snd.komelia.ui.LocalSharedTransitionScope
import snd.komelia.ui.common.images.ThumbnailImage
import kotlin.math.roundToInt

private enum class CardDragValue { COLLAPSED, EXPANDED }

private class DirectionalSnapSpec : AnimationSpec<Float> {
    override fun <V : AnimationVector> vectorize(
        converter: TwoWayConverter<Float, V>
    ): VectorizedAnimationSpec<V> {
        val expandSpec = tween<Float>(
            durationMillis = 500,
            easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
        ).vectorize(converter)
        val collapseSpec = tween<Float>(
            durationMillis = 200,
            easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
        ).vectorize(converter)
        return object : VectorizedAnimationSpec<V> {
            override val isInfinite = false
            private fun pick(initialValue: V, targetValue: V) =
                if (converter.convertFromVector(targetValue) < converter.convertFromVector(initialValue)) expandSpec else collapseSpec
            override fun getDurationNanos(initialValue: V, initialVelocity: V, targetValue: V) =
                pick(initialValue, targetValue).getDurationNanos(initialValue, initialVelocity, targetValue)
            override fun getValueFromNanos(playTimeNanos: Long, initialValue: V, targetValue: V, initialVelocity: V) =
                pick(initialValue, targetValue).getValueFromNanos(playTimeNanos, initialValue, targetValue, initialVelocity)
            override fun getVelocityFromNanos(playTimeNanos: Long, initialValue: V, targetValue: V, initialVelocity: V) =
                pick(initialValue, targetValue).getVelocityFromNanos(playTimeNanos, initialValue, targetValue, initialVelocity)
        }
    }
}

private val emphasizedEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
private val emphasizedAccelerateEasing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)

@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ImmersiveDetailScaffold(
    coverData: Any,
    coverKey: String,
    cardColor: Color?,
    modifier: Modifier = Modifier,
    immersive: Boolean = false,
    initiallyExpanded: Boolean = false,
    onExpandChange: (Boolean) -> Unit = {},
    topBarContent: @Composable () -> Unit,
    fabContent: @Composable () -> Unit,
    cardContent: @Composable ColumnScope.(expandFraction: Float) -> Unit,
) {
    val density = LocalDensity.current
    val backgroundColor = cardColor ?: MaterialTheme.colorScheme.surfaceVariant

    // Read shared transition scopes OUTSIDE BoxWithConstraints (which uses SubcomposeLayout).
    // SubcomposeLayout defers content composition to the layout phase, so any CompositionLocal
    // reads inside it happen too late for SharedTransitionLayout's composition-phase matching.
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    // The entire scaffold is the shared element — it morphs from the source thumbnail's bounds
    // to full-screen. Applied to an inner Box (not BoxWithConstraints) so the outer
    // BoxWithConstraints always measures at real screen dimensions, keeping collapsedOffset
    // correct throughout the animation.
    val scaffoldSharedModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Modifier.sharedBounds(
                rememberSharedContentState(key = "cover-$coverKey"),
                animatedVisibilityScope = animatedVisibilityScope,
                enter = EnterTransition.None,
                exit = fadeOut(tween(200)),
                boundsTransform = { _, _ -> tween(durationMillis = 600, easing = emphasizedEasing) },
            )
        }
    } else Modifier

    // Whether a shared transition is in progress — used to suppress crossfade during animation.
    val inSharedTransition = sharedTransitionScope != null && animatedVisibilityScope != null

    val uiEnterExitModifier = if (animatedVisibilityScope != null) {
        with(animatedVisibilityScope) {
            Modifier.animateEnterExit(
                enter = fadeIn(tween(durationMillis = 150, delayMillis = 450)),
                exit = fadeOut(tween(durationMillis = 100))
            )
        }
    } else Modifier

    val fabOverlayModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            with(animatedVisibilityScope) {
                Modifier
                    .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 1f)
                    .animateEnterExit(
                        enter = fadeIn(tween(300, delayMillis = 50)),
                        exit = slideOutVertically(tween(200, easing = emphasizedAccelerateEasing)) { it / 2 }
                               + fadeOut(tween(150))
                    )
            }
        }
    } else Modifier

    // Outer BoxWithConstraints is NOT under sharedBounds, so maxHeight always equals the real
    // screen height — never the thumbnail size mid-morph. All layout values (collapsedOffset,
    // card height, cover height) are derived from this real measurement.
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenHeight = maxHeight
        val collapsedOffset = screenHeight * 0.65f
        val collapsedOffsetPx = with(density) { collapsedOffset.toPx() }

        // Use remember (not rememberSaveable) so pager pages don't restore stale saved state.
        var savedExpanded by remember { mutableStateOf(initiallyExpanded) }

        val state = remember(collapsedOffsetPx) {
            AnchoredDraggableState(
                initialValue = if (savedExpanded) CardDragValue.EXPANDED else CardDragValue.COLLAPSED,
                anchors = DraggableAnchors {
                    CardDragValue.COLLAPSED at collapsedOffsetPx
                    CardDragValue.EXPANDED at 0f
                },
                positionalThreshold = { d -> d * 0.5f },
                velocityThreshold = { with(density) { 100.dp.toPx() } },
                // M3 Emphasize Decelerate (expand, 500ms) / Emphasize Accelerate (collapse, 200ms)
                snapAnimationSpec = DirectionalSnapSpec(),
                decayAnimationSpec = exponentialDecay(),
            )
        }

        val cardOffsetPx = if (state.offset.isNaN()) collapsedOffsetPx else state.offset
        val expandFraction = (1f - cardOffsetPx / collapsedOffsetPx).coerceIn(0f, 1f)

        // Snap already-composed pages (e.g. adjacent in a pager) when the parent changes the
        // shared expand state. Skips the snap if the card is already in the right position.
        LaunchedEffect(initiallyExpanded) {
            val target = if (initiallyExpanded) CardDragValue.EXPANDED else CardDragValue.COLLAPSED
            if (state.currentValue != target) {
                state.snapTo(target)
            }
            savedExpanded = initiallyExpanded
        }

        LaunchedEffect(state.currentValue) {
            savedExpanded = state.currentValue == CardDragValue.EXPANDED
            onExpandChange(savedExpanded)
        }

        val nestedScrollConnection = remember(state) {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    val delta = available.y
                    val currentOffset = if (state.offset.isNaN()) collapsedOffsetPx else state.offset
                    return if (delta < 0 && currentOffset > 0f) {
                        state.dispatchRawDelta(delta)
                        Offset(0f, delta)
                    } else {
                        Offset.Zero
                    }
                }

                override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                    val delta = available.y
                    return if (delta > 0 && source == NestedScrollSource.UserInput) {
                        val cardConsumed = state.dispatchRawDelta(delta)
                        Offset(0f, cardConsumed)
                    } else Offset.Zero
                }

                override suspend fun onPreFling(available: Velocity): Velocity {
                    val currentOffset = if (state.offset.isNaN()) collapsedOffsetPx else state.offset
                    if (available.y < 0f && currentOffset > 0f) {
                        state.settle(-1000f)
                        return available
                    }
                    return Velocity.Zero
                }

                override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                    val currentOffset = if (state.offset.isNaN()) collapsedOffsetPx else state.offset
                    if (currentOffset <= 0f || currentOffset >= collapsedOffsetPx) return Velocity.Zero

                    return when {
                        available.y > 0f -> {
                            // Downward fling: snap to COLLAPSED
                            state.settle(available.y)
                            available
                        }
                        available.y < 0f -> {
                            // Upward fling: snap to EXPANDED
                            state.settle(-1000f)
                            available
                        }
                        else -> {
                            // Slow stop after a collapse drag: settle by positional threshold
                            state.settle(0f)
                            Velocity.Zero
                        }
                    }
                }
            }
        }

        val topCornerRadiusDp = lerp(28f, 0f, expandFraction).dp
        val statusBarDp = LocalRawStatusBarHeight.current
        val statusBarPx = with(density) { statusBarDp.toPx() }

        // Inner Box carries sharedBounds — it morphs from thumbnail size to full-screen.
        // Because layout values come from the outer BoxWithConstraints, the cover image and card
        // are always sized/positioned for the full screen, with the morphing clip revealing them
        // naturally as the bounds expand. Image and card z-order never flip.
        Box(modifier = Modifier.fillMaxSize().then(scaffoldSharedModifier)) {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {

                // Layer 1: Cover image — fades out as card expands
                // Extends by the card corner radius so it fills behind the rounded corners
                // When immersive=true, shifts up behind the status bar
                ThumbnailImage(
                    data = coverData,
                    cacheKey = coverKey,
                    crossfade = !inSharedTransition,
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
                val cardShape = RoundedCornerShape(topStart = topCornerRadiusDp, topEnd = topCornerRadiusDp)
                Column(
                    modifier = Modifier
                        .offset { IntOffset(0, cardOffsetPx.roundToInt()) }
                        .fillMaxWidth()
                        .height(screenHeight)
                        .nestedScroll(nestedScrollConnection)
                        .anchoredDraggable(state, Orientation.Vertical)
                        .shadow(elevation = 6.dp, shape = cardShape)
                        .clip(cardShape)
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

                // Layer 5: Top bar
                Box(modifier = Modifier.fillMaxWidth().then(uiEnterExitModifier).statusBarsPadding()) {
                    topBarContent()
                }
            }
        }

        // Layer 4: FAB — outside shared bounds so it is never clipped by the morphing container
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .then(fabOverlayModifier)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 16.dp)
        ) {
            fabContent()
        }
    }
}
