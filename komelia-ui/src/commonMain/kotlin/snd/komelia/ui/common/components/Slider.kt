package snd.komelia.ui.common.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppSliderDefaults {
    @Composable
    fun colors(
        accentColor: Color? = null,
        thumbColor: Color = accentColor ?: MaterialTheme.colorScheme.tertiaryContainer,
        activeTrackColor: Color = accentColor ?: MaterialTheme.colorScheme.tertiary,
        activeTickColor: Color = (accentColor ?: MaterialTheme.colorScheme.tertiaryContainer).copy(alpha = 0.5f),
        inactiveTrackColor: Color = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.5f),
        inactiveTickColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        disabledThumbColor: Color = Color.Unspecified,
        disabledActiveTrackColor: Color = Color.Unspecified,
        disabledActiveTickColor: Color = Color.Unspecified,
        disabledInactiveTrackColor: Color = Color.Unspecified,
        disabledInactiveTickColor: Color = Color.Unspecified
    ) = SliderDefaults.colors(
        thumbColor = thumbColor,
        activeTrackColor = activeTrackColor,
        activeTickColor = activeTickColor,
        inactiveTrackColor = inactiveTrackColor,
        inactiveTickColor = inactiveTickColor,
        disabledThumbColor = disabledThumbColor,
        disabledActiveTrackColor = disabledActiveTrackColor,
        disabledActiveTickColor = disabledActiveTickColor,
        disabledInactiveTrackColor = disabledInactiveTrackColor,
        disabledInactiveTickColor = disabledInactiveTickColor
    )
}