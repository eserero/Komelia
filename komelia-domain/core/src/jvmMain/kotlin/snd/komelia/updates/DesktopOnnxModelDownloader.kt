package snd.komelia.updates

import io.ktor.client.statement.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.io.readByteArray
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.io.IOUtils
import snd.komelia.AppDirectories.mangaJaNaiInstallPath
import snd.komelia.AppDirectories.panelDetectionInstallPath
import snd.komelia.AppNotifications
import snd.komelia.updates.OnnxModelDownloader.CompletionEvent
import snd.komelia.updates.OnnxModelDownloader.CompletionEvent.*
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.*

private const val mangaJaNaiDownloadLink =
    "https://github.com/Snd-R/mangajanai/releases/download/1.0.0/MangaJaNaiOnnxModels.zip"

class DesktopOnnxModelDownloader(
    private val updateClient: UpdateClient,
    private val appNotifications: AppNotifications
) : OnnxModelDownloader {
    override val downloadCompletionEvents = MutableSharedFlow<CompletionEvent>()

    override fun mangaJaNaiDownload(): Flow<UpdateProgress> {
        return flow {
            if (mangaJaNaiInstallPath.notExists()) {
                mangaJaNaiInstallPath.createDirectories()
            }

            emit(UpdateProgress(0, 0, mangaJaNaiDownloadLink))
            val archiveFile = createTempFile("MangaJaNaiOnnxModels.zip")
            archiveFile.toFile().deleteOnExit()

            appNotifications.runCatchingToNotifications {
                downloadFile(mangaJaNaiDownloadLink, archiveFile)
                emit(UpdateProgress(0, 0))
                extractZipArchive(from = archiveFile, to = mangaJaNaiInstallPath)
                archiveFile.deleteIfExists()
                downloadCompletionEvents.emit(MangaJaNaiDownloaded)
            }.onFailure { archiveFile.deleteIfExists() }
        }
    }

    override fun panelDownload(url: String): Flow<UpdateProgress> {
        return flow {
            if (panelDetectionInstallPath.notExists()) {
                panelDetectionInstallPath.createDirectories()
            }

            emit(UpdateProgress(0, 0, url))
            val archiveFile = createTempFile("rf-detr-med.onnx.zip")
            archiveFile.toFile().deleteOnExit()

            appNotifications.runCatchingToNotifications {
                downloadFile(url, archiveFile)
                emit(UpdateProgress(0, 0))
                extractZipArchive(archiveFile, panelDetectionInstallPath)
                archiveFile.deleteIfExists()
                downloadCompletionEvents.emit(PanelModelDownloaded)
            }.onFailure { archiveFile.deleteIfExists() }
        }
    }

    override fun ncnnDownload(url: String): Flow<UpdateProgress> {
        return flow {
            // NCNN upscaler is currently Android-only in this project
            // but we implement this to satisfy the interface.
            val ncnnInstallPath = Path(System.getProperty("user.home")).resolve(".komelia/ncnn_models")
            if (ncnnInstallPath.notExists()) {
                ncnnInstallPath.createDirectories()
            }

            emit(UpdateProgress(0, 0, url))
            val archiveFile = createTempFile("NcnnUpscalerModels.zip")
            archiveFile.toFile().deleteOnExit()

            appNotifications.runCatchingToNotifications {
                downloadFile(url, archiveFile)
                emit(UpdateProgress(0, 0))
                extractZipArchive(archiveFile, ncnnInstallPath)
                archiveFile.deleteIfExists()
                downloadCompletionEvents.emit(NcnnModelDownloaded)
            }.onFailure { archiveFile.deleteIfExists() }
        }
    }

    private suspend fun FlowCollector<UpdateProgress>.downloadFile(url: String, file: Path) {
        updateClient.streamFile(url) { response ->
            val length = response.headers["Content-Length"]?.toLong() ?: 0L
            emit(UpdateProgress(length, 0, url))
            val channel = response.bodyAsChannel().counted()

            file.outputStream().buffered().use { outputStream ->
                while (!channel.isClosedForRead) {
                    val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                    while (!packet.exhausted()) {
                        val bytes = packet.readByteArray()
                        outputStream.write(bytes)
                    }
                    outputStream.flush()
                    emit(UpdateProgress(length, channel.totalBytesRead, url))
                }
            }
        }
    }

    private fun extractZipArchive(from: Path, to: Path) {
        ZipArchiveInputStream(from.inputStream().buffered()).use { archiveStream ->
            var entry: ZipArchiveEntry? = archiveStream.nextEntry
            while (entry != null) {
                val targetPath = to.resolve(entry.name)
                if (entry.isDirectory) {
                    Files.createDirectories(targetPath)
                } else {
                    Files.createDirectories(targetPath.parent)
                    targetPath.outputStream()
                        .use { output -> IOUtils.copy(archiveStream, output) }
                }
                entry = archiveStream.nextEntry
            }
        }
    }
}
