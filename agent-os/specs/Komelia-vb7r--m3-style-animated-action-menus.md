---
# Komelia-vb7r
title: M3-Style Animated Action Menus
status: completed
type: feature
priority: normal
created_at: 2026-03-02T23:47:01Z
updated_at: 2026-03-02T23:49:55Z
---

Implement M3-style fade + scale animation for action menus (BookActionsMenu, SeriesActionsMenu, etc.) via AnimatedDropdownMenu wrapper.

## Tasks
- [x] Create AnimatedDropdownMenu.kt in commonMain
- [x] Update BookActionsMenu.kt
- [x] Update SeriesActionsMenu.kt
- [x] Update OneshotActionsMenu.kt
- [x] Update ReadListActionsMenu.kt
- [x] Update CollectionActionsMenu.kt
- [x] Update LibraryActionsMenu.kt

## Summary of Changes

Created `AnimatedDropdownMenu` — a thin wrapper around CMP Material3 `DropdownMenu` that adds M3-style fade + scale enter/exit animations:

- **New file:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/common/components/AnimatedDropdownMenu.kt`
  - Uses `MutableTransitionState(false)` so enter animation starts from frame 1 (no blank-card flash)
  - `popupVisible` state keeps the popup alive during the exit animation
  - Wraps content in `AnimatedVisibility`: scale 0.85→1.0 + fade over 200ms enter, 120ms exit
  - Transform origin defaults to top-right (1f, 0f) — correct for a trailing 3-dots button

- **Updated 6 action menus:** BookActionsMenu, SeriesActionsMenu, OneshotActionsMenu, ReadListActionsMenu, CollectionActionsMenu, LibraryActionsMenu — replaced `DropdownMenu` with `AnimatedDropdownMenu` (import swap + rename only)
