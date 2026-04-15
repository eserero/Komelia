# Standards for Immersive Detail Screens

The following standards apply to this work. Note: M3 fluid animation requirements take precedence where they conflict with navigation standards.

---

## compose-ui/navigation

Use [Voyager](https://voyager.adriel.cafe/) for navigation. Do NOT use Jetpack Compose Navigation.

- Screens implement `Screen` (or `ReloadableScreen` if they support pull-to-refresh/reload)
- Use the domain ID as `ScreenKey` to prevent duplicate screens on the back stack
- Define constructors that accept both an ID and a full domain object (object is `@Transient` — not serialized)
- Use top-level factory functions when navigation logic must choose between Screen subclasses

**Override for this feature:** The Navigator's `CurrentScreen` rendering will be wrapped in `SharedTransitionLayout + AnimatedContent` to support shared element transitions. This is an exception to the standard Voyager `Navigator { }` block.

---

## compose-ui/theming

Themes are defined as an enum where each value embeds its own `ColorScheme`.

- `MaterialTheme(colorScheme = theme.colorScheme)` is the standard application point
- **Override for this feature:** The detail screen card uses a dynamically extracted dominant color from the cover image instead of a theme-defined surface color. This color is local to the detail screen composable and does not affect `MaterialTheme`.

---

## compose-ui/view-models

Use Voyager's `StateScreenModel` instead of Android `ViewModel`.

- Extend `StateScreenModel<LoadState<T>>` with `Uninitialized` as initial state
- Use `screenModelScope` for coroutines
- Obtain instances via `LocalViewModelFactory.current`
- Pass explicit `key` to `rememberScreenModel` when VM depends on runtime parameters

**For this feature:** No new ViewModels are needed. Existing `SeriesViewModel`, `BookViewModel`, and `OneshotViewModel` are reused. The new immersive content composables receive the same state and callbacks as the existing content composables.

---

## compose-ui/dependency-injection

Use `CompositionLocal` as a service locator for dependencies in the UI layer.

- Re-provided values: `compositionLocalOf` with `error("X not provided")` default
- Static/never-changes: `staticCompositionLocalOf`

**New locals added by this feature:**
```kotlin
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }
```
These are nullable (default null) so composables that don't participate in shared transitions degrade gracefully.
