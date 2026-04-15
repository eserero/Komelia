---
# Komelia-e7hc
title: 'Epub3 reader: bottom navigation controls card'
status: completed
type: feature
priority: normal
created_at: 2026-03-19T16:54:04Z
updated_at: 2026-03-19T17:05:25Z
---

Add a bottom navigation card to the epub3 reader with a page slider, chapter chip, and animated audio player padding. Matches M3 style with drag-to-dismiss handle.

## Summary of Changes

- **Epub3ReaderState.kt**: Added  and  StateFlows,  ref, , positions loading after epub opens, currentLocator update on locator change, epubView store/clear on create/close.
- **Epub3ControlsCard.kt**: New composable with M3 Surface (rounded top corners), drag handle with dismiss gesture, AppSlider with accent color, 'Page X of Y' label, and optional chapter chip.
- **Epub3ReaderContent.android.kt**: Added scrim, AnimatedVisibility bottom card, animated AudioMiniPlayer padding that rises above card when open.
