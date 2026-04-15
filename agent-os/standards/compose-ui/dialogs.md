# Dialogs

## Loading state in dialogs

Use `DialogLoadIndicator` instead of showing a spinner immediately. It delays 200ms before rendering to avoid flicker when the operation completes quickly.

```kotlin
// In a composable that shows a loading dialog:
if (isLoading) {
    DialogLoadIndicator(onDismissRequest = { /* cancel */ })
}
```

- Do NOT show a `CircularProgressIndicator` or custom loading dialog directly — always use `DialogLoadIndicator`
- The 200ms delay is intentional; do not remove it

## Implementation reference

```kotlin
@Composable
fun DialogLoadIndicator(onDismissRequest: () -> Unit) {
    var showLoadIndicator by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(200)
        showLoadIndicator = true
    }
    if (showLoadIndicator) {
        AppDialog(onDismissRequest = onDismissRequest) {
            CircularProgressIndicator()
        }
    }
}
```
