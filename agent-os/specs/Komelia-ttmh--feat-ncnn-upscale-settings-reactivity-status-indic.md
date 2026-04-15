---
# Komelia-ttmh
title: 'feat: NCNN upscale settings reactivity + status indicator'
status: completed
type: feature
priority: normal
created_at: 2026-03-07T18:00:42Z
updated_at: 2026-03-07T23:39:34Z
---

1. Toggle 'upscale on load' ON immediately upscales the visible page (watch settingsFlow in AndroidReaderImage)
2. Badge/spinner in BottomSheetSettingsOverlay top bar showing upscale status of current page

## Implementation Tasks

- [ ] Feature 2: Add UpscaleStatus sealed class + upscaleStatus to ReaderImage interface + TilingReaderImage default
- [ ] Feature 1+4: Add settingsFlow to AndroidNcnnUpscaler; add settingsFlow subscriptions to AndroidReaderImage
- [ ] Feature 5: Fix close() to acquire mutex; add NonCancellable wrapping; ensureActive() guards
- [ ] Feature 3: Add globalUpscaleActivities to AndroidNcnnUpscaler companion; add expect/actual; expose on NcnnSettingsState
- [ ] Feature 3+2: Add upscaleStatus tracking and registerActivity/unregisterActivity calls in AndroidReaderImage
- [ ] UI: Update BottomSheetSettingsOverlay top bar with global upscale indicator
