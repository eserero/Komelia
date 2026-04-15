---
# Komelia-553f
title: 'Transition Polish: Series Card Root Cause Fixes'
status: completed
type: task
priority: normal
created_at: 2026-02-25T10:53:27Z
updated_at: 2026-02-25T10:59:37Z
parent: Komelia-uler
---

Fix 4 concrete bugs in series-card → detail-screen transition:
1. Flicker on first open (cache key issue)
2. Cover image appears above card during transition
3. Card pops in instantly (no entry animation)
4. Back from expanded card: cover jumps

Fix 1: Remove memoryCacheKeyExtra scale from ThumbnailImage.kt
Fix 2: Container Transform - move sharedBounds from cover image to outer BoxWithConstraints in ImmersiveDetailScaffold.kt

## Summary of Changes

- **ThumbnailImage.kt**: Removed `memoryCacheKeyExtra("scale", ...)` so library view (Fit) and detail view (Crop) share the same memory cache entry. Eliminates first-open flicker.

- **ImmersiveDetailScaffold.kt**: Container Transform implementation:
  - Renamed `coverSharedModifier` → `scaffoldSharedModifier`, applied to `BoxWithConstraints` (outer container) instead of Layer 1 cover image
  - Changed exit to `fadeOut(tween(200))` — content fades while bounds collapse, works cleanly from any state
  - Removed `coverSharedModifier` from Layer 1 ThumbnailImage (no shared modifier needed, just a regular image inside the container)
  - Removed `uiEnterExitModifier` from Layer 2 (card) and Layer 4 (FAB) — Container Transform reveals these naturally
  - Updated `uiEnterExitModifier` delay from 150ms → 450ms on Layer 5 (top bar) to prevent tiny back button appearing inside thumbnail bounds
  - Removed unused `ExitTransition` import
