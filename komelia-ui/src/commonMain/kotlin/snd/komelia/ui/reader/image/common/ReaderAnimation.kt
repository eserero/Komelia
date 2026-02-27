package snd.komelia.ui.reader.image.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

object ReaderAnimation {
    /**
     * Unified spring spec for all manual navigation (taps, arrow keys).
     * Damping: NoBouncy (1.0f) for a clean, professional finish.
     * Normalized by density to ensure consistent physical speed across devices.
     */
    fun <T> navSpringSpec(density: Float) = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = (Spring.StiffnessLow * (2f / density))
            .coerceIn(Spring.StiffnessVeryLow..Spring.StiffnessMedium)
    )
}
