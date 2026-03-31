# Frosted Glass Navigation & Top Bars — Implementation Plan

## Goal

Add true frosted glass blur to the Navigation Bar and Library Top App Bar when the **Light Modern** or **Dark Modern** themes are active. The effect should match CSS `backdrop-filter: blur()` — content scrolling behind the bars is visibly blurred through them.

---

## Library: Haze by Chris Banes

**`dev.chrisbanes.haze:haze` + `dev.chrisbanes.haze:haze-materials`** (v1.7.2, Maven Central)

This is a Kotlin Multiplatform library — works on Android, Desktop (JVM), and Web (wasmJs), matching this project's targets exactly.

| Android version | Behavior |
|---|---|
| API 31+ (Android 12+) | Real hardware-accelerated blur via AGSL/RenderEffect |
| API < 31 | Semi-transparent scrim fallback (no blur, same as current behavior) |
| Desktop / Web | No-op (bars keep their semi-transparent color) |

---

## How Haze Works

Two modifiers share a `HazeState` object:

- `Modifier.hazeSource(state)` — placed on the **scrollable content** (what gets blurred through)
- `Modifier.hazeEffect(state) { style = ... }` — placed on the **bar** (NavigationBar, TopAppBar)

The bar's `containerColor` is set to `Color.Transparent`; Haze renders the blurred pixels itself.

---

## Changes Required

### 1. `gradle/libs.versions.toml`

```toml
# [versions]
haze = "1.7.2"

# [libraries]
haze = { module = "dev.chrisbanes.haze:haze", version.ref = "haze" }
haze-materials = { module = "dev.chrisbanes.haze:haze-materials", version.ref = "haze" }
```

### 2. `komelia-ui/build.gradle.kts`

Add to the `commonMain` dependencies block:

```kotlin
implementation(libs.haze)
implementation(libs.haze.materials)
```

### 3. `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/CompositionLocals.kt`

Add a new local so any composable can read the shared HazeState:

```kotlin
import dev.chrisbanes.haze.HazeState
val LocalHazeState = compositionLocalOf<HazeState?> { null }
```

### 4. `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/MainScreen.kt`

**In `MobileLayout`:**
- Create `val hazeState = if (transparentBars) rememberHazeState() else null`
- Add `LocalHazeState provides hazeState` to the existing `CompositionLocalProvider`
- Add `Modifier.hazeSource(hazeState!!)` to the content Box when `transparentBars` (the Box inside `ModalNavigationDrawer`, lines ~227-262)

**In `AppNavigationBar`:**
- Read `val hazeState = LocalHazeState.current`
- When `theme.transparentBars && hazeState != null`:
  - Pass `containerColor = Color.Transparent` to `NavigationBar`
  - Add `Modifier.hazeEffect(hazeState) { style = HazeMaterials.regular(theme.navBarContainerColor) }` to `NavigationBar`
- Otherwise: keep existing `containerColor` behavior

New imports needed: `dev.chrisbanes.haze.hazeSource`, `dev.chrisbanes.haze.hazeEffect`, `dev.chrisbanes.haze.rememberHazeState`, `dev.chrisbanes.haze.materials.HazeMaterials`

### 5. `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/library/LibraryScreen.kt`

**In `LibraryToolBar`:**
- Read `val hazeState = LocalHazeState.current`
- When `theme.transparentBars && hazeState != null`:
  - Change TopAppBar `containerColor` to `Color.Transparent`
  - Add `Modifier.hazeEffect(hazeState) { style = HazeMaterials.thin(theme.topBarContainerColor) }` on the `TopAppBar`
- The existing `Box` wrapper remains unchanged

New import needed: `dev.chrisbanes.haze.hazeEffect`, `dev.chrisbanes.haze.materials.HazeMaterials`

---

## HazeStyle per Bar

| Bar | Style | Tint color |
|---|---|---|
| NavigationBar | `HazeMaterials.regular(tint)` | `theme.navBarContainerColor` (surface @ 60% alpha) |
| TopAppBar | `HazeMaterials.thin(tint)` | `theme.topBarContainerColor` (surface @ 80% alpha) |

`theme.navBarContainerColor` and `theme.topBarContainerColor` are already computed in the `Theme` enum from the Light Modern / Dark Modern color schemes.

---

## Scope

- `hazeSource` is placed on the content Box in `MobileLayout` — covers **all screens** (Library, Home, Search, Settings) automatically with no per-screen changes needed
- Immersive screens (Book/Series/Oneshot) already hide the nav bar (`isImmersiveScreen` check), so `LocalHazeState` will be `null` and no blur is applied
- FAB positioning fix (`LocalTransparentNavBarPadding` in `HomeScreen.kt`) is already in place
- No changes needed in individual screen files beyond `LibraryScreen.kt`

---

## Already Implemented (in current build)

- Light Modern & Dark Modern `AppTheme` enum entries
- Full M3 color schemes for both themes in `Theme.kt`
- `transparentBars: Boolean` property on `Theme` enum
- `navBarContainerColor` / `topBarContainerColor` computed properties on `Theme`
- Scaffold bottom padding = 0dp when `transparentBars` (content extends behind nav bar)
- `LocalTransparentNavBarPadding` for lazy grid bottom content padding
- Library screen Box overlay layout (toolbar floats over scrolling content)
- FAB above nav bar fix in `HomeScreen.kt`
- Theme selection race condition fix in `AppSettingsViewModel.kt`
