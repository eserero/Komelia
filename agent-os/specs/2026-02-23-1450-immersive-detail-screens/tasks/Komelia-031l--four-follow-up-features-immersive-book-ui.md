---
# Komelia-031l
title: 'Four follow-up features: immersive book UI'
status: completed
type: task
priority: normal
created_at: 2026-02-24T18:09:21Z
updated_at: 2026-02-24T20:26:27Z
---

Implement four follow-up features for the immersive book/series UI:
1. Metadata stats line in book card header
2. Reading lists section in book screen
3. Bottom scroll padding to clear floating buttons
4. Tappable series-title link

Files:
- ImmersiveBookContent.kt
- BookScreen.kt
- SeriesLists.kt
- ImmersiveSeriesContent.kt

## Summary of Changes

- **Issue 1**: Added `BookStatsLine` composable showing pages, release date, read progress, and last-read timestamp. Cross-fades between collapsed (above header) and expanded (below header) positions using `expandFraction`.
- **Issue 2**: Added `readLists`, `onReadListClick`, `onReadListBookPress`, `cardWidth` params to `ImmersiveBookContent`; added `BookReadListsContent` grid item; threaded from `BookScreen.kt`.
- **Issue 3**: Added `navBarBottom + 80.dp` bottom `contentPadding` to `ImmersiveBookContent` and `ImmersiveSeriesContent` grids; updated `SeriesLazyCardGrid` from 50.dp to `navBarBottom + 65.dp`.
- **Issue 4**: Replaced series title `Text` with a clickable `Row` (primary color + NavigateNext icon); added `onSeriesClick` param threaded from `BookScreen.kt`.
