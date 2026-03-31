# Fix Audio Mini Player in New UI 2

## Objective
Update the `AudioMiniPlayer` in the Epub3 reader to fit the style of the new control card when "New UI 2" mode is enabled. Specifically, it should adopt the exact width, horizontal padding (16.dp), and corner rounding (28.dp) used by the new `ReaderControlsCard`.

## Key Files & Context
- `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/audio/AudioMiniPlayer.kt`: Defines the mini player UI.
- `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/Epub3ReaderContent.android.kt`: Integrates the mini player into the epub reader screen.

## Implementation Steps

### 1. Update `AudioMiniPlayer.kt`
- Add `useNewUI2: Boolean = false` to the `AudioMiniPlayer` composable signature.
- Change the hardcoded `val pillShape = RoundedCornerShape(50)` to determine the shape dynamically:
  `val shape = if (useNewUI2) RoundedCornerShape(28.dp) else RoundedCornerShape(50)`
- Update the `Surface` and `OverlayClip` to use the dynamic `shape` instead of `pillShape`.

### 2. Update `Epub3ReaderContent.android.kt`
- In the `AnimatedVisibility` wrapping the `AudioMiniPlayer`, update the modifier to apply the correct width and padding based on `useNewUI2`:
  ```kotlin
  modifier = Modifier
      .align(Alignment.BottomCenter)
      .then(if (useNewUI2) Modifier.fillMaxWidth().padding(horizontal = 16.dp) else Modifier.padding(horizontal = 2.dp))
      .padding(bottom = audioPlayerBottomPadding),
  ```
- Pass `useNewUI2 = useNewUI2` to the `AudioMiniPlayer` instantiation inside the block.

## Verification
- Run the app and open an epub book with audio in standard mode to ensure the mini player remains a round pill shape and is edge-to-edge.
- Toggle "New UI 2" in the settings, open the same epub book, and verify the mini player matches the width and corner radius (28.dp) of the main control card.
