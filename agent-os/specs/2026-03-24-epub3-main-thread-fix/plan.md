# Fix: Epub3 Reader Freezes on Open (Main Thread Starvation)

## Context

Opening an epub3 book with 4833 audio clips causes the app to "completely freeze" — the loading spinner freezes and the reader never opens. This followed two earlier fixes:

1. **parseTimer fix** — `STMediaOverlayNode.kt`: guard against missing `t=` prefix to avoid `NegativeArraySizeException`
2. **Async MediaOverlayController init** — `Epub3ReaderState.kt`: moved `controller.initialize(clips)` to a background `coroutineScope.launch` after `LoadState.Success` is set

These fixes were correct but revealed (fix 1) and partially addressed (fix 2) the underlying problem.

## Root Cause

**Main thread starvation before `LoadState.Success` is set.**

`Epub3ReaderState.initialize()` is called from a `LaunchedEffect` in `EpubScreen.kt`, which runs on `screenModelScope` — Voyager's `CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)`. Every call inside `initialize()` that is not explicitly dispatched to IO/Default runs on the **main thread**.

Before the parseTimer fix, `BookService.getOverlayClips()` would crash fast (on the first malformed clip) with `NegativeArraySizeException`, caught by `runCatchingToNotifications`, setting `LoadState.Error`. The main thread was unblocked almost immediately.

After the parseTimer fix, parsing succeeds for all 4833 clips. All of this now runs on the main thread:

| Operation | Where | Problem |
|-----------|-------|---------|
| `prepareEpubDirectory()` | Before success | ZIP extraction — file IO on main thread |
| Cache load: `clipsFile.readText()` + JSON parse 4833 objects | Before success | File read + CPU on main thread |
| `BookService.openPublication()` | Before success | Processes publication + clips |
| `BookService.getOverlayClips()` | Before success | SMIL XML parse of 4833 clips on main thread |
| `JSONArray(4833 clips).toString()` + `clipsFile.writeText()` | Before success | JSON serialization + file write on main thread |

With the main thread blocked, **Compose's frame clock cannot tick** → the loading spinner is frozen → the app appears to not open at all. Android may also fire an ANR after 5 seconds.

The async MediaOverlayController launch (fix 2) is also on `Dispatchers.Main` (since it uses `screenModelScope.launch`), so even after `LoadState.Success` is set, the heavy `controller.initialize()` work competes with UI rendering on the main thread.

## Fix

All IO/CPU-heavy work must be dispatched off the main thread using `withContext(Dispatchers.IO)` or `withContext(Dispatchers.Default)`.

### File to modify

`komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/Epub3ReaderState.kt`

Add imports if not present:
```kotlin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
```

---

### Change 1: `prepareEpubDirectory()` — dispatch to IO

The entire function does file operations (directory creation, zip extraction, disk writes). Wrap the body:

```kotlin
private suspend fun prepareEpubDirectory(): File = withContext(Dispatchers.IO) {
    val extractedDir = File(context.cacheDir, "epub3/$bookUuid").also { it.mkdirs() }
    if (extractedDir.list().isNullOrEmpty()) {
        // ... existing extraction logic ...
    }
    extractedDir
}
```

---

### Change 2: Cache loading block — dispatch to IO

Around lines 173–180 of current file:

```kotlin
val persistedClips: List<OverlayPar>? = withContext(Dispatchers.IO) {
    if (clipsFile.exists()) {
        try {
            val json = JSONArray(clipsFile.readText())
            List(json.length()) { i -> OverlayPar.fromJson(json.getJSONObject(i).toMap()) }
        } catch (e: Exception) {
            null  // corrupt/old cache → re-parse
        }
    } else null
}
```

---

### Change 3: `BookService.openPublication()` — dispatch to IO

```kotlin
withContext(Dispatchers.IO) {
    BookService.openPublication(bookUuid, extractedDir.toURI().toURL(), clips = persistedClips)
}
tableOfContents.value = BookService.getPublication(bookUuid)?.tableOfContents ?: emptyList()
```

---

### Change 4: Fresh clip parsing + JSON write — dispatch to IO

Around lines 186–193:

```kotlin
val clips: List<OverlayPar> = withContext(Dispatchers.IO) {
    persistedClips ?: run {
        val freshClips = BookService.getOverlayClips(bookUuid)
        if (freshClips.isNotEmpty()) {
            val json = JSONArray(freshClips.map { JSONObject(it.toJson()) })
            clipsFile.writeText(json.toString())
        }
        freshClips
    }
}
```

---

### Change 5: Background MediaOverlayController launch — dispatch to Default

Inside the existing `coroutineScope.launch { ... }` (around lines 220–236), wrap the heavy work:

```kotlin
if (clips.isNotEmpty()) {
    coroutineScope.launch {
        logger.debug { "[epub3-init] initializing media overlay controller in background (${clips.size} clips)" }
        runCatching {
            withContext(Dispatchers.Default) {
                val controller = MediaOverlayController(context, coroutineScope, bookUuid, extractedDir)
                controller.initialize(clips)
                controller.applyAudioSettings(settings.value)
                mediaOverlayController.value = controller
                epubView?.let { view ->
                    controller.attachView(view)
                    savedLocator?.let { controller.handleUserLocatorChange(it) }
                }
            }
            logger.debug { "[epub3-init] media overlay controller ready" }
        }.onFailure { e ->
            logger.error { "[epub3-init] media overlay controller FAILED: ${e::class.qualifiedName}: ${e.message}\n${e.stackTraceToString()}" }
        }
    }
}
```

> **Note:** If `mediaOverlayController.value = controller` or `controller.attachView(view)` must run on Main (e.g., if they touch Android view state), split the `withContext(Dispatchers.Default)` block to end before those lines and switch back. In practice, StateFlow assignment is thread-safe and `attachView` likely just stores a reference, so `Dispatchers.Default` should be safe for the whole block.

---

## Verification

1. Build and install debug APK
2. Open an epub3 book with media overlays (the 4833-clip book)
3. **Loading spinner must animate** — proves main thread is free
4. Reader screen must open without ANR or freeze
5. Audio overlay controls appear after a few seconds (async init completes)
6. Playback works correctly
7. Open a regular epub book — verify no regression
8. Open an epub3 book with NO audio clips — verify no regression
