---
# Komelia-cs0a
title: 'Fix: ncnn upscale-on-load — black image on zoom + missed upscale on load'
status: completed
type: bug
priority: normal
created_at: 2026-03-05T16:21:54Z
updated_at: 2026-03-05T16:25:35Z
---

Two bugs after fixing VK_ERROR_DEVICE_LOST crash:
1. Black on zoom: wrong cache key causes runaway re-upscale, resized.close() recycles tile bitmap, upscaled.toReaderImageData() shares cachedUpscaledImage.bitmap with tile
2. Missed upscale on load: race condition — settings loaded async, checkAndUpscale called before reinit() finishes returns original image with no retry

Files: AndroidNcnnUpscaler.kt (add isReady StateFlow), AndroidReaderImage.kt (3 bitmap lifecycle fixes + retry logic)

## Changes Made

### AndroidNcnnUpscaler.kt
- Added  
- Set  at end of successful 
- Set  in the disabled path

### AndroidReaderImage.kt
- Added  block: subscribes to , calls  on each  emission
- Added : checks if already upscaled, calls , updates image.value and triggers re-render
- Fixed : identity cache check (), removed , fixed else branch to resize (fresh bitmap)
- Fixed : identity cache check (), fixed finally to only close  when it was intermediate
- Fixed  non-upscale path: same finally pattern — don't close  if it owns tile bitmap
