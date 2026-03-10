package snd.komelia.ui.reader.image

import snd.komelia.ui.reader.image.common.ReaderAnimation
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.Orientation.Horizontal
import androidx.compose.foundation.gestures.Orientation.Vertical
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.concurrent.Volatile
import kotlin.math.max
import kotlin.math.min

private val logger = KotlinLogging.logger {}

class ScreenScaleState {
    val zoom = MutableStateFlow(1f)
    private val zoomLimits = MutableStateFlow(1.0f..5f)
    private var currentOffset = Offset.Zero

    val areaSize = MutableStateFlow(IntSize.Zero)
    val targetSize = MutableStateFlow(Size(1f, 1f))

    val offsetXLimits = MutableStateFlow(-1f..1f)
    val offsetYLimits = MutableStateFlow(-1f..1f)

    private val scrollScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val velocityTracker = VelocityTracker()

    val scrollOrientation = MutableStateFlow<Orientation?>(null)
    val scrollReversed = MutableStateFlow(false)
    private val scrollState = MutableStateFlow<ScrollableState?>(null)

    val transformation = MutableStateFlow(Transformation(offset = Offset.Zero, scale = 1f))

    val isGestureInProgress = MutableStateFlow(false)
    val isFlinging = MutableStateFlow(false)
    var edgeHandoffEnabled = false
    private var gestureStartedAtLeftEdge = false
    private var gestureStartedAtRightEdge = false
    private var cumulativePagerScroll = 0f

    @Volatile
    var composeScope: CoroutineScope? = null

    @Volatile
    private var scrollJob: Job? = null

    @Volatile
    private var zoomJob: Job? = null

    @Volatile
    private var baseZoom = 1f

    @Volatile
    private var enableOverscrollArea = false

    private var density = 1f

    fun setDensity(density: Float) {
        this.density = density
    }

    fun scaleFor100PercentZoom() =
        max(
            areaSize.value.width.toFloat() / targetSize.value.width,
            areaSize.value.height.toFloat() / targetSize.value.height
        )

    fun scaleForFullVisibility() =
        min(
            areaSize.value.width.toFloat() / targetSize.value.width,
            areaSize.value.height.toFloat() / targetSize.value.height
        )

    fun zoomToScale(zoom: Float) = zoom * scaleFor100PercentZoom()

    private fun limitTargetInsideArea(areaSize: IntSize, targetSize: Size, zoom: Float?) {
        this.areaSize.value = areaSize
        this.targetSize.value = Size(
            width = targetSize.width,
            height = targetSize.height
        )
        zoomLimits.value = (scaleForFullVisibility() / scaleFor100PercentZoom())..zoomLimits.value.endInclusive
        if (zoom != null) this.zoom.value = zoom

        applyLimits()
    }

    fun setAreaSize(areaSize: IntSize) {
        this.areaSize.value = areaSize
        if (targetSize.value == Size(1f, 1f)) {
            setTargetSize(areaSize.toSize())
        }
    }

    fun setTargetSize(targetSize: Size, zoom: Float? = null) {
        if (targetSize == this.targetSize.value && zoom == this.zoom.value) return
        limitTargetInsideArea(areaSize.value, targetSize, zoom)
    }

    private fun applyLimits() {
        zoom.value = zoom.value.coerceIn(zoomLimits.value)
        val scale = zoomToScale(zoom.value)
        offsetXLimits.update { offsetLimits(targetSize.value.width * scale, areaSize.value.width.toFloat()) }
        offsetYLimits.update { offsetLimits(targetSize.value.height * scale, areaSize.value.height.toFloat()) }

        currentOffset = Offset(
            currentOffset.x.coerceIn(offsetXLimits.value),
            currentOffset.y.coerceIn(offsetYLimits.value),
        )

        val newTransform = Transformation(offset = currentOffset, scale = zoomToScale(zoom.value))
        transformation.value = newTransform
    }

    private fun offsetLimits(targetSize: Float, areaSize: Float): ClosedFloatingPointRange<Float> {
        val areaCenter = areaSize / 2
        val targetCenter = targetSize / 2
        val extra = (targetCenter - areaCenter).coerceAtLeast(0f)
        val overscroll = if (enableOverscrollArea) areaCenter else 0f
        return -extra - overscroll..extra + overscroll
    }

    fun animateTo(offset: Offset, zoom: Float) {
        val coroutineScope = composeScope ?: return
        scrollJob?.cancel()
        zoomJob?.cancel()
        scrollJob = coroutineScope.launch {
            val initialZoom = this@ScreenScaleState.zoom.value
            val initialOffset = currentOffset
            val targetZoom = zoom.coerceIn(zoomLimits.value)

            AnimationState(initialValue = 0f).animateTo(
                targetValue = 1f,
                animationSpec = ReaderAnimation.navSpringSpec(density)
            ) {
                this@ScreenScaleState.zoom.value = initialZoom + (targetZoom - initialZoom) * value
                currentOffset = initialOffset + (offset - initialOffset) * value
                applyLimits()
            }
        }
    }

    suspend fun performFling(spec: DecayAnimationSpec<Offset>) {
        val scale = transformation.value.scale
        val velocity = velocityTracker.calculateVelocity().div(scale)
        velocityTracker.resetTracking()

        isFlinging.value = true
        var lastValue = Offset(0f, 0f)
        try {
            AnimationState(
                typeConverter = Offset.VectorConverter,
                initialValue = Offset.Zero,
                initialVelocity = Offset(velocity.x, velocity.y),
            ).animateDecay(spec) {
                val delta = value - lastValue
                lastValue = value

                if (scrollState.value == null) {
                    val canPanHorizontally = when {
                        delta.x < 0 -> canPanLeft()
                        delta.x > 0 -> canPanRight()
                        else -> false
                    }
                    val canPanVertically = when {
                        delta.y > 0 -> canPanDown()
                        delta.y < 0 -> canPanUp()
                        else -> false
                    }
                    if (!canPanHorizontally && !canPanVertically) {
                        this.cancelAnimation()
                        return@animateDecay
                    }
                }

                addPan(delta)
            }
        } finally {
            isFlinging.value = false
        }
    }

    fun onGestureStart() {
        gestureStartedAtLeftEdge = isAtLeftEdge()
        gestureStartedAtRightEdge = isAtRightEdge()
        cumulativePagerScroll = 0f
    }

    private fun isAtLeftEdge(): Boolean {
        if (zoom.value <= baseZoom + 0.01f) return true
        return currentOffset.x >= offsetXLimits.value.endInclusive - 0.5f
    }

    private fun isAtRightEdge(): Boolean {
        if (zoom.value <= baseZoom + 0.01f) return true
        return currentOffset.x <= offsetXLimits.value.start + 0.5f
    }

    private fun canPanUp(): Boolean {
        return currentOffset.y > offsetYLimits.value.start
    }

    private fun canPanDown(): Boolean {
        return currentOffset.y < offsetYLimits.value.endInclusive
    }

    private fun canPanLeft(): Boolean {
        return currentOffset.x > offsetXLimits.value.start
    }

    private fun canPanRight(): Boolean {
        return currentOffset.x < offsetXLimits.value.endInclusive
    }

    fun addPan(pan: Offset) {
        val zoomToScale = zoomToScale(zoom.value)
        val newOffset = currentOffset + (pan * zoomToScale)
        currentOffset = newOffset
        applyLimits()
        val delta = (newOffset - currentOffset)

        val pagerValue = (delta.x / -zoomToScale)
        val isRtl = scrollReversed.value
        
        val allowNextPage = if (isRtl) {
            gestureStartedAtLeftEdge && pagerValue > 0
        } else {
            gestureStartedAtRightEdge && pagerValue > 0
        }
        
        val allowPrevPage = if (isRtl) {
            gestureStartedAtRightEdge && pagerValue < 0
        } else {
            gestureStartedAtLeftEdge && pagerValue < 0
        }

        if (!edgeHandoffEnabled || allowNextPage || allowPrevPage) {
            when (scrollOrientation.value) {
                Vertical -> applyScroll((delta / -zoomToScale).y)
                Horizontal -> applyScroll(pagerValue)
                null -> {}
            }
        }
    }

    fun addPan(changes: List<PointerInputChange>, pan: Offset) {
        changes.forEach { velocityTracker.addPointerInputChange(it) }
        addPan(pan)
    }

    private fun applyScroll(value: Float) {
        if (value == 0f) return
        val scrollState = this.scrollState.value
        if (scrollState != null) {
            val delta = if (scrollReversed.value) -value else value
            if (edgeHandoffEnabled) {
                val screenWidth = areaSize.value.width.toFloat()
                val remaining = if (delta > 0) {
                    (screenWidth - cumulativePagerScroll).coerceAtLeast(0f)
                } else {
                    (-screenWidth - cumulativePagerScroll).coerceAtMost(0f)
                }
                val consumed = if (delta > 0) min(delta, remaining) else max(delta, remaining)
                cumulativePagerScroll += consumed
                scrollState.dispatchRawDelta(consumed)
            } else {
                scrollState.dispatchRawDelta(delta)
            }
        }
    }

    fun multiplyZoom(zoomMultiplier: Float, focus: Offset = Offset.Zero) {
        setZoom(zoom.value * zoomMultiplier, focus)
    }

    fun addZoom(addZoom: Float, focus: Offset = Offset.Zero) {
        setZoom(zoom.value + addZoom, focus)
    }

    fun setScrollState(scrollableState: ScrollableState?) {
        this.scrollState.value = scrollableState
    }

    fun setScrollOrientation(orientation: Orientation, reversed: Boolean) {
        this.scrollOrientation.value = orientation
        this.scrollReversed.value = reversed
    }

    fun setZoom(zoom: Float, focus: Offset = Offset.Zero, updateBase: Boolean = false) {
        val newZoom = zoom.coerceIn(zoomLimits.value)
        if (updateBase) baseZoom = newZoom
        val newOffset = Transformation.offsetOf(
            point = transformation.value.pointOf(focus),
            transformedPoint = focus,
            scale = zoomToScale(newZoom)
        )
        this.currentOffset = newOffset
        this.zoom.value = newZoom
        applyLimits()
    }

    fun setOffset(offset: Offset) {
        currentOffset = offset
        applyLimits()
    }

    fun toggleZoom(focus: Offset) {
        val coroutineScope = composeScope ?: return
        zoomJob?.cancel()
        val currentZoom = zoom.value
        val targetZoom = if (currentZoom > baseZoom + 0.1f) {
            baseZoom
        } else {
            max(baseZoom * 2.5f, 2.5f)
        }

        zoomJob = coroutineScope.launch {
            AnimationState(initialValue = currentZoom).animateTo(
                targetValue = targetZoom,
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            ) {
                setZoom(value, focus)
            }
        }
    }

    fun resetVelocity() {
        velocityTracker.resetTracking()
    }

    fun enableOverscrollArea(enable: Boolean) {
        this.enableOverscrollArea = enable
        applyLimits()
    }

    fun apply(other: ScreenScaleState) {
        scrollJob?.cancel()
        currentOffset = other.currentOffset
        this.baseZoom = other.baseZoom

        if (other.targetSize.value != this.targetSize.value || other.zoom.value != this.zoom.value) {
            this.areaSize.value = other.areaSize.value
            this.targetSize.value = Size(
                width = other.targetSize.value.width,
                height = other.targetSize.value.height
            )
            zoomLimits.value = (scaleForFullVisibility() / scaleFor100PercentZoom())..zoomLimits.value.endInclusive
            this.zoom.value = other.zoom.value
        }
        applyLimits()
    }

    data class Transformation(
        val offset: Offset,
        val scale: Float,
    ) {
        fun pointOf(transformedPoint: Offset) = (transformedPoint - offset) / scale

        companion object {
            // is derived from the equation `point = (transformedPoint - offset) / scale`
            fun offsetOf(point: Offset, transformedPoint: Offset, scale: Float) =
                transformedPoint - point * scale
        }
    }
}
