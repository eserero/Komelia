---
# Komelia-545n
title: 'fix(android): NCNN upscaler triggered when disabled for narrow/webtoon images'
status: completed
type: bug
priority: normal
created_at: 2026-03-10T20:55:28Z
updated_at: 2026-03-10T20:55:53Z
---

When upscaling is disabled, narrow/webtoon images still trigger NCNN upscaling because resizeImage() and getImageRegion() only check ncnnUpscaler != null, not whether it is enabled. Fix: add isEnabled property to AndroidNcnnUpscaler and guard both resize methods.

## Summary of Changes\n\n- Added  property to  (returns )\n- Guarded  in  with  check\n- Guarded  in  with  check
