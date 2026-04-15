---
# Komelia-oax1
title: M3 Search Bar — Mobile
status: completed
type: feature
priority: normal
created_at: 2026-03-03T11:27:17Z
updated_at: 2026-03-07T23:39:47Z
---

Replace the custom SearchTextField on the mobile SearchScreen with a proper M3 SearchBar using SearchBarState, rememberSearchBarState(), and SearchBarDefaults.InputField. The new SearchBarWithResults composable wraps the M3 SearchBar + results slot.

## Tasks
- [ ] Add SearchBarWithResults composable to SearchBar.kt
- [ ] Update SearchScreen to use SearchBarWithResults (mobile only)
- [ ] Verify M3 visual alignment: 56dp, surfaceContainerHigh, rounded corners
- [ ] Verify back arrow, close button behavior
- [ ] Confirm desktop SearchBar is untouched

## Polish Follow-up\n- [ ] Fix 1: Remove fillMaxWidth from SearchBar in SearchBarWithResults\n- [ ] Fix 2: Replace FilterChip with SecondaryTabRow in SearchToolBar
