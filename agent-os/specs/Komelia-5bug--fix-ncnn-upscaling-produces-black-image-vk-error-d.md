---
# Komelia-5bug
title: 'Fix: ncnn upscaling produces black image (VK_ERROR_DEVICE_LOST)'
status: completed
type: bug
priority: normal
created_at: 2026-03-05T15:49:59Z
updated_at: 2026-03-05T15:50:33Z
---

setScale/setNoise never called after init() in AndroidNcnnUpscaler.reinit(). With scale=0, process() creates a zero-size VkMat causing VK_ERROR_DEVICE_LOST on every tile.

## Summary of Changes

- ****: Added  helper that parses scale/noise from model path string. In , added calls to  and  between  and  — this is the primary fix. Without these calls, scale=0 caused zero-size VkMat allocations leading to VK_ERROR_DEVICE_LOST on every tile.
- ****: Added defensive initialization of  in the  constructor.
- ****: Added defensive initialization of  in the  constructor.
