package snd.komelia.ui.reader.image.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import snd.komelia.settings.model.ReaderTapNavigationMode
import snd.komelia.settings.model.ReaderTapNavigationMode.*
import snd.komelia.ui.LocalStrings

@Composable
fun NavigationSettings(
    currentMode: ReaderTapNavigationMode,
    onModeChange: (ReaderTapNavigationMode) -> Unit,
) {
    val strings = LocalStrings.current.reader
    val modes = listOf(
        Triple(LEFT_RIGHT, strings.modeLeftRight, strings.modeLeftRightDesc),
        Triple(RIGHT_LEFT, strings.modeRightLeft, strings.modeRightLeftDesc),
        Triple(HORIZONTAL_SPLIT, strings.modeHorizontalSplit, strings.modeHorizontalSplitDesc),
        Triple(REVERSED_HORIZONTAL_SPLIT, strings.modeReversedHorizontalSplit, strings.modeReversedHorizontalSplitDesc),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = strings.tapNavigation,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        modes.forEach { (mode, label, description) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (mode == currentMode),
                        onClick = { onModeChange(mode) },
                        role = Role.RadioButton
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RadioButton(
                    selected = (mode == currentMode),
                    onClick = null // null recommended for accessibility with screen readers
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TapNavigationDiagram(mode = mode)
            }
        }
    }
}
