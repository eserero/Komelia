package snd.komelia.ui.reader.epub

import androidx.compose.runtime.Composable

@Composable
expect fun EpubLoadingScreen(steps: List<EpubLoadingStep>, bookTitle: String?)
