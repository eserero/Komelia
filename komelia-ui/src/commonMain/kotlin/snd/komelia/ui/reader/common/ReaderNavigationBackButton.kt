package snd.komelia.ui.reader.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.HazeMaterials
import snd.komelia.ui.LocalAccentColor
import snd.komelia.ui.LocalHazeState
import snd.komelia.ui.LocalTheme

@Composable
fun ReaderNavigationBackButton(
    isVisible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val theme = LocalTheme.current
    val hazeState = LocalHazeState.current
    val accentColor = LocalAccentColor.current ?: MaterialTheme.colorScheme.primary
    val hazeStyle = if (hazeState != null) {
        HazeMaterials.thin(theme.colorScheme.surface.copy(alpha = 0.4f))
    } else null

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .size(56.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .then(
                    if (hazeState != null && hazeStyle != null)
                        Modifier.hazeEffect(hazeState) { style = hazeStyle }
                    else
                        Modifier
                ),
            shape = CircleShape,
            color = if (hazeState != null) Color.Transparent else MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            shadowElevation = 8.dp
        ) {
            IconButton(
                onClick = onClick,
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(
                    Icons.Outlined.History,
                    contentDescription = "Go back",
                    tint = accentColor,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
