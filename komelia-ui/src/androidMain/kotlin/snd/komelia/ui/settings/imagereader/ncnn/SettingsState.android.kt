package snd.komelia.ui.settings.imagereader.ncnn

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import snd.komelia.image.AndroidNcnnUpscaler
import snd.komelia.image.UpscaleStatus

actual fun isNcnnSupported() = true
actual fun globalNcnnUpscaleActivities(): StateFlow<Map<Int, UpscaleStatus>> =
    AndroidNcnnUpscaler.globalUpscaleActivities

actual fun isNcnnModelsDownloaded(): Flow<Boolean> =
// This is a bit of a hack since we don't have easy access to the singleton instance here 
// without passing it through many layers. 
// But in this app, we can probably use the static access if we add it to AndroidNcnnUpscaler,
// or just return a flow that checks periodically.
// Actually, AndroidNcnnUpscaler.isDownloaded is NOT static.
// Let's make it static or provide a way to access it.
    AndroidNcnnUpscaler.isDownloadedFlow
