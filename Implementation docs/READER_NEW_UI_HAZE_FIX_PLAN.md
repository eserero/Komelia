# Reader New UI — Frosted Glass & Status Bar Fix Plan

## Context

Four bugs found after the initial reader new-UI implementation (spec: `agent-os/specs/2026-03-30-reader-new-ui-controls`):

1. Settings panel / controls card has no frosted glass and appears pinkish in modern themes
2. Status bar area is transparent in the reader top bar (no frosted glass); epub3 status bar disappears entirely when controls open
3. Upscaling indicator not shown in the new top bar (only existed in old top bar)
4. Audio mini player is covered by the epub3 controls card (padding not updated for new UI)

**Issues 3 and 4 are DONE. Issues 1 and 2 still need the hazeSource fix.**

---

## Root Causes

### Issues 1 + 2: No hazeSource in reader screens (CONFIRMED TRUE ROOT CAUSE)

LibraryScreen/HomeScreen use this pattern:
```
Box(fillMaxSize — NO hazeSource) {
    Box(fillMaxSize + hazeSource(screenHazeState)) {
        scrollable page content   // content to blur THROUGH
    }
    NewTopAppBar(...)             // OUTSIDE hazeSource — sibling with hazeEffect
}
```

Reader screens have NO `hazeSource` at all on the content boxes. The `hazeEffect` elements (ReaderTopBar, ReaderControlsCard) have nothing to blur through, so they render transparent/wrong color.

Additionally, the first fix attempt removed the old broken `Modifier.haze()` calls but never added proper `hazeSource` — the frosted glass still doesn't work.

The epub3 status bar disappearing is a separate issue: when controls open, `setFullscreen(true)` stays active (hides system bars). Need a `DisposableEffect(showControls)` to call `setFullscreen(false)` while controls are showing.

### Issues 3 + 4: Already fixed
- Upscaling indicator: Added to `ReaderTopBar` with `AnimatedVisibility`; connected in `ReaderContent.kt`; duplicate removed from `BottomSheetSettingsOverlay.kt`
- Audio player: `onCardHeightChanged` added to `Epub3ControlsCardNewUI` and connected in `Epub3ReaderContent.android.kt`

---

## Remaining Changes

### 1. `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/reader/image/common/ReaderContent.kt`

Re-add `LocalHazeState` import + `hazeSource` import. Re-add `val hazeState = LocalHazeState.current`.

Restructure the outer Box so the reader pages `when` block is wrapped in its own hazeSource Box, and overlays are siblings:

```kotlin
Box(Modifier.fillMaxSize()...) {  // outer box — NO hazeSource

    // INNER content box — has hazeSource
    Box(Modifier.fillMaxSize().then(
        if (hazeState != null) Modifier.hazeSource(hazeState) else Modifier
    )) {
        when (commonReaderState.readerType...) {
            PAGED -> PagedReaderContent(...)
            CONTINUOUS -> ContinuousReaderContent(...)
            PANELS -> PanelsReaderContent(...)
        }
    }

    // Context menu, SettingsOverlay, ReaderTopBar, EInkFlashOverlay — ALL outside hazeSource
    Box(Modifier.offset { ... }) { AnimatedDropdownMenu(...) }
    SettingsOverlay(...)
    if (showSettingsMenu && useNewUI2) { ReaderTopBar(...) }
    EInkFlashOverlay(...)
}
```

Note: Keep the `areaSize == IntSize.Zero` early-return BEFORE the inner box, since it needs the outer Box scope.

### 2. `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/Epub3ReaderContent.android.kt`

Re-add imports: `snd.komelia.ui.LocalHazeState`, `dev.chrisbanes.haze.hazeSource`. Re-add `val hazeState = LocalHazeState.current`.

Wrap only the `AndroidView(EpubView)` in a hazeSource Box:
```kotlin
Box(Modifier.fillMaxSize().then(
    if (hazeState != null) Modifier.hazeSource(hazeState) else Modifier
)) {
    AndroidView(factory = { ... }, modifier = Modifier.fillMaxSize().padding(top = 56.dp, bottom = 66.dp))
}
```

All UI overlays (info bar, controls card, top bar, audio player, settings card, TOC dialog) stay OUTSIDE this Box.

Also add `DisposableEffect(showControls)` to fix status bar disappearing when controls open:
```kotlin
if (LocalPlatform.current == MOBILE) {
    val windowState = LocalWindowState.current
    DisposableEffect(showControls) {
        if (showControls) windowState.setFullscreen(false)
        else windowState.setFullscreen(true)
        onDispose { windowState.setFullscreen(true) }
    }
}
```

This needs: `import snd.komelia.ui.LocalWindowState`, `import snd.komelia.ui.LocalPlatform`, `import snd.komelia.ui.platform.PlatformType.MOBILE`, `import androidx.compose.runtime.DisposableEffect`.

---

## Already-Done Changes (no re-work needed)

- `Epub3ControlsCard.kt`: `onCardHeightChanged` param added + `onSizeChanged` modifier ✅
- `ReaderTopBar.kt`: `upscaleActivities` param + `AnimatedVisibility(UpscaleActivityIndicator)` + fixed height in Column ✅
- `ReaderContent.kt`: Removed old broken `haze()` call; upscaleActivities collected and passed ✅
- `Epub3ReaderContent.android.kt`: Removed old broken `haze()` call; `onCardHeightChanged` connected ✅
- `BottomSheetSettingsOverlay.kt`: Duplicate upscale indicator removed from useNewUI2 branch ✅

---

## Execution Order

1. `ReaderContent.kt` — add hazeSource to inner content Box
2. `Epub3ReaderContent.android.kt` — add hazeSource to EpubView Box + DisposableEffect for fullscreen

---

## Verification

Per `BUILDING.md`:
1. Build debug APK and install via ADB
2. Open image reader in a modern theme → open settings overlay → verify frosted glass (no pink tint, blurs book content)
3. Verify top bar has frosted glass covering status bar (matches library screen appearance)
4. Start NCNN upscaling → verify upscaling indicator appears in top bar
5. Open epub3 reader → open controls → verify status bar still visible with frosted glass
6. Open epub3 audiobook → open controls → verify audio mini player sits ABOVE the controls card
