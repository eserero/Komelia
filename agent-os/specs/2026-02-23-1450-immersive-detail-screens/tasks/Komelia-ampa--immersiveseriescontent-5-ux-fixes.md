---
# Komelia-ampa
title: 'ImmersiveSeriesContent: 5 UX fixes'
status: completed
type: task
priority: normal
created_at: 2026-02-24T14:47:55Z
updated_at: 2026-02-24T14:54:07Z
---

Fix 5 UX issues in the immersive series screen:
- [x] #1 Bold series title
- [x] #2 Fix Z-axis: move title+writers to header Box above the LazyVerticalGrid
- [x] #3 Title alignment with thumbnail top
- [x] #4 Tags tab in SeriesImmersiveTabRow
- [x] #5 Persist card expansion + innerScroll state in ImmersiveDetailScaffold

## Summary of Changes

- **#1** Added `fontWeight = FontWeight.Bold` to title Text style
- **#2/#3** Moved title + writers out of `LazyVerticalGrid` into a `Box` header above the grid; `heightIn(min=…)` pushes grid start below thumbnail bottom when expanded; `lerp` top-padding aligns title with thumbnail top edge
- **#4** Added `ImmersiveTab` enum with BOOKS/COLLECTIONS/TAGS; updated `SeriesImmersiveTabRow` to accept `ImmersiveTab`; Tags content (SeriesChipTags) moved from fixed items into the TAGS tab
- **#5** Changed `remember` → `rememberSaveable` for `innerScrollPx`; added `savedExpanded` rememberSaveable bool; `AnchoredDraggableState` initial value reads from `savedExpanded`; `LaunchedEffect` updates `savedExpanded` on state change
