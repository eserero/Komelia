# Audio Player Non-Immersive Theme Fix — Shaping Notes

## Scope

Fix two visual bugs that appear when "Immersive Card Color" is disabled in app settings:

1. **Mini audio player** still shows a tinted/immersive background instead of matching the control panel exactly.
2. **Expanded audio player** shows a semi-transparent (see-through) background instead of the correct opaque theme background with haze effect on modern themes.

Both players should match `ReaderControlsCard` exactly when immersive is OFF:
- Modern themes (`transparentBars = true`): frosted glass haze effect over page content
- Classic themes: solid `MaterialTheme.colorScheme.surface`

## Decisions

- Read `LocalImmersiveColorEnabled`, `LocalHazeState`, `LocalTheme` directly inside each player composable (already in scope via `CompositionLocalProvider` in `Epub3ReaderContent.android.kt`)
- Mini player: keep `Surface` for `sharedBounds` animation; set `color = Transparent` and `tonalElevation = 0` when immersive OFF; add explicit background Box using same haze/surface logic as `ReaderControlsCard`
- Expanded player: the inner overlay Box that currently always uses `backgroundColor.copy(alpha = 0.72f)` should be conditionally replaced with haze or solid surface when immersive OFF
- Haze source for expanded player = `LocalHazeState` (page content), ensuring the dialog controls below are not visible through the haze

## Context

- **Visuals:** None
- **References:** `ReaderControlsCard.kt` — the canonical pattern to replicate
- **Product alignment:** Bug fix; no new features

## Standards Applied

- `compose-ui/theming` — enum-based theme, `LocalTheme` for color access
