# References

## Offline button pattern (AppBar.kt)

- **Location:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/topbar/AppBar.kt` lines 126–142
- **Relevance:** Shows the existing `ConfirmationDialog` pattern for offline mode toggle
- **Key patterns:** `ElevatedButton` + `ConfirmationDialog` with `showConfirmationDialog` state variable

## Crash sites (ReaderState / Epub3ReaderState)

- **Location:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/reader/image/ReaderState.kt` lines 149–153
- **Location:** `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/Epub3ReaderState.kt` lines 630–634
- **Relevance:** `.launchIn()` without exception handling; `initialSync()` makes network calls that can throw `ConnectTimeoutException`

## goOnline() pattern (MainScreenViewModel)

- **Location:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/MainScreenViewModel.kt` lines 110–117
- **Relevance:** Template for `goOffline()` — mirror this function calling `putOfflineMode(true)`

## NCNN defaults

- **Location:** `komelia-domain/core/src/commonMain/kotlin/snd/komelia/settings/model/NcnnUpscalerSettings.kt` lines 7–8
- **Relevance:** Change `engine = WAIFU2X` / `model = "models-cunet/scale2.0x_model"` to `REAL_ESRGAN` / `"models-realesrgan"`

## Model download infrastructure

- **Location:** `komelia-domain/core/src/androidMain/kotlin/snd/komelia/updates/AndroidOnnxModelDownloader.kt`
- **Relevance:** `ncnnDownload(url)` and `panelDownload(url)` — used by the model download dialog

## ImageReaderSettings (for prompt flag)

- **Location:** `komelia-infra/database/shared/src/commonMain/kotlin/snd/komelia/db/ImageReaderSettings.kt`
- **Relevance:** Add `modelDownloadPromptShown: Boolean = false` here

## ReaderSettingsRepositoryWrapper

- **Location:** `komelia-infra/database/shared/src/commonMain/kotlin/snd/komelia/db/repository/ReaderSettingsRepositoryWrapper.kt`
- **Relevance:** Wire the new flag through (follow existing boolean field patterns)
