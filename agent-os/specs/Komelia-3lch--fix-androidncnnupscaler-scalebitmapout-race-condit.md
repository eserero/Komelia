---
# Komelia-3lch
title: 'fix: AndroidNcnnUpscaler scale/bitmapOut race condition and currentSettings visibility'
status: completed
type: bug
priority: normal
created_at: 2026-03-07T23:14:54Z
updated_at: 2026-03-07T23:16:27Z
---

Two thread-safety issues in AndroidNcnnUpscaler:

1. (Crash risk) scale captured outside jniMutex — if model changes from 2x to 4x while requests are queued, old request allocates 2x bitmapOut but ncnn engine writes 4x data → native heap corruption.
   Fix: move scale + bitmapOut allocation inside jniMutex.withLock.

2. (Minor) currentSettings not @Volatile — read without synchronization in willUpscale/checkAndUpscale, JIT may cache stale values.
   Fix: add @Volatile annotation.

File: komelia-domain/core/src/androidMain/kotlin/snd/komelia/image/AndroidNcnnUpscaler.kt

## Summary of Changes

- **Fix 1 (crash risk):** Moved  calculation and  allocation inside  in .  is now  declared before , allocated only after acquiring the lock alongside the active engine. When , returns -1 before allocation, so  is a safe no-op.
- **Fix 2 (thread visibility):** Added  to  so reads in  and  always see the latest written value without relying on JIT register caching.
