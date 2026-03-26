package snd.komelia.offline.sync

import io.github.vinceglb.filekit.PlatformFile
import kotlin.time.Instant

class JvmOfflineFileSystem : OfflineFileSystem {
    override fun listDirectories(root: PlatformFile): List<PlatformFile> {
        val file = root.file
        return file.listFiles { f -> f.isDirectory }?.map { PlatformFile(it) } ?: emptyList()
    }

    override fun listFiles(root: PlatformFile): List<PlatformFile> {
        val file = root.file
        return file.listFiles { f -> f.isFile }?.map { PlatformFile(it) } ?: emptyList()
    }

    override fun getFileSize(file: PlatformFile): Long {
        return file.file.length()
    }

    override fun getLastModified(file: PlatformFile): Instant {
        return Instant.fromEpochMilliseconds(file.file.lastModified())
    }

    override fun getName(file: PlatformFile): String {
        return file.file.name
    }
}

actual fun createOfflineFileSystem(): OfflineFileSystem = JvmOfflineFileSystem()
