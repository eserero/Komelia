# Fix: Epub3 Reader OOM/Freeze on Open

## Context

Opening an epub3 book with many audio clips (e.g. 4833 clips, 79 SMIL files) causes an OOM kill or a prolonged main-thread freeze.

Root cause confirmed by log analysis: the `overlay_clips.json` cache file is loaded **on the main thread** inside `Epub3ReaderState.initialize()`, which runs on `screenModelScope` (`Dispatchers.Main.immediate`). For the failing book, JSON loading (`readText` → `JSONArray` tree → 4833 `OverlayPar` objects all live simultaneously) consumes ~500 MB of heap and kills the process before the reader ever opens.

Additionally, `BookService.openPublication()` (parses 79 SMIL files) also runs on the main thread, freezing the UI spinner.

**Three-tier open behavior:**
- **First-ever open:** download + extract zip + SMIL parse + write JSON → very slow
- **Cold start (process restarted):** epub on disk, JSON on disk → JSON loading causes OOM/freeze
- **Hot open (same process):** `BookService` memory intact → instant

## Fix

Two changes, both in `Epub3ReaderState.kt`:

### 1. Remove the `overlay_clips.json` cache entirely

The JSON round-trip loads `String + JSONArray tree + List<OverlayPar>` simultaneously — 3× the data in memory at once. SMIL parsing in `openPublication()` processes files sequentially; intermediate XML trees are GC'd between files. Removing the cache eliminates the OOM.

- Delete `clipsFile` / `persistedClips` logic (both read and write)
- Always call `openPublication(bookUuid, ..., clips = null)` so Readium parses SMIL
- Get clips via `BookService.getOverlayClips(bookUuid)` (fast in-memory lookup after `openPublication`)
- Delete stale `overlay_clips.json` if it exists (one-time disk cleanup)

### 2. Move `openPublication()` and `prepareEpubDirectory()` to `Dispatchers.IO`

Both do file I/O and CPU work. Keeping them on Main freezes the spinner and risks ANR.

- `prepareEpubDirectory()`: change to `= withContext(Dispatchers.IO) { ... }` expression form
- `openPublication()`: wrap in `withContext(Dispatchers.IO) { ... }`
- `BookService.getOverlayClips()` after openPublication: plain call — it's a fast in-memory map lookup, no IO

### 3. Launch `MediaOverlayController` init after `LoadState.Success` (background coroutine)

Set `LoadState.Success` first so the epub content renders immediately, then init the audio controller in a background `coroutineScope.launch`.

Keep the coroutine on `Main` (screenModelScope dispatcher) so `MediaController.Builder.buildAsync()` gets a Looper — but `controllerFuture.await()` inside `player.loadTracks()` suspends the coroutine, freeing Main for UI frames during the PlaybackService connection wait.

Include in the launch block:
- `controller.applyAudioSettings(settings.value)`
- Attach to `epubView` if already created (race condition fix)
- Seed `pendingUserLocator` from `savedLocator`
- Wrap in `runCatching` with error logging

## File to Modify

`komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/Epub3ReaderState.kt`

Add imports:
```kotlin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
```

## Result: `initialize()` structure after fix

```kotlin
logger.debug { "[epub3-init] starting for bookId=..." }
state.value = LoadState.Loading
notifications.runCatchingToNotifications {
    if (book.value == null) {
        logger.debug { "[epub3-init] fetching book metadata" }
        book.value = bookApi.getOne(bookId.value)
    }

    logger.debug { "[epub3-init] preparing epub directory" }
    val extractedDir = prepareEpubDirectory()          // IO dispatcher
    logger.debug { "[epub3-init] epub directory ready: $extractedDir" }

    // Delete stale JSON cache if present (one-time cleanup)
    withContext(Dispatchers.IO) {
        File(extractedDir, "overlay_clips.json").delete()
    }

    logger.debug { "[epub3-init] opening publication" }
    withContext(Dispatchers.IO) {
        BookService.openPublication(bookUuid, extractedDir.toURI().toURL(), clips = null)
    }
    logger.debug { "[epub3-init] publication opened" }
    tableOfContents.value = BookService.getPublication(bookUuid)?.tableOfContents ?: emptyList()

    val clips: List<OverlayPar> = BookService.getOverlayClips(bookUuid)

    val r2Prog = bookApi.getReadiumProgression(bookId.value)
    // ... savedLocator setup (unchanged) ...

    logger.debug { "[epub3-init] succeeded" }
    state.value = LoadState.Success(Unit)
    settings.value = epubSettingsRepository.getEpub3NativeSettings()
    coroutineScope.launch {
        runCatching { positions.value = BookService.getPositions(bookUuid) }
            .onFailure { logger.catching(it) }
    }

    if (clips.isNotEmpty()) {
        coroutineScope.launch {                        // runs on Main; await() inside suspends it
            logger.debug { "[epub3-init] initializing media overlay controller in background (${clips.size} clips)" }
            runCatching {
                val controller = MediaOverlayController(context, coroutineScope, bookUuid, extractedDir)
                controller.initialize(clips)
                controller.applyAudioSettings(settings.value)
                mediaOverlayController.value = controller
                epubView?.let { view ->
                    controller.attachView(view)
                    savedLocator?.let { controller.handleUserLocatorChange(it) }
                }
                logger.debug { "[epub3-init] media overlay controller ready" }
            }.onFailure { e ->
                logger.error { "[epub3-init] media overlay controller FAILED: ${e::class.qualifiedName}: ${e.message}\n${e.stackTraceToString()}" }
            }
        }
    }
}.onFailure { e ->
    logger.error { "[epub3-init] FAILED: ${e::class.qualifiedName}: ${e.message}\n${e.stackTraceToString()}" }
    state.value = LoadState.Error(e)
}
```

`prepareEpubDirectory()` becomes:
```kotlin
private suspend fun prepareEpubDirectory(): File = withContext(Dispatchers.IO) {
    val extractedDir = File(context.cacheDir, "epub3/$bookUuid").also { it.mkdirs() }
    if (extractedDir.list().isNullOrEmpty()) {
        // ... existing extraction logic unchanged ...
    }
    extractedDir   // bare expression, no return keyword
}
```

## Verification

1. Build debug APK: `./gradlew :komelia-app:assembleDebug`
2. Clear app cache (Settings → Apps → Komelia → Clear Cache) to force cold start
3. Open the failing book (4833 clips) — loading spinner must animate; reader must open without OOM
4. Check logs: all `[epub3-init]` steps appear in sequence; audio controller logs appear after reader is visible
5. Open a working audio book — verify audio controls appear and playback works
6. Open a non-audio epub — verify no regression
7. Close app, reopen failing book (cold start) — must open without freeze
8. Open failing book again in same session (hot open) — must be instant
