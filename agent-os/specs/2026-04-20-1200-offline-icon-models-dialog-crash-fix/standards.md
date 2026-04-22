# Standards for Offline Icon, Model Dialog, Crash Fix

## compose-ui/dialogs

Use `DialogLoadIndicator` instead of showing a spinner immediately. It delays 200ms before rendering to avoid flicker.

```kotlin
if (isLoading) {
    DialogLoadIndicator(onDismissRequest = { /* cancel */ })
}
```

Do NOT show `CircularProgressIndicator` directly — always use `DialogLoadIndicator`.

## compose-ui/view-models

Use Voyager's `StateScreenModel`. Do NOT use `viewModel()` or `ViewModelProvider`.

```kotlin
class HomeViewModel(...) : StateScreenModel<LoadState<Unit>>(Uninitialized) {
    val cardWidth = cardWidthFlow.stateIn(screenModelScope, Eagerly, defaultCardWidth.dp)
}
```

Obtain via `LocalViewModelFactory.current` → `rememberScreenModel(key) { factory.getXViewModel() }`.
