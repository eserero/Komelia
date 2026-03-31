# Reader New UI — Frosted Glass & Status Bar Fix Plan (Updated)

## Context

### Why is `ReaderControlsCard` pinkish? (not `ReaderTopBar`)

`ReaderControlsCard` has `tonalElevation = 3.dp` when `hazeState == null`:

```kotlin
// ReaderControlsCard.kt
color = if (hazeState != null) Color.Transparent else MaterialTheme.colorScheme.surface,
tonalElevation = if (hazeState == null) 3.dp else 0.dp,   // ← pinkish source
```

Material 3 tonal elevation tints the surface with the **primary color**. For modern dark/wine themes where the primary is in the pink/magenta spectrum, 3dp elevation makes the card visibly pinkish. `ReaderTopBar` has **no** tonal elevation — it falls back to plain `colorScheme.surface` (correct color).

When `hazeState != null` but no `hazeSource` exists, the card uses `Color.Transparent + hazeEffect`. `HazeMaterials.thin(surface)` then renders a semi-transparent surface-color tint over the book content — also pinkish, different mechanism.

**The fix (adding a proper `hazeSource`) gives the hazeEffect something to blur through, replacing the pinkish fallback with real frosted glass.**

### Why did the previous fix not work?

The previous plan read `LocalHazeState.current` (the global MainScreen hazeState) and added `hazeSource` using that. This is unreliable: the global hazeState may be null in the reader's composition scope, or the scope may differ from what the library uses.

The working `LibraryScreen` and `HomeScreen` create a **new, per-screen `HazeState`** via `rememberHazeState()` and override `LocalHazeState` via `CompositionLocalProvider`. The reader must follow the same pattern exactly.

---

## Working Pattern to Match (Library/Home — confirmed correct)

```kotlin
// LibraryScreen.kt
val screenHazeState = if (theme.transparentBars) rememberHazeState() else null
CompositionLocalProvider(
    LocalHazeState provides screenHazeState,   // creates + overrides per-screen
) {
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier.fillMaxSize()
                .then(if (screenHazeState != null) Modifier.hazeSource(screenHazeState) else Modifier)
        ) {
            tabContent()   // content to blur THROUGH
        }
        NewTopAppBar(...)  // sibling — reads LocalHazeState.current = screenHazeState → hazeEffect
    }
}

// NewTopAppBar.kt — how status bar inset is handled (NO statusBarsPadding modifier)
Column(modifier = Modifier.fillMaxWidth().hazeEffect(hazeState)) {
    if (theme.transparentBars) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))  // INNER spacer
    }
    Row(height = 45.dp) { ... }
}
```

---

## Changes Required

### 1. `ReaderTopBar.kt` — fix status bar inset (match `NewTopAppBar` pattern)

**File:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/reader/ReaderTopBar.kt`

Replace `.statusBarsPadding()` as a Surface modifier with a `Spacer` as the first child of the Column — exactly matching `NewTopAppBar`:

**Before:**
```kotlin
Surface(
    modifier = modifier
        .fillMaxWidth()
        .then(if (hazeState != null && hazeStyle != null) Modifier.hazeEffect(hazeState) { style = hazeStyle } else Modifier)
        .statusBarsPadding()   // ← remove this
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 4.dp)) { ... }
        AnimatedVisibility(visible = upscaleActivities.isNotEmpty()) { UpscaleActivityIndicator(upscaleActivities) }
    }
}
```

**After:**
```kotlin
Surface(
    modifier = modifier
        .fillMaxWidth()
        .then(if (hazeState != null && hazeStyle != null) Modifier.hazeEffect(hazeState) { style = hazeStyle } else Modifier)
        // NO .statusBarsPadding() modifier
) {
    Column {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))   // ← add inner spacer
        Row(modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 4.dp)) { ... }
        AnimatedVisibility(visible = upscaleActivities.isNotEmpty()) { UpscaleActivityIndicator(upscaleActivities) }
    }
}
```

**Import changes:**
- Add: `androidx.compose.foundation.layout.WindowInsets`, `androidx.compose.foundation.layout.statusBars`, `androidx.compose.foundation.layout.windowInsetsTopHeight`
- Remove: `androidx.compose.foundation.layout.statusBarsPadding`

---

### 2. `ReaderContent.kt` — per-screen hazeState + CompositionLocalProvider + hazeSource

**File:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/reader/image/common/ReaderContent.kt`

**Add imports:**
```kotlin
import androidx.compose.runtime.CompositionLocalProvider
import dev.chrisbanes.haze.hazeSource
import snd.komelia.ui.LocalHazeState
import snd.komelia.ui.LocalTheme
```

In `ReaderContent`, before the outer Box, add:
```kotlin
val theme = LocalTheme.current
val readerHazeState = if (theme.transparentBars) rememberHazeState() else null
```

Wrap the outer Box with `CompositionLocalProvider`, and split the content into an inner hazeSource Box (reader pages only) with all overlays as siblings outside it:

```kotlin
CompositionLocalProvider(LocalHazeState provides readerHazeState) {
    Box(
        Modifier
            .fillMaxSize()
            .onSizeChanged { screenScaleState.setAreaSize(it) }
            .focusable()
            .focusRequester(topLevelFocus)
            .onFocusChanged { hasFocus = it.hasFocus }
            .onKeyEvent { ... }
    ) {
        val areaSize = screenScaleState.areaSize.collectAsState()
        if (areaSize.value == IntSize.Zero) {
            LoadingMaxSizeIndicator()
            return   // non-local return from ReaderContent — still works inside CompositionLocalProvider
        }

        // INNER content box — hazeSource marks book pages as the blur source
        Box(
            Modifier.fillMaxSize().then(
                if (readerHazeState != null) Modifier.hazeSource(readerHazeState) else Modifier
            )
        ) {
            when (commonReaderState.readerType.collectAsState().value) {
                PAGED -> PagedReaderContent(...)
                CONTINUOUS -> ContinuousReaderContent(...)
                PANELS -> PanelsReaderContent(...)
            }
        }

        // All overlays OUTSIDE the hazeSource box
        Box(Modifier.offset { IntOffset(contextMenuAnchorOffset.x.toInt(), contextMenuAnchorOffset.y.toInt()) }) {
            AnimatedDropdownMenu(...)
        }
        SettingsOverlay(...)
        if (showSettingsMenu && useNewUI2) {
            val book = commonReaderState.booksState.collectAsState().value?.currentBook
            val allUpscaleActivities by ncnnSettingsState.globalUpscaleActivities.collectAsState()
            ReaderTopBar(
                seriesTitle = book?.seriesTitle ?: "",
                bookTitle = book?.metadata?.title ?: "",
                onBack = onExit,
                upscaleActivities = allUpscaleActivities,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
        EInkFlashOverlay(...)
    }
}
LaunchedEffect(hasFocus) { if (!hasFocus) topLevelFocus.requestFocus() }
```

---

### 3. `Epub3ReaderContent.android.kt` — per-screen hazeState + hazeSource + DisposableEffect

**File:** `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/Epub3ReaderContent.android.kt`

**Add imports:**
```kotlin
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import dev.chrisbanes.haze.hazeSource
import snd.komelia.ui.LocalHazeState
import snd.komelia.ui.LocalPlatform
import snd.komelia.ui.LocalTheme
import snd.komelia.ui.LocalWindowState
import snd.komelia.ui.platform.PlatformType.MOBILE
```

In `Epub3ReaderContent`, add per-screen hazeState and wrap the outer Box:

```kotlin
actual fun Epub3ReaderContent(state: EpubReaderState) {
    // ... existing setup unchanged (activity, epub3State, settings, playerTransition, etc.) ...

    val theme = LocalTheme.current
    val readerHazeState = if (theme.transparentBars) rememberHazeState() else null

    CompositionLocalProvider(LocalHazeState provides readerHazeState) {
        Box(modifier = Modifier.fillMaxSize().background(themeBgColor)) {

            // INNER box with hazeSource — wraps ONLY the AndroidView (EpubView)
            Box(
                Modifier.fillMaxSize().then(
                    if (readerHazeState != null) Modifier.hazeSource(readerHazeState) else Modifier
                )
            ) {
                AndroidView(
                    factory = { ctx ->
                        EpubView(context = ctx, activity = activity).also { view ->
                            epub3State?.onEpubViewCreated(view)
                        }
                    },
                    modifier = Modifier.fillMaxSize().padding(top = 56.dp, bottom = 66.dp)
                )
            }

            // All overlays OUTSIDE hazeSource — no changes to their content, just their position
            if (epub3State != null) {
                val showControls by epub3State.showControls.collectAsState()
                // ... (all other existing state collection unchanged) ...

                // ADD: DisposableEffect to fix status bar disappearing when epub controls open
                if (LocalPlatform.current == MOBILE) {
                    val windowState = LocalWindowState.current
                    DisposableEffect(showControls) {
                        if (showControls) windowState.setFullscreen(false)
                        else windowState.setFullscreen(true)
                        onDispose { windowState.setFullscreen(true) }
                    }
                }

                // Persistent info bar — unchanged
                if (positions.isNotEmpty() && !useNewUI2) { ... }

                // Scrim + top bar when controls open — unchanged
                if (showControls) {
                    Box(scrim modifier)
                    if (useNewUI2) {
                        ReaderTopBar(...)   // now reads LocalHazeState.current = readerHazeState ✓
                    } else {
                        Row(old top bar) { ... }
                    }
                }

                // Bottom navigation card — unchanged
                if (positions.isNotEmpty()) {
                    AnimatedVisibility(visible = showControls) {
                        if (useNewUI2) {
                            Epub3ControlsCardNewUI(...)  // ReaderControlsCard inside reads readerHazeState ✓
                        } else {
                            Epub3ControlsCard(...)
                        }
                    }
                }

                // Audio player, settings card, TOC dialog — all unchanged, stay outside hazeSource
            }
        }
    }
    BackPressHandler { ... }
}
```

---

## Execution Order

1. `ReaderTopBar.kt` — fix statusBarsPadding → inner Spacer
2. `ReaderContent.kt` — add per-screen hazeState, CompositionLocalProvider, hazeSource inner box
3. `Epub3ReaderContent.android.kt` — add per-screen hazeState, CompositionLocalProvider, hazeSource inner box, DisposableEffect

---

## Verification

Per `BUILDING.md`, build debug APK and install via ADB with a **modern theme (LIGHT_MODERN or DARK_MODERN)**:

1. **Image reader — top bar**: open a book → tap center → verify `ReaderTopBar` shows frosted glass matching the library top bar (no pink tint, blurs book content including status bar area)
2. **Image reader — controls card**: open a book → tap center → open settings panel → verify `ReaderControlsCard` shows frosted glass (no pink tint, no tonal elevation artifact)
3. **NCNN upscaling**: start upscaling → verify upscaling indicator appears in `ReaderTopBar`
4. **Epub3 — top bar + status bar**: open an epub → tap → verify `ReaderTopBar` shows frosted glass + status bar remains visible (no disappearing when controls open)
5. **Epub3 audiobook**: open controls → verify audio mini player sits ABOVE the controls card
6. **Non-modern theme (DARK/LIGHT)**: verify reader falls back cleanly to opaque surface (no crash, no pink tint)
