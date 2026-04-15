# EPUB3 Reader — Shaping Decisions

## Problem

Komelia's two existing EPUB readers (Komga, TTSU) use WebViews, which limits rendering fidelity
and doesn't support synchronized audio overlays (SMIL). Users reading EPUB 3 audiobooks with
text/audio sync get no native experience.

## Solution

Integrate the `epub-reader/` Android library module (already in repo) into the Komelia reader
framework. The module wraps Readium Kotlin Toolkit v3.1.2 and ExoPlayer/Media3.

## Key Decisions

### Android-only
The Readium navigator uses native Android fragments. The option is hidden on Desktop/Web via
expect/actual `epub3ReaderAvailable` flags. Non-Android platforms fall through to
`KomgaEpubReaderState` as an unreachable-in-practice fallback.

### EpubView as AndroidView in Compose
`EpubView` is a `FrameLayout` subclass; it's embedded via `AndroidView` in a Compose-based
`Epub3ReaderContent` composable. The `FragmentActivity` required by `EpubView` is obtained via
`LocalContext.current as FragmentActivity`.

### File access pattern
Komelia downloads EPUBs before opening them (see `TtsuReaderState` / `KomgaEpubReaderState`).
`Epub3ReaderState` follows the same pattern: it uses `KomgaBookApi.downloadBook()` to get the
local path, then passes it to `BookService.openPublication()`.

### No custom Progress persistence (MVP)
Readium tracks locators internally. MVP doesn't wire locator persistence back to Komga's read
progress API — that's a follow-up.

### minSdk alignment
`epub-reader/build.gradle` declares `minSdk 24`. Komelia's Android target is minSdk 26, which
is higher, so there's no conflict.
