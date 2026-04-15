---
# Komelia-l7e5
title: 'fix(ncnn): SIGSEGV - VipsImage freed before upscale block executes'
status: completed
type: bug
priority: normal
created_at: 2026-03-09T01:21:14Z
updated_at: 2026-03-09T01:22:26Z
---

## Summary of Changes

Extracted `VipsBackedImage.vipsImage.toSoftwareBitmap()` from inside the `UpscaleRequest.block` lambda to before the request is enqueued in `upscale()`. This materialises an owned Android Bitmap while the caller still holds a live reference to the original image, eliminating the race where the VipsImage\* native pointer was freed by `close()` on another thread while the request sat in the worker queue. Added `onDiscard: () -> Unit` to `UpscaleRequest` so the pre-converted bitmap is recycled correctly if the request is dropped due to a generation mismatch.
