package snd.komelia.ui.reader.epub.audio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.HazeMaterials
import snd.komelia.ui.LocalHazeState
import snd.komelia.ui.LocalTheme

/**
 * Renders the appropriate background for audio players when immersive color is disabled.
 * Matches [snd.komelia.ui.reader.ReaderControlsCard]: haze effect on modern themes,
 * solid surface color on classic themes.
 *
 * Must live in commonMain because [hazeEffect] is only importable from commonMain.
 */
@Composable
fun NonImmersiveAudioBackground(modifier: Modifier = Modifier) {
    val hazeState = LocalHazeState.current
    val theme = LocalTheme.current
    if (hazeState != null) {
        val hazeStyle = HazeMaterials.thin(theme.colorScheme.surface.copy(alpha = 0.4f))
        Box(modifier.hazeEffect(hazeState) {
            style = hazeStyle
        })
    } else {
        Box(modifier.background(MaterialTheme.colorScheme.surface))
    }
}
