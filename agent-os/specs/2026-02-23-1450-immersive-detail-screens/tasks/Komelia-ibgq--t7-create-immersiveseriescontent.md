---
# Komelia-ibgq
title: 'T7: Create ImmersiveSeriesContent'
status: completed
type: task
priority: normal
created_at: 2026-02-23T12:02:47Z
updated_at: 2026-02-24T15:21:22Z
parent: Komelia-uler
---

New immersive layout for Series detail, wired into SeriesScreen.

**Files:**
- `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/series/immersive/ImmersiveSeriesContent.kt` (new)
- `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/series/SeriesScreen.kt` (modify)

**Signature:**
```kotlin
fun ImmersiveSeriesContent(
    series: KomgaSeries,
    library: KomgaLibrary?,
    onLibraryClick: (KomgaLibrary) -> Unit,
    seriesMenuActions: SeriesMenuActions,
    onFilterClick: (SeriesScreenFilter) -> Unit,
    currentTab: SeriesTab,
    onTabChange: (SeriesTab) -> Unit,
    booksState: SeriesBooksState,
    onBookClick: (KomeliaBook) -> Unit,
    onBookReadClick: (KomeliaBook, Boolean) -> Unit,
    collectionsState: SeriesCollectionsState,
    onCollectionClick: (KomgaCollection) -> Unit,
    onSeriesClick: (KomgaSeries) -> Unit,
    onBackClick: () -> Unit,
    onDownload: () -> Unit,
)
```

**ImmersiveDetailScaffold usage:**
- `coverData = SeriesDefaultThumbnailRequest(series.id)`
- `coverKey = series.id.value`
- `cardColor = null` (will be wired to Palette in T4)

**Card content layout** (all in the `cardContent` ColumnScope slot):
```
Text(series.metadata.title, style=headlineMedium, padding 16dp)
SeriesDescriptionRow(library, onLibraryClick, series metadata fields)
SeriesSummary(series.metadata.summary, "", "")
HorizontalDivider()
SeriesChipTags(series metadata — publisher/genres/tags/authors)
HorizontalDivider()
TabRow(currentTab, onTabChange)
// Tab content fills remaining space:
when(currentTab) {
    BOOKS -> SeriesBooksContent(...)   // has its own LazyVerticalGrid
    COLLECTIONS -> SeriesCollectionsContent(...)
}
```

**Top bar content** (back + 3-dot menu):
```
Row(fillMaxWidth, spacedBetween) {
    IconButton(onClick=onBackClick) { Icon(ArrowBack) }
    SeriesActionsMenu(series, actions, showEditOption=true, showDownloadOption=false)
}
```
Note: Edit is now in the 3-dot menu (`showEditOption=true`). Download goes to the FAB.

**FAB:** `ImmersiveDetailFab(onReadClick, onReadIncognitoClick, onDownloadClick=onDownload)`
- `onReadClick`: first unread book in `booksState`
- `onReadIncognitoClick`: same book, incognito flag

**SeriesScreen.Content() modification:**
```kotlin
val platform = LocalPlatform.current
val useNewUI = LocalUseNewLibraryUI.current
if (platform == PlatformType.MOBILE && useNewUI) {
    ImmersiveSeriesContent(...)
} else {
    SeriesContent(...)  // unchanged
}
```

**Subtasks:**
- [x] Create directory `series/immersive/`
- [x] Create `ImmersiveSeriesContent.kt` using `ImmersiveDetailScaffold`
- [x] Card content: title text + `SeriesDescriptionRow` + `SeriesSummary` + `SeriesChipTags` + `TabRow` + books/collections content
- [x] Top bar: back `IconButton` + `SeriesActionsMenu(showEditOption=true, showDownloadOption=false, expanded=...)`
- [x] FAB: `ImmersiveDetailFab` with read/incognito/download actions (read = first unread book)
- [x] Modify `SeriesScreen.Content()`: add `if (MOBILE && newUI)` branch
- [x] Import `LocalPlatform`, `LocalUseNewLibraryUI`, `PlatformType` in `SeriesScreen.kt`

## Summary of Changes

Implemented layout fixes, download dialog, and multi-select bars for ImmersiveSeriesContent:

- **Change 1**: Title uses 2/3 of headlineMedium fontSize (~18.7sp instead of 28sp)
- **Change 2**: Writers + year row added below title (from `series.booksMetadata.authors`), shows e.g. `Stan Lee (1963)`
- **Change 3**: New layout order — title → writers+year → description row (no thumbnailOffset, showReleaseYear=false) → summary → chip tags → divider → tab row → tab content; both extra HorizontalDividers removed
- **Change 4**: Download FAB now gates on `showDownloadConfirmationDialog` flag; DownloadNotificationRequestDialog + ConfirmationDialog shown before triggering download
- **Change 5**: topBarContent conditionally shows BulkActionsContainer (with select-all / cancel / count) when in selection mode; BottomPopupBulkActionsPanel with BooksBulkActionsContent shown when books are selected
- **Fix**: `SeriesDescriptionRow` got `showReleaseYear: Boolean = true` parameter; immersive screen passes `showReleaseYear=false` to avoid duplicate year display
