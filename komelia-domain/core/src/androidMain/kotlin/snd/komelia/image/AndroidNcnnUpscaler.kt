package snd.komelia.image

import android.content.Context
import android.graphics.Bitmap
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.infra.ncnn.NcnnSharedLibraries
import io.github.snd_r.komelia.infra.ncnn.NcnnUpscaler
import io.github.snd_r.komelia.infra.ncnn.NcnnUpscaler.Companion.ENGINE_REALCUGAN
import io.github.snd_r.komelia.infra.ncnn.NcnnUpscaler.Companion.ENGINE_REALSR
import io.github.snd_r.komelia.infra.ncnn.NcnnUpscaler.Companion.ENGINE_REAL_ESRGAN
import io.github.snd_r.komelia.infra.ncnn.NcnnUpscaler.Companion.ENGINE_WAIFU2X
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import snd.komelia.image.AndroidBitmap.toSoftwareBitmap
import snd.komelia.settings.ImageReaderSettingsRepository
import snd.komelia.settings.model.NcnnEngine
import snd.komelia.settings.model.NcnnUpscalerSettings

private val logger = KotlinLogging.logger {}

class AndroidNcnnUpscaler(
    private val context: Context,
    private val settingsRepository: ImageReaderSettingsRepository,
) : AutoCloseable {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var ncnn: NcnnUpscaler? = null
    private var currentSettings: NcnnUpscalerSettings? = null
    private var gpuInstanceCreated = false
    val isReady = MutableStateFlow(false)

    companion object {
        private val mutex = Mutex()
    }

    fun initialize() {
        if (!NcnnSharedLibraries.isAvailable) {
            logger.warn { "NCNN shared libraries are not available. Upscaler will be disabled." }
            return
        }

        scope.launch {
            mutex.withLock {
                if (!gpuInstanceCreated) {
                    val result = NcnnUpscaler().createGpuInstance()
                    if (result == 0) {
                        gpuInstanceCreated = true
                        logger.info { "NCNN GPU instance created" }
                    } else {
                        logger.error { "Failed to create NCNN GPU instance: $result" }
                    }
                }
            }
        }

        settingsRepository.getNcnnUpscalerSettings()
            .onEach { settings ->
                try {
                    mutex.withLock {
                        if (settings.enabled) {
                            if (ncnn == null || shouldReinit(settings)) {
                                reinit(settings)
                            }
                        } else {
                            ncnn?.release()
                            ncnn = null
                            currentSettings = null
                            isReady.value = false
                        }
                    }
                } catch (e: Throwable) {
                    logger.error(e) { "Failed to initialize NCNN upscaler" }
                }
            }.launchIn(scope)
    }

    suspend fun upscale(image: KomeliaImage): KomeliaImage? {
        val bitmapIn = when (image) {
            is AndroidBitmapBackedImage -> {
                if (image.bitmap.config == Bitmap.Config.HARDWARE) {
                    image.bitmap.copy(Bitmap.Config.ARGB_8888, false)
                } else {
                    image.bitmap
                }
            }

            is VipsBackedImage -> image.vipsImage.toSoftwareBitmap()
            else -> return null
        }

        return try {
            val scale = parseModelScaleAndNoise(currentSettings?.model ?: "").first
            val bitmapOut = Bitmap.createBitmap(
                bitmapIn.width * scale,
                bitmapIn.height * scale,
                Bitmap.Config.ARGB_8888
            )

            val result = mutex.withLock {
                val upscaler = ncnn ?: return@withLock -1
                upscaler.process(bitmapIn, bitmapOut)
            }

            if (result != 0) {
                logger.error { "NCNN upscaling failed with code $result" }
                bitmapOut.recycle()
                null
            } else {
                AndroidBitmapBackedImage(bitmapOut)
            }
        } catch (e: Throwable) {
            logger.error(e) { "Failed to upscale image" }
            null
        } finally {
            if (image !is AndroidBitmapBackedImage) {
                bitmapIn.recycle()
            } else if (bitmapIn != image.bitmap) {
                bitmapIn.recycle()
            }
        }
    }

    suspend fun checkAndUpscale(image: KomeliaImage): KomeliaImage {
        val settings = mutex.withLock { currentSettings } ?: return image
        if (settings.enabled && settings.upscaleOnLoad && image.width < settings.upscaleThreshold) {
            logger.info { "[NCNN] Pre-emptive upscale triggered: ${image.width}px < ${settings.upscaleThreshold}px" }
            return upscale(image) ?: image
        }
        return image
    }

    private fun shouldReinit(newSettings: NcnnUpscalerSettings): Boolean {
        val current = currentSettings ?: return true
        return current.engine != newSettings.engine ||
                current.model != newSettings.model ||
                current.gpuId != newSettings.gpuId ||
                current.ttaMode != newSettings.ttaMode ||
                current.numThreads != newSettings.numThreads
    }

    private fun parseModelScaleAndNoise(modelPath: String): Pair<Int, Int> {
        val modelName = modelPath.substringAfterLast("/")
        return when {
            modelName == "scale2.0x_model" -> Pair(2, -1)
            modelName.startsWith("noise") && modelName.contains("scale2.0x") -> {
                val noise = modelName.removePrefix("noise").substringBefore("_").toIntOrNull() ?: 0
                Pair(2, noise)
            }
            modelName.startsWith("noise") -> {
                val noise = modelName.removePrefix("noise").substringBefore("_").toIntOrNull() ?: 0
                Pair(1, noise)
            }
            modelName.contains("up2x") -> Pair(2, 0)
            modelName.contains("realsr") -> Pair(4, -1)
            modelName.contains("x2") -> Pair(2, -1)
            modelName.contains("x4") -> Pair(4, -1)
            else -> Pair(2, -1)
        }
    }

    private fun reinit(settings: NcnnUpscalerSettings) {
        ncnn?.release()
        val newNcnn = NcnnUpscaler()
        val engineType = when (settings.engine) {
            NcnnEngine.WAIFU2X -> ENGINE_WAIFU2X
            NcnnEngine.REALCUGAN -> ENGINE_REALCUGAN
            NcnnEngine.REALSR -> ENGINE_REALSR
            NcnnEngine.REAL_ESRGAN -> ENGINE_REAL_ESRGAN
        }
        newNcnn.init(engineType, settings.gpuId, settings.ttaMode, settings.numThreads)

        val (scale, noise) = parseModelScaleAndNoise(settings.model)
        newNcnn.setScale(scale)
        newNcnn.setNoise(noise)

        val modelPath = settings.model
        val paramPath: String
        val binPath: String

        if (settings.engine == NcnnEngine.REALSR || settings.engine == NcnnEngine.REAL_ESRGAN) {
            val scale = parseModelScaleAndNoise(modelPath).first
            paramPath = "$modelPath/x$scale.param"
            binPath = "$modelPath/x$scale.bin"
        } else {
            paramPath = "$modelPath.param"
            binPath = "$modelPath.bin"
        }

        val loadResult = newNcnn.load(context.assets, paramPath, binPath)
        if (loadResult != 0) {
            logger.error { "Failed to load NCNN model $modelPath: $loadResult" }
            newNcnn.release()
            ncnn = null
            currentSettings = null
        } else {
            ncnn = newNcnn
            currentSettings = settings
            isReady.value = true
        }
    }

    override fun close() {
        ncnn?.release()
        ncnn = null
        if (gpuInstanceCreated) {
            NcnnUpscaler().destroyGpuInstance()
            gpuInstanceCreated = false
        }
    }
}
