---
# Komelia-y79t
title: Controls overlay, back nav & progress tracking for EPUB3 reader
status: completed
type: task
priority: normal
created_at: 2026-03-18T00:15:30Z
updated_at: 2026-03-18T00:21:41Z
parent: Komelia-ecr6
---

Implement: (1) controls overlay toggled by center tap, (2) back press handler, (3) save/restore reading position via bookApi

## Summary of Changes

- **Epub3ReaderState.kt**: Added `showControls: MutableStateFlow<Boolean>` and `toggleControls()`. Updated `onEpubViewCreated` listener to call `toggleControls()` on `onMiddleTouch` and to save progress via `bookApi.updateReadiumProgression` on `onLocatorChange`. Added progress restore in `initialize` by calling `bookApi.getReadiumProgression` and converting R2Locator → Readium Locator.
- **Epub3ReaderContent.android.kt**: Wrapped AndroidView in a Box with controls overlay (scrim + top bar with back button and book title), toggled by `showControls`. Added `BackPressHandler` for Android hardware back button support.
