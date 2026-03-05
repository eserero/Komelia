package snd.komelia.ui.settings.imagereader.ncnn

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import snd.komelia.settings.ImageReaderSettingsRepository
import snd.komelia.settings.model.NcnnUpscalerSettings

expect fun isNcnnSupported(): Boolean

class NcnnSettingsState(
    private val settingsRepository: ImageReaderSettingsRepository,
    private val coroutineScope: CoroutineScope,
) {
    val ncnnUpscalerSettings = settingsRepository.getNcnnUpscalerSettings()
        .stateIn(coroutineScope, SharingStarted.Eagerly, NcnnUpscalerSettings())

    suspend fun initialize() {
        // ...
    }

    fun onSettingsChange(settings: NcnnUpscalerSettings) {
        coroutineScope.launch { settingsRepository.putNcnnUpscalerSettings(settings) }
    }
}
