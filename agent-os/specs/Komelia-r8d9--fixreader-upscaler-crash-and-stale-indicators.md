---
# Komelia-r8d9
title: 'fix(reader): upscaler crash and stale indicators'
status: completed
type: bug
priority: normal
created_at: 2026-03-08T20:31:23Z
updated_at: 2026-03-11T11:41:08Z
---

Revision 3: Fix crash from TOCTOU race on bitmap recycle during tile render, and fix stale upscale status indicators persisting when navigating to next/previous book

## Summary of Changes

Fixed SIGSEGV crash in AndroidNcnnUpscaler when user exits reader while upscale is in progress.

**Root cause**: When  is cancelled, the catch block called  while the worker's  (running under ) still held a reference to the same bitmap. The worker then called  on the recycled bitmap → JNI null pointer → SIGSEGV.

**Fix** (): Track whether  succeeded before  can throw. Only recycle  in the catch block if send failed (i.e., the request never entered the channel, so neither the worker block nor  will clean it up). If send succeeded, the worker owns the bitmap lifecycle.
