---
# Komelia-jb7t
title: 'epub3 OOM fix: clip persistence & dedup getOverlayClips'
status: completed
type: task
priority: normal
created_at: 2026-03-18T16:48:09Z
updated_at: 2026-03-18T16:50:43Z
---

Fix double getOverlayClips call and implement clip persistence to avoid 50-100MB SMIL parse spike on every book open

## Summary of Changes

- **MediaOverlayController.kt**: Changed `initialize()` to accept `clips: List<OverlayPar>` parameter, removing the internal redundant `BookService.getOverlayClips()` call
- **Epub3ReaderState.kt**: Implemented clip persistence via `overlay_clips.json` in the extracted EPUB directory; added private `JSONObject.toMap()` helper for deserialization; first open saves clips to file, subsequent opens load from file and pass to `openPublication()` to skip the 50–100 MB SMIL parse spike
