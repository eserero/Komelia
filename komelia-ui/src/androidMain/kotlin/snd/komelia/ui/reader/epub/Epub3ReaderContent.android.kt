package snd.komelia.ui.reader.epub

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import snd.komelia.settings.model.Epub3NativeSettings
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import com.storyteller.reader.EpubView
import snd.komelia.ui.platform.BackPressHandler
import snd.komelia.ui.reader.epub.audio.AudioFullScreenPlayer
import snd.komelia.ui.reader.epub.audio.AudioMiniPlayer

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
actual fun Epub3ReaderContent(state: EpubReaderState) {
    val activity = LocalContext.current as FragmentActivity
    val epub3State = state as? Epub3ReaderState

    val settingsFlow = remember(epub3State) {
        epub3State?.settings ?: MutableStateFlow(Epub3NativeSettings())
    }
    val settings by settingsFlow.collectAsState()
    val themeBgColor = Color(settings.theme.background)

    var showFullPlayer by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(themeBgColor)) {
        AndroidView(
            factory = { ctx ->
                EpubView(context = ctx, activity = activity).also { view ->
                    epub3State?.onEpubViewCreated(view)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp, bottom = 66.dp)
        )

        if (epub3State != null) {
            val showControls by epub3State.showControls.collectAsState()
            val showSettings by epub3State.showSettings.collectAsState()
            val showToc by epub3State.showToc.collectAsState()
            val toc by epub3State.tableOfContents.collectAsState()
            val positions by epub3State.positions.collectAsState()
            val controller by epub3State.mediaOverlayController.collectAsState()

            val density = LocalDensity.current
            var cardHeightPx by remember { mutableStateOf(0) }
            val audioPlayerBottomPadding by animateDpAsState(
                targetValue = if (showControls && positions.isNotEmpty()) {
                    with(density) { cardHeightPx.toDp() } + 10.dp
                } else {
                    10.dp
                },
                label = "AudioPlayerBottomPadding"
            )

            if (showControls) {
                // Scrim — tap outside dismisses
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { epub3State.toggleControls() }
                )
                // Top bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                        .background(MaterialTheme.colorScheme.surface)
                        .statusBarsPadding()
                ) {
                    IconButton(onClick = { epub3State.closeWebview() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Leave")
                    }
                    val book by epub3State.book.collectAsState()
                    Text(
                        text = book?.metadata?.title ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Bottom navigation card
            if (positions.isNotEmpty()) {
                AnimatedVisibility(
                    visible = showControls,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it }),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Epub3ControlsCard(
                        state = epub3State,
                        onDismiss = { epub3State.toggleControls() },
                        onCardHeightChanged = { cardHeightPx = it },
                        onSettingsClick = { epub3State.toggleSettings() },
                        onChapterClick = { epub3State.toggleToc() },
                    )
                }
            }

            // SharedTransitionLayout fills the full screen so shared elements have the full
            // coordinate space to fly between the mini-player pill and the full-screen sheet.
            controller?.let { ctrl ->
                val book by epub3State.book.collectAsState()
                val currentLocator by epub3State.currentLocator.collectAsState()

                val chapterTitle = remember(currentLocator, toc) {
                    currentLocator?.let { loc ->
                        loc.title
                            ?: findTocLink(toc, loc.href.toString())?.title
                            ?: loc.href.toString()
                                .substringAfterLast('/').substringBeforeLast('.')
                                .replace('-', ' ').replace('_', ' ')
                    } ?: ""
                }

                SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Mini player at bottom — fades out as shared elements morph upward
                        AnimatedVisibility(
                            visible = !showFullPlayer,
                            enter = fadeIn(tween(300)),
                            exit = fadeOut(tween(200)),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = audioPlayerBottomPadding),
                        ) {
                            AudioMiniPlayer(
                                controller = ctrl,
                                bookId = epub3State.bookId.value,
                                bookTitle = book?.metadata?.title ?: "",
                                chapterTitle = chapterTitle,
                                onCoverClick = { showFullPlayer = true },
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedVisibilityScope = this,
                            )
                        }

                        // Full-screen player — sharedBounds on its Surface drives the animation
                        AnimatedVisibility(
                            visible = showFullPlayer,
                            enter = EnterTransition.None,
                            exit = ExitTransition.None,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            AudioFullScreenPlayer(
                                controller = ctrl,
                                bookId = epub3State.bookId.value,
                                bookTitle = book?.metadata?.title ?: "",
                                chapterTitle = chapterTitle,
                                positions = positions,
                                currentLocator = currentLocator,
                                onNavigateToPosition = epub3State::navigateToPosition,
                                onDismiss = { showFullPlayer = false },
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedVisibilityScope = this,
                                modifier = Modifier.fillMaxSize().align(Alignment.BottomCenter),
                            )
                        }
                    }
                }
            }

            // Settings card
            AnimatedVisibility(
                visible = showSettings,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Epub3SettingsCard(
                    settings = settings,
                    onSettingsChange = epub3State::updateSettings,
                    onDismiss = { epub3State.toggleSettings() },
                )
            }

            // TOC dialog
            if (showToc) {
                Epub3TocDialog(
                    toc = toc,
                    onNavigate = { link ->
                        epub3State.navigateToLink(link)
                        epub3State.showToc.value = false
                    },
                    onDismiss = { epub3State.showToc.value = false },
                )
            }
        }
    }

    BackPressHandler {
        if (showFullPlayer) showFullPlayer = false
        else state.onBackButtonPress()
    }
}
