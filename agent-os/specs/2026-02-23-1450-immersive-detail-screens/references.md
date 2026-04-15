# References for Immersive Detail Screens

## Similar Implementations

### Series Detail Screen (existing)

- **Location:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/series/`
  - `SeriesScreen.kt` — Screen wrapper, Voyager Screen impl, navigation callbacks
  - `SeriesViewModel.kt` — State management, SSE events, download, menu actions
  - `view/SeriesContent.kt` — Main layout composable, tabs, book grid
- **Relevance:** All series logic (toolbar, metadata card, books grid, collections) is reused as-is inside the new immersive card
- **Key patterns:**
  - `seriesMenuActions()` provides the 3-dot menu items — reuse directly
  - `SeriesDescriptionRow`, `SeriesChipTags` composables move into the scrollable card
  - `SeriesBooksContent` / `SeriesCollectionsContent` go into the expandable card section
  - `onDownload = vm::onDownload` is the existing series download callback

### Book Detail Screen (existing)

- **Location:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/book/`
  - `BookScreen.kt` — Screen wrapper, `bookScreen()` factory function
  - `BookViewModel.kt` — Download, read lists, siblings context, SSE events
  - `BookScreenContent.kt` — Main layout
  - `BookInfoContent.kt` — `BookInfoRow`, `BookInfoColumn`, `BookReadListsContent`
- **Relevance:** All book metadata, download, and read actions are reused
- **Key patterns:**
  - `BookReadButton` in `common/BookReadButton.kt` — already handles incognito split; adapt into new split pill FAB
  - `BookSiblingsContext.Series` is available to determine if pager should be shown
  - `bookMenuActions` provides 3-dot menu items — reuse

### Oneshot Detail Screen (existing)

- **Location:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/oneshot/`
  - `OneshotScreen.kt`, `OneshotViewModel.kt`, `OneshotScreenContent.kt`
- **Relevance:** Hybrid series + book — `SeriesDescriptionRow` + `BookInfoRow` both appear in card
- **Key patterns:**
  - Existing `OneshotActionsMenu` provides 3-dot items; Edit already in the VM

### Platform Type Detection

- **Location:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/platform/PlatformType.kt`
- **Relevance:** `LocalPlatform.current == PlatformType.MOBILE` gates the new UI
- **Key patterns:** `LocalPlatform` is a `compositionLocalOf`, read in Screen's `Content()`

### New UI Toggle

- **Location:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/CompositionLocals.kt`
  - `val LocalUseNewLibraryUI = compositionLocalOf { true }`
- **Relevance:** Combined check `LocalPlatform == MOBILE && LocalUseNewLibraryUI` enables new screens

### Existing Color Presets (Appearance Settings)

- **Location:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/settings/appearance/AppearanceSettingsContent.kt`
- **Relevance:** Shows the existing pattern for color swatches; card color in new UI replaces manual selection with palette extraction

### Platform-specific expect/actual Pattern

- **Location:** `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/platform/PlatformTheme.android.kt`
- **Relevance:** Model for the `expect fun extractDominantColor(...)` expect/actual pattern
- **Key patterns:** `expect` in `commonMain`, `actual` in `androidMain` / `jvmMain` / `wasmJsMain`

### Coil Image Loading

- **Location:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/common/images/ThumbnailImage.kt`
- **Relevance:** Shows how to use `AsyncImagePainter.onState` / `rememberAsyncImagePainter` for callback-based image loading; needed for Palette extraction on successful load

### Existing FAB / Pill Button Reference

- **Location:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/common/BookReadButton.kt`
- **Relevance:** Existing split read button with incognito option — adapt the visual/UX for the new floating pill FAB design

### Compose Multiplatform Version

- `compose-multiplatform = "1.11.0-alpha01"` — `SharedTransitionLayout` + `Modifier.sharedElement()` are available in this version (Compose Foundation 1.7+)
