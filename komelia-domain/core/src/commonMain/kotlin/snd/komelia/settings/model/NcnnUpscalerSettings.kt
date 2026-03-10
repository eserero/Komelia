package snd.komelia.settings.model

import kotlinx.serialization.Serializable

@Serializable
data class NcnnUpscalerSettings(
    val engine: NcnnEngine = NcnnEngine.WAIFU2X,
    val model: String = "models-cunet/scale2.0x_model",
    val enabled: Boolean = false,
    val gpuId: Int = 0,
    val ttaMode: Boolean = false,
    val numThreads: Int = 4,
    val upscaleOnLoad: Boolean = false,
    val upscaleThreshold: Int = 1200,
    val ncnnUpscalerUrl: String = "https://github.com/eserero/Komelia/releases/download/model/NcnnUpscalerModels.zip",
    val isDownloaded: Boolean = false,
)

@Serializable
enum class NcnnEngine {
    WAIFU2X,
    REALCUGAN,
    REALSR,
    REAL_ESRGAN
}
