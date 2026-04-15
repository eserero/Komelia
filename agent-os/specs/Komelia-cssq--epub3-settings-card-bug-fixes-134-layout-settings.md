---
# Komelia-cssq
title: 'Epub3 Settings Card: Bug Fixes + §13.4 Layout Settings'
status: completed
type: task
priority: normal
created_at: 2026-03-19T22:21:59Z
updated_at: 2026-03-19T22:26:37Z
parent: Komelia-ecr6
---

Fix 5 UI bugs in Epub3SettingsCard and add new Layout section (scroll, columnCount, pageMargins, publisherStyles) with data model + EpubView wiring

## Summary of Changes

**5 Bug Fixes in Epub3SettingsCard:**
- Fix 1: ThemeChip always reserves 14dp checkmark space (Box wrapper) → no height shift on select
- Fix 2: Theme chips Row gets 12dp top padding after section divider
- Fix 3: Align SegmentedButtons use label slot for icons (icon= param removed) → icons now visible
- Fix 4: Slider labels widened to 112dp; renamed Size→Font size, Line→Line height, Para→Para spacing
- Fix 5: Read-aloud FilterChip shows accent border + color name label when selected

**§13.4 Layout Settings added:**
-  enum (AUTO/ONE/TWO) + 4 new fields in  (scroll, columnCount, pageMargins, publisherStyles)
- : Props, FinalizedProps, pendingProps, finalizeProps, EpubPreferences all updated
- : initialPreferences includes new 4 fields
- : applySettingsToView() maps new settings to view.pendingProps with ColumnCount conversion
- : new Layout section with Continuous scroll switch, Columns segmented button (Auto/1/2), Margins slider (0.5×–2.0×), Publisher styles switch
