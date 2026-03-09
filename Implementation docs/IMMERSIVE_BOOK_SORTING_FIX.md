# Plan: Synchronize Book Sorting in Immersive Views

## Root Cause Analysis (RCA)
The discrepancy in book ordering between the **Series Page** and the **Immersive Book View** (the horizontal swipeable viewer) is caused by inconsistent fetching logic:

1.  **Series Page:** Uses `SeriesBooksState`, which incorporates a `BooksFilterState`. This allows users to apply specific sorting (e.g., by number, release date) and filters (read status, tags).
2.  **Immersive Book View:** Uses `BookViewModel`, which independently fetches "sibling books" (all books in the same series) in `loadSiblingBooks()`.
3.  **The Bug:** `BookViewModel.loadSiblingBooks()` currently fetches books using only the `seriesId` and a default `KomgaPageRequest(unpaged = true)`. It ignores any active filters or sort orders that were applied on the series page, resulting in a default API-defined order that often differs from the user's view.

## Implementation Plan

### 1. Update Navigation Context
Modify `BookSiblingsContext` to carry the filter state from the originating screen.

**File:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/BookSiblingsContext.kt`
- Change `data object Series` to `data class Series(val filter: BookFilter? = null)`.

### 2. Ensure Serializability
The filter state must be serializable to persist across navigation and platform-specific lifecycles (like Android process death).

**File:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/book/BookFilterState.kt`
- Make `BookFilter` implement `ScreenSerializable`.
- Make `BooksSort` implement `ScreenSerializable`.

**File:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/series/SeriesFilterState.kt`
- Make `TagInclusionMode` and `TagExclusionMode` implement `ScreenSerializable`.

### 3. Capture Filter on Navigation
Update the series screen to pass the current filter state when a book is selected.

**File:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/series/SeriesScreen.kt`
- In `onBookClick`, change the call to `bookScreen(it)` to include the context: `bookScreen(it, BookSiblingsContext.Series(vm.booksState.filterState.state.value))`.

### 4. Apply Filter in BookViewModel
Update the sibling fetching logic to respect the passed filter.

**File:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/book/BookViewModel.kt`
- Update the constructor to accept `bookSiblingsContext: BookSiblingsContext`.
- Update `loadSiblingBooks()`:
    - If `bookSiblingsContext` is `Series(filter)` and `filter` is not null:
        - Use `filter.addConditionTo(this)` inside the `allOfBooks` builder.
        - Pass `filter.sortOrder.komgaSort` to the `KomgaPageRequest`.

### 5. Update Factory and Screen
Ensure the new parameter is wired through the ViewModel factory.

**File:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/book/BookScreen.kt`
- Update `viewModelFactory.getBookViewModel(...)` call to pass `bookSiblingsContext`.

## Verification Steps
1. Open a series in the Immersive UI.
2. Change the sort order (e.g., Sort by Number Descending).
3. Click on a book to enter the Immersive Book View.
4. Verify that swiping left/right follows the Descending order established on the previous screen.
5. Apply a filter (e.g., Hide Read) and verify that "Read" books are excluded from the swipe navigation.
