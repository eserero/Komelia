---
# Komelia-1awo
title: 'feat: gentler scroll fling deceleration (spline + velocity boost)'
status: in-progress
type: feature
priority: normal
created_at: 2026-03-15T13:10:46Z
updated_at: 2026-03-15T13:48:45Z
---

Replace exponentialDecay approach with spline-based decay + velocity multiplier to preserve native Android feel while extending coast distance

## Implementation Plan\n\n- [ ] Rewrite FlingDefaults.kt with two-phase spline+spring approach\n- [ ] Update ScreenScaleState.kt: revert multiplier, add 2D spring phase\n- [ ] Verify build compiles
