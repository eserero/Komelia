package snd.komelia.offline

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes

actual fun PlatformFile.localFilePath(): String? = null

actual suspend fun PlatformFile.readChunked(chunkSize: Int, onChunk: suspend (ByteArray) -> Unit) {
    val bytes = this.readBytes()
    var offset = 0
    while (offset < bytes.size) {
        val end = minOf(offset + chunkSize, bytes.size)
        onChunk(bytes.copyOfRange(offset, end))
        offset = end
    }
}
