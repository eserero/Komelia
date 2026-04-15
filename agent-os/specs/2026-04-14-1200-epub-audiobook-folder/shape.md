# Epub Audiobook Folder Player — Shaping Notes

## Scope

Add a second audio player mode to the epub3 reader. When an epub has no SMIL synchronized audio but contains a folder named `audio` or `audiobook` (case-insensitive) with audio files, show the existing mini/full-screen player in "folder mode". Audio plays independently from the text — no synchronization, no text highlighting. From the library/book detail perspective, the epub still looks like a normal epub.

## Decisions

- **Priority**: SMIL always wins. Folder detection only runs when `clips.isEmpty()`.
- **Detection**: Folders named `audio` or `audiobook` (case-insensitive) anywhere within the extracted epub directory. Audio extensions: mp3, m4a, m4b, ogg, aac, flac, opus.
- **Chapters**: One chapter = one audio file, sorted by filename. Track title = cleaned filename (strip leading numbers/underscores/separators). Future: parse `manifest.json` for richer metadata (out of scope).
- **Duration**: Read via `MediaMetadataRetriever` on IO dispatcher during `initialize()`. Failed reads default to 0.0 (benign — player still loads and plays).
- **Bookmarks**: Both resume position (auto-save single row per book on pause/release) AND user-created explicit bookmarks list (stored in DB, shown in `AudioTrackListDialog`).
- **UI**: Same mini + full-screen player as SMIL mode. Differences handled at `Epub3ReaderContent` level — no new player composables needed. Chapter chip routes to audio track list dialog instead of epub TOC. Bookmark button saves audio position instead of epub text locator.
- **Time display**: Moved outside the `positions.size > 1` guard in `AudioFullScreenPlayer` so it shows in folder mode (no epub positions available).
- **No text sync**: `AudiobookFolderController` has no `handleUserLocatorChange`/`handleDoubleTap`/`attachView`. SMIL-only calls in `Epub3ReaderState` narrow-cast with `as? MediaOverlayController` before invoking.

## Constraints

- SMIL behavior must be completely unchanged.
- From library perspective the epub looks like a normal epub (no special indicators in this plan).
- Future: support `manifest.json` for richer chapter metadata (deferred).

## Context

- **Visuals:** None — reuse existing player UI exactly.
- **References studied:** `MediaOverlayController.kt`, `AudioMiniPlayer.kt`, `AudioFullScreenPlayer.kt`, `ExposedEpubBookmarkRepository.kt`, `AudiobookPlayer.kt`, `PlaybackService.kt`, `Epub3ReaderState.kt`, `Epub3ReaderContent.android.kt`.
- **Product alignment:** N/A.

## Standards Applied

- `compose-ui/dialogs` — Use `DialogLoadIndicator` for any loading state in new dialogs (N/A in this plan since AudioTrackListDialog loads synchronously).
