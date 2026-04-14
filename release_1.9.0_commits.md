# Release 1.9.0 Summary

## User-Facing Features

### 📂 Open Local Files (Android)
- **Open from Device**: You can now open `.cbz` and `.epub` files directly from your device's file manager using Komelia.
- **Saved Progress**: Komelia now remembers your reading progress and bookmarks for these local files.

### ✨ Visual Customization
- **Thumbnail Styling**: New appearance settings for **Thumbnail Shadow** and **Corner Radius** are now available.
- **Improved Consistency**: Reverted New UI 2 thumbnail fonts to match the standard UI for a more uniform look.

### 🎧 Audio Player Updates (EPUB)
- **New Controls**: Added a **Bookmark button** and a **0.5x playback speed** option to the main audio player screen.

## Bug Fixes
- **Selection Bars**: Fixed multi-selection bars to correctly use the app accent color and improved text contrast for better readability.
- **Grid Alignment**: Fixed thumbnails not being centered in the grid when using the "card width scale" setting.
- **Immersive Menus**: Fixed the missing "Edit" option in the oneshot screen menu.
- **Layout Stability**: Fixed various alignment and scaling issues in list views to ensure a more stable layout.

---

# Full Commit History Since 1.8.0

## Commit: 65fd150bcd291109989f611f63807ef268680e5d
**Author:** eserero <eserero@hotmail.com>
**Date:** Tue Apr 14 12:03:14 2026 +0300

docs: add implementation details for local file intent support

- Document the intent filter setup for CBZ and EPUB files
- Explain the integration between LocalFileBookApi and the reader UI
- Detail the read progress persistence for local files

---

## Commit: cf52137f9bf9341ee27189682294c67300d7553f
**Author:** eserero <eserero@hotmail.com>
**Date:** Tue Apr 14 11:58:20 2026 +0300

feat(android): open local CBZ/EPUB files via intent

- Add ACTION_VIEW intent filters to MainActivity so Android offers
  Komelia in the "Open with" dialog for .cbz and .epub files
- Split intent filters: one MIME-type-based (fixes dot-in-filename
  issue with content:// URIs) and one path-pattern-based for file://
- Use singleTask launch mode so re-opening a file calls onNewIntent
  on the existing activity instead of creating a duplicate
- LocalFileBookApi reads pages from local zip archives via
  ContentResolver; supports both CBZ and EPUB
- LocalFileReadProgressRepository persists read progress and EPUB
  bookmarks across sessions keyed by a stable virtual book ID
- When a local file intent arrives while a book is already open,
  popUntilRoot() closes the current reader cleanly before opening
  the new one

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthreply.com>

---

## Commit: 4b813f279ab5d54f479bb3258cd7af3ce898ca98
**Author:** eserero <eserero@hotmail.com>
**Date:** Mon Apr 13 23:44:41 2026 +0300

feat(ui): fix list card layout and thumbnail scaling

- Update LibraryItemCard to use IntrinsicSize.Min width and height-priority aspect ratio for stable list layouts
- Fix BookDetailedListCard and SeriesDetailedListCard to use top alignment and fixed width for thumbnails
- Restrict list card titles and summaries to 2 lines for better compactness
- Update README.md with improved formatting for the fork introduction

---

## Commit: 10ffff0b48eb610db3acf7a7cafc4734ae963b8f
**Author:** eserero <eserero@hotmail.com>
**Date:** Mon Apr 13 23:16:15 2026 +0300

feat(epub): improve audio player media controls

- Add bookmark toggle button to the right of the chapter chip, reusing Epub3BookmarkToggleButton with identical behavior to the controls card
- Add 0.5× speed option to playback speed chips
- Remove "Speed" text label from speed section (chips are self-explanatory)
- Center speed chip rows

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthreply.com>

---

## Commit: 36f9599e86ffbcdafd7128655554d8ebc938cb09
**Author:** eserero <eserero@hotmail.com>
**Date:** Mon Apr 13 15:26:37 2026 +0300

feat(ui): add thumbnail shadow and corner radius settings and fix layout issues

---

## Commit: e0603ebae57e99d971ce458d00d6ec6ca45b2239
**Author:** eserero <eserero@hotmail.com>
**Date:** Mon Apr 13 13:42:25 2026 +0300

ui: improve multi-selection bars

- use Popup for BulkActionsContainer to ensure it's rendered above other elements
- use LocalAccentColor for background of both top and bottom selection bars
- add status bar padding to top selection bar when using transparent bars
- add luminance-based content coloring for better contrast

---

## Commit: 36861f039af2d278642a52df61cd036616b08332
**Author:** eserero <eserero@hotmail.com>
**Date:** Mon Apr 13 13:20:49 2026 +0300

style: revert New UI 2 thumbnail font to match standard UI

---

## Commit: 82bbd3f10b85224ef6b29a2dcde1b89fc61c2d95
**Author:** eserero <eserero@hotmail.com>
**Date:** Mon Apr 13 13:04:45 2026 +0300

fix(ui): add edit option to oneshot immersive screen dropdown

---

## Commit: 0dff584ab2da2d2f103416b77b504dab4bce5dcf
**Author:** eserero <eserero@hotmail.com>
**Date:** Mon Apr 13 12:52:45 2026 +0300

fix(ui): center thumbnails within grid cells when cardWidthScale is applied

---
