package snd.komelia.offline

import io.github.vinceglb.filekit.PlatformFile

internal expect fun PlatformFile.localFilePath(): String?

internal expect suspend fun PlatformFile.readChunked(chunkSize: Int, onChunk: suspend (ByteArray) -> Unit)
