package snd.komelia.image

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import snd.komelia.image.AndroidBitmap.toBitmap
import java.nio.ByteBuffer

actual suspend fun KomeliaImage.toImageBitmap(): ImageBitmap {
    return this.toBitmap().asImageBitmap()
}

actual fun ByteArray.toImageBitmap(width: Int, height: Int): ImageBitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(this))
    return bitmap.asImageBitmap()
}