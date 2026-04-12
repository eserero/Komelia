package snd.komelia.ui.reader.epub

import androidx.compose.runtime.Composable
import snd.komelia.ui.common.components.LoadingMaxSizeIndicator

@Composable
actual fun EpubLoadingScreen(steps: List<EpubLoadingStep>, bookTitle: String?) {
    LoadingMaxSizeIndicator()
}
