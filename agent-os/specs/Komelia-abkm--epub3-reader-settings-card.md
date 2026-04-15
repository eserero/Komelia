---
# Komelia-abkm
title: 'Epub3 Reader: Settings Card'
status: completed
type: feature
priority: normal
created_at: 2026-03-19T21:25:03Z
updated_at: 2026-03-19T21:37:29Z
parent: Komelia-ecr6
---

Settings card UI for epub3 reader with theme, font, and read-aloud settings, persisted via EpubReaderSettingsRepository

## Summary of Changes

- Created `Epub3NativeSettings.kt` data model with Epub3Theme, Epub3TextAlign, Epub3ReadAloudColor enums and Epub3NativeSettings data class
- Added `epub3NativeSettings` field to `EpubReaderSettings.kt` (defaults to Epub3NativeSettings() for backward compatibility)
- Added `getEpub3NativeSettings()`/`putEpub3NativeSettings()` to `EpubReaderSettingsRepository` interface
- Implemented the new methods in `EpubReaderSettingsRepositoryWrapper`
- Added `epub3_native_settings_json` column to `EpubReaderSettingsTable`
- Updated `ExposedEpubReaderSettingsRepository` to read/write the new column
- Added V29 SQL migration to add the column to existing DBs
- Wired `epubSettingsRepository` into `Epub3ReaderState` via the factory
- Added `showSettings`, `settings`, `toggleSettings()`, `updateSettings()`, and `applySettingsToView()` to `Epub3ReaderState`
- Added settings gear icon button to `Epub3ControlsCard` (bottom-left, `onSettingsClick` parameter)
- Created `Epub3SettingsCard.kt` with theme chips, font/alignment segmented buttons, sliders for size/line/para, and read-aloud color filter chips
- Layered `Epub3SettingsCard` above `Epub3ControlsCard` in `Epub3ReaderContent` with AnimatedVisibility slide animation
