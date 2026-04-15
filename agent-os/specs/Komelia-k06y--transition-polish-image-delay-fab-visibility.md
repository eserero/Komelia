---
# Komelia-k06y
title: 'Transition polish: image delay + FAB visibility'
status: completed
type: task
priority: normal
created_at: 2026-02-25T11:20:06Z
updated_at: 2026-02-25T11:23:09Z
---

Fix two remaining transition issues:
1. Cover background appears blank on first open - fix with placeholderMemoryCacheKey
2. FAB appears after transition instead of during - move outside sharedBounds

Files: ThumbnailImage.kt, ImmersiveDetailScaffold.kt

## Summary of Changes

- **ThumbnailImage.kt**: Added `.placeholderMemoryCacheKey(cacheKey)` to ImageRequest builder so Coil shows the already-cached thumbnail immediately as a placeholder while the full-size image loads, eliminating the blank cover on first open.

- **ImmersiveDetailScaffold.kt**:
  - Layer 1 ThumbnailImage changed to `crossfade = true` for smooth placeholder→full-size fade.
  - Added `fabEnterExitModifier` (slideInVertically + fadeIn with 50ms delay, slideOutVertically + fadeOut on exit).
  - Wrapped everything in an outer `Box(modifier.fillMaxSize())` with no shared modifier.
  - Moved Layer 4 (FAB) outside `BoxWithConstraints` into the outer Box with `fabEnterExitModifier`, so it slides in during the transition instead of appearing after the bounds reach full screen.
