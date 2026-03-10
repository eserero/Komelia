package snd.komelia.ui.settings.imagereader.ncnn

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import snd.komelia.image.UpscaleStatus
import snd.komelia.settings.ImageReaderSettingsRepository
import snd.komelia.settings.model.NcnnUpscalerSettings
import snd.komelia.updates.OnnxModelDownloader
import snd.komelia.updates.UpdateProgress

expect fun isNcnnSupported(): Boolean
expect fun globalNcnnUpscaleActivities(): StateFlow<Map<Int, UpscaleStatus>>
expect fun isNcnnModelsDownloaded(): Flow<Boolean>

class NcnnSettingsState(
    private val onnxModelDownloader: OnnxModelDownloader?,
    private val settingsRepository: ImageReaderSettingsRepository,
    private val coroutineScope: CoroutineScope,
) {
    val ncnnUpscalerSettings = combine(
        settingsRepository.getNcnnUpscalerSettings(),
        isNcnnModelsDownloaded()
    ) { settings, isDownloaded ->
        settings.copy(isDownloaded = isDownloaded)
    }.stateIn(coroutineScope, SharingStarted.Eagerly, NcnnUpscalerSettings())

    val globalUpscaleActivities = globalNcnnUpscaleActivities()

    suspend fun initialize() {
        // ...
    }

    fun onSettingsChange(settings: NcnnUpscalerSettings) {
        coroutineScope.launch { settingsRepository.putNcnnUpscalerSettings(settings) }
    }

    fun onNcnnDownloadRequest(): Flow<UpdateProgress> {
        checkNotNull(onnxModelDownloader) { "onnx model downloader is not initialized" }
        return onnxModelDownloader.ncnnDownload(ncnnUpscalerSettings.value.ncnnUpscalerUrl)
    }
}
