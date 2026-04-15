# Lazy Layout Patterns

## LazyGridScope extension functions

Group related grid items (header + item list) into a named extension function on `LazyGridScope` instead of inlining them at the call site.

```kotlin
private fun LazyGridScope.BookFilterEntry(
    label: String,
    books: List<KomeliaBook>,
    onBookClick: (KomeliaBook) -> Unit,
) {
    if (books.isEmpty()) return
    item(span = { GridItemSpan(maxLineSpan) }) {
        Text(label)
    }
    items(books) { book ->
        BookImageCard(book = book, onClick = { onBookClick(book) })
    }
}
```

Usage in the grid:

```kotlin
LazyVerticalGrid(columns = GridCells.Adaptive(cardWidth)) {
    BookFilterEntry(label = "Keep Reading", books = keepReading, onBookClick = onBookClick)
    BookFilterEntry(label = "Recently Added", books = recentlyAdded, onBookClick = onBookClick)
}
```

- Use this pattern when the same header+items structure repeats for multiple sections
- Early-return (`if (books.isEmpty()) return`) to hide empty sections
- Keep these functions `private` — they are not reusable outside the file
