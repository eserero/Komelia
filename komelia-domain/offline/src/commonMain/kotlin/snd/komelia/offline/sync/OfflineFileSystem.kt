package snd.komelia.offline.sync

import io.github.vinceglb.filekit.PlatformFile
import kotlin.time.Instant

interface OfflineFileSystem {
    fun listDirectories(root: PlatformFile): List<PlatformFile>
    fun listFiles(root: PlatformFile): List<PlatformFile>
    fun getFileSize(file: PlatformFile): Long
    fun getLastModified(file: PlatformFile): Instant
    fun getName(file: PlatformFile): String
}

expect fun createOfflineFileSystem(): OfflineFileSystem
