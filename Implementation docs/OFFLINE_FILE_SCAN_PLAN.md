# Plan: Offline File Scanning and Discovery

This document outlines the implementation plan for a feature that allows the Komelia app to "discover" and "re-adopt" previously downloaded offline files. This is particularly useful after an app reinstallation or when pointing the app to an existing download directory.

## 1. Problem Statement
Komelia's offline mode relies on a local SQLite database to track downloaded books. When the app is reinstalled, this database is lost. Even if the user points the new installation to their old download directory, the app won't "know" those files exist because there's no scanning mechanism to rebuild the database from the filesystem.

## 2. Core Scanning Logic

### `OfflineScannerService` (New)
A new service in `komelia-domain/offline` responsible for the scanning process.

#### **Responsibilities:**
1.  **Filesystem Traversal**: Iterate through the configured offline root directory.
2.  **Path Parsing**: Extract server, library, series, and book names from the directory structure:
    `[Root]/[Server_Host_Port]/[Library_Name]/[Series_Name]/[Book_Filename]`
3.  **Server Matching**: 
    - Match `Library_Name` -> `Series_Name` -> `Book_Filename`.
4.  **Registration & Persistence**: 
    - Books are registered in the local database **immediately** upon matching.
    - If the scan is cancelled, all successfully processed books remain in the database.
5.  **Change Detection (Sync Check)**:
    - Compare local `fileSize` and `lastModified` with server metadata.
    - Identify "Out of Sync" files where the server version differs.
6.  **Reporting**: Maintain a detailed log including:
    - **Imported**: New files added to the database.
    - **Updated**: Existing entries where metadata was refreshed.
    - **Out of Sync**: Files that exist but differ from the server version (size/date).
    - **Already Indexed**: Files already in the DB and matching the server.
    - **No Match**: Files that couldn't be found on the server.

#### **Scanning Algorithm:**
```kotlin
fun scan(root: PlatformFile) = flow {
    val serverFolders = listDirectories(root)
    for (serverFolder in serverFolders) {
        val libraries = listDirectories(serverFolder)
        for (libraryFolder in libraries) {
            val seriesFolders = listDirectories(libraryFolder)
            for (seriesFolder in seriesFolders) {
                val bookFiles = listFiles(seriesFolder)
                for (bookFile in bookFiles) {
                    ensureActive() // Handle cancellation
                    val result = processBookFile(
                        bookFile, 
                        serverFolder.name, 
                        libraryFolder.name, 
                        seriesFolder.name
                    )
                    emit(result)
                }
            }
        }
    }
}

private suspend fun processBookFile(...): OfflineScanResult {
    val existing = bookRepository.findByPath(bookFile.path)
    val serverBook = serverClient.findBookByPath(...) ?: return NoMatch
    
    val isOutOfSync = serverBook.size != bookFile.size || 
                      serverBook.lastModified != bookFile.lastModified

    if (existing == null) {
        importAction.execute(serverBook, bookFile.path)
        return if (isOutOfSync) OutOfSync else Imported
    } else {
        updateAction.execute(serverBook, existing.id)
        return if (isOutOfSync) OutOfSync else AlreadyIndexed
    }
}
```

## 3. UI Implementation

### **Settings Integration**
1.  **Location**: Add a "Scan for existing files" button in `OfflineDownloadsContent.kt`, just below the "Download Directory" configuration.
2.  **Trigger**: Clicking the button opens a confirmation dialog explaining the process (requires an active server connection).

### **Scan Report UI**
1.  **Progress**: A dialog with a linear progress bar and a "Cancel" button.
2.  **Final Report**: 
    - **Summary Stats**: Counts for each status (Imported, Out of Sync, etc.).
    - **Actionable List**: A scrollable list of files with status icons:
        - ✅ (Green): Successfully imported or already indexed.
        - ⚠️ (Yellow): Out of sync (Size/Date mismatch).
        - ❌ (Red): No match found on server or corrupted.
    - **Persistence**: Results are shown even if the scan was cancelled midway.

## 4. Implementation Steps

### **Phase 1: Domain Logic (`komelia-domain/offline`)**
1.  Define `OfflineScanResult` data class (Success, Failure, AlreadyExists).
2.  Implement `OfflineScannerService` in `snd.komelia.offline.sync`.
3.  Register the service in `OfflineModule`.

### **Phase 2: ViewModel and State (`komelia-ui`)**
1.  Add `onScanClick()` and `scanState` to `OfflineSettingsViewModel`.
2.  `scanState` will track: `Idle`, `Scanning(progress, currentFile)`, `Finished(report)`.
3.  Inject `OfflineScannerService` into `OfflineSettingsViewModel` via `ViewModelFactory`.

### **Phase 3: UI Components (`komelia-ui`)**
1.  Update `OfflineDownloadsContent` to include the scan button.
2.  Create `OfflineScanDialog` to show progress and the final report.

## 5. Technical Considerations
- **Concurrency**: The scan should run on a background thread and be cancellable.
- **Server Load**: Implement a small delay or batch requests to avoid overwhelming the Komga server during a large scan.
- **File Validation**: Compare `fileSize` from the server with the local `PlatformFile` size before importing to ensure the file is complete.
- **Duplicate Prevention**: Skip files that already have an entry in `OfflineBookRepository` matching the same path.
