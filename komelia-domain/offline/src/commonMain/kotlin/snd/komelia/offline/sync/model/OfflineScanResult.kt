package snd.komelia.offline.sync.model

import snd.komga.client.book.KomgaBookId

sealed interface OfflineScanResult {
    val serverName: String
    val libraryName: String
    val seriesName: String
    val bookName: String

    data class Imported(
        override val serverName: String,
        override val libraryName: String,
        override val seriesName: String,
        override val bookName: String,
        val bookId: KomgaBookId,
    ) : OfflineScanResult

    data class Updated(
        override val serverName: String,
        override val libraryName: String,
        override val seriesName: String,
        override val bookName: String,
        val bookId: KomgaBookId,
    ) : OfflineScanResult

    data class OutOfSync(
        override val serverName: String,
        override val libraryName: String,
        override val seriesName: String,
        override val bookName: String,
        val bookId: KomgaBookId,
    ) : OfflineScanResult

    data class AlreadyIndexed(
        override val serverName: String,
        override val libraryName: String,
        override val seriesName: String,
        override val bookName: String,
        val bookId: KomgaBookId,
    ) : OfflineScanResult

    data class NoMatch(
        override val serverName: String,
        override val libraryName: String,
        override val seriesName: String,
        override val bookName: String,
        val error: String? = null,
    ) : OfflineScanResult
}
