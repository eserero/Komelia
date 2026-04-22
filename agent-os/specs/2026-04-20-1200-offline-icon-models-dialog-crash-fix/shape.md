# Shape Notes

## Scope

Four features shaped together because they're all small, orthogonal, and share the same release.

## Decisions

- **Offline icon (online state):** Show WiFi icon always (not only in offline mode). User confirmed: tapping online state shows "Go Offline?" dialog; tapping offline state shows "Go Online?" dialog.
- **First-launch prompt:** Shown once, after first successful login. Stored via `modelDownloadPromptShown` flag in `ImageReaderSettings`. Only shown if BOTH models are missing — if either is already downloaded, skip.
- **Default NCNN:** Simple default value change; existing installations unaffected.
- **Crash fix:** `runCatching` wrapping is minimal and correct — sync will simply retry on the next `ReadProgressChanged` event if the current one fails.

## Context

- **Visuals:** None provided.
- **References:** `AppBar.kt` (existing offline button pattern), `ReaderState.kt` / `Epub3ReaderState.kt` (crash sites), `NcnnUpscalerSettings.kt` (model defaults).
- **Product alignment:** All changes improve first-run UX and stability.

## Standards Applied

- `compose-ui/dialogs` — `DialogLoadIndicator` for download progress in model dialog
- `compose-ui/view-models` — `StateScreenModel` / `screenModelScope` patterns
