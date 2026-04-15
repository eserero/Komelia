---
# Komelia-gbt0
title: 'feat(epub3): chapter navigation TOC dialog'
status: completed
type: feature
priority: normal
created_at: 2026-03-20T00:10:23Z
updated_at: 2026-03-20T00:13:11Z
---

Implement a TOC dialog that opens when tapping the chapter chip in the controls card. Shows hierarchical table of contents with collapsible nested chapters. Tapping a chapter navigates the reader to it.

## Summary of Changes

- **Epub3ReaderState.kt**: Added  and  state flows,  and  methods; populated TOC after  in 
- **Epub3ControlsCard.kt**: Added  parameter; wired chapter chip's  to it
- **Epub3TocDialog.kt**: New file — Dialog with lazy column of hierarchical TOC rows; expandable nested chapters via ; empty-state text when TOC is empty
- **Epub3ReaderContent.android.kt**: Collects  and  state; passes  to controls card; shows  when  is true
