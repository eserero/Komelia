# References

## Existing audio player code (SMIL mode)

- `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/audio/MediaOverlayController.kt` — existing SMIL controller to refactor
- `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/audio/AudioMiniPlayer.kt` — mini player composable
- `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/audio/AudioFullScreenPlayer.kt` — full-screen player composable

## Existing epub reader

- `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/Epub3ReaderState.kt` — state/controller init
- `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/Epub3ReaderContent.android.kt` — UI composition
- `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/Epub3ReaderFactory.android.kt` — factory / DI

## Existing DB patterns to replicate

- `komelia-infra/database/sqlite/src/commonMain/kotlin/snd/komelia/db/epub/ExposedEpubBookmarkRepository.kt` — reactive Flow bookmark pattern
- `komelia-infra/database/sqlite/src/commonMain/kotlin/snd/komelia/db/ExposedRepository.kt` — base class with `transaction {}` helper
- `komelia-infra/database/sqlite/src/commonMain/kotlin/snd/komelia/db/migrations/AppMigrations.kt` — migration registration

## Existing UI dialog to replicate

- `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/Epub3ContentDialog.kt` — bottom sheet with drag handle, tabs, drag-to-dismiss (reference for AudioTrackListDialog)

## DI entry point

- `komelia-app/src/androidMain/kotlin/snd/komelia/AndroidAppModule.kt` — instantiates repos and wires factory
