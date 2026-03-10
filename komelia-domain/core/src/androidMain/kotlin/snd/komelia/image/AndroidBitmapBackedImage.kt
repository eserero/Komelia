package snd.komelia.image

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer

class AndroidBitmapBackedImage(val bitmap: Bitmap) : KomeliaImage {
    override val width: Int = bitmap.width
    override val height: Int = bitmap.height
    override val bands: Int = 4 // Bitmaps are usually ARGB_8888
    override val type: ImageFormat = ImageFormat.RGBA_8888

    override val pagesLoaded: Int = 1
    override val pagesTotal: Int = 1
    override val pageHeight: Int = bitmap.height
    override val pageDelays: IntArray? = null

    override suspend fun extractArea(rect: ImageRect): KomeliaImage {
        return withContext(Dispatchers.Default) {
            val area = Bitmap.createBitmap(
                bitmap,
                rect.left,
                rect.top,
                rect.width,
                rect.height
            )
            AndroidBitmapBackedImage(area)
        }
    }

    override suspend fun resize(
        scaleWidth: Int,
        scaleHeight: Int,
        linear: Boolean,
        kernel: ReduceKernel
    ): KomeliaImage {
        return withContext(Dispatchers.Default) {
            val resized = Bitmap.createScaledBitmap(bitmap, scaleWidth, scaleHeight, linear)
            AndroidBitmapBackedImage(resized)
        }
    }

    override suspend fun shrink(factor: Double): KomeliaImage {
        return resize((width / factor).toInt(), (height / factor).toInt(), true, ReduceKernel.LINEAR)
    }

    override suspend fun findTrim(): ImageRect {
        // Minimal implementation, could be optimized
        return ImageRect(0, 0, width, height)
    }

    override suspend fun makeHistogram(): KomeliaImage {
        throw UnsupportedOperationException("Not implemented for Bitmap")
    }

    override suspend fun mapLookupTable(table: ByteArray): KomeliaImage {
        throw UnsupportedOperationException("Not implemented for Bitmap")
    }

    override suspend fun getBytes(): ByteArray {
        return withContext(Dispatchers.Default) {
            val byteBuffer = ByteBuffer.allocate(bitmap.byteCount)
            bitmap.copyPixelsToBuffer(byteBuffer)
            byteBuffer.array()
        }
    }

    override suspend fun averageColor(): Int? {
        return withContext(Dispatchers.Default) {
            val resized = Bitmap.createScaledBitmap(bitmap, 1, 1, true)
            val color = resized.getPixel(0, 0)
            resized.recycle()
            color
        }
    }

    override fun close() {
        bitmap.recycle()
    }
}
