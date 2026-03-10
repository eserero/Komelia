package snd.komelia.image

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ImageInfo
import snd.komelia.image.SkiaBitmap.toSkiaBitmap

actual suspend fun KomeliaImage.toImageBitmap(): ImageBitmap =
    this.toSkiaBitmap().asComposeImageBitmap()

actual fun ByteArray.toImageBitmap(width: Int, height: Int): ImageBitmap {
    val bitmap = Bitmap()
    bitmap.allocPixels(ImageInfo.makeS32(width, height, ColorAlphaType.PREMUL))
    bitmap.installPixels(this)
    return bitmap.asComposeImageBitmap()
}