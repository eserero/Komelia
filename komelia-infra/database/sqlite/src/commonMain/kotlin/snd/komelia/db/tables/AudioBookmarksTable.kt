package snd.komelia.db.tables

import org.jetbrains.exposed.v1.core.Table

object AudioBookmarksTable : Table("audio_bookmarks") {
    val id = text("id")
    val bookId = text("book_id")
    val trackIndex = integer("track_index")
    val positionSeconds = double("position_seconds")
    val trackTitle = text("track_title")
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}
