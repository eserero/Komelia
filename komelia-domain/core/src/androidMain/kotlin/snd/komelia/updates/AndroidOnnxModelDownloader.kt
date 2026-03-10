package snd.komelia.updates

import io.ktor.client.statement.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.io.readByteArray
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.io.IOUtils
import snd.komelia.AppNotifications
import snd.komelia.updates.OnnxModelDownloader.CompletionEvent
import snd.komelia.updates.OnnxModelDownloader.CompletionEvent.NcnnModelDownloaded
import snd.komelia.updates.OnnxModelDownloader.CompletionEvent.PanelModelDownloaded
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.*

class AndroidOnnxModelDownloader(
    private val updateClient: UpdateClient,
    private val appNotifications: AppNotifications,
    private val dataDir: Path,
) : OnnxModelDownloader {
    override val downloadCompletionEvents = MutableSharedFlow<CompletionEvent>()

    override fun mangaJaNaiDownload(): Flow<UpdateProgress> {
        return emptyFlow()
    }

    override fun panelDownload(url: String): Flow<UpdateProgress> {
        return flow {
            emit(UpdateProgress(0, 0, url))
            val archiveFile = createTempFile("rf-detr-med.onnx.zip")
            archiveFile.toFile().deleteOnExit()

            appNotifications.runCatchingToNotifications {
                downloadFile(url, archiveFile)
                emit(UpdateProgress(0, 0))
                val targetDir = dataDir.resolve("onnx")
                if (targetDir.notExists()) targetDir.createDirectories()
                extractZipArchive(archiveFile, targetDir)
                archiveFile.deleteIfExists()
                downloadCompletionEvents.emit(PanelModelDownloaded)
            }.onFailure { archiveFile.deleteIfExists() }
        }.flowOn(Dispatchers.IO)
    }

    override fun ncnnDownload(url: String): Flow<UpdateProgress> {
        return flow {
            emit(UpdateProgress(0, 0, url))
            val archiveFile = createTempFile("NcnnUpscalerModels.zip")
            archiveFile.toFile().deleteOnExit()

            appNotifications.runCatchingToNotifications {
                downloadFile(url, archiveFile)
                emit(UpdateProgress(0, 0))
                val targetDir = dataDir.resolve("ncnn_models")
                if (targetDir.exists()) targetDir.toFile().deleteRecursively()
                targetDir.createDirectories()
                extractZipArchive(archiveFile, targetDir)
                archiveFile.deleteIfExists()
                downloadCompletionEvents.emit(NcnnModelDownloaded)
            }.onFailure { archiveFile.deleteIfExists() }
        }.flowOn(Dispatchers.IO)
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