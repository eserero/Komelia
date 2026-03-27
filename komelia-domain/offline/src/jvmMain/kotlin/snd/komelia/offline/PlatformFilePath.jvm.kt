package snd.komelia.offline

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import java.io.File

internal actual fun PlatformFile.localFilePath(): String? = this.path

internal actual suspend fun PlatformFile.readChunked(chunkSize: Int, onChunk: suspend (ByteArray) -> Unit) {
    File(this.path).inputStream().use { stream ->
        val buffer = ByteArray(chunkSize)
        var n: Int
        while (stream.read(buffer).also { n = it } != -1) {
            onChunk(if (n < chunkSize) buffer.copyOf(n) else buffer)
        }
    }
}
