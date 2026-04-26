package snd.komelia.image

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import io.github.hzkitty.RapidOCR
import kotlinx.coroutines.tasks.await
import snd.komelia.image.AndroidBitmap.toBitmap
import androidx.compose.ui.geometry.Rect
import snd.komelia.settings.model.OcrEngine
import snd.komelia.settings.model.OcrLanguage
import snd.komelia.settings.model.OcrSettings

actual class OcrService {
    private val latinRecognizer by lazy { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    private val chineseRecognizer by lazy { TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build()) }
    private val devanagariRecognizer by lazy { TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build()) }
    private val japaneseRecognizer by lazy { TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build()) }
    private val koreanRecognizer by lazy { TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build()) }

    private val rapidOcr by lazy { RapidOCR.create(context) }

    actual suspend fun recognizeText(image: ReaderImage, settings: OcrSettings): List<OcrElementBox> {
        val komeliaImage = image.getOriginalImage().getOrNull() ?: return emptyList()
        val bitmap = when (komeliaImage) {
            is AndroidBitmapBackedImage -> komeliaImage.bitmap
            else -> komeliaImage.toBitmap()
        }

        return when (settings.engine) {
            OcrEngine.ML_KIT -> recognizeWithMlKit(bitmap, settings.selectedLanguage)
            OcrEngine.RAPID_OCR -> {
                val softwareBitmap = if (bitmap.config == Bitmap.Config.HARDWARE) {
                    bitmap.copy(Bitmap.Config.ARGB_8888, false)
                } else bitmap
                recognizeWithRapidOcr(softwareBitmap)
            }
        }
    }

    private suspend fun recognizeWithMlKit(bitmap: android.graphics.Bitmap, language: OcrLanguage): List<OcrElementBox> {
        val recognizer = when (language) {
            OcrLanguage.LATIN -> latinRecognizer
            OcrLanguage.CHINESE -> chineseRecognizer
            OcrLanguage.DEVANAGARI -> devanagariRecognizer
            OcrLanguage.JAPANESE -> japaneseRecognizer
            OcrLanguage.KOREAN -> koreanRecognizer
        }

        val inputImage = InputImage.fromBitmap(bitmap, 0)
        val result = recognizer.process(inputImage).await()

        val boxes = mutableListOf<OcrElementBox>()
        result.textBlocks.forEachIndexed { blockIdx, block ->
            val blockBoundingBox = block.boundingBox ?: return@forEachIndexed
            val blockRect = Rect(
                left = blockBoundingBox.left.toFloat(),
                top = blockBoundingBox.top.toFloat(),
                right = blockBoundingBox.right.toFloat(),
                bottom = blockBoundingBox.bottom.toFloat()
            )
            block.lines.forEachIndexed { lineIdx, line ->
                line.elements.forEachIndexed { elementIdx, element ->
                    val rect = element.boundingBox ?: return@forEachIndexed
                    boxes.add(
                        OcrElementBox(
                            text = element.text,
                            imageRect = Rect(
                                left = rect.left.toFloat(),
                                top = rect.top.toFloat(),
                                right = rect.right.toFloat(),
                                bottom = rect.bottom.toFloat()
                            ),
                            blockRect = blockRect,
                            blockIndex = blockIdx,
                            lineIndex = lineIdx,
                            elementIndex = elementIdx
                        )
                    )
                }
            }
        }
        return boxes
    }

    private fun recognizeWithRapidOcr(bitmap: android.graphics.Bitmap): List<OcrElementBox> {
        val result = rapidOcr.run(bitmap)
        val boxes = mutableListOf<OcrElementBox>()

        result.recRes.forEachIndexed { index, recResult ->
            val points = recResult.dtBoxes
            if (points == null || points.size < 4) return@forEachIndexed

            val xCoords = points.map { it.x }
            val yCoords = points.map { it.y }
            val rect = Rect(
                left = xCoords.min().toFloat(),
                top = yCoords.min().toFloat(),
                right = xCoords.max().toFloat(),
                bottom = yCoords.max().toFloat()
            )

            boxes.add(
                OcrElementBox(
                    text = recResult.text,
                    imageRect = rect,
                    blockRect = rect,
                    blockIndex = index,
                    lineIndex = 0,
                    elementIndex = 0
                )
            )
        }
        return boxes
    }

    companion object {
        lateinit var context: Context
    }
}
