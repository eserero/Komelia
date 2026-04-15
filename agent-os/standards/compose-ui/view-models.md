# ViewModels (Voyager StateScreenModel)

Use Voyager's `StateScreenModel` instead of Android `ViewModel`. Do NOT use `viewModel()` or `ViewModelProvider`.

## Defining a ViewModel

```kotlin
class HomeViewModel(
    private val seriesApi: KomgaSeriesApi,
    // ...
) : StateScreenModel<LoadState<Unit>>(Uninitialized) {
    // expose state as StateFlow
    val cardWidth = cardWidthFlow.stateIn(screenModelScope, Eagerly, defaultCardWidth.dp)
}
```

- Extend `StateScreenModel<LoadState<T>>` — initial state is typically `Uninitialized`
- Use `screenModelScope` (not `viewModelScope`) for coroutines

## Obtaining a ViewModel in a Screen

```kotlin
@Composable
override fun Content() {
    val viewModelFactory = LocalViewModelFactory.current

    // Build a stable key from all inputs that affect VM identity
    val vmKey = remember(libraryId, serverUrl) {
        buildString {
            libraryId?.let { append(it.value) }
            append(serverUrl)
        }
    }

    val vm = rememberScreenModel(vmKey) { viewModelFactory.getHomeViewModel() }
}
```

- Always pass an explicit `key` to `rememberScreenModel` when the VM depends on runtime parameters
- Obtain instances via `LocalViewModelFactory` — never construct VMs directly in composables
- Add all parameters that affect VM identity to both the `remember` key and the `buildString` block
