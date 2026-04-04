# Library Screen Improvements Plan

This plan details the changes required to improve the Library screen by adding a "Reading" toggle chip, controlling the visibility of the "Continue Reading" section, and refining its UI.

## Objective
- Add a persistent "Reading" toggle chip to the library screen.
- Show/hide the "Continue Reading" section based on the "Reading" chip state.
- Improve the "Continue Reading" section UI with a themed background and internal header.
- Remove the "Browse" header from the series list.
- Make "Continue Reading" available in all library tabs (Series, Collections, Read Lists).

## Key Files & Context
- `komelia-infra/database/shared/src/commonMain/kotlin/snd/komelia/db/AppSettings.kt`: Settings data class.
- `komelia-domain/core/src/commonMain/kotlin/snd/komelia/settings/CommonSettingsRepository.kt`: Settings repository interface.
- `komelia-infra/database/shared/src/commonMain/kotlin/snd/komelia/db/repository/SettingsRepositoryWrapper.kt`: Settings repository implementation.
- `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/library/LibraryViewModel.kt`: Main ViewModel for the library screen.
- `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/library/LibrarySeriesTabState.kt`: ViewModel state for the Series tab.
- `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/library/LibraryScreen.kt`: UI for the library screen.
- `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/series/list/SeriesListContent.kt`: UI for the series list grid.

## Implementation Steps

### 1. Persistence Layer
- **`AppSettings.kt`**: Add `val showContinueReading: Boolean = true` to the `AppSettings` data class.
- **`CommonSettingsRepository.kt`**:
    - Add `fun getShowContinueReading(): Flow<Boolean>`.
    - Add `suspend fun putShowContinueReading(enabled: Boolean)`.
- **`SettingsRepositoryWrapper.kt`**: Implement the new methods.

### 2. ViewModel Changes
- **`LibraryViewModel.kt`**:
    - Add `val showContinueReading: StateFlow<Boolean>` initialized from `settingsRepository`.
    - Add `var keepReadingBooks by mutableStateOf<List<KomeliaBook>>(emptyList())`.
    - Move `loadKeepReadingBooks()` from `LibrarySeriesTabState` to `LibraryViewModel`.
    - Update `initialize()` to call `loadKeepReadingBooks()`.
    - Add `fun toggleContinueReading()` to update the setting.
- **`LibrarySeriesTabState.kt`**:
    - Remove `keepReadingBooks` and `loadKeepReadingBooks()`.
    - Remove calls to `loadKeepReadingBooks()`.

### 3. UI Changes - `LibraryScreen.kt`
- **`LibraryTabChips`**:
    - Add `showContinueReading` and `onReadingClick` parameters.
    - Remove the early return `if (collectionsCount == 0 && readListsCount == 0) return`.
    - Add the "Reading" `FilterChip` at the end of the `LazyRow`.
    - Ensure tab chips ("Series", "Collections", "Read Lists") are only shown if `collectionsCount > 0 || readListsCount > 0`.
- **`ContinueReadingSection`**:
    - Create a new `@Composable` component for the "Continue Reading" section.
    - Use `Surface` with a background color matching the settings sections (`Color(43, 43, 43)` for dark theme, `surfaceVariant` for light theme).
    - Include the "Continue Reading" header *inside* the `Surface`.
    - Include the `LazyRow` of books inside the `Surface`.
- **`newUI2BeforeContent`**:
    - Update to include `ContinueReadingSection` if `showContinueReading` is true and `keepReadingBooks` is not empty.

### 4. UI Changes - `SeriesListContent.kt`
- Remove the hardcoded "Keep Reading" (now "Continue Reading") section logic.
- Remove the "Browse" header.

## Verification & Testing
- **Manual Verification**:
    - Open the Library screen.
    - Verify the "Reading" chip is present and toggles the "Continue Reading" section.
    - Verify the "Reading" chip state persists across app restarts.
    - Verify the "Continue Reading" section appears in "Collections" and "Read Lists" tabs when toggled ON.
    - Verify the "Continue Reading" section has the correct background color and internal header.
    - Verify the "Browse" header is gone from the Series tab.
    - Verify tab chips correctly appear/disappear based on content.
- **Automated Tests**:
    - Check if existing tests for `LibraryViewModel` need updates.
