---
# Komelia-xgx4
title: 'fix(android): NPE in AndroidReaderImage.loadImage - upscaleStatus null'
status: completed
type: bug
priority: normal
created_at: 2026-03-10T20:03:03Z
updated_at: 2026-03-10T20:04:24Z
---

Constructor leakage race condition: TilingReaderImage.init launches loadImage() coroutine before subclass field upscaleStatus is initialized. Fix by extracting startImageLoading() helper called at end of each subclass init.

## Summary of Changes

- **TilingReaderImage.kt**: Removed  from  block; added  helper that subclasses call explicitly.
- **AndroidReaderImage.kt**: Added  at the end of , after all ncnnUpscaler flow subscriptions are set up.
- **DesktopReaderImage.kt**: Added  at the end of , after the upscaler mode subscription.
- **WasmReaderImage.kt**: Added a new  block with .

This eliminates the constructor leakage race condition where the Default dispatcher could pick up the  coroutine before 's  field was initialized.
