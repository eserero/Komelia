package snd.komelia.db.localfile

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import snd.komelia.db.ExposedRepository
import snd.komga.client.book.KomgaBookId

internal object LocalFileReadProgressTable : Table("local_file_read_progress") {
    val virtualBookId = text("virtual_book_id")
    val page = integer("page").default(1)
    val completed = bool("completed").default(false)
    val readiumProgression = text("readium_progression").nullable()
    override val primaryKey = PrimaryKey(virtualBookId)
}

class LocalFileReadProgressRepository(database: Database) : ExposedRepository(database) {

    suspend fun saveProgress(bookId: KomgaBookId, page: Int, completed: Boolean) {
        transaction {
            LocalFileReadProgressTable.upsert {
                it[virtualBookId] = bookId.value
                it[LocalFileReadProgressTable.page] = page
                it[LocalFileReadProgressTable.completed] = completed
            }
        }
    }

    suspend fun getProgress(bookId: KomgaBookId): Pair<Int, Boolean>? {
        return transaction {
            LocalFileReadProgressTable
                .selectAll()
                .where { LocalFileReadProgressTable.virtualBookId eq bookId.value }
                .firstOrNull()
                ?.let {
                    Pair(
                        it[LocalFileReadProgressTable.page],
                        it[LocalFileReadProgressTable.completed]
                    )
                }
        }
    }

    suspend fun saveReadiumProgression(bookId: KomgaBookId, json: String) {
        transaction {
            LocalFileReadProgressTable.upsert {
                it[virtualBookId] = bookId.value
                it[readiumProgression] = json
            }
        }
    }

    suspend fun getReadiumProgression(bookId: KomgaBookId): String? {
        return transaction {
            LocalFileReadProgressTable
                .selectAll()
                .where { LocalFileReadProgressTable.virtualBookId eq bookId.value }
                .firstOrNull()
                ?.get(LocalFileReadProgressTable.readiumProgression)
        }
    }
}
