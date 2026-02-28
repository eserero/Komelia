package snd.komelia.ui.common.immersive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import snd.komelia.ui.LocalNavBarColor

@Composable
fun ImmersiveDetailFab(
    onReadClick: () -> Unit,
    onReadIncognitoClick: () -> Unit,
    onDownloadClick: () -> Unit,
    accentColor: Color? = null,
    showReadActions: Boolean = true,
) {
    val navBarColor = LocalNavBarColor.current
    val readNowContainerColor = navBarColor ?: MaterialTheme.colorScheme.primaryContainer

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))

        if (showReadActions) {
            ExtendedFloatingActionButton(
                onClick = onReadClick,
                containerColor = readNowContainerColor,
                contentColor = contentColorFor(readNowContainerColor),
                icon = {
                    Icon(
                        Icons.AutoMirrored.Rounded.MenuBook,
                        contentDescription = null
                    )
                },
                text = { Text("Read Now") }
            )

            FloatingActionButton(
                onClick = onReadIncognitoClick,
            ) {
                Icon(
                    Icons.Rounded.VisibilityOff,
                    contentDescription = "Read Incognito"
                )
            }
        }

        // Download FAB
        FloatingActionButton(
            onClick = onDownloadClick,
        ) {
            Icon(
                Icons.Rounded.Download,
                contentDescription = "Download"
            )
        }
    }
}
