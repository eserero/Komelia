# Standards

## compose-ui/theming

Themes are defined as an enum where each value embeds its own `ColorScheme`. Access theme colors via `LocalTheme.current.colorScheme.surface` (not raw `MaterialTheme.colorScheme`) when you need to reference the theme outside of Material3 slots — this ensures consistency with the custom theme enum.

Modern themes have `transparentBars = true`; this is the canonical signal for whether haze effects should be applied. `LocalHazeState` is `null` on non-modern themes.
