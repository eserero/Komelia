package snd.komelia.offline.sync

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Instant
import snd.komelia.offline.book.actions.BookKomgaImportAction
import snd.komelia.offline.book.repository.OfflineBookRepository
import snd.komelia.offline.library.actions.LibraryKomgaImportAction
import snd.komelia.offline.library.repository.OfflineLibraryRepository
import snd.komelia.offline.series.actions.SeriesKomgaImportAction
import snd.komelia.offline.series.repository.OfflineSeriesRepository
import snd.komelia.offline.server.repository.OfflineMediaServerRepository
import snd.komelia.offline.server.actions.MediaServerSaveAction
import snd.komelia.offline.sync.model.OfflineScanResult
import snd.komelia.offline.user.actions.UserKomgaImportAction
import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.book.KomgaBookSearch
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.library.KomgaLibraryClient
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.search.allOfBooks
import snd.komga.client.search.allOfSeries
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesClient
import snd.komga.client.series.KomgaSeriesSearch
import snd.komga.client.user.KomgaUser
import kotlinx.coroutines.flow.StateFlow

class OfflineScannerService(
    private val libraryClient: KomgaLibraryClient,
    private val seriesClient: KomgaSeriesClient,
    private val bookClient: KomgaBookClient,
    private val bookRepository: OfflineBookRepository,
    private val bookImportAction: BookKomgaImportAction,
    private val libraryImportAction: LibraryKomgaImportAction,
    private val seriesImportAction: SeriesKomgaImportAction,
    private val libraryRepository: OfflineLibraryRepository,
    private val seriesRepository: OfflineSeriesRepository,
    private val mediaServerRepository: OfflineMediaServerRepository,
    private val mediaServerSaveAction: MediaServerSaveAction,
    private val userImportAction: UserKomgaImportAction,
    private val onlineServerUrl: StateFlow<String>,
    private val fileSystem: OfflineFileSystem,
) {

    fun scan(root: PlatformFile, user: KomgaUser): Flow<OfflineScanResult> = flow {
        val serverFolders = fileSystem.listDirectories(root)
        val libraries = libraryClient.getLibraries()
        val mediaServer = mediaServerRepository.findByUserId(user.id)
            ?: mediaServerRepository.findAll().firstOrNull()
            ?: mediaServerSaveAction.execute(onlineServerUrl.value)
        userImportAction.execute(user, mediaServer.id)

        for (serverFolder in serverFolders) {
            val libraryFolders = fileSystem.listDirectories(serverFolder)
            for (libraryFolder in libraryFolders) {
                currentCoroutineContext().ensureActive()
                val libraryFolderName = fileSystem.getName(libraryFolder)
                val remoteLibrary = libraries.find { it.name == libraryFolderName }
                if (remoteLibrary == null) {
                    emitNoMatchForFolder(fileSystem.getName(serverFolder), libraryFolderName)
                    continue
                }

                val existingLibrary = libraryRepository.find(remoteLibrary.id)
                if (existingLibrary == null) {
                    libraryImportAction.execute(remoteLibrary, mediaServer.id)
                }

                scanLibrary(fileSystem.getName(serverFolder), libraryFolder, remoteLibrary).collect { emit(it) }
            }
        }
    }

    private fun scanLibrary(
        serverName: String,
        libraryFolder: PlatformFile,
        remoteLibrary: KomgaLibrary,
    ): Flow<OfflineScanResult> = flow {
        val seriesFolders = fileSystem.listDirectories(libraryFolder)
        for (seriesFolder in seriesFolders) {
            currentCoroutineContext().ensureActive()
            val seriesFolderName = fileSystem.getName(seriesFolder)
            val remoteSeries = findSeriesByName(remoteLibrary.id, seriesFolderName)
            if (remoteSeries == null) {
                emitNoMatchForFolder(serverName, remoteLibrary.name, seriesFolderName)
                continue
            }

            val existingSeries = seriesRepository.find(remoteSeries.id)
            if (existingSeries == null) {
                seriesImportAction.execute(remoteSeries)
            }

            scanSeries(serverName, remoteLibrary.name, seriesFolder, remoteSeries).collect { emit(it) }
        }
    }

    private fun scanSeries(
        serverName: String,
        libraryName: String,
        seriesFolder: PlatformFile,
        remoteSeries: KomgaSeries,
    ): Flow<OfflineScanResult> = flow {
        val bookFiles = fileSystem.listFiles(seriesFolder)
        val remoteBooks = getAllBooks(remoteSeries.id)

        for (bookFile in bookFiles) {
            currentCoroutineContext().ensureActive()
            val bookFileName = fileSystem.getName(bookFile)
            val bookFileNameWithoutExtension = bookFileName.substringBeforeLast(".")
            val remoteBook = remoteBooks.find { it.name == bookFileNameWithoutExtension || it.url.endsWith(bookFileName) }
            if (remoteBook == null) {
                emit(
                    OfflineScanResult.NoMatch(
                        serverName = serverName,
                        libraryName = libraryName,
                        seriesName = remoteSeries.metadata.title,
                        bookName = bookFileName,
                        error = "No matching book found on server"
                    )
                )
                continue
            }

            val result = processBookFile(
                serverName = serverName,
                libraryName = libraryName,
                seriesName = remoteSeries.metadata.title,
                bookFile = bookFile,
                remoteBook = remoteBook,
            )
            emit(result)
        }
    }

    private suspend fun processBookFile(
        serverName: String,
        libraryName: String,
        seriesName: String,
        bookFile: PlatformFile,
        remoteBook: KomgaBook,
    ): OfflineScanResult {
        val existing = bookRepository.find(remoteBook.id)
        val localFileSize = fileSystem.getFileSize(bookFile)
        val isOutOfSync = remoteBook.sizeBytes != localFileSize
        // Use the server's file date so the book appears in-sync after import (same as BookDownloadService)
        val fileModifiedDate = remoteBook.fileLastModified

        return if (existing == null) {
            bookImportAction.execute(
                book = remoteBook,
                offlinePath = bookFile,
                userId = null,
                localFileModifiedDate = fileModifiedDate
            )
            if (isOutOfSync) {
                OfflineScanResult.OutOfSync(
                    serverName = serverName,
                    libraryName = libraryName,
                    seriesName = seriesName,
                    bookName = remoteBook.metadata.title,
                    bookId = remoteBook.id
                )
            } else {
                OfflineScanResult.Imported(
                    serverName = serverName,
                    libraryName = libraryName,
                    seriesName = seriesName,
                    bookName = remoteBook.metadata.title,
                    bookId = remoteBook.id
                )
            }
        } else {
            // If already indexed, update to refresh metadata/path
            bookImportAction.execute(
                book = remoteBook,
                offlinePath = bookFile,
                userId = null,
                localFileModifiedDate = fileModifiedDate
            )
            if (isOutOfSync) {
                OfflineScanResult.OutOfSync(
                    serverName = serverName,
                    libraryName = libraryName,
                    seriesName = seriesName,
                    bookName = remoteBook.metadata.title,
                    bookId = remoteBook.id
                )
            } else {
                OfflineScanResult.AlreadyIndexed(
                    serverName = serverName,
                    libraryName = libraryName,
                    seriesName = seriesName,
                    bookName = remoteBook.metadata.title,
                    bookId = remoteBook.id
                )
            }
        }
    }

    private suspend fun findSeriesByName(libraryId: KomgaLibraryId, name: String): KomgaSeries? {
        var page = 0
        while (true) {
            val condition = allOfSeries {
                library { isEqualTo(libraryId) }
            }.toSeriesCondition()

            val seriesPage = seriesClient.getSeriesList(
                KomgaSeriesSearch(condition = condition),
                KomgaPageRequest(page++, 100)
            )
            val series = seriesPage.content.find { it.metadata.title == name || it.name == name }
            if (series != null) return series
            if (seriesPage.last) break
        }
        return null
    }

    private suspend fun getAllBooks(seriesId: snd.komga.client.series.KomgaSeriesId): List<KomgaBook> {
        val allBooks = mutableListOf<KomgaBook>()
        var page = 0
        while (true) {
            val condition = allOfBooks {
                seriesId { isEqualTo(seriesId) }
            }.toBookCondition()

            val booksPage = bookClient.getBookList(
                KomgaBookSearch(condition = condition),
                KomgaPageRequest(page++, 100)
            )
            allBooks.addAll(booksPage.content)
            if (booksPage.last) break
        }
        return allBooks
    }

    private suspend fun emitNoMatchForFolder(
        serverName: String,
        libraryName: String,
        seriesName: String? = null
    ) {
        // If it's a folder, we can't really emit a "book" result easily unless we want to report folders too.
        // For now, let's just ignore or log.
    }
}
