# EPUB3 Audio & Text Synchronization Investigation Plan

## Objective
Investigate and diagnose the root causes of the EPUB3 reader's audio and text synchronization issues. Specifically, we aim to understand why:
1. Audio restarts from the beginning when a book is re-opened.
2. Navigation works for text but is sometimes ignored by the audio player.
3. The system experiences a "warm-up" period where synchronization fails initially but suddenly starts working perfectly after some manual interaction.

## Background & Motivation
The EPUB3 reader uses `EpubView` (Readium) for text and `MediaOverlayController` (backed by `AudiobookPlayer` and an ExoPlayer Service) for audio. The suspected causes for the reported issues are:
*   **Initialization Race Conditions:** The audio controller initializes asynchronously. Early navigation events might be swallowed because the controller isn't ready.
*   **ExoPlayer Playlist Clearing:** `AudiobookPlayer.loadTracks` calls `unload()`, which clears the active background session's playlist, causing playback to restart from index 0.
*   **Aggressive Ignore Guards:** The controller has intentional guards (e.g., waiting for playback, checking for spanning paragraphs) that might be falsely triggering and ignoring legitimate user navigation.
*   **Database/Clip Lookup Failures:** The mapping between text fragments and audio timestamps might be missing or delayed during the initial load.

To prove these theories, we need to inject surgical diagnostic logging into the "handshake" points between the text viewer and the audio controller.

## Proposed Diagnostic Logging Implementation

We will add `[epub3-diag]` prefixed logs to the following files to trace the lifecycle of a navigation event and see exactly where it gets dropped.

### 1. `Epub3ReaderState.kt` (The Coordinator)
Tracks the initial load and the exact moment the text viewer reports a move.

*   **In `initialize()`:**
    ```kotlin
    logger.info { "[EPUB-DIAG] [INIT] BookID: ${bookId.value.value} | SavedLocator: ${savedLocator?.href}#${savedLocator?.locations?.fragments?.firstOrNull()}" }
    ```
*   **In `onEpubViewCreated()`:**
    ```kotlin
    logger.info { "[EPUB-DIAG] [VIEW-READY] SavedLocator: ${savedLocator?.href} | ControllerReady: ${mediaOverlayController.value != null}" }
    ```
*   **In `EpubViewListener.onLocatorChange()`:**
    ```kotlin
    logger.info { "[EPUB-DIAG] [TEXT-MOVE] NewLocator: ${locator.href}#${locator.locations.fragments.firstOrNull()} | ControllerReady: ${mediaOverlayController.value != null}" }
    ```

### 2. `MediaOverlayController.kt` (The Bridge)
Tracks how the audio controller processes or ignores the text's movements.

*   **In `initialize()`:**
    ```kotlin
    logger.info { "[EPUB-DIAG] [AUDIO-READY] ClipsLoaded: ${clips.size} | TracksCreated: ${loadedTracks.size}" }
    ```
*   **In `handleUserLocatorChange()`:**
    ```kotlin
    if (!_isPlaying.value) {
        logger.info { "[EPUB-DIAG] [AUDIO-IGNORE] Reason: PAUSED | Action: Stored in pendingUserLocator -> ${locator.href}" }
        // ... existing code ...
    }
    ```
*   **In `handlePlayingLocatorChange()` (Adjacent Clip Check):**
    ```kotlin
    logger.info { "[EPUB-DIAG] [SPAN-CHECK] CurrentIdx: $currentIdx | NewIdx: $newIdx | Fragment: $fragmentId" }
    ```
*   **Inside `checkIsEntirelyOnScreen` callback:**
    ```kotlin
    if (!isOnScreen) {
        logger.info { "[EPUB-DIAG] [AUDIO-IGNORE] Reason: SPANNING PARAGRAPH | Fragment: $fragmentId is not fully on screen." }
        // ... existing code ...
    }
    ```
*   **In `doSeekToLocator()`:**
    ```kotlin
    val clip = findClipForLocator(locator)
    logger.info { "[EPUB-DIAG] [AUDIO-LOOKUP] TargetLocator: ${locator.href} | ClipFound: ${clip != null} | StartTime: ${clip?.start}" }
    ```
*   **In `Listener.onClipChanged()`:**
    ```kotlin
    logger.info { "[EPUB-DIAG] [AUDIO-MOVE] Audio playing clip: ${overlayPar.audioResource} at ${overlayPar.start}s -> Target Text: ${overlayPar.locator.href}" }
    ```

### 3. `AudiobookPlayer.kt` (The Engine)
Tracks the destruction of the audio session.

*   **In `loadTracks()`:**
    ```kotlin
    logger.info { "[EPUB-DIAG] [PLAYER-RESET] Calling unload() - clearing existing ExoPlayer session." }
    ```

## How to Read the Results

After compiling with these logs, filter logcat using `adb logcat -s EPUB-DIAG`. Perform the actions that cause issues (open book, navigate immediately, start playback, etc.) and look for these patterns:

### Pattern A: The "Cold Start" Ignore
*   `[TEXT-MOVE] NewLocator: ... | ControllerReady: false`
*   **Verdict:** The user navigated before the background audio initialization finished. The event was swallowed.

### Pattern B: The "Wait for Play" Ignore
*   `[TEXT-MOVE] NewLocator: ... | ControllerReady: true`
*   `[AUDIO-IGNORE] Reason: PAUSED | Action: Stored in pendingUserLocator`
*   **Verdict:** Normal behavior, but confirms the audio player received the location and is waiting for the user to press Play. If it doesn't jump when Play is pressed, the bug is in the toggle play logic.

### Pattern C: The "Missing Map" Ignore
*   `[TEXT-MOVE] NewLocator: ...`
*   `[AUDIO-LOOKUP] TargetLocator: ... | ClipFound: false`
*   **Verdict:** The text moved, but `findClipForLocator` failed to find a matching audio timestamp in the database for that specific HTML ID or progression.

### Pattern D: The "Spanning Bug"
*   `[TEXT-MOVE] NewLocator: ...`
*   `[SPAN-CHECK] CurrentIdx: 5 | NewIdx: 6`
*   `[AUDIO-IGNORE] Reason: SPANNING PARAGRAPH`
*   **Verdict:** The JavaScript `isEntirelyOnScreen` function incorrectly reported that the previous paragraph was still visible, causing the audio to refuse to seek to the new page.

### Pattern E: The "Reset Loop"
*   `[INIT] SavedLocator: chapter3.xhtml`
*   `[PLAYER-RESET] Calling unload()...`
*   `[AUDIO-MOVE] Audio playing clip: chapter1.mp3 at 0.0s`
*   **Verdict:** The `unload()` call wiped the session, destroying the background player's state and forcing it to start from the beginning, overriding the `SavedLocator`.

## Next Steps
1. Add the logging statements to the respective files.
2. Build and deploy the app (`./gradlew :komelia-app:assembleDebug`).
3. Reproduce the exact user flow while monitoring the `EPUB-DIAG` tags.
4. Use the identified failure pattern to implement a targeted fix (e.g., delaying `onLocatorChange` until the controller is ready, fixing the `unload()` logic in `AudiobookPlayer`, or adjusting the spanning paragraph JavaScript).
