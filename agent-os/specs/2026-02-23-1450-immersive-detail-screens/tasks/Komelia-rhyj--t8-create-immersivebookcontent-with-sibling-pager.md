---
# Komelia-rhyj
title: 'T8: Create ImmersiveBookContent with sibling pager'
status: completed
type: task
priority: normal
created_at: 2026-02-23T12:02:47Z
updated_at: 2026-02-24T16:25:45Z
parent: Komelia-uler
---

New immersive layout for Book detail with HorizontalPager cover carousel and AnimatedContent card crossfade.

**Files:**
- `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/book/immersive/ImmersiveBookContent.kt` (new)
- `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/book/BookScreen.kt` (modify)
- `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/book/BookViewModel.kt` (modify)

**Sibling books loading in BookViewModel:**

Add to `BookViewModel`:
```kotlin
val siblingBooks = MutableStateFlow<List<KomeliaBook>>(emptyList())

fun loadSiblingBooks() {
    screenModelScope.launch {
        val seriesId = book.value?.seriesId ?: return@launch
        notifications.runCatchingToNotifications {
            val page = bookApi.getBookList(
                conditionBuilder = BookConditionBuilder().apply { seriesId { isEqualTo(seriesId) } },
                pageRequest = KomgaPageRequest(size = 500, sort = listOf("metadata.numberSort,asc"))
            )
            siblingBooks.value = page.content
        }
    }
}
```
Call `loadSiblingBooks()` from `initialize()`. Import: `snd.komga.client.search.BookConditionBuilder`, `snd.komga.client.common.KomgaPageRequest`.

**ImmersiveBookContent layout:**

The scaffold's `coverData` slot is replaced by a `HorizontalPager`:
- The scaffold signature is adapted: instead of `coverData`, pass a custom `coverContent: @Composable () -> Unit` slot to the scaffold, OR render the pager above the card outside the scaffold.
- **Simpler approach**: Wrap `ImmersiveDetailScaffold` but override the cover area with a `HorizontalPager` that peers at adjacent covers. The scaffold's cover slot accepts a composable — but as designed, the cover is rendered inside the scaffold via `ThumbnailImage`.
- **Alternative**: Pass current book's `BookDefaultThumbnailRequest(book.id)` as `coverData`. The pager swipe gesture changes `currentBookIndex` which changes `coverData`. This reloads the image. Siblings can be shown as a separate peeking strip below the main cover. **Use this approach** for T8 — simpler, doesn't require redesigning the scaffold.

**Pager approach (revised):**
- `HorizontalPager` is placed INSIDE the card content (not the cover area)
- The main cover in the scaffold shows the current book's cover (updates as page changes)
- The pager inside the card shows the list of siblings as a cover strip with text info
- When a sibling is tapped, `vm.book.value = siblingBooks[index]` and the main cover updates

OR (cleaner):
- Keep scaffold as designed with `coverData = BookDefaultThumbnailRequest(currentBook.id)`
- Show the sibling pager as a horizontal strip at the TOP of the card content
- Each sibling has a small thumbnail; selected one is highlighted
- When a different sibling is tapped: update current book via `vm.loadBook(sibling.id)`

**Chosen approach:** Sibling strip at top of card content. Cleaner than complicating the scaffold.

**Card content layout:**
```
LazyRow(siblings) { BookThumbnail + book number, tappable to switch }
AnimatedContent(currentBook, crossfade) {
    Column {
        Text(book.seriesTitle + " #" + book.number, headlineMedium)
        BookInfoRow(book, onSeriesButtonClick)
        HorizontalDivider()
        BookInfoColumn(publisher, genres, authors, tags, links, ...)
        HorizontalDivider()
        BookReadListsContent(readLists, ...)
    }
}
```

**Top bar:** back `IconButton` + `BookActionsMenu(book, actions, showEditOption=true, showDownloadOption=false)`

**FAB:** `ImmersiveDetailFab(onReadClick, onReadIncognitoClick, onDownloadClick=onBookDownload)`

**BookScreen.Content() modification:**
```kotlin
val platform = LocalPlatform.current
val useNewUI = LocalUseNewLibraryUI.current
if (platform == PlatformType.MOBILE && useNewUI) {
    ImmersiveBookContent(vm, navigator, bookSiblingsContext)
} else {
    BookScreenContent(...)
}
```

**Subtasks:**
- [ ] Add `siblingBooks: MutableStateFlow<List<KomeliaBook>>` to `BookViewModel`
- [ ] Add `loadSiblingBooks()` using `bookApi.getBookList(BookConditionBuilder { seriesId = ... }, KomgaPageRequest(size=500))`
- [ ] Call `loadSiblingBooks()` from `initialize()` when `book.value?.seriesId != null`
- [ ] Create directory `book/immersive/`
- [ ] Create `ImmersiveBookContent.kt` using `ImmersiveDetailScaffold`
- [ ] `coverData = BookDefaultThumbnailRequest(book.id)`, `coverKey = book.id.value`
- [ ] Sibling strip: `LazyRow` of small thumbnails at top of card, tapping calls `vm.loadBook(sibling.id)`
- [ ] `AnimatedContent(book, crossfade)` wrapping the book metadata section in the card
- [ ] Card: `BookInfoRow` + `BookInfoColumn` + `BookReadListsContent`
- [ ] Top bar: back + `BookActionsMenu(showEditOption=true, showDownloadOption=false)`
- [ ] FAB: `ImmersiveDetailFab` wired to read/incognito/download
- [ ] Modify `BookScreen.Content()`: add `if (MOBILE && newUI)` branch

## Summary of Changes

Implemented T8: ImmersiveBookContent with lateral swipe pager.

**Files changed:**
- `BookViewModel.kt`: Added `siblingBooks: MutableStateFlow<List<KomeliaBook>>` + `loadSiblingBooks()` (called in `initialize()`). Uses `allOfBooks { seriesId { isEqualTo(seriesId) } }` with `unpaged = true` to load all books in the series sorted by number.
- `ImmersiveBookContent.kt` (new): Double-pager architecture — outer `HorizontalPager` slides the full `ImmersiveDetailScaffold` (cover + card) per sibling book; inner `HorizontalPager` (same `pagerState`) slides the card content in sync. Fixed overlays (back circle, 3-dot `BookActionsMenu`, `ImmersiveDetailFab`) sit above the pager. Two-step download confirmation dialog matches series screen pattern.
- `BookScreen.kt`: Replaced boilerplate placeholder with `ImmersiveBookContent()`. Fixed `readerScreen()` parameter order (`book, markReadProgress, bookSiblingsContext`).
