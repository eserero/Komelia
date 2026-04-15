---
# Komelia-ugip
title: 'Five follow-up tweaks: immersive book/series UI'
status: completed
type: task
priority: normal
created_at: 2026-02-24T17:06:50Z
updated_at: 2026-02-24T17:15:03Z
---

Fix five issues in the immersive book/series UI:
1. Series screen thumbnails touch screen edges (add contentPadding to LazyVerticalGrid)
2. Back from reader lands on wrong book (guard LaunchedEffect scroll to first-time only)
3/4. Cover image flickers when swiping (add crossfade=false parameter to ThumbnailImage)
5. Swiping to new book ignores expand/collapse state (lift state into ImmersiveDetailScaffold param)

Files:
- komelia-ui/src/commonMain/kotlin/snd/komelia/ui/series/immersive/ImmersiveSeriesContent.kt
- komelia-ui/src/commonMain/kotlin/snd/komelia/ui/book/immersive/ImmersiveBookContent.kt
- komelia-ui/src/commonMain/kotlin/snd/komelia/ui/common/images/ThumbnailImage.kt
- komelia-ui/src/commonMain/kotlin/snd/komelia/ui/common/immersive/ImmersiveDetailScaffold.kt

- [x] Issue 1: Add contentPadding to ImmersiveSeriesContent LazyVerticalGrid
- [x] Issue 2: Guard initial scroll in ImmersiveBookContent
- [x] Issue 3/4: Add crossfade param to ThumbnailImage; pass crossfade=false in ImmersiveDetailScaffold
- [ ] Issue 5: Lift expand state into ImmersiveDetailScaffold param; share across pages in ImmersiveBookContent
- [ ] Build verification
