package snd.komelia.ui.settings.imagereader.ncnn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import snd.komelia.settings.model.NcnnEngine
import snd.komelia.settings.model.NcnnUpscalerSettings
import snd.komelia.ui.LocalStrings
import snd.komelia.ui.common.components.DropdownChoiceMenu
import snd.komelia.ui.common.components.LabeledEntry
import snd.komelia.ui.common.components.NumberField
import snd.komelia.ui.common.components.SwitchWithLabel

@Composable
expect fun NcnnLogViewerDialog(onDismiss: () -> Unit)

@Composable
expect fun NcnnCrashLogViewerDialog(onDismiss: () -> Unit)

@Composable
fun NcnnSettingsContent(
    settings: NcnnUpscalerSettings,
    onSettingsChange: (NcnnUpscalerSettings) -> Unit,
) {
    val strings = LocalStrings.current.imageSettings
    var showLogs by remember { mutableStateOf(false) }
    var showCrashLogs by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SwitchWithLabel(
                checked = settings.enabled,
                onCheckedChange = { onSettingsChange(settings.copy(enabled = it)) },
                label = { Text("Enable NCNN upscaler (Mobile only)") },
                modifier = Modifier.weight(1f)
            )

            if (isNcnnSupported()) {
                TextButton(onClick = { showLogs = true }) {
                    Text("View Logs")
                }
                TextButton(onClick = { showCrashLogs = true }) {
                    Text("Crash Logs")
                }
            }
        }

        if (showLogs) {
            NcnnLogViewerDialog(onDismiss = { showLogs = false })
        }
        if (showCrashLogs) {
            NcnnCrashLogViewerDialog(onDismiss = { showCrashLogs = false })
        }

        if (settings.enabled) {
            DropdownChoiceMenu(
                selectedOption = when (settings.engine) {
                    NcnnEngine.WAIFU2X -> LabeledEntry(NcnnEngine.WAIFU2X, strings.ncnnUpscaleModeWaifu2x)
                    NcnnEngine.REALCUGAN -> LabeledEntry(NcnnEngine.REALCUGAN, strings.ncnnUpscaleModeRealCugan)
                    NcnnEngine.REALSR -> LabeledEntry(NcnnEngine.REALSR, strings.ncnnUpscaleModeRealSr)
                    NcnnEngine.REAL_ESRGAN -> LabeledEntry(NcnnEngine.REAL_ESRGAN, strings.ncnnUpscaleModeRealEsrgan)
                },
                options = remember {
                    listOf(
                        LabeledEntry(NcnnEngine.WAIFU2X, strings.ncnnUpscaleModeWaifu2x),
                        LabeledEntry(NcnnEngine.REALCUGAN, strings.ncnnUpscaleModeRealCugan),
                        LabeledEntry(NcnnEngine.REALSR, strings.ncnnUpscaleModeRealSr),
                        LabeledEntry(NcnnEngine.REAL_ESRGAN, strings.ncnnUpscaleModeRealEsrgan),
                    )
                },
                onOptionChange = { onSettingsChange(settings.copy(engine = it.value, model = getDefaultModelForEngine(it.value))) },
                label = { Text("Engine") },
                inputFieldModifier = Modifier.fillMaxSize()
            )

            val models = when (settings.engine) {
                NcnnEngine.WAIFU2X -> ncnnWaifu2xModels
                NcnnEngine.REALCUGAN -> ncnnRealCuganModels
                NcnnEngine.REALSR -> ncnnRealSrModels
                NcnnEngine.REAL_ESRGAN -> ncnnRealEsrganModels
            }

            DropdownChoiceMenu(
                selectedOption = LabeledEntry(settings.model, settings.model),
                options = remember(settings.engine) {
                    models.map { LabeledEntry(it, it) }
                },
                onOptionChange = { onSettingsChange(settings.copy(model = it.value)) },
                label = { Text("Model") },
                inputFieldModifier = Modifier.fillMaxSize()
            )

            SwitchWithLabel(
                checked = settings.upscaleOnLoad,
                onCheckedChange = { onSettingsChange(settings.copy(upscaleOnLoad = it)) },
                label = { Text(strings.ncnnUpscaleOnLoad) },
                supportingText = { Text(strings.ncnnUpscaleOnLoadTooltip) }
            )

            if (settings.upscaleOnLoad) {
                NumberField(
                    value = settings.upscaleThreshold,
                    onValueChange = { onSettingsChange(settings.copy(upscaleThreshold = it ?: 1200)) },
                    label = { Text(strings.ncnnUpscaleOnLoadThreshold) },
                )
            }

            // Advanced settings
            SwitchWithLabel(
                checked = settings.ttaMode,
                onCheckedChange = { onSettingsChange(settings.copy(ttaMode = it)) },
                label = { Text("TTA Mode") },
                supportingText = { Text("Test-Time Augmentation, slower but higher quality") }
            )
        }
    }
}

val ncnnWaifu2xModels = listOf(
    "models-cunet/noise0_scale2.0x_model",
    "models-cunet/noise1_scale2.0x_model",
    "models-cunet/noise2_scale2.0x_model",
    "models-cunet/noise3_scale2.0x_model",
    "models-cunet/scale2.0x_model",
    "models-upconv_7_anime_style_art_rgb/noise0_scale2.0x_model",
    "models-upconv_7_anime_style_art_rgb/noise1_scale2.0x_model",
    "models-upconv_7_anime_style_art_rgb/noise2_scale2.0x_model",
    "models-upconv_7_anime_style_art_rgb/noise3_scale2.0x_model",
    "models-upconv_7_anime_style_art_rgb/scale2.0x_model",
)
val ncnnRealCuganModels = listOf(
    "models-realcugan/up2x-conservative"
)
val ncnnRealSrModels = listOf(
    "models-realsr"
)
val ncnnRealEsrganModels = listOf(
    "models-realesrgan"
)

private fun getDefaultModelForEngine(engine: NcnnEngine): String {
    return when (engine) {
        NcnnEngine.WAIFU2X -> ncnnWaifu2xModels.first()
        NcnnEngine.REALCUGAN -> ncnnRealCuganModels.first()
        NcnnEngine.REALSR -> ncnnRealSrModels.first()
        NcnnEngine.REAL_ESRGAN -> ncnnRealEsrganModels.first()
    }
}
