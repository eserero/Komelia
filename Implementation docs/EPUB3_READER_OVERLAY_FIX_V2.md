# EPUB3 Reader Gesture and Overlay Fix (V2)

## Objective
1. Allow swipe and scroll gestures to reach the EPUB3 reader even when the control panel is visible.
2. Remove the redundant and blocking full-screen overlay.
3. Improve responsiveness of dismissing the controls by removing unnecessary delays.

## Key Files & Context
- `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/Epub3ReaderContent.android.kt`: Contains the UI structure. I will remove the blocking overlay here.
- `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/Epub3ReaderState.kt`: Handles the tap events from the reader. I will remove the artificial delay here.
- `epub-reader/src/main/java/com/storyteller/reader/EpubView.kt`: The underlying view that handles gestures and distinguishes taps from swipes.

## Implementation Steps
1. **Modify `Epub3ReaderContent.android.kt`**:
   - Remove the `if (showControls) { Box(...) }` block entirely. This block was added to detect taps but was blocking swipes.
   - Clean up unnecessary imports (`detectTapGestures`, `pointerInput`, `clickable`).

2. **Modify `Epub3ReaderState.kt`**:
   - Update `onMiddleTouch()` in `onEpubViewCreated`.
   - Remove the `delay(400L)`. The Javascript side in `EpubView.kt` already provides a 350ms window for double-taps, making the Kotlin delay redundant and slow.
   - Ensure that any tap while controls/settings/TOC are open immediately closes everything.

## Verification & Testing
- Open an EPUB book.
- Tap the center to open controls.
- **Verify** you can swipe pages or scroll while controls are visible.
- **Verify** that a single tap on the reader area closes controls immediately.
- **Verify** that double-tap still works for audio (if available in the book).
