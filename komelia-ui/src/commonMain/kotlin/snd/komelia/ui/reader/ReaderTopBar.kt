package snd.komelia.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.HazeMaterials
import snd.komelia.image.UpscaleStatus
import snd.komelia.ui.LocalAccentColor
import snd.komelia.ui.LocalHazeState
import snd.komelia.ui.LocalTheme
import snd.komelia.ui.reader.image.settings.UpscaleActivityIndicator

@Composable
fun ReaderTopBar(
    seriesTitle: String,
    bookTitle: String,
    onBack: () -> Unit,
    upscaleActivities: Map<Int, UpscaleStatus> = emptyMap(),
    modifier: Modifier = Modifier,
) {
    val theme = LocalTheme.current
    val hazeState = LocalHazeState.current
    val accentColor = LocalAccentColor.current ?: MaterialTheme.colorScheme.primary
    val hazeStyle = if (hazeState != null) HazeMaterials.thin(theme.colorScheme.surface) else null

    var seriesMultiplier by remember(seriesTitle) { mutableFloatStateOf(1f) }
    var bookMultiplier by remember(bookTitle) { mutableFloatStateOf(1f) }

    Surface(
        color = if (hazeState != null) Color.Transparent else MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (hazeState != null && hazeStyle != null)
                    Modifier.hazeEffect(hazeState) { style = hazeStyle }
                else
                    Modifier
            )
    ) {
        Column {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = accentColor
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = seriesTitle,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = (18.sp * seriesMultiplier)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = { textLayoutResult ->
                            if (textLayoutResult.hasVisualOverflow) {
                                seriesMultiplier *= 0.9f
                            }
                        }
                    )

                    Text(
                        text = bookTitle,
                        color = accentColor,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = (14.sp * bookMultiplier)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = { textLayoutResult ->
                            if (textLayoutResult.hasVisualOverflow) {
                                bookMultiplier *= 0.9f
                            }
                        }
                    )
                }
            }
            AnimatedVisibility(visible = upscaleActivities.isNotEmpty()) {
                UpscaleActivityIndicator(upscaleActivities)
            }
        }
    }
}
