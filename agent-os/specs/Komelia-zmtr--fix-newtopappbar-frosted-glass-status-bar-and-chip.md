---
# Komelia-zmtr
title: 'Fix NewTopAppBar: frosted glass, status bar, and chip layout bugs'
status: completed
type: bug
priority: high
created_at: 2026-03-30T15:12:20Z
updated_at: 2026-03-30T16:21:42Z
---

4 regressions from the NewTopAppBar implementation: (1) top bar is fully transparent instead of frosted glass, (2) Android status bar is not frosted/immersive, (3) Library chips overlap with library name on Collections/ReadLists tabs, (4) Home screen chips overlap with Home header when non-All filter selected.

## Summary of Changes

Fixed 4 regressions from the NewTopAppBar implementation:

1. **Frosted glass not working**: hazeEffect (NewTopAppBar) was a descendant of the hazeSource Box — Haze requires sibling relationship. Fixed by adding per-screen hazeState with hazeSource on inner content Box in LibraryScreen and HomeScreen.
2. **Status bar not frosted**: .statusBarsPadding() was always applied in MainScreen, pushing NewTopAppBar below the status bar. Fixed by making it conditional on !isModernNewTopBar.
3. **Library chips overlapping header**: newUI2BeforeContent lambda called LibraryHeaderSection + LibraryTabChips without a Column — they overlaid in LazyVerticalGrid BoxScope. Fixed with Column wrapper.
4. **Home chips overlapping header**: Same issue in topContent lambda in HomeContent. Fixed with Column wrapper.

Commit: c777b280
