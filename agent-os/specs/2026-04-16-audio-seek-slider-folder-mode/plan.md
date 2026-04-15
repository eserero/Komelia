# Fix: Audio Seek Slider in Expanded Player (Folder Mode)

## Context

The expanded audio player (`AudioFullScreenPlayer`) shows a page-navigation slider
(`Epub3PageNavigatorRow`) whenever `positions.size > 1`. For SMIL-synchronized epubs
this is correct — the slider navigates epub reading positions. But for folder-mode
audiobooks (non-SMIL), the epub still has chapter positions, so `positions.size > 1`
is true and the same slider appears — but it turns epub pages instead of seeking audio.

The fix: detect folder mode via `audioTracks.isNotEmpty()` and show an audio seek slider
(position within the current track) instead. The epub location string above the slider
remains unchanged (informational only). SMIL mode is untouched.

---

## Files to Modify

### 1. `AudioFullScreenPlayer.kt`
**Path:** `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/audio/AudioFullScreenPlayer.kt`

**Step 1 — Add two parameters to the composable function signature:**
```kotlin
currentAudioTrackIndex: Int = 0,
onSeekToTrackPosition: ((trackIndex: Int, positionSeconds: Double) -> Unit)? = null,
```

**Step 2 — Add the following imports if not already present:**
```kotlin
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
```
(`remember`, `mutableStateOf`, `getValue`, `setValue` may already be imported — check before adding.)

**Step 3 — Replace the existing slider block (currently reads):**
```kotlin
// Page slider — only in SMIL mode
if (positions.size > 1) {
    Epub3PageNavigatorRow(
        positions = positions,
        currentLocator = currentLocator,
        onNavigateToPosition = onNavigateToPosition,
        modifier = fadeModifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .padding(top = 16.dp),
    )
}
```

**Replace with:**
```kotlin
if (audioTracks.isNotEmpty() && onSeekToTrackPosition != null) {
    // Folder-mode: seek within current track
    val prevTrackDuration = audioTracks.take(currentAudioTrackIndex).sumOf { it.durationSeconds }
    val currentTrackDuration = audioTracks.getOrNull(currentAudioTrackIndex)?.durationSeconds ?: 0.0
    val positionInTrack = (elapsedSeconds - prevTrackDuration).coerceIn(0.0, currentTrackDuration)

    var sliderDraft by remember(currentAudioTrackIndex) { mutableStateOf(positionInTrack.toFloat()) }
    var isInteracting by remember { mutableStateOf(false) }

    AppSlider(
        value = if (isInteracting) sliderDraft else positionInTrack.toFloat(),
        onValueChange = { isInteracting = true; sliderDraft = it },
        onValueChangeFinished = {
            isInteracting = false
            onSeekToTrackPosition(currentAudioTrackIndex, sliderDraft.toDouble())
        },
        valueRange = 0f..currentTrackDuration.toFloat().coerceAtLeast(1f),
        accentColor = accentColor,
        colors = AppSliderDefaults.colors(accentColor = accentColor),
        modifier = fadeModifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .padding(top = 16.dp),
    )
} else if (positions.size > 1) {
    // SMIL mode: navigate epub reading position
    Epub3PageNavigatorRow(
        positions = positions,
        currentLocator = currentLocator,
        onNavigateToPosition = onNavigateToPosition,
        modifier = fadeModifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .padding(top = 16.dp),
    )
}
```

---

### 2. `Epub3ReaderContent.android.kt`
**Path:** `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/Epub3ReaderContent.android.kt`

In the `AudioFullScreenPlayer(...)` call (search for `AudioFullScreenPlayer(`), add
two parameters alongside the existing ones:

```kotlin
currentAudioTrackIndex = currentAudioTrackIndex,
onSeekToTrackPosition = { trackIndex, positionSeconds ->
    folderController?.seekToTrackPosition(trackIndex, positionSeconds)
},
```

Both `currentAudioTrackIndex` and `folderController` are already derived in the
composable's body — no new state collection needed.

---

## What is NOT Changed

- `EpubAudioController` interface — no new methods needed
- `AudiobookFolderController` — `seekToTrackPosition(index, seconds)` already exists at line ~232
- `MediaOverlayController` — SMIL mode is entirely unchanged
- The epub location string above the slider — left as-is

---

## Key Behaviour Details

- **Slider range:** `0f..currentTrackDuration` (duration of the currently playing track only, not total album)
- **Slider value while idle:** derived live from `elapsedSeconds - sum(previous track durations)`
- **Slider value while dragging:** held at `sliderDraft` to prevent polling updates from jumping the thumb
- **On release:** calls `seekToTrackPosition(currentAudioTrackIndex, sliderDraft.toDouble())`
- **Track change:** `remember(currentAudioTrackIndex)` resets `sliderDraft` when the track changes so the thumb starts at the correct position for the new track

---

## Verification

1. Build debug APK: `./gradlew :composeApp:assembleDebug`
2. Install on device and open a **folder-mode audiobook epub**
   - Expanded player should show an audio seek slider scoped to the current track
   - Dragging the slider should jump audio to that position
   - Pressing next/prev should change the slider range to the new track's duration
3. Open a **SMIL epub** (synchronized reading)
   - Expanded player should still show the `Epub3PageNavigatorRow` page slider unchanged
