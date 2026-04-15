# Epub Audiobook Folder Player — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a second audio player mode that activates when an epub contains a folder of audio files (`audio/` or `audiobook/`), enabling unsynchronized audiobook playback alongside manual text reading.

**Architecture:** Introduce a shared `EpubAudioController` interface that both `MediaOverlayController` (SMIL sync) and the new `AudiobookFolderController` (folder mode) implement. The existing mini/full-screen player UI is reused with minimal parameter additions. Detection runs in `Epub3ReaderState.initialize()` after epub extraction, only when no SMIL clips are found.

**Tech Stack:** Kotlin, Jetpack Compose, Media3/ExoPlayer (existing), Exposed/SQLite (existing), Readium Publication API (existing).

**Spec folder:** `agent-os/specs/2026-04-14-1200-epub-audiobook-folder/`

---

## Key Design Decisions

- **Detection**: If epub has SMIL clips → existing SMIL mode (no change). Otherwise scan for folders named `audio` or `audiobook` (case-insensitive) and collect audio files from them.
- **Priority**: SMIL always wins. Folder detection only runs when `clips.isEmpty()`.
- **Chapters**: One chapter = one audio file, sorted by filename. Future: parse `manifest.json`.
- **Bookmarks**: Auto-save resume position (per book, single row) + user-created explicit bookmarks (list).
- **UI**: Exactly same mini + full-screen player UI. Two behavioral differences:
  1. Chapter chip opens audio track list instead of epub TOC.
  2. Bookmark toggle saves audio position instead of epub text locator.
  3. Time display row always shown (not gated on epub positions).
- **No text sync**: `AudiobookFolderController` has no `handleUserLocatorChange` / `handleDoubleTap` / `attachView`. Locator callbacks in `Epub3ReaderState` narrow-cast to `MediaOverlayController` before calling SMIL-specific methods.

---

## Files Overview

**New files:**
- `komelia-domain/core/src/commonMain/kotlin/snd/komelia/audiobook/AudioFolderTrack.kt`
- `komelia-domain/core/src/commonMain/kotlin/snd/komelia/audiobook/AudioPosition.kt`
- `komelia-domain/core/src/commonMain/kotlin/snd/komelia/audiobook/AudioPositionRepository.kt`
- `komelia-domain/core/src/commonMain/kotlin/snd/komelia/audiobook/AudioBookmark.kt`
- `komelia-domain/core/src/commonMain/kotlin/snd/komelia/audiobook/AudioBookmarkRepository.kt`
- `komelia-infra/database/sqlite/src/commonMain/kotlin/snd/komelia/db/tables/AudioPositionTable.kt`
- `komelia-infra/database/sqlite/src/commonMain/kotlin/snd/komelia/db/tables/AudioBookmarksTable.kt`
- `komelia-infra/database/sqlite/src/commonMain/kotlin/snd/komelia/db/audiobook/ExposedAudioPositionRepository.kt`
- `komelia-infra/database/sqlite/src/commonMain/kotlin/snd/komelia/db/audiobook/ExposedAudioBookmarkRepository.kt`
- `komelia-infra/database/sqlite/src/commonMain/composeResources/files/migrations/app/V40__audio_folder.sql`
- `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/audio/EpubAudioController.kt`
- `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/audio/AudiobookFolderController.kt`
- `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/audio/AudioTrackListDialog.kt`

**Modified files:**
- `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/audio/MediaOverlayController.kt` — implement `EpubAudioController`; rename `seekToNextClip`/`seekToPrevClip` → `seekToNext`/`seekToPrev`
- `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/audio/AudioMiniPlayer.kt` — accept `EpubAudioController` instead of `MediaOverlayController`
- `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/audio/AudioFullScreenPlayer.kt` — accept `EpubAudioController`; move time display outside positions block; add `audioTracks` + `audioBookmarks` + `isAudioBookmarked` params
- `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/Epub3ReaderState.kt` — change controller type; add folder detection; narrow SMIL-specific calls; add new repo params
- `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/Epub3ReaderContent.android.kt` — route chapter click and bookmark toggle based on controller type
- `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/Epub3ReaderFactory.android.kt` — add new repo params
- `komelia-infra/database/sqlite/src/commonMain/kotlin/snd/komelia/db/migrations/AppMigrations.kt` — add V40
- `komelia-app/src/androidMain/kotlin/snd/komelia/AndroidAppModule.kt` — instantiate new repos

---

## Task 1: Save spec documentation

**Files:**
- Create: `agent-os/specs/2026-04-14-1200-epub-audiobook-folder/plan.md`
- Create: `agent-os/specs/2026-04-14-1200-epub-audiobook-folder/shape.md`
- Create: `agent-os/specs/2026-04-14-1200-epub-audiobook-folder/standards.md`
- Create: `agent-os/specs/2026-04-14-1200-epub-audiobook-folder/references.md`

- [ ] **Step 1: Write shape.md**

```markdown
# Epub Audiobook Folder Player — Shaping Notes

## Scope

Add a second audio player mode to the epub3 reader. When an epub has no SMIL synchronized audio but contains a folder named `audio` or `audiobook` (case-insensitive) with audio files, show the existing mini/full-screen player in "folder mode". Audio plays independently from the text — no synchronization, no text highlighting.

## Decisions

- SMIL takes priority: folder detection only runs when `clips.isEmpty()`
- Detection: folder names `audio` or `audiobook` (case-insensitive) anywhere in extracted epub
- Chapter = one audio file, sorted by filename. Track title = cleaned filename (strip leading numbers/underscores)
- Duration: read via `MediaMetadataRetriever` on IO dispatcher during init
- Bookmarks: auto-save resume position (single row per book) + user-created bookmarks list
- UI: same mini + full-screen player; routing differences handled in `Epub3ReaderContent`
- No text sync: SMIL-only methods narrowly cast with `as? MediaOverlayController` before calling

## Constraints

- SMIL behavior must be completely unchanged
- From library perspective the epub looks like a normal epub (no special indicators)
- Future: support `manifest.json` for richer chapter metadata (out of scope for this plan)

## Context

- **Visuals:** None — reuse existing player UI exactly
- **References:** `MediaOverlayController.kt`, `AudioMiniPlayer.kt`, `AudioFullScreenPlayer.kt`, `ExposedEpubBookmarkRepository.kt`
- **Product alignment:** N/A

## Standards Applied

- compose-ui/dialogs — DialogLoadIndicator for any loading state in new dialogs
```

- [ ] **Step 2: Write standards.md, references.md, and copy plan.md**

```
Copy this plan file to agent-os/specs/2026-04-14-1200-epub-audiobook-folder/plan.md
```

- [ ] **Step 3: Commit spec files**

```bash
git add agent-os/specs/2026-04-14-1200-epub-audiobook-folder/
git commit -m "docs: add spec for epub audiobook folder player"
```

---

## Task 2: Domain models and interfaces

**Files:**
- Create: `komelia-domain/core/src/commonMain/kotlin/snd/komelia/audiobook/AudioFolderTrack.kt`
- Create: `komelia-domain/core/src/commonMain/kotlin/snd/komelia/audiobook/AudioPosition.kt`
- Create: `komelia-domain/core/src/commonMain/kotlin/snd/komelia/audiobook/AudioPositionRepository.kt`
- Create: `komelia-domain/core/src/commonMain/kotlin/snd/komelia/audiobook/AudioBookmark.kt`
- Create: `komelia-domain/core/src/commonMain/kotlin/snd/komelia/audiobook/AudioBookmarkRepository.kt`
- Create: `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/audio/EpubAudioController.kt`

- [ ] **Step 1: Create AudioFolderTrack.kt**

```kotlin
package snd.komelia.audiobook

data class AudioFolderTrack(
    val index: Int,
    val title: String,
    val durationSeconds: Double,
)
```

- [ ] **Step 2: Create AudioPosition.kt**

```kotlin
package snd.komelia.audiobook

import snd.komga.client.book.KomgaBookId

data class AudioPosition(
    val bookId: KomgaBookId,
    val trackIndex: Int,
    val positionSeconds: Double,
    val savedAt: Long,
)
```

- [ ] **Step 3: Create AudioPositionRepository.kt**

```kotlin
package snd.komelia.audiobook

import snd.komga.client.book.KomgaBookId

interface AudioPositionRepository {
    suspend fun getPosition(bookId: KomgaBookId): AudioPosition?
    suspend fun savePosition(position: AudioPosition)
}
```

- [ ] **Step 4: Create AudioBookmark.kt**

```kotlin
package snd.komelia.audiobook

import snd.komga.client.book.KomgaBookId

data class AudioBookmark(
    val id: String,
    val bookId: KomgaBookId,
    val trackIndex: Int,
    val positionSeconds: Double,
    val trackTitle: String,
    val createdAt: Long,
)
```

- [ ] **Step 5: Create AudioBookmarkRepository.kt**

```kotlin
package snd.komelia.audiobook

import kotlinx.coroutines.flow.Flow
import snd.komga.client.book.KomgaBookId

interface AudioBookmarkRepository {
    fun getBookmarks(bookId: KomgaBookId): Flow<List<AudioBookmark>>
    suspend fun saveBookmark(bookmark: AudioBookmark)
    suspend fun deleteBookmark(id: String)
}
```

- [ ] **Step 6: Create EpubAudioController.kt**

```kotlin
package snd.komelia.ui.reader.epub.audio

import kotlinx.coroutines.flow.StateFlow
import snd.komelia.settings.model.Epub3NativeSettings

interface EpubAudioController {
    val isPlaying: StateFlow<Boolean>
    val volume: StateFlow<Float>
    val elapsedSeconds: StateFlow<Double>
    val totalDurationSeconds: StateFlow<Double>

    fun togglePlayPause()
    fun seekToNext()
    fun seekToPrev()
    fun setVolume(v: Float)
    fun applyAudioSettings(settings: Epub3NativeSettings)
    fun release()
}
```

- [ ] **Step 7: Commit**

```bash
git add komelia-domain/core/src/commonMain/kotlin/snd/komelia/audiobook/
git add komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/audio/EpubAudioController.kt
git commit -m "feat: add EpubAudioController interface and audio folder domain models"
```

---

## Task 3: DB layer — tables, repositories, migration

**Files:**
- Create: `komelia-infra/database/sqlite/src/commonMain/kotlin/snd/komelia/db/tables/AudioPositionTable.kt`
- Create: `komelia-infra/database/sqlite/src/commonMain/kotlin/snd/komelia/db/tables/AudioBookmarksTable.kt`
- Create: `komelia-infra/database/sqlite/src/commonMain/kotlin/snd/komelia/db/audiobook/ExposedAudioPositionRepository.kt`
- Create: `komelia-infra/database/sqlite/src/commonMain/kotlin/snd/komelia/db/audiobook/ExposedAudioBookmarkRepository.kt`
- Create: `komelia-infra/database/sqlite/src/commonMain/composeResources/files/migrations/app/V40__audio_folder.sql`
- Modify: `komelia-infra/database/sqlite/src/commonMain/kotlin/snd/komelia/db/migrations/AppMigrations.kt`

- [ ] **Step 1: Create AudioPositionTable.kt**

```kotlin
package snd.komelia.db.tables

import org.jetbrains.exposed.v1.core.Table

object AudioPositionTable : Table("audio_position") {
    val bookId = text("book_id")
    val trackIndex = integer("track_index")
    val positionSeconds = double("position_seconds")
    val savedAt = long("saved_at")
    override val primaryKey = PrimaryKey(bookId)
}
```

- [ ] **Step 2: Create AudioBookmarksTable.kt**

```kotlin
package snd.komelia.db.tables

import org.jetbrains.exposed.v1.core.Table

object AudioBookmarksTable : Table("audio_bookmarks") {
    val id = text("id")
    val bookId = text("book_id")
    val trackIndex = integer("track_index")
    val positionSeconds = double("position_seconds")
    val trackTitle = text("track_title")
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}
```

- [ ] **Step 3: Create V40__audio_folder.sql**

```sql
CREATE TABLE IF NOT EXISTS audio_position (
    book_id TEXT PRIMARY KEY,
    track_index INTEGER NOT NULL DEFAULT 0,
    position_seconds REAL NOT NULL DEFAULT 0.0,
    saved_at INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS audio_bookmarks (
    id TEXT PRIMARY KEY,
    book_id TEXT NOT NULL,
    track_index INTEGER NOT NULL DEFAULT 0,
    position_seconds REAL NOT NULL DEFAULT 0.0,
    track_title TEXT NOT NULL DEFAULT '',
    created_at INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_audio_bookmarks_book_id ON audio_bookmarks(book_id);
```

- [ ] **Step 4: Register V40 in AppMigrations.kt**

In `komelia-infra/database/sqlite/src/commonMain/kotlin/snd/komelia/db/migrations/AppMigrations.kt`, add to the migrations list after `"V39__local_file_read_progress.sql"`:

```kotlin
        "V40__audio_folder.sql",
```

- [ ] **Step 5: Create ExposedAudioPositionRepository.kt**

Study the pattern in `ExposedEpubBookmarkRepository.kt` and `ExposedRepository.kt`. The `transaction` helper runs a blocking Exposed transaction on `Dispatchers.IO`.

```kotlin
package snd.komelia.db.audiobook

import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import snd.komelia.audiobook.AudioPosition
import snd.komelia.audiobook.AudioPositionRepository
import snd.komelia.db.ExposedRepository
import snd.komelia.db.tables.AudioPositionTable
import snd.komga.client.book.KomgaBookId

class ExposedAudioPositionRepository(database: Database) : ExposedRepository(database), AudioPositionRepository {

    override suspend fun getPosition(bookId: KomgaBookId): AudioPosition? {
        return transaction {
            AudioPositionTable.selectAll()
                .where { AudioPositionTable.bookId eq bookId.value }
                .singleOrNull()
                ?.let {
                    AudioPosition(
                        bookId = bookId,
                        trackIndex = it[AudioPositionTable.trackIndex],
                        positionSeconds = it[AudioPositionTable.positionSeconds],
                        savedAt = it[AudioPositionTable.savedAt],
                    )
                }
        }
    }

    override suspend fun savePosition(position: AudioPosition) {
        transaction {
            AudioPositionTable.upsert {
                it[bookId] = position.bookId.value
                it[trackIndex] = position.trackIndex
                it[positionSeconds] = position.positionSeconds
                it[savedAt] = position.savedAt
            }
        }
    }
}
```

- [ ] **Step 6: Create ExposedAudioBookmarkRepository.kt**

```kotlin
package snd.komelia.db.audiobook

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import snd.komelia.audiobook.AudioBookmark
import snd.komelia.audiobook.AudioBookmarkRepository
import snd.komelia.db.ExposedRepository
import snd.komelia.db.tables.AudioBookmarksTable
import snd.komga.client.book.KomgaBookId

class ExposedAudioBookmarkRepository(database: Database) : ExposedRepository(database), AudioBookmarkRepository {

    private val bookmarksChanged = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override fun getBookmarks(bookId: KomgaBookId): Flow<List<AudioBookmark>> {
        return bookmarksChanged.onStart { emit(Unit) }.map {
            transaction {
                AudioBookmarksTable.selectAll()
                    .where { AudioBookmarksTable.bookId eq bookId.value }
                    .orderBy(AudioBookmarksTable.createdAt, org.jetbrains.exposed.v1.core.SortOrder.ASC)
                    .map {
                        AudioBookmark(
                            id = it[AudioBookmarksTable.id],
                            bookId = KomgaBookId(it[AudioBookmarksTable.bookId]),
                            trackIndex = it[AudioBookmarksTable.trackIndex],
                            positionSeconds = it[AudioBookmarksTable.positionSeconds],
                            trackTitle = it[AudioBookmarksTable.trackTitle],
                            createdAt = it[AudioBookmarksTable.createdAt],
                        )
                    }
            }
        }
    }

    override suspend fun saveBookmark(bookmark: AudioBookmark) {
        transaction {
            AudioBookmarksTable.insert {
                it[id] = bookmark.id
                it[bookId] = bookmark.bookId.value
                it[trackIndex] = bookmark.trackIndex
                it[positionSeconds] = bookmark.positionSeconds
                it[trackTitle] = bookmark.trackTitle
                it[createdAt] = bookmark.createdAt
            }
        }
        bookmarksChanged.tryEmit(Unit)
    }

    override suspend fun deleteBookmark(id: String) {
        transaction {
            AudioBookmarksTable.deleteWhere { AudioBookmarksTable.id eq id }
        }
        bookmarksChanged.tryEmit(Unit)
    }
}
```

- [ ] **Step 7: Verify compile**

```bash
./gradlew :komelia-infra:database:sqlite:compileKotlinAndroid --quiet
```

Expected: BUILD SUCCESSFUL or only warnings (no errors).

- [ ] **Step 8: Commit**

```bash
git add komelia-infra/database/sqlite/src/commonMain/kotlin/snd/komelia/db/tables/AudioPositionTable.kt
git add komelia-infra/database/sqlite/src/commonMain/kotlin/snd/komelia/db/tables/AudioBookmarksTable.kt
git add komelia-infra/database/sqlite/src/commonMain/kotlin/snd/komelia/db/audiobook/
git add komelia-infra/database/sqlite/src/commonMain/composeResources/files/migrations/app/V40__audio_folder.sql
git add komelia-infra/database/sqlite/src/commonMain/kotlin/snd/komelia/db/migrations/AppMigrations.kt
git commit -m "feat: add audio folder DB tables, repositories, and V40 migration"
```

---

## Task 4: MediaOverlayController — implement EpubAudioController interface

**Files:**
- Modify: `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/audio/MediaOverlayController.kt`

The existing `seekToNextClip()` and `seekToPrevClip()` public methods are called from `AudioMiniPlayer` and `AudioFullScreenPlayer`. We rename them to `seekToNext()` and `seekToPrev()` to match the interface. Internal usages within the class also need updating.

- [ ] **Step 1: Read MediaOverlayController.kt fully before editing**

Read the full file to understand all `seekToNextClip`/`seekToPrevClip` call sites.

- [ ] **Step 2: Add interface declaration and rename methods**

Add ` : EpubAudioController` to the class declaration.

Rename internal methods:
```kotlin
// Before (find all occurrences):
fun seekToNextClip() {
fun seekToPrevClip() {
// After:
override fun seekToNext() {
override fun seekToPrev() {
```

Add `override` keyword to the existing `isPlaying`, `volume`, `elapsedSeconds`, `totalDurationSeconds` StateFlow declarations, `togglePlayPause()`, `setVolume()`, `applyAudioSettings()`.

Add `override fun release()` — check if `release()` already exists; if not, add it to call `player.release()` and cancel coroutine jobs.

- [ ] **Step 3: Verify all internal call sites are updated**

Search within `MediaOverlayController.kt` for any remaining `seekToNextClip` or `seekToPrevClip` calls and rename them to `seekToNext()`/`seekToPrev()`.

- [ ] **Step 4: Verify compile**

```bash
./gradlew :komelia-ui:compileDebugKotlinAndroid --quiet 2>&1 | head -40
```

Expected: Errors only about `AudioMiniPlayer`/`AudioFullScreenPlayer` still referencing old method names (those are fixed in Task 5). `MediaOverlayController` itself should compile.

- [ ] **Step 5: Commit**

```bash
git add komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/audio/MediaOverlayController.kt
git commit -m "refactor: MediaOverlayController implements EpubAudioController interface"
```

---

## Task 5: Update AudioMiniPlayer and AudioFullScreenPlayer to use EpubAudioController

**Files:**
- Modify: `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/audio/AudioMiniPlayer.kt`
- Modify: `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/audio/AudioFullScreenPlayer.kt`

### AudioMiniPlayer changes

The composable signature currently takes `controller: MediaOverlayController`. Change to `EpubAudioController`.
The internal `PlayerRow` composable also takes `controller: MediaOverlayController` — update its signature too.
Replace `controller::seekToPrevClip` → `controller::seekToPrev`
Replace `controller::seekToNextClip` → `controller::seekToNext`

- [ ] **Step 1: Update AudioMiniPlayer.kt**

Find the `fun AudioMiniPlayer(controller: MediaOverlayController, ...)` signature and change to:
```kotlin
fun AudioMiniPlayer(
    controller: EpubAudioController,
    ...
```

Find the `PlayerRow(controller: MediaOverlayController, ...)` private composable and change to `EpubAudioController`.

Replace all `seekToPrevClip` → `seekToPrev` and `seekToNextClip` → `seekToNext` within this file.

### AudioFullScreenPlayer changes

More substantial. In addition to the controller type change, we need to:
1. Add params for folder mode: `audioTracks: List<AudioFolderTrack> = emptyList()`, `audioBookmarks: List<AudioBookmark> = emptyList()`, `isAudioBookmarked: Boolean = false`, `onAudioBookmarkToggle: () -> Unit = {}`
2. Move the time display (elapsed/remaining/total row) outside the `if (positions.size > 1)` block so it shows in folder mode too.
3. Replace `controller::seekToPrevClip` → `controller::seekToPrev` and `controller::seekToNextClip` → `controller::seekToNext`.

- [ ] **Step 2: Update AudioFullScreenPlayer.kt — imports and signature**

Add imports:
```kotlin
import snd.komelia.audiobook.AudioBookmark
import snd.komelia.audiobook.AudioFolderTrack
```

Change signature:
```kotlin
fun AudioFullScreenPlayer(
    controller: EpubAudioController,   // changed from MediaOverlayController
    bookId: KomgaBookId,
    bookTitle: String,
    chapterTitle: String,
    backgroundColor: Color,
    positions: List<Locator>,
    currentLocator: Locator?,
    onNavigateToPosition: (Int) -> Unit,
    onDismiss: () -> Unit,
    onDrag: (fraction: Float) -> Unit,
    onDragEnd: (fraction: Float) -> Unit,
    onChapterClick: () -> Unit,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
    audioTracks: List<AudioFolderTrack> = emptyList(),      // NEW
    audioBookmarks: List<AudioBookmark> = emptyList(),       // NEW
    isAudioBookmarked: Boolean = false,                      // NEW
    onAudioBookmarkToggle: () -> Unit = {},                  // NEW
    playbackSpeed: Double,
    onSpeedChange: (Double) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
)
```

- [ ] **Step 3: Move time display outside positions block**

Find the current structure (around line 270-307):
```kotlin
// Page slider
if (positions.size > 1) {
    Epub3PageNavigatorRow(...)

    // Three-column time row: elapsed | remaining | total
    val remaining = ...
    Row(...) { /* elapsed | remaining | total texts */ }
}
```

Refactor to:
```kotlin
// Page slider — only shown in SMIL mode (positions present)
if (positions.size > 1) {
    Epub3PageNavigatorRow(
        positions = positions,
        currentLocator = currentLocator,
        onNavigateToPosition = onNavigateToPosition,
        modifier = fadeModifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .padding(top = 16.dp),
    )
}

// Time display — shown whenever there is a known duration (both modes)
if (totalDurationSeconds > 0) {
    val remaining = (totalDurationSeconds - elapsedSeconds).coerceAtLeast(0.0)
    Row(
        modifier = fadeModifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp)
            .padding(top = if (positions.size > 1) 0.dp else 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = formatHMS(elapsedSeconds),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = formatTimeLeft(remaining),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = formatHMS(totalDurationSeconds),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
}
```

- [ ] **Step 4: Update bookmark button in AudioFullScreenPlayer**

The `AudioFullScreenPlayer` itself chooses between SMIL bookmark and audio bookmark based on whether `audioTracks` is non-empty:

```kotlin
val effectiveIsBookmarked = if (audioTracks.isNotEmpty()) isAudioBookmarked else isBookmarked
val effectiveOnBookmarkToggle = if (audioTracks.isNotEmpty()) onAudioBookmarkToggle else onBookmarkToggle
Epub3BookmarkToggleButton(
    isBookmarked = effectiveIsBookmarked,
    onClick = effectiveOnBookmarkToggle,
    accentColor = accentColor,
)
```

- [ ] **Step 5: Replace seekToPrevClip/seekToNextClip in AudioFullScreenPlayer**

```kotlin
// Before:
IconButton(onClick = controller::seekToPrevClip) { ... }
IconButton(onClick = controller::seekToNextClip) { ... }
// After:
IconButton(onClick = controller::seekToPrev) { ... }
IconButton(onClick = controller::seekToNext) { ... }
```

- [ ] **Step 6: Verify compile**

```bash
./gradlew :komelia-ui:compileDebugKotlinAndroid --quiet 2>&1 | head -40
```

Expected: Compile errors only in `Epub3ReaderState.kt` and `Epub3ReaderContent.android.kt` (controller type mismatch — fixed in Task 8/9). The audio package itself should be clean.

- [ ] **Step 7: Commit**

```bash
git add komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/audio/AudioMiniPlayer.kt
git add komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/audio/AudioFullScreenPlayer.kt
git commit -m "refactor: AudioMiniPlayer/AudioFullScreenPlayer accept EpubAudioController interface"
```

---

## Task 6: AudiobookFolderController

**Files:**
- Create: `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/audio/AudiobookFolderController.kt`

This is the new controller. It uses the existing `AudiobookPlayer` (Media3/ExoPlayer) and `PlaybackService` without any changes to those classes. It implements `EpubAudioController` and adds folder-specific state.

- [ ] **Step 1: Read AudiobookPlayer.kt before writing**

Read `epub-reader/src/main/java/com/storyteller/reader/AudiobookPlayer.kt` to verify exact method signatures for `loadTracks`, `play`, `pause`, `release`, `setVolume`, `setRate`, `setAutomaticRewind`, `getPosition`, `seekTo`.

- [ ] **Step 2: Create AudiobookFolderController.kt**

```kotlin
package snd.komelia.ui.reader.epub.audio

import android.content.Context
import android.media.MediaMetadataRetriever
import androidx.core.net.toUri
import com.storyteller.reader.AudiobookPlayer
import com.storyteller.reader.BookService
import com.storyteller.reader.Listener
import com.storyteller.reader.OverlayPar
import com.storyteller.reader.Track
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import snd.komelia.audiobook.AudioBookmark
import snd.komelia.audiobook.AudioBookmarkRepository
import snd.komelia.audiobook.AudioFolderTrack
import snd.komelia.audiobook.AudioPosition
import snd.komelia.audiobook.AudioPositionRepository
import snd.komelia.settings.model.Epub3NativeSettings
import snd.komga.client.book.KomgaBookId
import java.io.File
import java.util.UUID
import kotlin.time.Clock

private val logger = KotlinLogging.logger {}

private val AUDIO_EXTENSIONS = setOf("mp3", "m4a", "m4b", "ogg", "aac", "flac", "opus")
private val AUDIO_FOLDER_NAMES = setOf("audio", "audiobook")

class AudiobookFolderController(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val bookUuid: String,
    private val bookId: KomgaBookId,
    private val extractedDir: File,
    private val audioPositionRepository: AudioPositionRepository,
    private val audioBookmarkRepository: AudioBookmarkRepository,
) : EpubAudioController {

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _volume = MutableStateFlow(1f)
    override val volume: StateFlow<Float> = _volume

    private val _elapsedSeconds = MutableStateFlow(0.0)
    override val elapsedSeconds: StateFlow<Double> = _elapsedSeconds

    private val _totalDurationSeconds = MutableStateFlow(0.0)
    override val totalDurationSeconds: StateFlow<Double> = _totalDurationSeconds

    private val _tracks = MutableStateFlow<List<AudioFolderTrack>>(emptyList())
    val tracks: StateFlow<List<AudioFolderTrack>> = _tracks

    private val _currentTrackIndex = MutableStateFlow(0)
    val currentTrackIndex: StateFlow<Int> = _currentTrackIndex

    private val _audioBookmarks = MutableStateFlow<List<AudioBookmark>>(emptyList())
    val audioBookmarks: StateFlow<List<AudioBookmark>> = _audioBookmarks

    private val _isCurrentPositionBookmarked = MutableStateFlow(false)
    val isCurrentPositionBookmarked: StateFlow<Boolean> = _isCurrentPositionBookmarked

    private var loadedTracks: List<Track> = emptyList()
    private var elapsedTimeJob: Job? = null

    private val player: AudiobookPlayer = AudiobookPlayer(
        context = context,
        coroutineScope = coroutineScope,
        listener = object : Listener {
            override fun onClipChanged(overlayPar: OverlayPar) {
                // No-op: folder mode has no SMIL clips
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (!isPlaying) {
                    elapsedTimeJob?.cancel()
                    savePosition()
                } else {
                    startElapsedTimeTracking()
                }
            }

            override fun onPositionChanged(position: Double) {
                // Handled by polling loop
            }

            override fun onTrackChanged(track: Track, position: Double, index: Int) {
                _currentTrackIndex.value = index
                updateElapsed(index, position)
            }
        }
    )

    suspend fun initialize() {
        withContext(Dispatchers.IO) {
            val audioFiles = detectAudioFiles(extractedDir)
            if (audioFiles.isEmpty()) {
                logger.warn { "[audiobook-folder] No audio files found" }
                return@withContext
            }

            val retriever = MediaMetadataRetriever()
            val folderTracks = mutableListOf<AudioFolderTrack>()
            val playerTracks = mutableListOf<Track>()
            val pub = BookService.getPublication(bookUuid)

            audioFiles.forEachIndexed { index, file ->
                val durationMs = try {
                    retriever.setDataSource(file.absolutePath)
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                } catch (e: Exception) {
                    logger.warn { "[audiobook-folder] Could not read duration for ${file.name}: ${e.message}" }
                    0L
                }
                val durationSeconds = durationMs / 1000.0
                val title = cleanTrackTitle(file.nameWithoutExtension)

                folderTracks.add(AudioFolderTrack(index, title, durationSeconds))
                playerTracks.add(
                    Track(
                        uri = file.toUri(),
                        bookUuid = bookUuid,
                        title = title,
                        duration = durationSeconds,
                        bookTitle = pub?.metadata?.title ?: "",
                        author = pub?.metadata?.authors?.firstOrNull()?.name,
                        coverUri = null,
                        relativeUri = "${file.parentFile?.name}/${file.name}",
                        narrator = null,
                        mimeType = "audio/${file.extension.lowercase()}",
                    )
                )
            }
            retriever.release()

            loadedTracks = playerTracks
            _tracks.value = folderTracks
            _totalDurationSeconds.value = folderTracks.sumOf { it.durationSeconds }

            val saved = audioPositionRepository.getPosition(bookId)
            val initialTrack = saved?.trackIndex?.coerceIn(0, playerTracks.lastIndex) ?: 0
            val initialPosition = saved?.positionSeconds ?: 0.0

            player.loadTracks(playerTracks, initialTrack, initialPosition)
            _currentTrackIndex.value = initialTrack
            updateElapsed(initialTrack, initialPosition)
        }

        coroutineScope.launch {
            audioBookmarkRepository.getBookmarks(bookId).collect { bookmarks ->
                _audioBookmarks.value = bookmarks
                updateBookmarkStatus(bookmarks)
            }
        }
    }

    fun seekToTrack(index: Int) {
        val track = loadedTracks.getOrNull(index) ?: return
        player.seekTo(track.relativeUri, 0.0, skipEmit = false)
    }

    fun seekToTrackPosition(index: Int, positionSeconds: Double) {
        val track = loadedTracks.getOrNull(index) ?: return
        player.seekTo(track.relativeUri, positionSeconds, skipEmit = false)
    }

    fun toggleAudioBookmark(currentTrackTitle: String) {
        coroutineScope.launch(Dispatchers.IO) {
            val idx = _currentTrackIndex.value
            val pos = player.getPosition()
            val nearby = _audioBookmarks.value.find { b ->
                b.trackIndex == idx && kotlin.math.abs(b.positionSeconds - pos) < 5.0
            }
            if (nearby != null) {
                audioBookmarkRepository.deleteBookmark(nearby.id)
            } else {
                audioBookmarkRepository.saveBookmark(
                    AudioBookmark(
                        id = UUID.randomUUID().toString(),
                        bookId = bookId,
                        trackIndex = idx,
                        positionSeconds = pos,
                        trackTitle = currentTrackTitle,
                        createdAt = Clock.System.now().toEpochMilliseconds(),
                    )
                )
            }
        }
    }

    fun deleteAudioBookmark(id: String) {
        coroutineScope.launch(Dispatchers.IO) {
            audioBookmarkRepository.deleteBookmark(id)
        }
    }

    override fun togglePlayPause() {
        if (_isPlaying.value) player.pause() else player.play()
    }

    override fun seekToNext() {
        val nextIndex = (_currentTrackIndex.value + 1).coerceAtMost(loadedTracks.lastIndex)
        seekToTrack(nextIndex)
    }

    override fun seekToPrev() {
        val prevIndex = (_currentTrackIndex.value - 1).coerceAtLeast(0)
        seekToTrack(prevIndex)
    }

    override fun setVolume(v: Float) {
        val clamped = v.coerceIn(0f, 1f)
        _volume.value = clamped
        player.setVolume(clamped)
    }

    override fun applyAudioSettings(settings: Epub3NativeSettings) {
        player.setRate(settings.playbackSpeed)
        if (settings.rewindEnabled) {
            player.setAutomaticRewind(
                afterInterruption = settings.rewindAfterInterruption,
                afterBreak = settings.rewindAfterBreak,
            )
        }
    }

    override fun release() {
        elapsedTimeJob?.cancel()
        savePosition()
        player.release()
    }

    private fun savePosition() {
        coroutineScope.launch(Dispatchers.IO) {
            if (loadedTracks.isEmpty()) return@launch
            audioPositionRepository.savePosition(
                AudioPosition(
                    bookId = bookId,
                    trackIndex = _currentTrackIndex.value,
                    positionSeconds = player.getPosition(),
                    savedAt = Clock.System.now().toEpochMilliseconds(),
                )
            )
        }
    }

    private fun startElapsedTimeTracking() {
        elapsedTimeJob?.cancel()
        elapsedTimeJob = coroutineScope.launch {
            while (true) {
                updateElapsed(_currentTrackIndex.value, player.getPosition())
                delay(500)
            }
        }
    }

    private fun updateElapsed(trackIndex: Int, position: Double) {
        val tracksBefore = _tracks.value.take(trackIndex)
        _elapsedSeconds.value = tracksBefore.sumOf { it.durationSeconds } + position
    }

    private fun updateBookmarkStatus(bookmarks: List<AudioBookmark>) {
        val idx = _currentTrackIndex.value
        val pos = player.getPosition()
        _isCurrentPositionBookmarked.value = bookmarks.any { b ->
            b.trackIndex == idx && kotlin.math.abs(b.positionSeconds - pos) < 5.0
        }
    }

    companion object {
        fun detectAudioFiles(extractedDir: File): List<File> {
            return extractedDir.walkTopDown()
                .filter { it.isDirectory }
                .filter { dir -> dir.name.lowercase() in AUDIO_FOLDER_NAMES }
                .flatMap { dir ->
                    dir.listFiles()
                        ?.filter { it.isFile && it.extension.lowercase() in AUDIO_EXTENSIONS }
                        ?.sortedBy { it.name }
                        ?: emptyList()
                }
                .toList()
        }

        fun cleanTrackTitle(nameWithoutExtension: String): String {
            return nameWithoutExtension
                .replace(Regex("^\\d+[-_.\\s]*"), "")
                .replace(Regex("[-_]"), " ")
                .trim()
                .replaceFirstChar { it.uppercase() }
                .ifBlank { nameWithoutExtension }
        }
    }
}
```

- [ ] **Step 3: Verify exact AudiobookPlayer API matches**

After reading `AudiobookPlayer.kt` in Step 1, adjust any method calls that don't match the actual signatures (e.g., `setAutomaticRewind` parameter names, `loadTracks` parameter order).

- [ ] **Step 4: Verify compile**

```bash
./gradlew :komelia-ui:compileDebugKotlinAndroid --quiet 2>&1 | head -60
```

Expected: Errors only in `Epub3ReaderState.kt` (not yet wired). `AudiobookFolderController` itself should compile.

- [ ] **Step 5: Commit**

```bash
git add komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/audio/AudiobookFolderController.kt
git commit -m "feat: add AudiobookFolderController for unsynchronized audiobook folder playback"
```

---

## Task 7: AudioTrackListDialog

**Files:**
- Create: `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/audio/AudioTrackListDialog.kt`

Must look and behave exactly like `Epub3ContentDialog`: same `Surface` container shape, drag handle, drag-to-dismiss, `SecondaryTabRow` + `HorizontalPager`, same color theming, same row styles. The only structural difference is 2 tabs (Tracks / Bookmarks) instead of 3 (no Search tab). It is placed at `Alignment.BottomCenter` in `Epub3ReaderContent` the same way `Epub3ContentDialog` is shown.

Key patterns from `Epub3ContentDialog` to replicate exactly:
- `Surface(shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp), color = surfaceColor)` where `surfaceColor` is `Color(43,43,43)` for dark theme, `MaterialTheme.colorScheme.background` for light (use `LocalTheme.current`)
- `heightIn(max = (screenHeightDp * 2f / 3f).dp)` height cap
- `offset { IntOffset(0, dragOffsetY.roundToInt().coerceAtLeast(0)) }` with `detectVerticalDragGestures` — dismiss when `dragOffsetY > 120f`
- `BottomSheetDefaults.DragHandle()` at top center
- `SecondaryTabRow(selectedTabIndex = pagerState.currentPage, containerColor = Color.Transparent)` with tab labels text-only (no icons)
- `HorizontalPager` filling remaining height with `verticalAlignment = Alignment.Top`
- Divider color: `MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)`
- Track rows: `TextButton` style matching `TocRow` in `ContentsTab`. Current track highlighted with `accentColor.copy(alpha = 0.15f)` container. `LaunchedEffect(currentTrackIndex)` scrolls list to current track on open.
- Bookmark rows: identical layout to `BookmarkRow` — `Row` with `Column(weight(1f))` showing track title (bold body/medium) + `formatHMS(positionSeconds)` as bodySmall subtitle, `IconButton(Delete)` on right.

- [ ] **Step 1: Create AudioTrackListDialog.kt**

```kotlin
package snd.komelia.ui.reader.epub.audio

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import snd.komelia.audiobook.AudioBookmark
import snd.komelia.audiobook.AudioFolderTrack
import snd.komelia.ui.LocalAccentColor
import snd.komelia.ui.LocalTheme
import snd.komelia.ui.Theme
import kotlin.math.roundToInt

private fun formatHMS(seconds: Double): String {
    val total = seconds.toLong().coerceAtLeast(0)
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioTrackListDialog(
    tracks: List<AudioFolderTrack>,
    bookmarks: List<AudioBookmark>,
    currentTrackIndex: Int,
    onTrackClick: (Int) -> Unit,
    onBookmarkClick: (AudioBookmark) -> Unit,
    onDeleteBookmark: (AudioBookmark) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var dragOffsetY by remember { mutableStateOf(0f) }
    val maxHeight = (LocalConfiguration.current.screenHeightDp * 2f / 3f).dp
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    val theme = LocalTheme.current
    val surfaceColor = if (theme.type == Theme.ThemeType.DARK) Color(43, 43, 43)
    else MaterialTheme.colorScheme.background

    Surface(
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = surfaceColor,
        tonalElevation = 0.dp,
        modifier = modifier
            .heightIn(max = maxHeight)
            .offset { IntOffset(0, dragOffsetY.roundToInt().coerceAtLeast(0)) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .navigationBarsPadding(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                if (dragOffsetY > 120f) onDismiss()
                                else dragOffsetY = 0f
                            },
                            onDragCancel = { dragOffsetY = 0f },
                            onVerticalDrag = { _, delta ->
                                dragOffsetY = (dragOffsetY + delta).coerceAtLeast(0f)
                            }
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                BottomSheetDefaults.DragHandle()
            }

            SecondaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.Transparent,
            ) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text("Tracks") },
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text("Bookmarks") },
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top,
            ) { page ->
                when (page) {
                    0 -> TracksTab(tracks, currentTrackIndex) { index ->
                        onTrackClick(index)
                        onDismiss()
                    }
                    1 -> BookmarksTab(bookmarks, { bookmark ->
                        onBookmarkClick(bookmark)
                        onDismiss()
                    }, onDeleteBookmark)
                }
            }
        }
    }
}

@Composable
private fun TracksTab(
    tracks: List<AudioFolderTrack>,
    currentTrackIndex: Int,
    onTrackClick: (Int) -> Unit,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(currentTrackIndex) {
        if (currentTrackIndex >= 0) listState.scrollToItem(currentTrackIndex)
    }

    if (tracks.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            Text(
                text = "No tracks available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        val accentColor = LocalAccentColor.current ?: MaterialTheme.colorScheme.secondary
        LazyColumn(state = listState) {
            itemsIndexed(tracks) { index, track ->
                if (index > 0) HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = dividerColor,
                )
                val isCurrentTrack = track.index == currentTrackIndex
                val buttonColors = if (isCurrentTrack) {
                    ButtonDefaults.textButtonColors(
                        containerColor = accentColor.copy(alpha = 0.15f),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    )
                } else {
                    ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TextButton(
                        onClick = { onTrackClick(track.index) },
                        colors = buttonColors,
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                    ) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = formatHMS(track.durationSeconds),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarksTab(
    bookmarks: List<AudioBookmark>,
    onBookmarkClick: (AudioBookmark) -> Unit,
    onDeleteBookmark: (AudioBookmark) -> Unit,
) {
    if (bookmarks.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            Text(
                text = "No bookmarks yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        LazyColumn {
            itemsIndexed(bookmarks) { index, bookmark ->
                if (index > 0) HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = dividerColor,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onBookmarkClick(bookmark) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = bookmark.trackTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = formatHMS(bookmark.positionSeconds),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    IconButton(onClick = { onDeleteBookmark(bookmark) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete bookmark")
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 2: Verify placement in Epub3ReaderContent**

Read `Epub3ReaderContent.android.kt` to find where `Epub3ContentDialog` is placed in the layout (container, alignment, z-ordering). Place `AudioTrackListDialog` at the same `Alignment.BottomCenter` position with the same modifier so it slides up from the bottom identically.

- [ ] **Step 3: Commit**

```bash
git add komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/audio/AudioTrackListDialog.kt
git commit -m "feat: add AudioTrackListDialog matching Epub3ContentDialog UX"
```

---

## Task 8: Wire AudiobookFolderController into Epub3ReaderState

**Files:**
- Modify: `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/Epub3ReaderState.kt`

- [ ] **Step 1: Update imports and constructor**

Add to imports:
```kotlin
import snd.komelia.audiobook.AudioBookmarkRepository
import snd.komelia.audiobook.AudioPositionRepository
import snd.komelia.ui.reader.epub.audio.AudiobookFolderController
import snd.komelia.ui.reader.epub.audio.EpubAudioController
```

Add to constructor:
```kotlin
private val audioPositionRepository: AudioPositionRepository,
private val audioBookmarkRepository: AudioBookmarkRepository,
```

- [ ] **Step 2: Change mediaOverlayController type**

```kotlin
// Before:
val mediaOverlayController = MutableStateFlow<MediaOverlayController?>(null)
// After:
val mediaOverlayController = MutableStateFlow<EpubAudioController?>(null)
```

- [ ] **Step 3: Narrow SMIL-specific method calls**

In `onLocatorChange`:
```kotlin
// Before:
mediaOverlayController.value?.handleUserLocatorChange(locator)
// After:
(mediaOverlayController.value as? MediaOverlayController)?.handleUserLocatorChange(locator)
```

In `onEpubViewCreated` where `attachView` and `handleUserLocatorChange` are called:
```kotlin
// Before:
epubView?.let { view ->
    controller.attachView(view)
    savedLocator?.let { controller.handleUserLocatorChange(it) }
}
// After:
epubView?.let { view ->
    (controller as? MediaOverlayController)?.let { smilController ->
        smilController.attachView(view)
        savedLocator?.let { smilController.handleUserLocatorChange(it) }
    }
}
```

Search for any other `handleUserLocatorChange` or `handleDoubleTap` calls on `mediaOverlayController` and apply the same `as? MediaOverlayController` narrowing cast.

- [ ] **Step 4: Add folder detection in initialize()**

After the existing SMIL block in `initialize()`:

```kotlin
if (clips.isNotEmpty()) {
    coroutineScope.launch {
        // ... existing SMIL controller setup (unchanged) ...
    }
} else {
    // Check for audiobook folder
    val audioFiles = AudiobookFolderController.detectAudioFiles(extractedDir)
    if (audioFiles.isNotEmpty()) {
        coroutineScope.launch {
            logger.debug { "[epub3-init] initializing audiobook folder controller (${audioFiles.size} audio files)" }
            runCatching {
                val controller = AudiobookFolderController(
                    context = context,
                    coroutineScope = coroutineScope,
                    bookUuid = bookUuid,
                    bookId = bookId.value,
                    extractedDir = extractedDir,
                    audioPositionRepository = audioPositionRepository,
                    audioBookmarkRepository = audioBookmarkRepository,
                )
                controller.initialize()
                controller.applyAudioSettings(settings.value)
                mediaOverlayController.value = controller
                logger.debug { "[epub3-init] audiobook folder controller ready" }
            }.onFailure { e ->
                logger.error { "[epub3-init] audiobook folder controller FAILED: ${e::class.qualifiedName}: ${e.message}" }
            }
        }
    }
}
```

- [ ] **Step 5: Ensure release() is called on cleanup**

Find the existing cleanup/close path in `Epub3ReaderState` and verify `mediaOverlayController.value?.release()` is called there. The `release()` method is in the `EpubAudioController` interface, so it works for both controller types.

- [ ] **Step 6: Verify compile**

```bash
./gradlew :komelia-ui:compileDebugKotlinAndroid --quiet 2>&1 | head -60
```

- [ ] **Step 7: Commit**

```bash
git add komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/Epub3ReaderState.kt
git commit -m "feat: Epub3ReaderState detects audiobook folder and initializes AudiobookFolderController"
```

---

## Task 9: Wire folder mode into Epub3ReaderContent

**Files:**
- Modify: `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/Epub3ReaderContent.android.kt`

- [ ] **Step 1: Add imports**

```kotlin
import snd.komelia.ui.reader.epub.audio.AudiobookFolderController
import snd.komelia.ui.reader.epub.audio.AudioTrackListDialog
import snd.komelia.audiobook.AudioFolderTrack
import snd.komelia.audiobook.AudioBookmark
```

- [ ] **Step 2: Derive folder controller and its state**

Inside the `controller?.let { ctrl -> ... }` block, add:

```kotlin
val folderController = ctrl as? AudiobookFolderController
val audioTracks by (folderController?.tracks ?: MutableStateFlow(emptyList<AudioFolderTrack>())).collectAsState()
val audioBookmarks by (folderController?.audioBookmarks ?: MutableStateFlow(emptyList<AudioBookmark>())).collectAsState()
val currentAudioTrackIndex by (folderController?.currentTrackIndex ?: MutableStateFlow(0)).collectAsState()
val isAudioBookmarked by (folderController?.isCurrentPositionBookmarked ?: MutableStateFlow(false)).collectAsState()
```

- [ ] **Step 3: Compute effective chapter title**

Wrap the existing `chapterTitle` computation to prefer audio track title in folder mode:

```kotlin
val chapterTitle = remember(currentLocator, toc, currentAudioTrackIndex, audioTracks) {
    if (audioTracks.isNotEmpty()) {
        audioTracks.getOrNull(currentAudioTrackIndex)?.title ?: ""
    } else {
        // existing epub TOC-based title logic unchanged
        ...
    }
}
```

Read the existing `chapterTitle` computation first, then wrap it.

- [ ] **Step 4: Add audio track list dialog state**

```kotlin
var showAudioTrackDialog by remember { mutableStateOf(false) }
```

- [ ] **Step 5: Route onChapterClick in AudioFullScreenPlayer call**

```kotlin
// Before:
onChapterClick = { epub3State.openContentDialog(0) },
// After:
onChapterClick = {
    if (audioTracks.isNotEmpty()) showAudioTrackDialog = true
    else epub3State.openContentDialog(0)
},
```

- [ ] **Step 6: Pass folder mode params to AudioFullScreenPlayer**

```kotlin
AudioFullScreenPlayer(
    controller = ctrl,
    ...
    isBookmarked = isBookmarked,
    onBookmarkToggle = { currentLocator?.let { epub3State.toggleBookmark(it) } },
    audioTracks = audioTracks,                     // NEW
    audioBookmarks = audioBookmarks,               // NEW
    isAudioBookmarked = isAudioBookmarked,         // NEW
    onAudioBookmarkToggle = {                      // NEW
        folderController?.toggleAudioBookmark(
            audioTracks.getOrNull(currentAudioTrackIndex)?.title ?: ""
        )
    },
    ...
)
```

- [ ] **Step 7: Show AudioTrackListDialog**

After the `SharedTransitionLayout` block:

```kotlin
if (showAudioTrackDialog && folderController != null) {
    AudioTrackListDialog(
        tracks = audioTracks,
        bookmarks = audioBookmarks,
        currentTrackIndex = currentAudioTrackIndex,
        onTrackClick = { index -> folderController.seekToTrack(index) },
        onBookmarkClick = { bookmark ->
            folderController.seekToTrackPosition(bookmark.trackIndex, bookmark.positionSeconds)
        },
        onDeleteBookmark = { bookmark ->
            folderController.deleteAudioBookmark(bookmark.id)
        },
        onDismiss = { showAudioTrackDialog = false },
    )
}
```

- [ ] **Step 8: Verify compile**

```bash
./gradlew :komelia-ui:compileDebugKotlinAndroid --quiet 2>&1 | head -60
```

Expected: BUILD SUCCESSFUL or only warnings.

- [ ] **Step 9: Commit**

```bash
git add komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/Epub3ReaderContent.android.kt
git commit -m "feat: Epub3ReaderContent routes chapter/bookmark actions for audiobook folder mode"
```

---

## Task 10: Factory and DI wiring

**Files:**
- Modify: `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/Epub3ReaderFactory.android.kt`
- Modify: `komelia-app/src/androidMain/kotlin/snd/komelia/AndroidAppModule.kt`

- [ ] **Step 1: Read Epub3ReaderFactory.android.kt**

Confirm current constructor params passed to `Epub3ReaderState(...)`.

- [ ] **Step 2: Add new repos to factory function**

```kotlin
// Add to createEpub3ReaderState signature:
audioPositionRepository: snd.komelia.audiobook.AudioPositionRepository,
audioBookmarkRepository: snd.komelia.audiobook.AudioBookmarkRepository,

// Add to Epub3ReaderState(...) instantiation:
audioPositionRepository = audioPositionRepository,
audioBookmarkRepository = audioBookmarkRepository,
```

- [ ] **Step 3: Read AndroidAppModule.kt**

Find where `createEpub3ReaderState` is called and where `ExposedEpubBookmarkRepository` is instantiated, to know where to add the new repos.

- [ ] **Step 4: Instantiate new repos in AndroidAppModule.kt**

```kotlin
// Near the ExposedEpubBookmarkRepository line:
val audioPositionRepository = snd.komelia.db.audiobook.ExposedAudioPositionRepository(databases.app)
val audioBookmarkRepository = snd.komelia.db.audiobook.ExposedAudioBookmarkRepository(databases.app)
```

Pass them through to `createEpub3ReaderState(...)`.

- [ ] **Step 5: Full compile check**

```bash
./gradlew :komelia-app:compileDebugKotlinAndroid --quiet 2>&1 | head -60
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/Epub3ReaderFactory.android.kt
git add komelia-app/src/androidMain/kotlin/snd/komelia/AndroidAppModule.kt
git commit -m "feat: wire AudioPositionRepository and AudioBookmarkRepository into epub reader DI"
```

---

## Task 11: Build and test

- [ ] **Step 1: Build debug APK**

```bash
./gradlew :komelia-app:assembleDebug 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 2: Install on device**

```bash
adb install -r komelia-app/build/outputs/apk/debug/komelia-app-debug.apk
```

- [ ] **Step 3: Test SMIL mode (regression)**

Open an epub that has SMIL synchronized audio. Verify:
- Mini player appears at bottom of reader
- Play/pause, next/prev clip work as before
- Text highlighting follows audio
- Full-screen player opens — page slider (epub position) is visible AND time display (elapsed/remaining/total) is visible, same as before
- Chapter chip in full-screen player → opens `Epub3ContentDialog` (Contents / Bookmarks / Search tabs, same behavior as before)
- Bookmark toggle saves epub text locator, appears in Epub3ContentDialog Bookmarks tab
- Tap screen middle → reader controls appear → chapter button → `Epub3ContentDialog` opens (Contents / Bookmarks / Search)
- No crashes

- [ ] **Step 4: Test folder mode — audio player**

Open an epub that has an `audio/` or `audiobook/` folder with MP3 files but no SMIL:
- Mini player appears at bottom
- Play/pause works, audio plays
- Next/prev skips to next/prev audio file
- Full-screen player opens — **no page slider** (no epub positions), time display (elapsed/remaining/total) IS visible
- Chapter title in player shows cleaned audio filename (leading track numbers stripped)
- Chapter chip in full-screen player → opens `AudioTrackListDialog` — visually identical to `Epub3ContentDialog` (same surface shape, drag handle, drag-to-dismiss, same tab style)
  - **Tracks tab**: flat list of audio files; current track highlighted with accent background; list scrolled to current track on open; tap any track → audio jumps to it, dialog closes
  - **Bookmarks tab**: shows "No bookmarks yet" initially
- Bookmark button in player (top right of chapter row) → creates audio bookmark → dialog Bookmarks tab now shows it with track title (bold) and timestamp as subtitle, delete button on right
- Tap bookmark in Bookmarks tab → audio jumps to that track+position, dialog closes
- Delete bookmark → removed from list immediately
- Exit reader, reopen → resumes from saved track + position

- [ ] **Step 5: Test folder mode — reader controls (text navigation independent)**

With folder mode epub open:
- Tap screen middle → reader controls appear → chapter button → `Epub3ContentDialog` opens (Contents / Bookmarks / Search) — unchanged text navigation
- Text bookmarks work independently from audio bookmarks (epub TOC and text locators, unaffected by audio)
- Audio plays in background while `Epub3ContentDialog` is open

- [ ] **Step 6: Test no-audio epub (regression)**

Open a plain epub with neither SMIL nor audio folder:
- No mini player appears
- Reader works normally
- `Epub3ContentDialog` from reader controls works as before

- [ ] **Step 7: Test epub with both SMIL and audio folder**

Verify SMIL mode activates (not folder mode). No player duplication. Chapter chip opens `Epub3ContentDialog`.

---

## Verification Summary

| Scenario | Expected |
|----------|----------|
| Epub with SMIL | SMIL mode unchanged — page slider + time display, Epub3ContentDialog from chapter chip |
| Epub with audio/ folder, no SMIL | Folder mode — time display only (no page slider), AudioTrackListDialog from chapter chip |
| Epub with audiobook/ folder | Folder mode activated |
| Epub with no audio at all | No player |
| Epub with SMIL + audio folder | SMIL takes priority |
| AudioTrackListDialog UX | Identical to Epub3ContentDialog — same surface, drag handle, tab style, row styles |
| Reader controls chapter button (folder mode) | Always opens Epub3ContentDialog (TOC/Bookmarks/Search) — text nav independent |
| Audio bookmarks vs text bookmarks | Completely separate — audio bookmarks in AudioTrackListDialog, text bookmarks in Epub3ContentDialog |
| Position save/restore | Resumes on reopen |
| Speed/rewind settings | Applied to folder controller |
