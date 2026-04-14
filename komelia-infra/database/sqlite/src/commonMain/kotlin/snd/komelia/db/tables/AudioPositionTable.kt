package snd.komelia.db.tables

import org.jetbrains.exposed.v1.core.Table

object AudioPositionTable : Table("audio_position") {
    val bookId = text("book_id")
    val trackIndex = integer("track_index")
    val positionSeconds = double("position_seconds")
    val savedAt = long("saved_at")
    override val primaryKey = PrimaryKey(bookId)
}
