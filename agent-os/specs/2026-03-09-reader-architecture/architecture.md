# Komelia Reader — Image Loading & Upscaler Architecture

## Overview

The reader has three main layers:

```
User Navigation
      │
 [Mode State]              PagedReaderState | PanelsReaderState | ContinuousReaderState
      │
 [Image Cache]             cache4k in-memory (10 pages per mode)
      │
 [BookImageLoader]         Network fetch + Coil3 DiskCache
      │
 [TilingReaderImage]       Decode → Process → Tile → Paint
      │ (Android)
 [AndroidNcnnUpscaler]     NCNN JNI upscale queue
      │
 [Compose Painter]         Screen render
```

---

## 1. Image Loading Flow

### 1a. Entry Point: `BookImageLoader.loadReaderImage(bookId, page)`

File: `komelia-domain/core/src/commonMain/kotlin/snd/komelia/image/BookImageLoader.kt`

```
loadReaderImage(bookId, page)
├── doLoad(bookId, page)
│   ├── IF diskCache exists:
│   │   ├── openSnapshot(pageId.toString())  → return FilePathSource (cache HIT)
│   │   └── MISS: bookApi.getPage()          → writeToDiskCache() → return FilePathSource
│   └── ELSE (no disk cache):
│       └── bookApi.getPage()                → return MemorySource(bytes)
└── readerImageFactory.getImage(source, pageId)
    └── returns AndroidReaderImage (wrapped in ReaderImageResult.Success)
```

**Disk cache key**: `"bookId_pageNumber"` (persists across sessions, LRU eviction managed by Coil3)

### 1b. `TilingReaderImage` — Image Decode & Processing

File: `komelia-domain/core/src/commonMain/kotlin/snd/komelia/image/TilingReaderImage.kt`

On construction, immediately launches in `processingScope`:
```
init → processingScope.launch { loadImage() }
    ├── decodeImage(source)               → KomeliaImage (VIPS or Android Bitmap)
    ├── processingPipeline.process(id, img) → color correction, crop, etc.
    └── image.value = processed           [StateFlow emits, wakes waiters]
```

Then waits for `requestUpdate()` calls from the mode state.

### 1c. `requestUpdate()` → Tiling

Called whenever the visible area or zoom changes:

```
requestUpdate(maxDisplaySize, zoomFactor, visibleDisplaySize)
└── jobFlow.tryEmit(UpdateRequest)  [conflated — only latest is processed]
    └── doUpdate(request)
        ├── Wait for image.value to be non-null
        ├── Calculate displayScaleFactor = min(width/imgW, height/imgH)
        ├── actualScale = displayScaleFactor × zoomFactor
        ├── Determine tile mode by total pixel count:
        │   ├── < 2048²       → doFullResize()  (single bitmap)
        │   ├── 2048²-4096²   → doTile(1024px tiles)
        │   ├── 4096²-6144²   → doTile(512px tiles)
        │   └── > 6144²       → doTile(256px tiles)
        └── frameData.value = FrameData(tiles, displaySize, scaleFactor)
            └── painter.value = TiledPainter (Compose reads this)
```

**Tile lifecycle**: old tiles moved to `pendingTilesToClose`, recycled asynchronously after new tiles are set.

---

## 2. Cache Layers

| Layer | Location | Max Size | Key | Lifetime | Cleared When |
|-------|----------|----------|-----|----------|-------------|
| Disk cache | `BookImageLoader.diskCache` (Coil3) | Configurable | `bookId_pageNumber` | Across sessions | LRU auto-eviction |
| Reader image cache | `PagedReaderState.imageCache` | 10 pages | `PageId` | Reader session | `stop()` → `invalidateAll()` |
| Panels image cache | `PanelsReaderState.imageCache` | 10 pages | `PageId` | Reader session | `stop()` → `invalidateAll()` |
| Tile bitmap cache | `TilingReaderImage.frameData` | 1 per image | — | While image is open | On `doUpdate()` → close old tiles |
| Upscale cache | `AndroidReaderImage.cachedUpscaledImage` | 1 per image | — | While image is open | Engine/model change, `close()` |

**Eviction flow for reader image cache**:
```
imageCache eviction (LRU, Evicted/Expired/Removed)
└── cleanupScope.launch {
    deferred.await().imageResult?.image?.close()
    └── TilingReaderImage.close()
        ├── closeTileBitmaps(pendingTilesToClose)   → bitmap.recycle()
        ├── originalImage?.close()
        ├── frameData tiles → closeTileBitmaps()
        ├── image.value?.close()
        ├── imageSource.close()                     → release DiskCache.Snapshot lock
        └── Cancel processingScope, imageAwaitScope, animationScope
}
```

---

## 3. Paged Mode vs Panel Mode

### Paged Mode (`PagedReaderState`)

File: `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/reader/image/paged/PagedReaderState.kt`

- **Navigation unit**: Spread (1 or 2 pages)
- **Page change**: `nextPage()` → `onPageChange(spreadIndex)` → `loadSpread()`
- **Prefetch**: adjacent spreads (±1) loaded in background via `pageLoadScope.async`
- **On user pan/zoom**: `updateSpreadImageState()` recalculates visible `IntRect` per page → `image.requestUpdate()` → re-tiles
- **Cache invalidated**: on `stop()` (mode switch or reader close)

```
nextPage() → onPageChange(spreadIndex + 1) → pageLoadScope.launch { loadSpread() }
    ├── For each page in spread: imageCache.get(pageId) or load fresh
    ├── IMMEDIATE: show placeholder spread (no images)
    ├── PARALLEL: preload ±1 spreads into imageCache
    └── AWAIT: awaitAll(pages) → completeLoadJob()
        ├── calculateScreenScale(pages) → initial zoom/offset
        ├── updateSpreadImageState() → image.requestUpdate() per page
        └── screenScaleState.apply(newScale)
```

### Panel Mode (`PanelsReaderState`)

File: `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/reader/image/panels/PanelsReaderState.kt`

- **Navigation unit**: Individual panel within a page
- **Panel detection**: ONNX Rf-DETR model runs on decoded image → `List<ImageRect>` panels
- **Navigation**: panel-to-panel = scroll/zoom to panel bounds; page-to-page = load new PanelsPage
- **State**: `currentPageIndex = PageIndex(page=Int, panel=Int)`
- **Panels injected**: fullPageDisplayMode can add full-page views before/after panels

```
nextPanel():
├── IF more panels on this page:
│   └── scrollToPanel(panelBounds) → screenScaleState.animateTo(offset, zoom)
└── ELSE:
    └── nextPage() → doPageLoad(pageIdx + 1)
        ├── launchDownload(meta) → loadReaderImage() + detect panels
        ├── IMMEDIATE: show placeholder
        ├── PARALLEL: preload ±1 pages
        └── AWAIT: panelData ready → scrollToPanel(firstPanel)
```

**Key difference**: In panel mode, `image.requestUpdate()` is called once when the page loads (for initial full render), then the reader mostly just scrolls/zooms. No re-tiling on panel navigation unless the user manually zooms.

---

## 4. Upscaler Architecture

### 4a. Structure

File: `komelia-domain/core/src/androidMain/kotlin/snd/komelia/image/AndroidNcnnUpscaler.kt`

```
AndroidNcnnUpscaler (instance per reader)
    │
    ├── Companion object (GLOBAL, singleton-like):
    │   ├── requestChannel: Channel<UpscaleRequest>(UNLIMITED)  ← the queue
    │   ├── workerScope: single background worker coroutine
    │   ├── generation: AtomicInt                 ← increment to cancel all pending
    │   ├── currentPageNumber: AtomicInt           ← priority hint
    │   └── globalUpscaleActivities: MutableStateFlow<Map<Int, UpscaleStatus>>
    │
    └── Instance state:
        ├── ncnn: NcnnUpscaler (JNI handle)
        ├── jniMutex: Mutex (JNI thread safety)
        ├── isReady: MutableStateFlow<Boolean>
        └── settingsFlow: MutableStateFlow<NcnnUpscalerSettings?>
```

### 4b. Worker Algorithm

```
runWorker() [runs forever in workerScope]:
    while true:
        1. Drain all pending requests from channel into `pending` ArrayDeque
        2. Find request for currentPageNumber (priority) or take first
        3. IF request.generation != current generation → complete(null) and skip
        4. Execute block under NonCancellable + jniMutex:
           └── ncnn.process(bitmapIn, bitmapOut) → JNI call
        5. complete(result)
```

One upscale runs at a time. Generation number is how we cancel: incrementing it makes all pending requests self-discard when they reach the worker.

### 4c. "Upscale on Load" Flow

File: `komelia-domain/core/src/androidMain/kotlin/snd/komelia/image/AndroidReaderImage.kt`

```
AndroidReaderImage.loadImage():
    1. super.loadImage()            ← decode + process pipeline
    2. willUpscale = ncnnUpscaler?.willUpscale(image)
       └── enabled && upscaleOnLoad && image.width < threshold
    3. IF willUpscale:
       a. upscaleStatus = Upscaling
       b. registerActivity(pageNumber)     ← UI spinner appears
       c. ncnnUpscaler.checkAndUpscale(image, pageNumber)
          └── upscale() → requestChannel.send(UpscaleRequest) → await()
       d. IF result != null:
          - cachedUpscaledImage = upscaled
          - upscaleStatus = Upscaled        ← UI shows ✓
          - reloadLastRequest()             ← re-render with upscaled image
       e. ELSE:
          - upscaleStatus = Idle
    4. unregisterActivity(pageNumber, status)
```

### 4d. "Upscale on Demand" (Zoom-In)

When a tile is rendered and the display size is larger than the original image dimensions, the upscaler is used:

```
TilingReaderImage.doUpdate() → resizeImage() or getImageRegion()
    IF scaleTarget > original size AND ncnnUpscaler != null:
        └── upscaleImage() or upscaleRegion()
            ├── Use cachedUpscaledImage if available (reuse!)
            └── ELSE: queue new upscale, cache result
```

### 4e. When the Upscale Queue is Cleared

| Trigger | Queue Cleared? | How |
|---------|---------------|-----|
| Book navigation (next/prev) | ✅ YES | `AndroidNcnnUpscaler.cancelPendingRequests()` via `onBookChange` callback |
| Engine or model change | ✅ YES | `reinit()` calls `cancelPendingRequests()` first |
| Upscaling disabled | ✅ YES | Settings listener calls `cancelPendingRequests()` |
| App close / `close()` | ✅ YES | `AndroidNcnnUpscaler.cancelPendingRequests()` |
| Mode switch (paged↔panel) | ⚠️ NO | `cancelPendingRequests()` not called explicitly; old requests age out by generation mismatch or completion |
| "Upscale on load" toggled off | ⚠️ PARTIAL | `cachedUpscaledImage` cleared per-image, but queue not globally cancelled |
| Reader closes without book change | ✅ YES | `close()` on each image, `unregisterActivity()` cleans up tracking |

**Note**: The `generation` mechanism provides a soft cancel — pending requests complete instantly with `null` if generation mismatches. But the worker still processes one-at-a-time, so stale work can still delay real work if a long upscale is in-flight.

### 4f. Per-Image Upscale Cache (`cachedUpscaledImage`)

Each `AndroidReaderImage` holds:
```kotlin
@Volatile private var cachedUpscaledImage: KomeliaImage?
```
- Cleared when engine/model changes (triggers re-upscale if `upscaleOnLoad` is on)
- Cleared on `close()` (if different from `image.value`)
- NOT cleared on page navigation (image itself is evicted from cache → `close()` is called)

---

## 5. Visual UI Indicators for Upscaling

File: `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/reader/image/settings/BottomSheetSettingsOverlay.kt`

```kotlin
val allUpscaleActivities by ncnnSettingsState.globalUpscaleActivities.collectAsState()

AnimatedVisibility(visible = allUpscaleActivities.isNotEmpty()) {
    // Row of page indicators
    activities.sortedBy { it.key }.forEach { (page, status) ->
        when (status) {
            Upscaling → CircularProgressIndicator + "p$page"
            Upscaled  → "p$page ✓"
        }
    }
}
```

**Data flow**: `AndroidNcnnUpscaler.globalUpscaleActivities` (companion object StateFlow) → `NcnnSettingsState.globalUpscaleActivities` → `BottomSheetSettingsOverlay`

Indicator appears in the reader's bottom settings overlay while any upscale is in progress. Disappears once all complete.

---

## 6. Crash Reporting

File: `komelia-app/src/androidMain/kotlin/snd/komelia/GlobalExceptionHandler.kt`

```
Thread.setDefaultUncaughtExceptionHandler(GlobalExceptionHandler)

On uncaught exception:
    1. logger.catching(exception)   → logcat + file log
    2. Write java_crash_report.txt  → persistent file
    3. Serialize to ExceptionData JSON
    4. Start CrashActivity via Intent (extra = JSON)
    5. exitProcess(0)

CrashActivity:
    └── Shows ErrorView with exception name + stacktrace
        └── User can restart or exit
```

### Log Files (all under external storage, user-accessible)

```
Android/data/snd.komelia/files/komelia/logs/
├── komelia.log              ← current rolling log (Java/Kotlin messages, up to 5 MB)
├── komelia.1.log            ← previous rolling log
├── komelia.2.log            ← oldest rolling log
├── last_session_logcat.txt  ← logcat snapshot from last startup (includes native crashes)
└── java_crash_report.txt    ← written only if Java exception handler fires
```

---

## 7. Open Questions / Known Risks

### Likely Crash Causes (to investigate once logs are available)
1. **`cachedUpscaledImage` race condition** — `@Volatile` but multiple paths (tile render, close, settings change) can race. The existing `IllegalStateException` fallback catches some cases but not all.
2. **Upscale queue + panel mode** — Panel mode requests many upscales quickly (panel detection triggers re-upscale). If `cancelPendingRequests()` isn't called on mode switch, old results may land on the wrong image.
3. **JNI OOM** — NCNN allocates GPU memory. After reading many pages, accumulated memory may cause native OOM that bypasses the Java exception handler entirely.
4. **Double-close on tile bitmaps** — Tile lifetime is complex (shared references in some paths). Recycling a bitmap that's still being painted causes native crash.

### Queue Lifecycle Gaps (potential fixes)
- Mode switch (paged↔panel) should call `cancelPendingRequests()`
- "Upscale on load" toggle off should globally cancel, not just per-image

---

## Verification Plan

1. Build with file logging → trigger crash → inspect `komelia.log` and `java_crash_report.txt`
2. Review log for patterns: which page numbers, what operation, what exception
3. Look for `NCNN`, `bitmap recycled`, `IllegalState`, `OutOfMemory` in logs before crash point
4. Cross-reference with `globalUpscaleActivities` state at crash time
