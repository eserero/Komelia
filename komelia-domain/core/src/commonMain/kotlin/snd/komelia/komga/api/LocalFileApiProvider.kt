package snd.komelia.komga.api

import kotlinx.coroutines.flow.SharedFlow
import snd.komelia.komga.api.model.KomeliaBook
import snd.komga.client.book.KomgaBookId

interface LocalFileApiProvider {
    /** Emits a fully constructed KomeliaBook each time a new local file URI is processed. */
    val processedBooksFlow: SharedFlow<KomeliaBook>

    /** Returns the LocalFileBookApi for this virtual book ID, or null if not a local file. */
    fun getApiForBook(bookId: KomgaBookId): KomgaBookApi?
}
