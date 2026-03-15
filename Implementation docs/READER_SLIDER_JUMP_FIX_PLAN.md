# Reader Slider Jump Fix Plan

## Objective
Resolve the issue where moving the reader slider causes a jump back (forward seek) or jump forward (backward seek) by one page.

## Background & Root Cause Analysis
The issue is caused by race conditions when multiple progress updates are launched in parallel. During an animation or a jump (e.g., slider move), intermediate pages trigger `onProgressChange` calls. 

In the current implementation:
1. UI components (`PagedReaderContent`, `ContinuousReaderContent`) trigger `onPageChange` events as the view scrolls through intermediate pages.
2. `ReaderState.onProgressChange(page)` is called for each intermediate page.
3. `ReaderState.onProgressChange(page)` updates the `readProgressPage` StateFlow and then makes an asynchronous API call to the server.
4. Because these updates are often launched in parallel (e.g., using `stateScope.launch { readerState.onProgressChange(page) }` with `Dispatchers.Default`), they can execute out of order.
5. If an update for an earlier page (e.g., page 9) finishes after an update for a later page (e.g., page 10), the `readProgressPage` StateFlow is overwritten with the older value.
6. The `ProgressSlider` is driven by `readProgressPage`. When the StateFlow is overwritten with the older value, the slider thumb "jumps back" to the previous position.

## Key Files
- `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/reader/image/ReaderState.kt`
- `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/reader/image/paged/PagedReaderState.kt`
- `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/reader/image/continuous/ContinuousReaderState.kt`
- `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/reader/image/panels/PanelsReaderState.kt`
- `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/reader/image/paged/PagedReaderContent.kt`
- `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/reader/image/continuous/ContinuousReaderContent.kt`

## Proposed Solution

### 1. Sequential & Conflated Progress Updates in `ReaderState`
Refactor `ReaderState.kt` to ensure that progress updates are handled sequentially and that only the latest update is processed if multiple updates arrive rapidly.

**Implementation Details:**
- Add a `private val progressUpdateChannel = Channel<Int>(Channel.CONFLATED)` to `ReaderState`.
- In the `init` block of `ReaderState`, launch a coroutine in `stateScope` that collects from this channel.
- Use `Dispatchers.Main.immediate` for the collection loop to ensure UI state updates are snappy and sequential on the Main thread.
- The collection loop should:
    1. Receive a `page` from the channel.
    2. Update `readProgressPage.value = page` immediately.
    3. If `markReadProgress` is enabled, call `bookApi.markReadProgress(currentBook.id, KomgaBookReadProgressUpdateRequest(page))` and handle errors (e.g., using `runCatchingToNotifications`).
- Change `onProgressChange(page: Int)` to a non-suspending function (or keep it as `suspend` but only have it send to the channel) that simply does `progressUpdateChannel.trySend(page)`.

### 2. UI-Level Optimization to Avoid Intermediate Updates
Modify the reader UI components to avoid triggering `onPageChange` while a programmatic animation or jump is already in progress.

**Paged Reader (`PagedReaderContent.kt`) & Panels Reader (`PanelsReaderContent.kt`):**
- Update the `LaunchedEffect(pagerState.currentPage)` to check `pagerState.isScrollInProgress`.
- Only call `state.onPageChange(pagerState.currentPage)` if `!pagerState.isScrollInProgress`. This ensures that intermediate pages during an `animateScrollToPage` don't trigger progress updates.

**Continuous Reader (`ContinuousReaderContent.kt`):**
- In `handlePageScrollEvents`, consider adding a check to see if a programmatic scroll is in progress.
- Alternatively, improve the jump detection logic `(firstPage.pageNumber - previousFistPage.pageNumber) > 2` to ensure that it correctly identifies the target page when a large jump occurs.

### 3. Simplify Reader Mode States
- In `PagedReaderState.kt`, `ContinuousReaderState.kt`, and `PanelsReaderState.kt`, remove the `stateScope.launch { readerState.onProgressChange(pageNumber) }` wrappers.
- Call `readerState.onProgressChange(pageNumber)` directly. Since the new implementation in `ReaderState` will handle the sequencing, these calls no longer need to be wrapped in individual `launch` blocks that might run in parallel.

## Detailed Implementation Steps

### Step 1: `ReaderState.kt`
```kotlin
// Add to ReaderState class
private val progressUpdateChannel = Channel<Int>(Channel.CONFLATED)

// In init block or a dedicated method called after initialization
stateScope.launch(Dispatchers.Main.immediate) {
    for (page in progressUpdateChannel) {
        readProgressPage.value = page
        if (markReadProgress) {
            appNotifications.runCatchingToNotifications {
                val currentBook = booksState.value?.currentBook ?: return@runCatchingToNotifications
                bookApi.markReadProgress(
                    currentBook.id,
                    KomgaBookReadProgressUpdateRequest(page)
                )
            }
        }
    }
}

// Update onProgressChange
fun onProgressChange(page: Int) {
    progressUpdateChannel.trySend(page)
}
```

### Step 2: `PagedReaderContent.kt`
```kotlin
    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            if (pagerState.currentPage < spreads.size) {
                pagedReaderState.onPageChange(pagerState.currentPage)
            }
        }
    }
```

### Step 3: `PagedReaderState.kt` / `PanelsReaderState.kt` / `ContinuousReaderState.kt`
Remove redundant `stateScope.launch` calls around `readerState.onProgressChange(pageNumber)`.

## Verification Plan
1. **Slider Drag:** Drag the slider from the beginning to the end of a book. The page should update to the final position and stay there without jumping back.
2. **Rapid Clicks:** Click the "Next" button rapidly several times. The progress should update correctly and the slider should move smoothly.
3. **Continuous Reader Jumps:** Use the slider in Continuous mode and ensure it doesn't settle on an intermediate page.
4. **Network Delay Simulation:** (Optional) Add a artificial delay in `ReaderState` API call and verify that rapid progress updates still settle on the correct last page.
