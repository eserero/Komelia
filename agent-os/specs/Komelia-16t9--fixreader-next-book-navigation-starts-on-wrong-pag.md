---
# Komelia-16t9
title: 'fix(reader): next-book navigation starts on wrong page + IndexOutOfBoundsException'
status: completed
type: bug
priority: normal
created_at: 2026-03-15T17:00:13Z
updated_at: 2026-03-15T17:00:48Z
---

When navigating to the next book after using the progress slider near the end, the next book opens on the wrong page and sometimes crashes with IndexOutOfBoundsException: Index -1 out of bounds in PagedReaderState.onNewBookLoaded.

## Summary of Changes

- **ReaderState.kt:218** — Added  synchronously before  is updated in . This ensures  reads the correct page (1) when the next book is loaded, matching how  already handled this.
- **PagedReaderState.kt:243** — Added  to the  result in  as a defensive fallback, preventing  if the page number is somehow not found in the new book's spread map.
