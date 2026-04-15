---
# Komelia-zj4p
title: 'Settings screen cleanup: Switch accent colors and layout reorder'
status: completed
type: task
priority: normal
created_at: 2026-03-16T14:19:57Z
updated_at: 2026-03-16T14:22:06Z
---

Appearance settings: replace bare Switch with SwitchWithLabel, reorder settings, rename 'Card layout' to 'Text below card', add Cards header. Image reader settings: apply accent color to buttons.

## Summary of Changes

- **AppearanceSettingsContent.kt**: Replaced all bare  patterns with  (accent color support, no overflow). Reordered settings: App theme → Accent color (always shown, removed useNewLibraryUI gate) → New Library UI → [Immersive card color + tint slider] → Cards header → card size slider → Text below card → Card layout overlay background → Hide parentheses. Renamed 'Card layout' to 'Text below card'. Added 'Cards' section header using titleSmall + primary color.
- **ImageReaderSettingsContent.kt**: Read  and applied it to  (Clear image cache) and both s (View Logs, Crash Logs).
