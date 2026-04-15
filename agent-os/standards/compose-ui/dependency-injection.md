# Dependency Injection (CompositionLocal)

Use `CompositionLocal` as a service locator for dependencies in the UI layer. Do NOT use a DI framework (Hilt, Koin, etc.) in composables.

## Defining locals

```kotlin
// Re-provided at runtime — use compositionLocalOf
val LocalViewModelFactory = compositionLocalOf<ViewModelFactory> { error("ViewModelFactory not provided") }
val LocalToaster = compositionLocalOf<ToasterState> { error("Toaster not provided") }

// Static / never changes — use staticCompositionLocalOf
val LocalStrings = staticCompositionLocalOf { EnStrings }
```

- Always pass `error("X not provided")` as the default — crashes immediately if a caller forgets to provide it
- Use `staticCompositionLocalOf` only for values that never change after initial setup

## Consuming locals

```kotlin
val viewModelFactory = LocalViewModelFactory.current
val toaster = LocalToaster.current
```

## Providing locals

Wrap the root composable in `CompositionLocalProvider` at the app entry point:

```kotlin
CompositionLocalProvider(
    LocalViewModelFactory provides factory,
    LocalToaster provides toaster,
) {
    AppContent()
}
```
