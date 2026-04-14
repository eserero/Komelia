package snd.komelia.db.audiobook

import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import snd.komelia.audiobook.AudioPosition
import snd.komelia.audiobook.AudioPositionRepository
import snd.komelia.db.ExposedRepository
import snd.komelia.db.tables.AudioPositionTable
import snd.komga.client.book.KomgaBookId

class ExposedAudioPositionRepository(database: Database) : ExposedRepository(database), AudioPositionRepository {

    override suspend fun getPosition(bookId: KomgaBookId): AudioPosition? {
        return transaction {
            AudioPositionTable.selectAll()
                .where { AudioPositionTable.bookId eq bookId.value }
                .singleOrNull()
                ?.let {
                    AudioPosition(
                        bookId = bookId,
                        trackIndex = it[AudioPositionTable.trackIndex],
                        positionSeconds = it[AudioPositionTable.positionSeconds],
                        savedAt = it[AudioPositionTable.savedAt],
                    )
                }
        }
    }

    override suspend fun savePosition(position: AudioPosition) {
        transaction {
            AudioPositionTable.upsert {
                it[bookId] = position.bookId.value
                it[trackIndex] = position.trackIndex
                it[positionSeconds] = position.positionSeconds
                it[savedAt] = position.savedAt
            }
        }
    }
}
