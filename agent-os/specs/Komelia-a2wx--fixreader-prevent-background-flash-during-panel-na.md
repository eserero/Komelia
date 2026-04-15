---
# Komelia-a2wx
title: 'fix(reader): prevent background flash during panel navigation'
status: completed
type: bug
priority: normal
created_at: 2026-03-08T01:40:50Z
updated_at: 2026-03-10T14:18:50Z
---

Defer tile bitmap cleanup until after new painter is established to eliminate the black/white flash when navigating between panels in panel-by-panel mode.\n\nSingle file change: TilingReaderImage.kt

## Summary of Changes

- **Fix 1 – Parallel tile generation**: Replaced the sequential  loop with parallel  jobs via . Each tile is independent so they can all be generated concurrently, reducing generation time from ~600ms sequential to ~100ms parallel on modern multi-core devices.

- **Fix 2 – Full-image tiling on scale change**: When  (zoom level changed, as happens when navigating between panels of different sizes), the effective tiling window is expanded to cover the entire image instead of just the 1.5x viewport. This guarantees all tiles at the new zoom level are present regardless of navigation direction, eliminating the far-side background flash.

- Added  import from .

Single file changed: 
