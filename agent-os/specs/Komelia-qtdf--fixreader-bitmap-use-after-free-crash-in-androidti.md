---
# Komelia-qtdf
title: 'fix(reader): bitmap use-after-free crash in AndroidTiledPainter.onDraw'
status: completed
type: bug
priority: normal
created_at: 2026-03-09T00:50:00Z
updated_at: 2026-03-09T00:56:48Z
---

SIGABRT: bitmap.recycle() called on processing thread races with onDraw on main thread. Fix: defer recycle to main thread Handler.

## Summary of Changes\n\nDeferred  to the main thread via  in . Added  and  imports. This eliminates the TOCTOU race where the background thread recycled bitmaps while the Choreographer frame was still drawing them.
