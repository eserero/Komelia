# EPUB3 Reader Integration — Implementation Plan

## Overview

Add a third EPUB reader option (Readium Kotlin Toolkit v3.1.2 + ExoPlayer/Media3) that supports
EPUB 3 with synchronized audio overlays (SMIL). Android-only — hidden on Desktop and Web.

The `epub-reader/` module already exists with fonts pre-populated. This plan wires it into the
Gradle project and the Komelia reader framework.

Bean: Komelia-ecr6

---

## Tasks

### T1: Save Spec Documentation ✅
Create `agent-os/specs/2026-03-17-epub3-reader/` with plan.md, shape.md, etc.

### T2: Register `epub-reader` as a Gradle Module
- `settings.gradle.kts`: add `include(":epub-reader")`
- `komelia-ui/build.gradle.kts`: add `androidMain` dep on `:epub-reader`

### T3: Add `EPUB3_READER` to the Enum
- `komelia-domain/core/src/commonMain/.../EpubReaderType.kt`
- Add as third entry; serialization by name is backward-compatible

### T4: Add UI Strings
- `AppStrings.kt`: add `epubReaderTypeEpub3` field to `SettingsStrings`
- `EnStrings.kt`: add value "EPUB3 Reader"
- `AppStrings.kt`: add case in `forEpubReaderType()`

### T5: Update Settings UI (hide on non-Android)
- Add `epub3ReaderAvailable` expect/actual across source sets
- Filter enum entries when not on Android
- Add description `when` case for EPUB3_READER

### T6: Create `Epub3ReaderState` (androidMain)
- Implements `EpubReaderState`
- Opens publication via `BookService.openPublication()`
- Sets up `EpubView` and starts reading

### T7: Add `Epub3ReaderFactory` (expect/actual)
- commonMain expect function
- androidMain: returns `Epub3ReaderState`
- jvmMain + wasmJsMain: fallback to `KomgaEpubReaderState`

### T8: Add `Epub3ReaderContent` Composable (expect/actual)
- commonMain: `expect fun Epub3ReaderContent(state: EpubReaderState)`
- androidMain: `AndroidView` wrapping `EpubView`
- jvmMain + wasmJsMain: "not available" Text

### T9: Update `EpubScreen.kt`
- Expose `readerType` from ViewModel
- Route to `Epub3ReaderContent` when `EPUB3_READER`

### T10: AndroidManifest — PlaybackService
- Add `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_MEDIA_PLAYBACK` permissions
- Declare `PlaybackService` with `mediaPlayback` foregroundServiceType
