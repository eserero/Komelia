---
# Komelia-yuzl
title: 'Fix EPUB audio sync: pause highlight & cross-page bugs'
status: completed
type: bug
priority: normal
created_at: 2026-03-18T21:21:23Z
updated_at: 2026-03-18T21:50:32Z
---

Fix two behavioral bugs in synchronized EPUB reading: (1) highlight while paused, (2) cross-page paragraph handling

## Summary of Changes

- **EpubView.kt**: Added  JS function in ; added  and  Kotlin methods
- **MediaOverlayController.kt**: Added  +  fields; fixed  to use ; fixed  to cancel ; rewrote  to seek to pending locator on play; rewrote  to save locator while paused; added , ,  private methods
