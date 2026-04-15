# Reader New UI Controls — Implementation Plan

## Context

The image reader and epub3 reader each have their own control overlays that are visually inconsistent with each other and with the app's modern "New UI 2" design language. The user wants both controls redesigned to:

- Match the immersive card style shown in `Implementation docs/stitch ui/reader_new_ui.html`
- Share a common top bar component (series name + book name + back button)
- Share a common card container component (frosted glass in modern theme, surface color otherwise)
- Only activate when the "New UI 2" setting is enabled (`LocalUseNewLibraryUI2`)

---

## Shared Components to Create

### 1. `ReaderTopBar` (new composable)

**File:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/reader/ReaderTopBar.kt`

Replaces the current simple top bars in both readers. Used by both image reader and epub3 reader.

**Design:**
- Background: `MaterialTheme.colorScheme.surface` with `hazeEffect` if `LocalHazeState.current != null` (modern theme)
- `statusBarsPadding()` + fixed height ~64dp
- Left: `IconButton` with `ArrowBack` icon (existing back button behavior)
- Center/right: Two-line title block that fills available width in one line each:
  - Series name: `FontFamily.Serif`, `FontWeight.Bold`, slightly larger, `onSurface` color, `maxLines = 1`, auto-shrink via `onTextLayout`
  - Book name: `Inter` / default sans-serif, slightly smaller, `accentColor ?: primary` color, `maxLines = 1`, auto-shrink via `onTextLayout`
- Both title lines must fit on one line — use `onTextLayout { if (hasVisualOverflow) multiplier *= 0.9f }` pattern (same as `ImmersiveHeroText.kt`)

**Parameters:**
```kotlin
@Composable
fun ReaderTopBar(
    seriesTitle: String,
    bookTitle: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
)
```

---

### 2. `ReaderControlsCard` (new composable)

**File:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/reader/ReaderControlsCard.kt`

A styled card container used by both reader control panels.

**Design:**
- Shape: `RoundedCornerShape(28.dp)` (large rounded, matching the HTML's `rounded-[2rem]`)
- Background: `MaterialTheme.colorScheme.surface` with `hazeEffect` if `LocalHazeState.current != null`; otherwise solid surface with tonal elevation
- No drag handle
- `navigationBarsPadding()` + horizontal margin 16dp, bottom margin 16dp
- Shadow/elevation: `shadowElevation = 8.dp` on the `Surface`
- Content passed via `content` lambda (Column layout inside)

**Parameters:**
```kotlin
@Composable
fun ReaderControlsCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
)
```

---

## Epub3 Reader Controls Redesign

**File:** `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/Epub3ControlsCard.kt`
**File:** `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/Epub3ReaderContent.android.kt`

### New `Epub3ControlsCard` layout (when `useNewUI2 = true`):

```
┌─────────────────────────────────┐
│  Loc. X of Y  (bodySmall, 60% opacity, centered)    │
│  [════════════●════════════]     │  ← AppSlider (no +/- buttons)
│  [  Chapter (weight=1f)   ] [⚙] │  ← Row: wide chip + settings icon
└─────────────────────────────────┘
```

- Wrapped in `ReaderControlsCard`
- Location label sits INSIDE the card, above the slider (not outside like today)
- Slider: `AppSlider` without flanking `+/-` icon buttons (cleaner, matching HTML)
- Chapter button: `SuggestionChip` with `Modifier.weight(1f)` filling available width
- Settings button: same `IconButton` with `Tune` icon, same size as today
- Both chapter + settings in one `Row` with `horizontalArrangement = Arrangement.SpaceBetween`
- No drag handle

### Top bar (when `useNewUI2 = true`):

In `Epub3ReaderContent.android.kt`, replace the current inline top bar `Row` with `ReaderTopBar`, passing:
- `seriesTitle = book?.seriesTitle ?: ""`
- `bookTitle = book?.metadata?.title ?: ""`
- `onBack = { epub3State.closeWebview() }`

### Fallback (when `useNewUI2 = false`):

Keep existing `Epub3ControlsCard` and top bar unchanged. Use `if (useNewUI2) NewCard() else OldCard()` branching in `Epub3ReaderContent.android.kt`.

---

## Image Reader Controls Redesign

**File:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/reader/image/settings/BottomSheetSettingsOverlay.kt`
**File:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/reader/image/common/ReaderContent.kt`

### New image reader controls layout (when `useNewUI2 = true`):

```
┌─────────────────────────────────────────┐
│  Page X of Y  (bodySmall, 60% opacity, centered)    │
│  [══════════════●════════════════]       │  ← PageSpreadProgressSlider (restyled)
│  [🗎][▤][⊞]  │  [✨]  [⚙]              │  ← 5 buttons: 3 mode + separator + upscale + settings
└─────────────────────────────────────────┘
```

- Wrapped in `ReaderControlsCard`
- Page label ("Page X of Y") sits INSIDE the card above the slider
- Slider: existing `PageSpreadProgressSlider` logic but without the floating thumbnail pill above it; just the bare slider bar
- 5 buttons in one `Row`:
  - Paged, Continuous, Panels (3 `ReaderModeIconButton`s, existing component)
  - `VerticalDivider` (existing separator)
  - Upscale toggle `ReaderModeIconButton` (existing)
  - `IconButton(Tune)` — settings button, same size/style as epub3 settings button
- Settings button opens the existing `ModalBottomSheet` settings dialog (unchanged)
- Upscale status `UpscaleActivityIndicator` displayed inside the card, between the top bar and the slider card — or inline above the card (same relative position as today's top bar)

### Top bar (when `useNewUI2 = true`):

In `ReaderContent.kt` (or where the image reader renders its top bar), replace current top bar rendering with `ReaderTopBar`, passing:
- `seriesTitle = book?.seriesTitle ?: ""`
- `bookTitle = book?.metadata?.title ?: ""`
- `onBack = { onExit() }`

The `UpscaleActivityIndicator` should be shown between the top bar and the page content (same position as today — inside the settings header area), adapting to animate in/out as it does now.

### Fallback (when `useNewUI2 = false`):

Keep the existing `BottomSheetSettingsOverlay` (FAB + floating toolbar) unchanged. Branch on `LocalUseNewLibraryUI2.current` in `SettingsContent.kt`.

---

## Haze / Frosted Glass Integration

Both `ReaderTopBar` and `ReaderControlsCard` should use the existing haze pattern:

```kotlin
val hazeState = LocalHazeState.current
val theme = LocalTheme.current
val hazeStyle = if (hazeState != null) HazeMaterials.thin(theme.colorScheme.surface) else null

// On the composable modifier:
Modifier.hazeEffect(hazeState) { style = hazeStyle ?: return@hazeEffect }
```

The `LocalHazeState` is already provided from `MainScreen.kt` when `transparentBars || useNewTopBar` is true. Since these are reader screens, we need to ensure the reader `Box` also sources the haze — the reader content is the "blurred source" so both the top bar and controls card can apply `hazeEffect` against it.

**Important:** The reader's full-screen content (`Box` with images/epub view) must have `Modifier.haze(hazeState)` (source), while `ReaderTopBar` and `ReaderControlsCard` have `Modifier.hazeEffect(hazeState)` (blur consumer). Check if `LocalHazeState` is already set in the reader screens; if not, create a local `rememberHazeState()` scoped to the reader and provide it only when `useNewUI2 && isModernTheme`.

---

## Files to Create

| File | Purpose |
|------|---------|
| `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/reader/ReaderTopBar.kt` | Shared top bar (series + book title + back) |
| `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/reader/ReaderControlsCard.kt` | Shared frosted card container |

## Files to Modify

| File | Change |
|------|--------|
| `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/Epub3ReaderContent.android.kt` | Branch on `useNewUI2`; use `ReaderTopBar` + new controls card |
| `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/Epub3ControlsCard.kt` | Add new `Epub3ControlsCardNewUI` composable (or branch inside existing) |
| `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/reader/image/settings/BottomSheetSettingsOverlay.kt` | Add new image reader controls panel composable |
| `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/reader/image/settings/SettingsContent.kt` | Branch on `useNewUI2` to select old vs new overlay |
| `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/reader/image/common/ReaderContent.kt` | Use `ReaderTopBar` in new UI mode |
| `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/reader/image/common/ProgressSlider.kt` | Expose a "bare slider" variant (no floating page label pill) for new UI |

---

## Key Reuse

- `ImmersiveHeroText.kt` — font pattern reference (Serif bold for series, smaller for book)
- `AppSlider` / `AppSliderDefaults` — existing slider with accent color support
- `ReaderModeIconButton` — existing icon button for mode toggles (move from `private` to `internal` or extract to shared file)
- `UpscaleActivityIndicator` — existing component (move from `private` to accessible)
- `LocalHazeState`, `LocalAccentColor`, `LocalUseNewLibraryUI2`, `LocalTheme` — existing composition locals
- `HazeMaterials.thin()` + `.hazeEffect()` — existing haze pattern from `NewTopAppBar.kt`

---

## Verification

1. Build debug APK: `./gradlew :composeApp:assembleDebug`
2. Install on device/emulator: `adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk`
3. **New UI 2 OFF**: Open image reader → verify old floating toolbar + FAB still work. Open epub reader → verify old bottom sheet still appears.
4. **New UI 2 ON, Classic theme**: Open image reader → verify new card appears with solid surface color (no blur), correct series/book title in top bar, slider, 5 buttons. Open epub reader → verify new card with location, slider, chapter chip, settings button.
5. **New UI 2 ON, Modern theme**: Verify frosted glass effect on both top bar and controls card in both readers.
6. Verify accent color applied to book title and slider.
7. Verify upscale status indicator still appears in image reader.
8. Verify epub3 audio player mini/full still works (unchanged).
9. Verify epub3 settings card still opens correctly above the controls.
