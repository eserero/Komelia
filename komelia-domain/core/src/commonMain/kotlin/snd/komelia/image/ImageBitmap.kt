package snd.komelia.image

import androidx.compose.ui.graphics.ImageBitmap

expect suspend fun KomeliaImage.toImageBitmap(): ImageBitmap

expect fun ByteArray.toImageBitmap(width: Int, height: Int): ImageBitmap
