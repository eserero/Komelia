package snd.komelia.ui.settings.offline.downloads

import coil3.PlatformContext
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import snd.komelia.AppNotifications
import snd.komelia.KomgaAuthenticationState
import snd.komelia.offline.settings.OfflineSettingsRepository
import snd.komelia.offline.sync.OfflineScannerService
import snd.komelia.offline.sync.model.DownloadEvent
import snd.komelia.offline.sync.model.DownloadEvent.BookDownloadCompleted
import snd.komelia.offline.sync.model.DownloadEvent.BookDownloadError
import snd.komelia.offline.sync.model.DownloadEvent.BookDownloadProgress
import snd.komelia.offline.sync.model.OfflineScanResult
import snd.komelia.offline.tasks.OfflineTaskEmitter
import snd.komga.client.book.KomgaBookId

class OfflineDownloadsState(
    downloadEvents: SharedFlow<DownloadEvent>,
    platformContext: PlatformContext,
    private val taskEmitter: OfflineTaskEmitter,
    private val settingsRepository: OfflineSettingsRepository,
    private val offlineScannerService: OfflineScannerService,
    private val authState: KomgaAuthenticationState,
    private val appNotifications: AppNotifications,
    private val coroutineScope: CoroutineScope,
) {
    private val internalDownloadDir = getDefaultInternalDownloadsDir(platformContext)
    private val downloadsMap = MutableStateFlow<Map<KomgaBookId, DownloadEvent>>(emptyMap())
    val downloads = downloadsMap.map { it.values }
        .stateIn(coroutineScope, SharingStarted.Eagerly, emptyList())

    val storageLocation = settingsRepository.getDownloadDirectory()
        .stateIn(coroutineScope, SharingStarted.Eagerly, null)

    val scanState = MutableStateFlow<OfflineScanState>(OfflineScanState.Idle)

    init {
        downloadEvents.onEach { event ->
            when (event) {
                is BookDownloadProgress, is BookDownloadCompleted -> updateDownloads(event)
                is BookDownloadError -> handleErrorEvent(event)
            }
        }.launchIn(coroutineScope)
    }

    private fun handleErrorEvent(event: BookDownloadError) {
        if (event.book == null) {
            val previousEvent = downloadsMap.value[event.bookId]
            val newEvent =
                if (previousEvent is BookDownloadProgress) event.copy(book = previousEvent.book)
                else event
            updateDownloads(newEvent)
        } else {
            updateDownloads(event)
        }
    }

    fun onStorageLocationChange(directory: PlatformFile) {
        coroutineScope.launch { settingsRepository.putDownloadDirectory(directory) }
    }

    fun onStorageLocationReset() {
        coroutineScope.launch { settingsRepository.putDownloadDirectory(internalDownloadDir.platformFile) }
    }

    private fun updateDownloads(event: DownloadEvent) {
        downloadsMap.update {
            val mutable = it.toMutableMap()
            mutable[event.bookId] = event
            mutable
        }
    }

    fun onDownloadCancel(bookId: KomgaBookId) {
        coroutineScope.launch { taskEmitter.cancelBookDownload(bookId) }
    }

    fun onScanClick() {
        val root = storageLocation.value ?: return
        val user = authState.authenticatedUser.value ?: return

        coroutineScope.launch {
            scanState.value = OfflineScanState.Scanning(0, null)
            val results = mutableListOf<OfflineScanResult>()
            try {
                offlineScannerService.scan(root, user).collect { result ->
                    results.add(result)
                    scanState.value = OfflineScanState.Scanning(results.size, result)
                }
            } catch (e: Exception) {
                appNotifications.addErrorNotification(e)
            } finally {
                scanState.value = OfflineScanState.Finished(OfflineScanReport(results))
            }
        }
    }

    fun onScanDialogClose() {
        scanState.value = OfflineScanState.Idle
    }
}

sealed interface OfflineScanState {
    data object Idle : OfflineScanState
    data class Scanning(val count: Int, val lastResult: OfflineScanResult?) : OfflineScanState
    data class Finished(val report: OfflineScanReport) : OfflineScanState
}

data class OfflineScanReport(
    val results: List<OfflineScanResult>
) {
    val importedCount = results.count { it is OfflineScanResult.Imported }
    val updatedCount = results.count { it is OfflineScanResult.Updated }
    val outOfSyncCount = results.count { it is OfflineScanResult.OutOfSync }
    val alreadyIndexedCount = results.count { it is OfflineScanResult.AlreadyIndexed }
    val noMatchCount = results.count { it is OfflineScanResult.NoMatch }
}

internal data class DefaultDownloadStorageLocation(
    val platformFile: PlatformFile,
    val label: String,
)

internal expect fun getDefaultInternalDownloadsDir(platformContent: PlatformContext): DefaultDownloadStorageLocation
