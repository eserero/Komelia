# Standards Applied

## compose-ui/dialogs

- Use `DialogLoadIndicator` for any loading state in new dialogs.
- Bottom sheet dialogs use `Surface(shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))`.
- Drag-to-dismiss: detect vertical drag, dismiss when `dragOffsetY > 120f`.
- `heightIn(max = (screenHeightDp * 2f / 3f).dp)` height cap for bottom sheets.

## Data Layer

- Repository interfaces live in `komelia-domain/core/src/commonMain/kotlin/`.
- Exposed implementations live in `komelia-infra/database/sqlite/`.
- Use `ExposedRepository` base class with `transaction {}` helper (runs on `Dispatchers.IO`).
- Reactive lists use `MutableSharedFlow<Unit>(extraBufferCapacity = 1)` + `.onStart { emit(Unit) }` pattern.

## Audio Controllers

- SMIL always wins: folder detection only runs when `clips.isEmpty()`.
- Controllers implement `EpubAudioController` interface.
- SMIL-specific methods narrowcast with `as? MediaOverlayController` before calling.
- `release()` is always called on cleanup via the `EpubAudioController` interface.
