package snd.komelia.db.audiobook

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import snd.komelia.audiobook.AudioBookmark
import snd.komelia.audiobook.AudioBookmarkRepository
import snd.komelia.db.ExposedRepository
import snd.komelia.db.tables.AudioBookmarksTable
import snd.komga.client.book.KomgaBookId

class ExposedAudioBookmarkRepository(database: Database) : ExposedRepository(database), AudioBookmarkRepository {

    private val bookmarksChanged = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override fun getBookmarks(bookId: KomgaBookId): Flow<List<AudioBookmark>> {
        return bookmarksChanged.onStart { emit(Unit) }.map {
            transaction {
                AudioBookmarksTable.selectAll()
                    .where { AudioBookmarksTable.bookId eq bookId.value }
                    .orderBy(AudioBookmarksTable.createdAt, SortOrder.ASC)
                    .map {
                        AudioBookmark(
                            id = it[AudioBookmarksTable.id],
                            bookId = KomgaBookId(it[AudioBookmarksTable.bookId]),
                            trackIndex = it[AudioBookmarksTable.trackIndex],
                            positionSeconds = it[AudioBookmarksTable.positionSeconds],
                            trackTitle = it[AudioBookmarksTable.trackTitle],
                            createdAt = it[AudioBookmarksTable.createdAt],
                        )
                    }
            }
        }
    }

    override suspend fun saveBookmark(bookmark: AudioBookmark) {
        transaction {
            AudioBookmarksTable.insert {
                it[id] = bookmark.id
                it[bookId] = bookmark.bookId.value
                it[trackIndex] = bookmark.trackIndex
                it[positionSeconds] = bookmark.positionSeconds
                it[trackTitle] = bookmark.trackTitle
                it[createdAt] = bookmark.createdAt
            }
        }
        bookmarksChanged.tryEmit(Unit)
    }

    override suspend fun deleteBookmark(id: String) {
        transaction {
            AudioBookmarksTable.deleteWhere { AudioBookmarksTable.id eq id }
        }
        bookmarksChanged.tryEmit(Unit)
    }
}
