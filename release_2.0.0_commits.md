# Release 2.0.0 - Sipurra

## Summary of Changes

### Important Note
* Because the application was rebranded to Sipurra - it will no longer upgrade existing application or Komelia and need to be installed again. Note that you can recover local downloads using the offline sync feature in settings - so this should not be too complicated.

### New Features
*   **Rebranding to Sipurra**: The application has been fully renamed from Komelia to Sipurra. This includes the application name, Android launcher icon label, UI headers, and build artifacts.
*   **Parallel Installation**: Updated internal identifiers (applicationId) and data directories to allow Sipurra to be installed and run side-by-side with the original Komelia application.
*   **New Update System**: The app now tracks updates from the new Sipurra GitHub repository.
*   **Floating Navigation Bar (Mobile)**: A new optional centered, pill-shaped floating toolbar for mobile navigation. It supports "Haze" (transparency with blur) and adapts to theme accent colors.
*   **Audiobook Folder Playback**: Native support for playing audiobook folders (unsynchronized tracks) with dedicated track list dialogs and database-backed position/bookmark history. Since Komga do not support audio books then you must copy an audiobook folder (with the name audiobook or audio) inside an epub file. Sipurra will identify that this folder exist in an epub file and will show the media player. in this case the media player progress and the book progress cannot be synced, so the application provide a separate seeking, chapter and bookmarking for the audio book and a separate one for the epub.
*   **Audio Player Overhaul**:
    *   The full-screen audio player now utilizes the entire screen height.
    *   **Folder-Mode Audiobooks**: Added a full-book seek slider with live time display, ±20s/30s seek buttons, and persistent position tracking.
    *   **Synchronized Audio (SMIL)**: Improved navigation with dedicated page-flip buttons and skip-chapter buttons.
    *   UI refinements including better spacing, layout alignment, and accent color integration.

### Bug Fixes
*   **EPUB3 Reading Position**: Fixed a bug where rotating the screen would cause the EPUB3 reader to lose the current reading position.
*   **Floating UI Stability**: Fixed layout instability and flickering in the new floating navigation bar when switching between screens.

---

## feat: rename application from Komelia to Sipurra

- Update application visible name to "Sipurra" in UI and Android launcher
- Update Android applicationId to io.github.eserero.sipurra
- Update User-Agent to eserero/Sipurra
- Update GitHub repository URLs for updates and model downloads
- Rename build output artifacts to sipurra-app
- Update README and desktop branding

---

## fix: restore EPUB3 reading position after orientation change

On rotation, Readium's paginated WebView rebuilds its CSS column layout
for the new viewport size, resetting the horizontal scroll to 0 and
emitting progression=0.0 (beginning of chapter). Nobody was calling
go(savedLocator) to restore position in the new layout.

Fix: override onSizeChanged() in EpubView to capture the reading position
before the reflow occurs, then queue go() via post{} so it executes after
the new column layout is ready.

Also keep EpubView.props.locator in sync on every page turn so that
onSizeChanged always captures the user's actual current position rather
than the stale initial/server-fetched locator.

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>

---

## fix: replace split FAB with simple Read FAB + left dropdown menu in floating island

- Remove FloatingSplitFAB (caused layout instability in the island)
- Book screen floating mode now uses two separate round FABs:
  - Right: simple Read button (book icon, primary action)
  - Left: MoreVert button opening a standard M3 DropdownMenu
    with Read / Read Incognito / Download items
- Add LocalFloatingActionButtonLeft CompositionLocal for left-side island FABs
- Extend island Layout in MainScreen to place and overflow-handle both sides
- Non-floating mode (SplitFabMenu) is unchanged

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>

---

## feat: integrate FAB with floating navigation bar island

- Moved FABs to float next to the centered floating navigation bar in a unified 'island' UI.
- Implemented a custom Layout in MainScreen to keep the bar centered while placing the FAB to its right, with overflow handling for narrow screens.
- Added owner-based FAB registration (Pair<Any, @Composable>) to prevent content from being cleared during screen transitions.
- Created FloatingFAB, FloatingSplitFAB, and FloatingIslandMenuItem components matching the pill-shaped, hilled, and bordered style.
- Updated HomeScreen, SeriesListContent (Library), and Immersive Series/Book screens to use the floating island when enabled.
- Optimized immersive layouts to hide redundant FAB containers, fixing vertical misalignment.
- Restored standard FAB menu behavior for non-floating modes by resolving naming collisions with Material 3 components.

Known Issue: The Book screen split FAB menu still has layout/UX issues in floating mode.

---

## feat: add floating navigation bar option for New UI 2

- Implemented a centered, pill-shaped floating toolbar for mobile navigation.
- Added 'useFloatingNavigationBar' setting to AppSettings with database migration (V41).
- Integrated haze effect support, adhering to theme transparency settings.
- Refined selection style to use icon tinting based on accent color.
- Ensured visibility across light/dark themes with tonal elevation and subtle border.
- Updated layout to handle content padding correctly when the floating bar is enabled.

---

## feat: audio player improvements and implementation plans

- Expanded full-screen audio player to fill entire screen height.
- Enhanced folder-mode audiobooks:
  - Full-book seek slider.
  - Live time display during dragging.
  - Added ±20s/30s relative seek buttons.
  - Fixed seek snap-back bug when paused.
- Enhanced SMIL mode:
  - Added dedicated page navigation buttons.
  - Dedicated chapter navigation via SkipPrevious/SkipNext.
- UI Polish:
  - Play button respects accent color.
  - Added gap between chapter chip and bookmark button.
  - Aligned volume slider width with seek slider.
- Added agent-os specification for audio player improvements.
- Added implementation plans for AGP upgrade, library UI, and local file intent.

---

## feat: add audio seek slider for folder-mode expanded player

In folder-mode audiobooks the expanded player now shows a per-track
seek slider instead of the SMIL page-navigator. Dragging seeks within
the current track; track changes reset the thumb. SMIL mode is
unchanged. Includes agent-os spec and product docs.

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>

---

## feat: wire AudioPositionRepository and AudioBookmarkRepository into epub reader DI

Adds the two new audio repositories to AppRepositories, threads them through
EpubReaderViewModel and the createEpub3ReaderState expect/actual chain, and
provides ExposedAudioPositionRepository/ExposedAudioBookmarkRepository instances
in both AndroidAppModule and DesktopAppModule.

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>

---

## feat: Epub3ReaderContent routes chapter/bookmark actions for audiobook folder mode

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>

---

## feat: Epub3ReaderState detects audiobook folder and initializes AudiobookFolderController


---

## feat: add AudioTrackListDialog matching Epub3ContentDialog UX


---

## feat: add AudiobookFolderController for unsynchronized audiobook folder playback

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>

---

## feat: add audio folder DB tables, repositories, and V40 migration

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>

---

## feat: add EpubAudioController interface and audio folder domain models


---

## docs: add release 1.9.0 full commit history


---
