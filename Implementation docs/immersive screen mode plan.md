# Immersive Screen Modes Plan

## Objective
Decouple the morphing cover feature of the immersive screens (Book, Series, Oneshot) from the global "New UI 2" setting by introducing a new, dedicated setting: "Morphing Immersive Cover". This setting will be toggleable from both the global Appearance Settings page and the 3-dot action menu located in the top-right of the immersive screens.

## Proposed Changes

### 1. Database & Settings Repository
Add the new setting to the settings infrastructure.
*   **`komelia-infra/database/shared/src/commonMain/kotlin/snd/komelia/db/AppSettings.kt`**: Add `val useImmersiveMorphingCover: Boolean = false`.
*   **`komelia-infra/database/sqlite/src/commonMain/kotlin/snd/komelia/db/tables/AppSettingsTable.kt`**: Add `val useImmersiveMorphingCover = bool("use_immersive_morphing_cover").default(false)`.
*   **`komelia-infra/database/sqlite/src/commonMain/kotlin/snd/komelia/db/settings/ExposedSettingsRepository.kt`**: Map `useImmersiveMorphingCover` in updates and inserts.
*   **`komelia-domain/core/src/commonMain/kotlin/snd/komelia/settings/CommonSettingsRepository.kt`**: Define `fun getUseImmersiveMorphingCover(): Flow<Boolean>` and `suspend fun putUseImmersiveMorphingCover(enabled: Boolean)`.
*   **`komelia-infra/database/shared/src/commonMain/kotlin/snd/komelia/db/repository/SettingsRepositoryWrapper.kt`**: Implement the new Flow and Suspend methods.

### 2. State Management
Provide the global state and toggle action to the UI layer without prop-drilling through ViewModels.
*   **`komelia-ui/src/commonMain/kotlin/snd/komelia/ui/CompositionLocals.kt`**: 
    Add `LocalUseImmersiveMorphingCover` (Boolean) and `LocalToggleImmersiveMorphingCover` (`() -> Unit`).
*   **`komelia-ui/src/commonMain/kotlin/snd/komelia/ui/MainView.kt`**: 
    *   Add a `LaunchedEffect` to collect `useImmersiveMorphingCover` from the `settingsRepository`.
    *   Define a coroutine-backed lambda that toggles the value and persists it via `settingsRepository.putUseImmersiveMorphingCover()`.
    *   Provide both locals in the `CompositionLocalProvider`.

### 3. Settings Screen Update
Expose the toggle in the main Appearance Settings screen alongside other UI configurations.
*   **`komelia-ui/src/commonMain/kotlin/snd/komelia/ui/settings/appearance/AppSettingsViewModel.kt`**: Add state and mutation logic for `useImmersiveMorphingCover`.
*   **`komelia-ui/src/commonMain/kotlin/snd/komelia/ui/settings/appearance/AppSettingsScreen.kt`**: Pass state and mutation lambda to the content.
*   **`komelia-ui/src/commonMain/kotlin/snd/komelia/ui/settings/appearance/AppearanceSettingsContent.kt`**: Add a new `SwitchWithLabel` for the immersive setting inside the `useNewLibraryUI` section. Update the supporting text for the existing `useNewLibraryUI2` to reflect its true scope (e.g., modern top app bar and updated item cards).

### 4. Immersive Detail Scaffold
Make the immersive screen directly observe the new setting instead of the broader `New UI 2` setting.
*   **`komelia-ui/src/commonMain/kotlin/snd/komelia/ui/common/immersive/ImmersiveDetailScaffold.kt`**: Change `val useMorphingCover = LocalUseNewLibraryUI2.current` to `val useMorphingCover = LocalUseImmersiveMorphingCover.current`.

### 5. Action Menus (The Toggle)
Add an optional action to the 3-dot dropdown menus, making it visible only when invoked from an immersive screen.
*   **`komelia-ui/src/commonMain/kotlin/snd/komelia/ui/common/menus/BookActionsMenu.kt`, `SeriesActionsMenu.kt`, `OneshotActionsMenu.kt`**:
    *   Add an optional parameter `onToggleImmersiveMode: (() -> Unit)? = null` to the Composables.
    *   If `onToggleImmersiveMode != null`, render a new `DropdownMenuItem`.
    *   Use `LocalUseImmersiveMorphingCover.current` to dynamically set the label text (e.g., "Disable Morphing Cover" or "Enable Morphing Cover").
    *   Invoke `onToggleImmersiveMode()` on click.

### 6. Immersive Screens Integration
Pass the global toggle lambda into the action menus to enable the behavior.
*   **`komelia-ui/src/commonMain/kotlin/snd/komelia/ui/book/immersive/ImmersiveBookContent.kt`**:
    Pass `onToggleImmersiveMode = LocalToggleImmersiveMorphingCover.current` when creating the `BookActionsMenu` instance.
*   **`komelia-ui/src/commonMain/kotlin/snd/komelia/ui/series/immersive/ImmersiveSeriesContent.kt`**:
    Pass `onToggleImmersiveMode = LocalToggleImmersiveMorphingCover.current` when creating the `SeriesActionsMenu` instance.
*   **`komelia-ui/src/commonMain/kotlin/snd/komelia/ui/oneshot/immersive/ImmersiveOneshotContent.kt`**:
    Pass `onToggleImmersiveMode = LocalToggleImmersiveMorphingCover.current` when creating the `OneshotActionsMenu` instance.