# Implementation Plan: Open Local CBZ/EPUB Files via Android Intent (Full-Featured)

## Context

Komelia currently only reads books from a remote Komga server. This plan adds full support for opening local `.cbz` and `.epub` files directly from the Android file manager via the system "Open with" intent (`ACTION_VIEW`).

**Full-featured (Option B)**: read progress and EPUB bookmarks persist across sessions, keyed by a stable virtual book ID derived from the file URI. The rest of the app (Komga remote connections) is completely unaffected.

---

## Architecture Overview

```
Android FILE MANAGER
      │ ACTION_VIEW intent
      ▼
MainActivity.handleIntent()
      │ emits URI string
      ▼
LocalFileApiProviderImpl
  ├─ creates LocalFileBookApi (per URI)
  ├─ registers in in-memory registry map
  └─ emits KomeliaBook on processedBooksFlow
      │
      ▼
MainContent (LaunchedEffect inside Navigator block)
  └─ navigator.push(readerScreen(book, markReadProgress = true, BookSiblingsContext.Series()))
      │
      ▼
ImageReaderScreen / EpubScreen
  └─ viewModelFactory.getBookReaderViewModel(bookId, ...)
        │ checks localFileApiProvider.getApiForBook(bookId)
        ▼
  LocalFileBookApi      ←── BookImageLoader.fetchPage()
  ├─ CBZ: ZipInputStream via ContentResolver
  └─ EPUB: downloadBookRawFile() streams via ContentResolver
        │
        ▼
  LocalFileReadProgressRepository (SQLite via Exposed, V39 migration)
```

### Virtual Book ID
`KomgaBookId("local:${sha256(uriString).take(16)}")` — stable across sessions for the same file URI, ensuring read progress and bookmarks are correctly associated if the same file is reopened.

---

## Step 1: AndroidManifest.xml

**File:** `komelia-app/src/androidMain/AndroidManifest.xml`

Add inside the existing `<activity android:name="snd.komelia.MainActivity" ...>` tag, **after** the existing `MAIN` intent-filter:

```xml
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="content" />
    <data android:scheme="file" />
    <data android:host="*" />
    <data android:mimeType="application/zip" />
    <data android:mimeType="application/x-cbz" />
    <data android:mimeType="application/x-zip-compressed" />
    <data android:mimeType="application/epub+zip" />
    <data android:pathPattern=".*\\.cbz" />
    <data android:pathPattern=".*\\.epub" />
</intent-filter>
```

---

## Step 2: Handle Intents in MainActivity

**File:** `komelia-app/src/androidMain/kotlin/snd/komelia/MainActivity.kt`

At **file level** (outside the class), add:

```kotlin
import android.content.Intent
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

private val _incomingFileUriFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)
val incomingFileUriFlow: SharedFlow<String> = _incomingFileUriFlow.asSharedFlow()
```

Inside `MainActivity`, add these methods:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(null)
    // ... existing code unchanged ...
    handleIntent(intent)   // add this line after the existing initScope.launch block
}

override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    handleIntent(intent)
}

private fun handleIntent(intent: Intent?) {
    if (intent?.action == Intent.ACTION_VIEW) {
        intent.data?.toString()?.let { _incomingFileUriFlow.tryEmit(it) }
    }
}
```

---

## Step 3: `LocalFileApiProvider` Interface

**Create new file:** `komelia-domain/komga-api/src/commonMain/kotlin/snd/komelia/komga/api/LocalFileApiProvider.kt`

```kotlin
package snd.komelia.komga.api

import kotlinx.coroutines.flow.SharedFlow
import snd.komelia.komga.api.model.KomeliaBook
import snd.komga.client.book.KomgaBookId

interface LocalFileApiProvider {
    /** Emits a fully constructed KomeliaBook each time a new local file URI is processed. */
    val processedBooksFlow: SharedFlow<KomeliaBook>

    /** Returns the LocalFileBookApi for this virtual book ID, or null if not a local file. */
    fun getApiForBook(bookId: KomgaBookId): KomgaBookApi?
}
```

This lives in `komga-api` so both `ViewModelFactory` (in `komelia-ui`) and `DependencyContainer` can reference it without circular dependencies.

---

## Step 4: Database Migration V39

**Create new SQL file:**
`komelia-infra/database/sqlite/src/commonMain/composeResources/files/migrations/app/V39__local_file_read_progress.sql`

```sql
CREATE TABLE IF NOT EXISTS local_file_read_progress (
    virtual_book_id TEXT PRIMARY KEY,
    page INTEGER NOT NULL DEFAULT 1,
    completed INTEGER NOT NULL DEFAULT 0,
    readium_progression TEXT
);
```

**Register the migration** in `AppMigrations.migrations` list:
**File:** `komelia-infra/database/sqlite/src/commonMain/kotlin/snd/komelia/db/migrations/AppMigrations.kt`

Add `"V39__local_file_read_progress.sql"` as the last entry in the `migrations` list.

---

## Step 5: `LocalFileReadProgressRepository`

**Create new file:**
`komelia-infra/database/sqlite/src/commonMain/kotlin/snd/komelia/db/localfile/LocalFileReadProgressRepository.kt`

Follow the same Exposed pattern as `ExposedEpubBookmarkRepository`. Define an internal table object and implement four methods:

```kotlin
package snd.komelia.db.localfile

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.upsert
import org.jetbrains.exposed.v1.jdbc.selectAll
import snd.komelia.db.ExposedRepository
import snd.komga.client.book.KomgaBookId

internal object LocalFileReadProgressTable : Table("local_file_read_progress") {
    val virtualBookId = text("virtual_book_id")
    val page = integer("page").default(1)
    val completed = bool("completed").default(false)
    val readiumProgression = text("readium_progression").nullable()
    override val primaryKey = PrimaryKey(virtualBookId)
}

class LocalFileReadProgressRepository(database: Database) : ExposedRepository(database) {

    suspend fun saveProgress(bookId: KomgaBookId, page: Int, completed: Boolean) {
        transaction {
            LocalFileReadProgressTable.upsert {
                it[virtualBookId] = bookId.value
                it[LocalFileReadProgressTable.page] = page
                it[LocalFileReadProgressTable.completed] = completed
            }
        }
    }

    suspend fun getProgress(bookId: KomgaBookId): Pair<Int, Boolean>? {
        return transaction {
            LocalFileReadProgressTable
                .selectAll()
                .where { LocalFileReadProgressTable.virtualBookId eq bookId.value }
                .firstOrNull()
                ?.let {
                    Pair(
                        it[LocalFileReadProgressTable.page],
                        it[LocalFileReadProgressTable.completed]
                    )
                }
        }
    }

    suspend fun saveReadiumProgression(bookId: KomgaBookId, json: String) {
        transaction {
            LocalFileReadProgressTable.upsert {
                it[virtualBookId] = bookId.value
                it[readiumProgression] = json
            }
        }
    }

    suspend fun getReadiumProgression(bookId: KomgaBookId): String? {
        return transaction {
            LocalFileReadProgressTable
                .selectAll()
                .where { LocalFileReadProgressTable.virtualBookId eq bookId.value }
                .firstOrNull()
                ?.get(LocalFileReadProgressTable.readiumProgression)
        }
    }
}
```

---

## Step 6: `LocalFileBookApi`

**Create new file:**
`komelia-app/src/androidMain/kotlin/snd/komelia/localfile/LocalFileBookApi.kt`

```kotlin
package snd.komelia.localfile

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.serialization.json.Json
import snd.komelia.db.localfile.LocalFileReadProgressRepository
import snd.komelia.komga.api.KomgaBookApi
import snd.komelia.komga.api.model.KomeliaBook
import snd.komga.client.book.*
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.series.KomgaSeriesId
import java.security.MessageDigest
import java.util.zip.ZipInputStream
import kotlin.time.Clock

class LocalFileBookApi(
    private val context: Context,
    private val uriString: String,
    private val readProgressRepo: LocalFileReadProgressRepository,
) : KomgaBookApi {

    val virtualBookId: KomgaBookId = KomgaBookId(
        "local:" + uriString.sha256().take(16)
    )

    private val uri: Uri = Uri.parse(uriString)
    private val filename: String = uri.lastPathSegment?.substringAfterLast('/') ?: "Local File"

    val isEpub: Boolean = filename.endsWith(".epub", ignoreCase = true)
        || context.contentResolver.getType(uri)?.contains("epub") == true

    // Eagerly scan zip entries for CBZ (image file names, sorted)
    private val imageEntries: List<String> by lazy {
        if (isEpub) emptyList()
        else {
            val imageExtensions = setOf("jpg", "jpeg", "png", "webp", "gif")
            val entries = mutableListOf<String>()
            openZip { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    val ext = entry.name.substringAfterLast('.', "").lowercase()
                    if (!entry.isDirectory && ext in imageExtensions) {
                        entries.add(entry.name)
                    }
                    entry = zip.nextEntry
                }
            }
            entries.sortedWith(naturalOrder())
        }
    }

    // ── KomgaBookApi implementation ─────────────────────────────────────────

    override suspend fun getOne(bookId: KomgaBookId): KomeliaBook {
        require(bookId == virtualBookId)
        val (page, completed) = readProgressRepo.getProgress(bookId) ?: Pair(1, false)
        val storedReadProgress = if (page > 1 || completed) {
            ReadProgress(page = page, completed = completed, readDate = Clock.System.now(), createdDate = Clock.System.now(), lastModifiedDate = Clock.System.now(), deviceId = "", deviceName = "")
        } else null

        val now = Clock.System.now()
        return KomeliaBook(
            id = virtualBookId,
            seriesId = KomgaSeriesId("local"),
            seriesTitle = "",
            libraryId = KomgaLibraryId("local"),
            name = filename,
            url = uriString,
            number = 1,
            created = now,
            lastModified = now,
            fileLastModified = now,
            sizeBytes = 0L,
            size = "",
            media = Media(
                status = KomgaMediaStatus.READY,
                mediaType = if (isEpub) "application/epub+zip" else "application/zip",
                pagesCount = imageEntries.size,
                comment = "",
                epubDivinaCompatible = false,
                epubIsKepub = false,
                mediaProfile = if (isEpub) MediaProfile.EPUB else MediaProfile.DIVINA,
            ),
            metadata = KomgaBookMetadata(
                title = filename.substringBeforeLast('.'),
                summary = "", number = "", numberSort = 1f,
                releaseDate = null, authors = emptyList(), tags = emptyList(),
                isbn = "", links = emptyList(),
                titleLock = false, summaryLock = false, numberLock = false,
                numberSortLock = false, releaseDateLock = false, authorsLock = false,
                tagsLock = false, isbnLock = false, linksLock = false,
                created = now, lastModified = now,
            ),
            readProgress = storedReadProgress,
            deleted = false,
            fileHash = "",
            oneshot = true,
            downloaded = true,
            localFileLastModified = null,
            remoteFileUnavailable = false,
        )
    }

    override suspend fun getBookPages(bookId: KomgaBookId): List<KomgaBookPage> {
        require(bookId == virtualBookId)
        return imageEntries.mapIndexed { index, name ->
            KomgaBookPage(
                number = index + 1,
                fileName = name,
                mediaType = mimeTypeForEntry(name),
                width = null,
                height = null,
                sizeBytes = null,
                size = null,
            )
        }
    }

    override suspend fun getPage(bookId: KomgaBookId, page: Int): ByteArray {
        require(bookId == virtualBookId)
        val targetEntry = imageEntries.getOrNull(page - 1)
            ?: throw IllegalArgumentException("Page $page not found (${imageEntries.size} pages total)")
        var result: ByteArray? = null
        openZip { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name == targetEntry) {
                    result = zip.readBytes()
                    break
                }
                entry = zip.nextEntry
            }
        }
        return result ?: throw IllegalStateException("Entry $targetEntry not found in zip")
    }

    override suspend fun markReadProgress(bookId: KomgaBookId, request: KomgaBookReadProgressUpdateRequest) {
        require(bookId == virtualBookId)
        readProgressRepo.saveProgress(
            bookId = bookId,
            page = request.page ?: 1,
            completed = request.completed == true,
        )
    }

    override suspend fun getReadiumProgression(bookId: KomgaBookId): R2Progression? {
        require(bookId == virtualBookId)
        val json = readProgressRepo.getReadiumProgression(bookId) ?: return null
        return runCatching { Json.decodeFromString(R2Progression.serializer(), json) }.getOrNull()
    }

    override suspend fun updateReadiumProgression(bookId: KomgaBookId, progression: R2Progression) {
        require(bookId == virtualBookId)
        val json = Json.encodeToString(R2Progression.serializer(), progression)
        readProgressRepo.saveReadiumProgression(bookId, json)
    }

    override suspend fun hasLocalFile(bookId: KomgaBookId): Boolean = true

    // getBookLocalFilePath returns null — Epub3ReaderState will fall through to downloadBookRawFile()
    override suspend fun getBookLocalFilePath(bookId: KomgaBookId): String? = null

    override suspend fun downloadBookRawFile(bookId: KomgaBookId, onChunk: suspend (ByteArray) -> Unit) {
        require(bookId == virtualBookId)
        context.contentResolver.openInputStream(uri)!!.use { stream ->
            val buf = ByteArray(65536)
            var n: Int
            while (stream.read(buf).also { n = it } != -1) {
                onChunk(buf.copyOf(n))
            }
        }
    }

    override suspend fun getBookSiblingNext(bookId: KomgaBookId): KomeliaBook? = null
    override suspend fun getBookSiblingPrevious(bookId: KomgaBookId): KomeliaBook? = null

    // ── Unsupported stubs ────────────────────────────────────────────────────
    override suspend fun getBookList(conditionBuilder: snd.komga.client.search.BookConditionBuilder, fullTextSearch: String?, pageRequest: snd.komga.client.common.KomgaPageRequest?) = unsupported()
    override suspend fun getBookList(search: snd.komga.client.book.KomgaBookSearch, pageRequest: snd.komga.client.common.KomgaPageRequest?) = unsupported()
    override suspend fun getLatestBooks(pageRequest: snd.komga.client.common.KomgaPageRequest?) = unsupported()
    override suspend fun getBooksOnDeck(libraryIds: List<snd.komga.client.library.KomgaLibraryId>?, pageRequest: snd.komga.client.common.KomgaPageRequest?) = unsupported()
    override suspend fun getDuplicateBooks(pageRequest: snd.komga.client.common.KomgaPageRequest?) = unsupported()
    override suspend fun updateMetadata(bookId: KomgaBookId, request: KomgaBookMetadataUpdateRequest) = unsupported()
    override suspend fun analyze(bookId: KomgaBookId) = unsupported()
    override suspend fun refreshMetadata(bookId: KomgaBookId) = unsupported()
    override suspend fun deleteReadProgress(bookId: KomgaBookId) = unsupported()
    override suspend fun deleteBook(bookId: KomgaBookId) = unsupported()
    override suspend fun regenerateThumbnails(forBiggerResultOnly: Boolean) = unsupported()
    override suspend fun getDefaultThumbnail(bookId: KomgaBookId): ByteArray? = null
    override suspend fun getThumbnail(bookId: KomgaBookId, thumbnailId: snd.komga.client.common.KomgaThumbnailId): ByteArray = ByteArray(0)
    override suspend fun getThumbnails(bookId: KomgaBookId): List<KomgaBookThumbnail> = emptyList()
    override suspend fun uploadThumbnail(bookId: KomgaBookId, file: ByteArray, filename: String, selected: Boolean): KomgaBookThumbnail = unsupported()
    override suspend fun selectBookThumbnail(bookId: KomgaBookId, thumbnailId: snd.komga.client.common.KomgaThumbnailId) = unsupported()
    override suspend fun deleteBookThumbnail(bookId: KomgaBookId, thumbnailId: snd.komga.client.common.KomgaThumbnailId) = unsupported()
    override suspend fun getAllReadListsByBook(bookId: KomgaBookId): List<snd.komga.client.readlist.KomgaReadList> = emptyList()
    override suspend fun getPageThumbnail(bookId: KomgaBookId, page: Int): ByteArray = getPage(bookId, page)
    override suspend fun getReadiumPositions(bookId: KomgaBookId): snd.komga.client.book.R2Positions = R2Positions(total = 0, positions = emptyList())
    override suspend fun getWebPubManifest(bookId: KomgaBookId): snd.komga.client.book.WPPublication = unsupported()
    override suspend fun getBookEpubResource(bookId: KomgaBookId, resourceName: String): ByteArray = unsupported()
    override suspend fun getBookRawFile(bookId: KomgaBookId): ByteArray = unsupported()
    override suspend fun getDownloadedSeriesIds(seriesIds: List<KomgaSeriesId>): Set<KomgaSeriesId> = emptySet()

    // ── Private helpers ──────────────────────────────────────────────────────

    private fun openZip(block: (ZipInputStream) -> Unit) {
        context.contentResolver.openInputStream(uri)!!.use { raw ->
            ZipInputStream(raw).use(block)
        }
    }

    private fun mimeTypeForEntry(name: String): String {
        return when (name.substringAfterLast('.', "").lowercase()) {
            "png" -> "image/png"
            "webp" -> "image/webp"
            "gif" -> "image/gif"
            else -> "image/jpeg"
        }
    }

    private fun String.sha256(): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(toByteArray()).joinToString("") { "%02x".format(it) }
    }

    private fun unsupported(): Nothing =
        throw UnsupportedOperationException("Not supported for local files")
}
```

**Notes:**
- `imageEntries` is computed lazily on first access (during `getBookPages()` or `getPage()`), so the zip is not opened at construction time.
- `getOne()` re-reads progress from SQLite on each call so the reader gets the current page on initialization.
- For EPUB: `getBookLocalFilePath()` returns `null` so `Epub3ReaderState.prepareEpubDirectory()` falls through to `downloadBookRawFile()`, which streams bytes from the ContentResolver. The state then extracts to its cache dir as usual.

---

## Step 7: `LocalFileApiProviderImpl`

**Create new file:**
`komelia-app/src/androidMain/kotlin/snd/komelia/localfile/LocalFileApiProviderImpl.kt`

```kotlin
package snd.komelia.localfile

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import snd.komelia.db.localfile.LocalFileReadProgressRepository
import snd.komelia.komga.api.KomgaBookApi
import snd.komelia.komga.api.LocalFileApiProvider
import snd.komelia.komga.api.model.KomeliaBook
import snd.komga.client.book.KomgaBookId
import java.util.concurrent.ConcurrentHashMap

class LocalFileApiProviderImpl(
    private val context: Context,
    private val incomingUriFlow: SharedFlow<String>,
    private val readProgressRepo: LocalFileReadProgressRepository,
    scope: CoroutineScope,
) : LocalFileApiProvider {

    private val registry = ConcurrentHashMap<KomgaBookId, LocalFileBookApi>()

    private val _processedBooksFlow = MutableSharedFlow<KomeliaBook>(extraBufferCapacity = 1)
    override val processedBooksFlow: SharedFlow<KomeliaBook> = _processedBooksFlow.asSharedFlow()

    init {
        scope.launch {
            incomingUriFlow.collect { uriString ->
                val api = LocalFileBookApi(context, uriString, readProgressRepo)
                registry[api.virtualBookId] = api
                val book = api.getOne(api.virtualBookId)
                _processedBooksFlow.emit(book)
            }
        }
    }

    override fun getApiForBook(bookId: KomgaBookId): KomgaBookApi? = registry[bookId]
}
```

---

## Step 8: Wire Up in AndroidAppModule

**File:** `komelia-app/src/androidMain/kotlin/snd/komelia/AndroidAppModule.kt`

Inside `initDependencies()`, before the `return DependencyContainer(...)` call, add:

```kotlin
val localFileReadProgressRepo = LocalFileReadProgressRepository(databases.app)
val localFileApiProvider = LocalFileApiProviderImpl(
    context = applicationContext,
    incomingUriFlow = incomingFileUriFlow,   // the file-level val from MainActivity.kt
    readProgressRepo = localFileReadProgressRepo,
    scope = initScope,
)
```

Then update the `DependencyContainer(...)` call to include:
```kotlin
localFileApiProvider = localFileApiProvider,
```

And update the `createReaderImageLoader(...)` call to include:
```kotlin
localFileApiProvider = localFileApiProvider,
```

---

## Step 9: Update DependencyContainer

**File:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/DependencyContainer.kt`

Add one nullable field with a default of `null`:

```kotlin
val localFileApiProvider: LocalFileApiProvider? = null,
```

---

## Step 10: Update BookImageLoader

**File:** `komelia-domain/core/src/commonMain/kotlin/snd/komelia/image/BookImageLoader.kt`

Add a new constructor parameter after `offlineBookApi`:
```kotlin
private val localFileApiProvider: LocalFileApiProvider? = null,
```

Update `fetchPage()` to check local files **first**, before the offline/network fallback:

```kotlin
private suspend fun fetchPage(bookId: KomgaBookId, page: Int): ByteArray {
    // Local file (intent-opened) takes priority
    localFileApiProvider?.getApiForBook(bookId)?.let { localApi ->
        return localApi.getPage(bookId, page)
    }
    // Existing offline + network fallback (unchanged)
    if (offlineBookRepository?.find(bookId) != null && offlineBookApi != null) {
        return try {
            offlineBookApi.getPage(bookId, page)
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            logger.warn(e) { "Local page read failed for $bookId page $page, falling back to network" }
            bookClient.value.getPage(bookId, page)
        }
    }
    return bookClient.value.getPage(bookId, page)
}
```

Also update `createReaderImageLoader()` in `AppModule.kt`:
**File:** `komelia-app/src/commonMain/kotlin/snd/komelia/AppModule.kt`

The `createReaderImageLoader()` method signature needs a new parameter:
```kotlin
protected fun createReaderImageLoader(
    bookApi: StateFlow<KomgaBookApi>,
    imageFactory: ReaderImageFactory,
    imageDecoder: KomeliaImageDecoder,
    offlineBookRepository: OfflineBookRepository,
    offlineBookApi: KomgaBookApi,
    localFileApiProvider: LocalFileApiProvider? = null,   // NEW
): BookImageLoader {
    // ... existing disk cache setup unchanged ...
    return BookImageLoader(
        bookClient = bookApi,
        readerImageFactory = imageFactory,
        imageDecoder = imageDecoder,
        diskCache = diskCache,
        offlineBookRepository = offlineBookRepository,
        offlineBookApi = offlineBookApi,
        localFileApiProvider = localFileApiProvider,      // NEW
    )
}
```

---

## Step 11: Update ViewModelFactory

**File:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/ViewModelFactory.kt`

**Change 1 — `getBookReaderViewModel`:** Add `bookId: KomgaBookId` parameter and select the API dynamically:

```kotlin
fun getBookReaderViewModel(
    navigator: Navigator,
    markReadProgress: Boolean,
    bookSiblingsContext: BookSiblingsContext,
    bookId: KomgaBookId,                    // NEW parameter
): ReaderViewModel {
    val bookApi = dependencies.localFileApiProvider?.getApiForBook(bookId)
        ?: komgaApi.bookApi
    return ReaderViewModel(
        bookApi = bookApi,
        // ... all other parameters unchanged ...
    )
}
```

**Change 2 — `getEpubReaderViewModel`:** Already takes `bookId`, just select API dynamically:

```kotlin
fun getEpubReaderViewModel(
    bookId: KomgaBookId,
    // ... other params unchanged ...
): EpubReaderViewModel {
    val bookApi = dependencies.localFileApiProvider?.getApiForBook(bookId)
        ?: komgaApi.bookApi
    return EpubReaderViewModel(
        bookId = bookId,
        bookApi = bookApi,      // was: bookApi = komgaApi.bookApi
        // ... all other parameters unchanged ...
    )
}
```

---

## Step 12: Update ImageReaderScreen Call Site

**File:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/reader/ImageReaderScreen.kt`

In `ImageReaderScreen.Content()`, pass `bookId` to the factory:

```kotlin
// BEFORE:
val vm = rememberScreenModel(bookId.value) {
    viewModelFactory.getBookReaderViewModel(
        navigator = navigator,
        markReadProgress = markReadProgress,
        bookSiblingsContext = bookSiblingsContext
    )
}

// AFTER:
val vm = rememberScreenModel(bookId.value) {
    viewModelFactory.getBookReaderViewModel(
        navigator = navigator,
        markReadProgress = markReadProgress,
        bookSiblingsContext = bookSiblingsContext,
        bookId = bookId,    // NEW
    )
}
```

---

## Step 13: Navigation Integration in MainView

**File:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/MainView.kt`

**Change 1 — `MainContent` signature:** Add `localFileApiProvider` parameter:

```kotlin
@Composable
private fun MainContent(
    platformType: PlatformType,
    komgaSharedState: KomgaAuthenticationState,
    localFileApiProvider: LocalFileApiProvider? = null,   // NEW
)
```

**Change 2 — Call site in `MainView`:** Pass the provider:

```kotlin
// BEFORE:
MainContent(platformType, dependencies.komgaSharedState)

// AFTER:
MainContent(platformType, dependencies.komgaSharedState, dependencies.localFileApiProvider)
```

**Change 3 — Inside the `Navigator { navigator -> ... }` block in `MainContent`:**

Add a `LaunchedEffect` that collects processed books and navigates. Place it **inside** the navigator block so `navigator` is available, but **after** the `canProceed` logic:

```kotlin
LaunchedEffect(localFileApiProvider) {
    localFileApiProvider?.processedBooksFlow?.collect { book ->
        // Wait until the app is in a ready state (logged in, initialized)
        snapshotFlow { canProceed }.filter { it }.first()
        navigator.push(
            readerScreen(
                book = book,
                markReadProgress = true,
                bookSiblingsContext = BookSiblingsContext.Series(),
            )
        )
    }
}
```

The `snapshotFlow { canProceed }.filter { it }.first()` guard ensures we don't try to push a screen before login is complete. If a file is opened before the app finishes initializing, the URI is buffered in `processedBooksFlow` (extraBufferCapacity = 1) and navigation happens as soon as `canProceed` becomes `true`.

---

## Verification Checklist

1. **CBZ from file manager:** Tap "Open with Komelia" on a `.cbz` → image reader launches → pages are displayed → navigate to page 5 → exit → reopen same file → reader starts at page 5.
2. **EPUB from file manager:** Tap "Open with Komelia" on a `.epub` → Epub3 reader launches (extraction step visible) → navigate chapters → exit → reopen → reader restores to last chapter/locator.
3. **EPUB bookmarks:** While reading a local EPUB, add a bookmark → close → reopen → bookmark is still listed.
4. **App already running:** With Komelia open on the home screen, open a CBZ from the file manager → reader is pushed on top of the existing screen → back button returns to the home screen.
5. **App killed then reopened via intent:** Kill the app process, then open a CBZ from the file manager → app launches fresh → reader opens without crash → progress from last session is restored.
6. **Regression — remote books:** Browse Komga server, open a remote book → reads normally → no regressions.
7. **Regression — offline/downloaded books:** Open a downloaded offline book → reads normally → no regressions.
