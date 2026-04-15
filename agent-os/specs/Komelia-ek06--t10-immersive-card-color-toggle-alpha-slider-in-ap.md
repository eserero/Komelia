---
# Komelia-ek06
title: 'T10: Immersive card color toggle + alpha slider in Appearance Settings'
status: completed
type: task
priority: normal
created_at: 2026-03-08T00:50:30Z
updated_at: 2026-03-08T00:53:10Z
parent: Komelia-uler
---

Add enable/disable toggle and alpha strength slider for immersive card color tinting to Appearance Settings. Follows the cardLayoutBelow pattern through AppSettings → repository stack → CompositionLocals → ImmersiveDetailScaffold.

## Summary of Changes

Implemented immersive card color toggle + alpha slider in Appearance Settings:

- **AppSettings.kt**: Added `immersiveColorEnabled: Boolean = true` and `immersiveColorAlpha: Float = 0.12f`
- **AppSettingsTable.kt**: Added `immersive_color_enabled` (bool) and `immersive_color_alpha` (float) columns
- **CommonSettingsRepository.kt**: Added 4 new methods for get/put of both settings
- **SettingsRepositoryWrapper.kt**: Implemented the 4 new interface methods
- **ExposedSettingsRepository.kt**: Updated `save()` and `toAppSettings()` for new columns
- **V24__immersive_color_settings.sql**: New migration (V24, since V23 was already taken)
- **AppMigrations.kt**: Added V24 to the migrations list
- **AppSettingsViewModel.kt**: Added state vars and two handler functions
- **AppSettingsScreen.kt**: Passes new params to AppearanceSettingsContent
- **AppearanceSettingsContent.kt**: Added toggle + conditional alpha slider inside `if (useNewLibraryUI)` block
- **CompositionLocals.kt**: Added `LocalImmersiveColorEnabled` and `LocalImmersiveColorAlpha`
- **MainView.kt**: Added state vars, two LaunchedEffects, and provides both locals
- **ImmersiveDetailScaffold.kt**: Reads locals instead of hardcoded 0.12f; uses surfaceVariant fallback when disabled
