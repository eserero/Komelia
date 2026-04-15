# EPUB3 Reader — Code References

## Existing Reader States (patterns to follow)
- `komelia-ui/src/commonMain/.../reader/epub/TtsuReaderState.kt` — book download + webview pattern
- `komelia-ui/src/commonMain/.../reader/epub/KomgaEpubReaderState.kt` — Komga book API usage

## epub-reader Module
- `epub-reader/src/main/java/com/storyteller/reader/EpubView.kt` — main view class
- `epub-reader/src/main/java/com/storyteller/reader/BookService.kt` — publication lifecycle
- `epub-reader/src/main/java/com/storyteller/reader/AudiobookPlayer.kt` — media overlay playback
- `epub-reader/src/main/java/com/storyteller/reader/ReadingActivity.kt` — reference integration

## Platform expect/actual examples
- `komelia-ui/src/androidMain/.../platform/BackPressHandler.android.kt`
- `komelia-ui/src/jvmMain/.../settings/imagereader/ncnn/SettingsState.jvm.kt`

## Enum
- `komelia-domain/core/src/commonMain/.../settings/model/EpubReaderType.kt`

## Strings
- `komelia-ui/src/commonMain/.../strings/AppStrings.kt`
- `komelia-ui/src/commonMain/.../strings/EnStrings.kt`

## Settings UI
- `komelia-ui/src/commonMain/.../settings/epub/EpubReaderSettingsContent.kt`

## ViewModel + Screen
- `komelia-ui/src/commonMain/.../reader/epub/EpubReaderViewModel.kt`
- `komelia-ui/src/commonMain/.../reader/EpubScreen.kt`

## Manifest
- `komelia-app/src/androidMain/AndroidManifest.xml`
