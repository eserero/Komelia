---
# Komelia-4z4c
title: 'epub3 OOM Fix: Stream EPUB to Disk'
status: completed
type: task
priority: normal
created_at: 2026-03-18T17:26:22Z
updated_at: 2026-03-18T17:55:54Z
---

Fix OOM crash when loading large EPUBs by streaming remote files in 64KB chunks and using direct file path for offline books

## Summary of Changes

- **KomgaBookApi**: Added  (default buffers for wasmJs compat) and  (default null)
- **RemoteBookApi**: Overrides  to stream in 64 KB chunks via  — no full-file heap allocation
- **OfflineBookApi**: Overrides  to return the local filesystem path via  expect/actual helper
- **PlatformFilePath.kt**: New expect/actual in offline module — Android/JVM return , wasmJs returns null
- **Epub3ReaderState**:  now dispatches: local path → extract directly (0 bytes); no path → stream to temp file → extract
