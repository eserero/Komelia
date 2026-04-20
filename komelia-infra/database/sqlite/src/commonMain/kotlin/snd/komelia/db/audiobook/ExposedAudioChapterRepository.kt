package snd.komelia.db.audiobook

import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import snd.komelia.audiobook.AudioChapterEntry
import snd.komelia.audiobook.AudioChapterRepository
import snd.komelia.db.ExposedRepository
import snd.komelia.db.tables.AudioChapterCacheTable
import snd.komga.client.book.KomgaBookId

class ExposedAudioChapterRepository(database: Database) : ExposedRepository(database), AudioChapterRepository {

    override suspend fun getChapters(bookId: KomgaBookId): List<AudioChapterEntry> {
        return transaction {
            AudioChapterCacheTable.selectAll()
                .where { AudioChapterCacheTable.bookId eq bookId.value }
                .orderBy(AudioChapterCacheTable.chapterIndex)
                .map {
                    AudioChapterEntry(
                        bookId = bookId,
                        chapterIndex = it[AudioChapterCacheTable.chapterIndex],
                        fileIndex = it[AudioChapterCacheTable.fileIndex],
                        title = it[AudioChapterCacheTable.title],
                        fileOffsetSeconds = it[AudioChapterCacheTable.fileOffsetSeconds],
                        durationSeconds = it[AudioChapterCacheTable.durationSeconds],
                    )
                }
        }
    }

    override suspend fun saveChapters(chapters: List<AudioChapterEntry>) {
        if (chapters.isEmpty()) return
        transaction {
            AudioChapterCacheTable.batchInsert(chapters, ignore = true) { chapter ->
                this[AudioChapterCacheTable.bookId] = chapter.bookId.value
                this[AudioChapterCacheTable.chapterIndex] = chapter.chapterIndex
                this[AudioChapterCacheTable.fileIndex] = chapter.fileIndex
                this[AudioChapterCacheTable.title] = chapter.title
                this[AudioChapterCacheTable.fileOffsetSeconds] = chapter.fileOffsetSeconds
                this[AudioChapterCacheTable.durationSeconds] = chapter.durationSeconds
            }
        }
    }

    override suspend fun deleteChapters(bookId: KomgaBookId) {
        transaction {
            AudioChapterCacheTable.deleteWhere { AudioChapterCacheTable.bookId eq bookId.value }
        }
    }
}
