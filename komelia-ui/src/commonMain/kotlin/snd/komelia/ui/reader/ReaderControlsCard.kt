package snd.komelia.ui.reader

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.HazeMaterials
import snd.komelia.ui.LocalHazeState
import snd.komelia.ui.LocalTheme

@Composable
fun ReaderControlsCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val theme = LocalTheme.current
    val hazeState = LocalHazeState.current
    val hazeStyle = if (hazeState != null) HazeMaterials.thin(theme.colorScheme.surface) else null

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = if (hazeState != null) Color.Transparent else MaterialTheme.colorScheme.surface,
        tonalElevation = if (hazeState == null) 3.dp else 0.dp,
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .navigationBarsPadding()
            .clip(RoundedCornerShape(28.dp))
            .then(
                if (hazeState != null && hazeStyle != null)
                    Modifier.hazeEffect(hazeState) { style = hazeStyle }
                else
                    Modifier
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            content()
        }
    }
}
