---
# Komelia-4ujx
title: 'Fix ImmersiveOneshotContent: add BookStatsLine cross-fade'
status: completed
type: task
priority: normal
created_at: 2026-02-24T20:51:20Z
updated_at: 2026-02-24T20:54:22Z
---

Add the cross-fading BookStatsLine (pages | date | progress | last-read) to ImmersiveOneshotContent, matching ImmersiveBookContent's layout.

## Summary of Changes\n\nAdded cross-fading BookStatsLine to ImmersiveOneshotContent:\n- Added missing imports (kotlinx.datetime, localDateTimeFormat, roundToInt)\n- Collapsed stats item (fades out as card expands) before header item\n- Expanded stats item (fades in as card expands) before SeriesDescriptionRow\n- Private BookStatsLine helper at bottom of file (identical to ImmersiveBookContent)\n- Build verified: assembleDebug passes
