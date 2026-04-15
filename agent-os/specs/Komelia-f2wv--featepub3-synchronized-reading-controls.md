---
# Komelia-f2wv
title: 'feat(epub3): synchronized reading controls'
status: completed
type: feature
priority: normal
created_at: 2026-03-18T19:18:51Z
updated_at: 2026-03-18T20:42:14Z
parent: Komelia-ecr6
---

Wire up bidirectional sync between audio and text in the EPUB3 reader: page nav → audio seek (F1), double-tap → seek+play (F2), progressive highlighting (F3), auto page-turn (F4)

## Summary of Changes

- **AudiobookPlayer.kt**: Removed redundant  guard from  CLIP_CHANGED processing — PlayerMessages only fire during playback so the guard was harmful at clip boundaries (F3/F4 fix).
- **MediaOverlayController.kt**: Added / fields;  now records timestamp before setting locator;  implements position-based fallback highlight; added  (F1) and  (F2) public methods with  helper.
- **Epub3ReaderState.kt**:  now calls  (F1); added  override calling  (F2).
