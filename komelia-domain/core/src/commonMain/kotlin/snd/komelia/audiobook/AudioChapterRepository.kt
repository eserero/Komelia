package snd.komelia.audiobook

import snd.komga.client.book.KomgaBookId

interface AudioChapterRepository {
    suspend fun getChapters(bookId: KomgaBookId): List<AudioChapterEntry>
    suspend fun saveChapters(chapters: List<AudioChapterEntry>)
    suspend fun deleteChapters(bookId: KomgaBookId)
}
