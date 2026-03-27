package snd.komelia.offline

import io.github.vinceglb.filekit.AndroidFile
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.context

internal actual fun PlatformFile.localFilePath(): String? =
    when (val f = this.androidFile) {
        is AndroidFile.FileWrapper -> runCatching { f.file.path }.getOrNull()
        is AndroidFile.UriWrapper -> null  // SAF URIs are not direct filesystem paths
    }

internal actual suspend fun PlatformFile.readChunked(chunkSize: Int, onChunk: suspend (ByteArray) -> Unit) {
    val stream = when (val f = this.androidFile) {
        is AndroidFile.FileWrapper -> f.file.inputStream()
        is AndroidFile.UriWrapper -> FileKit.context.contentResolver.openInputStream(f.uri)
            ?: error("Cannot open input stream for $f")
    }
    stream.use {
        val buffer = ByteArray(chunkSize)
        var n: Int
        while (it.read(buffer).also { n = it } != -1) {
            onChunk(if (n < chunkSize) buffer.copyOf(n) else buffer)
        }
    }
}
