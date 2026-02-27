package snd.komelia.ui.common.immersive

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

@Composable
fun ImmersiveDetailFab(
    onReadClick: () -> Unit,
    onReadIncognitoClick: () -> Unit,
    onDownloadClick: () -> Unit,
    accentColor: Color? = null,
    showReadActions: Boolean = true,
) {
    val pillBackground = accentColor ?: MaterialTheme.colorScheme.primaryContainer
    val pillContentColor = remember(pillBackground) {
        if (pillBackground.luminance() > 0.35f) Color(0xFF1C1B1F) else Color(0xFFFFFFFF)
    }
    val fabBackground = accentColor?.let {
        if (it.luminance() > 0.5f) it.copy(alpha = 0.75f) else it.copy(alpha = 0.9f)
    } ?: MaterialTheme.colorScheme.secondaryContainer
    val fabContentColor = remember(fabBackground) {
        if (fabBackground.luminance() > 0.35f) Color(0xFF1C1B1F) else Color(0xFFFFFFFF)
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))

        if (showReadActions) {
            // Split pill: Read Now | Incognito
            Surface(
                shape = CircleShape,
                color = pillBackground,
                modifier = Modifier.height(56.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(56.dp)
                ) {
                    // Read Now
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxHeight()
                            .clickable(onClick = onReadClick)
                            .padding(horizontal = 20.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.MenuBook,
                            contentDescription = "Read Now",
                            tint = pillContentColor
                        )
                        Text(
                            text = "Read Now",
                            style = MaterialTheme.typography.labelLarge,
                            color = pillContentColor
                        )
                    }

                    VerticalDivider(
                        modifier = Modifier.fillMaxHeight(0.6f),
                        color = pillContentColor.copy(alpha = 0.3f)
                    )

                    // Incognito
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxHeight()
                            .clickable(onClick = onReadIncognitoClick)
                            .padding(horizontal = 16.dp)
                    ) {
                        Icon(
                            Icons.Default.VisibilityOff,
                            contentDescription = "Read Incognito",
                            tint = pillContentColor
                        )
                    }
                }
            }
        }

        // Download FAB
        FloatingActionButton(
            onClick = onDownloadClick,
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            containerColor = fabBackground,
        ) {
            Icon(
                Icons.Filled.Download,
                contentDescription = "Download",
                tint = fabContentColor
            )
        }
    }
}
