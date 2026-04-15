---
# Komelia-nrxg
title: 'feat: accent color for reader settings chips & toggles'
status: completed
type: feature
priority: normal
created_at: 2026-03-15T16:44:49Z
updated_at: 2026-03-15T16:49:04Z
---

Apply accent color to InputChip and SwitchWithLabel components in reader settings overlay

## Summary of Changes

- Created `ChipDefaults.kt` with `accentInputChipColors()` helper following the luminance-contrast pattern from `ReaderModeIconButton`
- Updated `SwitchWithLabel.kt` to use accent color for checked track/thumb, falling back to M3 secondary
- Applied `colors = accentInputChipColors()` to all 20 InputChips across `BottomSheetSettingsOverlay.kt` (17 chips) and `CommonImageSettings.kt` (3 chips)
