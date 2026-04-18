# Release 2.1.0 Summary

## Features
- **Annotations & Highlights**:
    - **Comic Reader**: Added support for page-level annotations with a new "Map Pin" visual indicator.
    - **EPUB3 Reader**: Implemented text highlighting and note attachment.
    - **Note Navigation**: Added the ability to navigate between notes (Previous/Next) directly from the note editor screen.
    - **UI Integration**: Added a dedicated "Notes" button to reader controls and renamed the "Annotations" tab to "Notes" for clarity.
- **Cross-device Synchronization**: Implemented real-time synchronization for bookmarks and annotations (notes) across devices for both EPUB3, Audio Books and Comic readers, leveraging Komga's Kobo progress API.
- **EPUB3 Customization**: Added a new "Respect publisher colors" setting, allowing users to maintain original book styling while using custom fonts or layouts.
- **Audiobook Improvements**: Progress synchronization for audiobooks between devices for non synchonized audiobooks.
- **UI Polish**: Improved note editor layout, refined context menus in the EPUB reader, and updated theme consistency across dialogs.

---

commit e95263d222ed80b057f6119b704a05ccdbac6d40
Author: eserero <eserero@hotmail.com>
Date:   Sun Apr 19 01:41:28 2026 +0300

    ui: improve note screen and add navigation between notes
    
    - Move delete button to color picker row as an icon
    - Add previous, next, and list navigation icons to note screen
    - Implement note navigation logic for EPUB and Comic readers
    - Fix EPUB3 search button opening wrong tab
    - Add safety checks to reader states to prevent IndexOutOfBoundsException during navigation

commit e7b33caf7d4ff9f9ef80d6c3571298189e85cb4d
Author: eserero <eserero@hotmail.com>
Date:   Sun Apr 19 00:54:54 2026 +0300

    fix: sync annotation text edits and highlighted text across devices
    
    Two bugs in cross-device annotation sync:
    
    1. Editing note text or color on one device was silently discarded on
       the other. Root causes: the merge used createdAt (immutable) to
       resolve conflicts, so edits never won; and initialSync only inserted
       new annotations, never updating existing ones.
    
       Fix: add updatedAt to BookAnnotation and CompactAnnotation (defaults
       to createdAt for backward compat). Merge now uses updatedAt for
       annotation conflict resolution. initialSync applies remote edits
       when compact.updatedAt > existing.updatedAt. updateAnnotation /
       updateComicAnnotation stamp updatedAt = now on every edit.
       DB migration V44 adds the updated_at column (backfilled from
       created_at).
    
    2. Synced EPUB annotations showed no highlighted sentence in the note
       screen (the highlight in the reader text was fine, since that uses
       locatorJson). The selectedText field was never included in the sync
       payload, so it arrived as null on the receiving device.
    
       Fix: add selectedText to CompactAnnotation (@SerialName("s"),
       optional/null for comics). Serialization includes it; deserialization
       restores it when creating new synced annotations.
    
    Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>

commit db9234fd1329cf70f76df773f4b53e2f0bd7d7be
Author: eserero <eserero@hotmail.com>
Date:   Sat Apr 18 20:53:23 2026 +0300

    fix: include page position in image reader progression to fix server validation

commit 5748eaea4684fc15e87892aca648a03ae88df28d
Author: eserero <eserero@hotmail.com>
Date:   Sat Apr 18 20:26:07 2026 +0300

    feat: sync audiobook folder progress and fix comic annotation positioning

commit c6d2d58f0bbdd29427f83c2ecebaec15bd4a2378
Author: eserero <eserero@hotmail.com>
Date:   Sat Apr 18 19:46:04 2026 +0300

    feat: cross-device bookmark and annotation sync via koboSpan hack
    
    - Implements cross-device synchronization for EPUB3 and Comic readers by
      leveraging Komga's koboSpan field within the read progression API.
    - Adds ReaderSyncService for encoding, decoding, and merging compact
      synchronization blobs (bookmarks, annotations, audio bookmarks).
    - Implements a deletion-aware merge strategy using lastModified timestamps
      to propagate deletions across devices without tombstones.
    - Updates Epub3ReaderState and ReaderState (Image) to:
      - Maintain a memory-cached sync blob.
      - Perform initialSync on book load (fetch, merge, update local DB).
      - Trigger updateCacheAndPush on any bookmark/annotation change or page turn.
      - Listen for real-time ReadProgressChanged events to sync concurrent sessions.
    - Updates dependency injection chain to propagate ReaderSyncService and ManagedKomgaEvents.
    - Improves Audiobook synchronization by wiring onBookmarkChange callbacks
      into AudiobookFolderController.

commit e0a397a7cc6a49a4c60490fa14dfa89be14dfeca
Author: eserero <eserero@hotmail.com>
Date:   Sat Apr 18 15:10:46 2026 +0300

    fix: polish annotation UI — context menu, pins, notes button, theming
    
    EPUB reader:
    - Context menu now uses DropdownMenu anchored at selection coordinates
      (dp→px conversion fixes top-of-screen positioning); color swatches
      restored at top, three vertical action items (Copy/Highlight/Note)
      with icons; surface color matches content dialog (no pink tint)
    - Highlight decorations now applied immediately after save by calling
      decorateHighlights() after updating epubView.props
    - Selection x/y stored in Epub3ReaderState and forwarded to menu
    - "Annotations" tab renamed to "Notes" in content dialog
    - Tab row horizontal padding reduced (16→6 dp) so "Bookmarks" fits
      on one line with four tabs visible
    - AnnotationDialog (note editor) container color matches theme
    
    Image reader:
    - "Add annotation" context menu item renamed to "Note"
    - "Annotations" tab renamed to "Notes" in ComicContentDialog
    - Tapping a note in the list now navigates to the annotation's page
      before opening the edit dialog (handles paged/continuous/panels)
    - Notes button (EditNote icon) added to controls bar between the
      upscale divider and settings button; onNotesClick threaded through
      SettingsOverlay → BottomSheetSettingsOverlay → ImageReaderControlsCardNewUI
    - Annotation pins replaced with Google Maps–style teardrop Canvas
      composable (MapPin); tip anchors exactly at the tapped point
    
    Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>

commit fe318fb1c713fe193e655b97c1891c315c95bd10
Author: eserero <eserero@hotmail.com>
Date:   Sat Apr 18 01:36:49 2026 +0300

    fix: add V43 to AppMigrations list, fix build errors in annotation composables

commit 42e96105aa0aa6d9f4bc0b22f063dfc31d509998
Author: eserero <eserero@hotmail.com>
Date:   Sat Apr 18 00:47:29 2026 +0300

    feat: add comic reader annotation state, overlay, and content dialog
    
    Implements Tasks 13–15: wires BookAnnotationRepository into ReaderState
    for CRUD and real-time annotation streaming; adds lastImageBounds /
    getCurrentPageNumber to PagedReaderState; creates ComicAnnotationOverlay
    for pin rendering and ComicContentDialog for the annotations list sheet;
    wires long-press "Add annotation" menu and AnnotationDialog into
    ReaderContent.
    
    Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>

commit d8b7db8f5a054bee8f957f4bb1b0024ee9a3dde0
Author: eserero <eserero@hotmail.com>
Date:   Sat Apr 18 00:26:28 2026 +0300

    feat: wire EPUB annotation state, factory chain, and UI
    
    Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>

commit 11285328c968775b1c2f301027b9c4233cae94cb
Author: eserero <eserero@hotmail.com>
Date:   Sat Apr 18 00:17:37 2026 +0300

    feat: add shared annotation composables and EPUB context menu
    
    Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>

commit cab96465e68848fc856820a3d3269905d1355b9f
Author: eserero <eserero@hotmail.com>
Date:   Sat Apr 18 00:12:18 2026 +0300

    feat: add lastHighlightColor to app settings

commit 179ad87f5bca9a24b940084f0131eb69b58c0526
Author: eserero <eserero@hotmail.com>
Date:   Sat Apr 18 00:10:53 2026 +0300

    feat: wire BookAnnotationRepository into app modules
    
    Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>

commit a82253750b64cb10b488eed5871f649d78c3e414
Author: eserero <eserero@hotmail.com>
Date:   Sat Apr 18 00:09:06 2026 +0300

    feat: add ExposedBookAnnotationRepository

commit 51010648bdd6d56fa9b26e2ef37dbe091850d891
Author: eserero <eserero@hotmail.com>
Date:   Sat Apr 18 00:04:03 2026 +0300

    feat: add BookAnnotationsTable and V43 migration

commit 41f57c0e68604c36d7ceca41709191e51f82cd6d
Author: eserero <eserero@hotmail.com>
Date:   Fri Apr 17 23:52:07 2026 +0300

    feat: add BookAnnotation domain model and repository interface
    
    Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>

commit 3fe2df3db362118974a104db1cc702c4b1f6acc2
Author: eserero <eserero@hotmail.com>
Date:   Fri Apr 17 20:01:44 2026 +0300

    feat: add 'Respect publisher colors' setting to EPUB3 reader
    
    - Added respectPublisherColors to Epub3NativeSettings
    - Added UI toggle in Epub3SettingsCard (Font & Text tab)
    - Updated EpubView and EpubFragment to omit color overrides when either 'Publisher styles' or 'Respect publisher colors' is enabled
    - Added V42 database migration and updated Exposed repositories
    - Minor README improvements

commit 827978f6123235aed843a82cb64119f06997bec4
Author: eserero <eserero@hotmail.com>
Date:   Fri Apr 17 16:55:35 2026 +0300

    chore: bump version to 2.0.0
