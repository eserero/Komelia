---
# Komelia-5r6b
title: Audio mini player UI improvements
status: completed
type: feature
priority: normal
created_at: 2026-03-18T23:59:30Z
updated_at: 2026-03-19T00:00:35Z
parent: Komelia-ecr6
---

1. Bar no longer overlaps text (bottom margin + 10dp positioning)\n2. Next/Previous clip buttons (pill with 3 buttons)\n3. Top text margin (2x status bar height)

## Summary of Changes\n\n- **MediaOverlayController.kt**: Added  and  after \n- **AudioMiniPlayer.kt**: Redesigned from circle to pill with 3 buttons (⏮ Play/Pause ⏭), using  and  layout\n- **Epub3ReaderContent.android.kt**: Added  to WebView,  clearance for bar, repositioned bar to  without 
