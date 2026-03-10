package snd.komelia.ui.common.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

/**
 * A [DropdownMenu] wrapper that applies M3-style fade + scale enter/exit animations.
 * Scale origin defaults to top-right (1f, 0f), matching a trailing 3-dots button.
 */
@Composable
fun AnimatedDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    transformOrigin: TransformOrigin = TransformOrigin(1f, 0f),
    content: @Composable ColumnScope.() -> Unit,
) {
    val transitionState = remember { MutableTransitionState(false) }
    var popupVisible by remember { mutableStateOf(false) }

    LaunchedEffect(expanded) {
        if (expanded) {
            popupVisible = true
            transitionState.targetState = true
        } else {
            transitionState.targetState = false
            // Wait for exit animation to finish before removing the popup
            snapshotFlow { transitionState.isIdle }
                .filter { it }
                .first()
            popupVisible = false
        }
    }

    if (popupVisible) {
        DropdownMenu(
            expanded = true,
            onDismissRequest = onDismissRequest,
            offset = offset,
            scrollState = rememberScrollState(),
        ) {
            AnimatedVisibility(
                visibleState = transitionState,
                enter = fadeIn(tween(200, easing = FastOutSlowInEasing)) +
                        scaleIn(
                            initialScale = 0.85f,
                            transformOrigin = transformOrigin,
                            animationSpec = tween(200, easing = FastOutSlowInEasing),
                        ),
                exit = fadeOut(tween(120)) +
                        scaleOut(
                            targetScale = 0.85f,
                            transformOrigin = transformOrigin,
                            animationSpec = tween(120),
                        ),
            ) {
                Column(modifier = modifier) { content() }
            }
        }
    }
}
