package snd.komelia.offline

import io.github.vinceglb.filekit.PlatformFile

expect fun PlatformFile.localFilePath(): String?

expect suspend fun PlatformFile.readChunked(chunkSize: Int, onChunk: suspend (ByteArray) -> Unit)
