package snd.komelia.db.tables

import org.jetbrains.exposed.v1.core.Table

object AudioChapterCacheTable : Table("audio_chapter_cache") {
    val bookId = text("book_id")
    val chapterIndex = integer("chapter_index")
    val fileIndex = integer("file_index")
    val title = text("title")
    val fileOffsetSeconds = double("file_offset_seconds")
    val durationSeconds = double("duration_seconds")
    override val primaryKey = PrimaryKey(bookId, chapterIndex)
}
