package snd.komelia.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.HazeMaterials
import snd.komelia.ui.LocalHazeState
import snd.komelia.ui.LocalNavBarColor
import snd.komelia.ui.LocalTheme

@Composable
fun FloatingFABContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val hazeState = LocalHazeState.current
    val theme = LocalTheme.current
    val containerColor = if (theme.transparentBars)
        theme.navBarContainerColor
    else
        LocalNavBarColor.current ?: MaterialTheme.colorScheme.surfaceVariant
    val useHaze = hazeState != null && theme.transparentBars
    val hazeStyle = if (useHaze) HazeMaterials.regular(containerColor) else null

    Surface(
        color = if (useHaze) Color.Transparent else containerColor,
        shape = CircleShape,
        tonalElevation = 3.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        modifier = modifier
            .wrapContentSize()
            .clip(CircleShape)
            .then(
                if (useHaze && hazeStyle != null)
                    Modifier.hazeEffect(hazeState!!) { style = hazeStyle }
                else Modifier
            )
    ) {
        content()
    }
}

@Composable
fun FloatingFAB(
    icon: ImageVector,
    onClick: () -> Unit,
    accentColor: Color?,
    iconTint: Color? = null,
    modifier: Modifier = Modifier
) {
    FloatingFABContainer(modifier = modifier.height(56.dp)) {
        val tint = iconTint ?: accentColor ?: MaterialTheme.colorScheme.primary
        IconButton(onClick = onClick, modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)) {
            Icon(icon, null, tint = tint)
        }
    }
}

/**
 * Island-styled round FAB that opens a standard M3 DropdownMenu when tapped.
 * Used as the left-side "more options" button in the floating navigation island.
 */
@Composable
fun FloatingFABWithDropdownMenu(
    icon: ImageVector,
    accentColor: Color?,
    modifier: Modifier = Modifier,
    menuContent: @Composable ColumnScope.() -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        FloatingFAB(
            icon = icon,
            onClick = { menuExpanded = true },
            accentColor = accentColor,
        )
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            menuContent()
        }
    }
}
