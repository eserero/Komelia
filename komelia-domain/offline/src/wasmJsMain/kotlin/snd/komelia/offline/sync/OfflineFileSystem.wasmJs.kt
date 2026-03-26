package snd.komelia.offline.sync

import io.github.vinceglb.filekit.PlatformFile
import kotlin.time.Instant

class WasmOfflineFileSystem : OfflineFileSystem {
    override fun listDirectories(root: PlatformFile): List<PlatformFile> = emptyList()
    override fun listFiles(root: PlatformFile): List<PlatformFile> = emptyList()
    override fun getFileSize(file: PlatformFile): Long = 0L
    override fun getLastModified(file: PlatformFile): Instant = Instant.fromEpochMilliseconds(0L)
    override fun getName(file: PlatformFile): String = ""
}

actual fun createOfflineFileSystem(): OfflineFileSystem = WasmOfflineFileSystem()
