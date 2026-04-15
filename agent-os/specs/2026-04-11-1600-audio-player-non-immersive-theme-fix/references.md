# References

## ReaderControlsCard (canonical pattern to replicate)

- **Location:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/reader/ReaderControlsCard.kt`
- **Relevance:** The control panel both players should visually match when immersive is OFF
- **Key pattern:**
  ```kotlin
  val hazeState = LocalHazeState.current
  val theme = LocalTheme.current
  val hazeStyle = if (hazeState != null) HazeMaterials.thin(theme.colorScheme.surface.copy(alpha = 0.4f)) else null
  if (hazeState != null && hazeStyle != null) {
      Box(Modifier.matchParentSize().shadow(...).clip(...).hazeEffect(hazeState) { style = hazeStyle })
  } else {
      Box(Modifier.matchParentSize().shadow(...).background(MaterialTheme.colorScheme.surface, shape))
  }
  ```

## Epub3ReaderContent.android.kt (haze setup context)

- **Location:** `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/Epub3ReaderContent.android.kt`
- **Relevance:** Shows how `readerHazeState` is created and provided, and how `playerBackgroundColor` is computed
- **Key lines:**
  - Line 93: `val readerHazeState = if (theme.transparentBars) rememberHazeState() else null`
  - Line 95: `CompositionLocalProvider(LocalHazeState provides readerHazeState)`
  - Line 103: epub content wrapped with `.hazeSource(readerHazeState)` — this is what hazeEffect blurs
  - Lines 248–252: `playerBackgroundColor` = immersive tint when ON, `surface` when OFF
