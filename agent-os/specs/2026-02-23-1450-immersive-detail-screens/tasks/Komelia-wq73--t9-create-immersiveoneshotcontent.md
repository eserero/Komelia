---
# Komelia-wq73
title: 'T9: Create ImmersiveOneshotContent'
status: completed
type: task
priority: normal
created_at: 2026-02-23T12:02:47Z
updated_at: 2026-02-24T20:39:28Z
parent: Komelia-uler
---

New immersive layout for Oneshot detail (hybrid series+book), wired into OneshotScreen.

**Files:**
- `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/oneshot/immersive/ImmersiveOneshotContent.kt` (new)
- `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/oneshot/OneshotScreen.kt` (modify)

**Signature:**
```kotlin
fun ImmersiveOneshotContent(
    series: KomgaSeries,
    book: KomeliaBook,
    library: KomgaLibrary,
    onLibraryClick: (KomgaLibrary) -> Unit,
    onBookReadClick: (markReadProgress: Boolean) -> Unit,
    oneshotMenuActions: BookMenuActions,
    collections: Map<KomgaCollection, List<KomgaSeries>>,
    onCollectionClick: (KomgaCollection) -> Unit,
    onSeriesClick: (KomgaSeries) -> Unit,
    readLists: Map<KomgaReadList, List<KomeliaBook>>,
    onReadListClick: (KomgaReadList) -> Unit,
    onReadlistBookClick: (KomeliaBook, KomgaReadList) -> Unit,
    onFilterClick: (SeriesScreenFilter) -> Unit,
    onBookDownload: () -> Unit,
    onBookDownloadDelete: () -> Unit,
    onBackClick: () -> Unit,
)
```

**ImmersiveDetailScaffold usage:**
- `coverData = SeriesDefaultThumbnailRequest(series.id)` (oneshot cover = series thumbnail)
- `coverKey = series.id.value`
- `cardColor = null` (wired to Palette in T4)

**Card content layout:**
```
Text(series.metadata.title, headlineMedium, padding 16dp)
SeriesDescriptionRow(library, onLibraryClick, series status/ageRating/language/readingDir)
BookInfoRow(book, onSeriesButtonClick=null)   ← page count, release date, read progress
HorizontalDivider()
BookInfoColumn(publisher, genres, authors, tags, links, fileInfo)
HorizontalDivider()
BookReadListsContent(readLists, ...)
SeriesCollectionsContent(collections, ...)
```

**Top bar:** back `IconButton` + `BookActionsMenu(book, oneshotMenuActions, showEditOption=true, showDownloadOption=false)`

**FAB:** `ImmersiveDetailFab(onReadClick={onBookReadClick(true)}, onReadIncognitoClick={onBookReadClick(false)}, onDownloadClick=onBookDownload)`

**OneshotScreen.Content() modification:**
```kotlin
val platform = LocalPlatform.current
val useNewUI = LocalUseNewLibraryUI.current
if (platform == PlatformType.MOBILE && useNewUI && book != null && series != null && library != null) {
    ImmersiveOneshotContent(...)
} else {
    OneshotScreenContent(...)  // unchanged
}
```

**Subtasks:**
- [ ] Create directory `oneshot/immersive/`
- [ ] Create `ImmersiveOneshotContent.kt` using `ImmersiveDetailScaffold`
- [ ] Card content: title + `SeriesDescriptionRow` + `BookInfoRow` + `BookInfoColumn` + read lists + collections
- [ ] Top bar: back `IconButton` + `BookActionsMenu(showEditOption=true, showDownloadOption=false)`
- [ ] FAB: `ImmersiveDetailFab` wired to read/incognito/download
- [ ] Modify `OneshotScreen.Content()`: add `if (MOBILE && newUI && data != null)` branch

## Summary of Changes

- Created  — mirrors  structure but for oneshots:
  - No pager (single book), no series-title link
  - Header shows  only (+ writers/year line)
  -  (library, ageRating, language, readingDirection) below header
  - Summary, divider,  (with publisher/genres from series), , 
  - Fixed overlay: back button + ; FAB with read/incognito/download
  -  bottom scroll padding
- Updated : replaced boilerplate placeholder with  call, guarded by 
