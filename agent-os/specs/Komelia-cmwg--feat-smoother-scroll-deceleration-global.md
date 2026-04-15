---
# Komelia-cmwg
title: 'feat: smoother scroll deceleration (global)'
status: completed
type: feature
priority: normal
created_at: 2026-03-14T20:10:46Z
updated_at: 2026-03-14T20:19:50Z
---

Add smooth fling behavior across library grids, lists, and reader using a single AppFlingFriction constant

## Summary of Changes

- Created `platform/SmoothFlingBehavior.kt` with `AppFlingFriction = 0.05f` constant and `rememberSmoothFlingBehavior()`
- Added `LocalFlingBehavior` to `CompositionLocals.kt`
- Provided `LocalFlingBehavior` at app root in `MainView.kt`
- Applied to `SeriesLists.kt`, `BookLists.kt`, `HomeContent.kt` (LazyColumn + LazyVerticalGrid)
- Replaced `rememberSplineBasedDecay` with `exponentialDecay(AppFlingFriction)` in `ScalableContainer.kt`
