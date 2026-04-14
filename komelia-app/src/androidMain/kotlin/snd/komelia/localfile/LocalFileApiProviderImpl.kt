package snd.komelia.localfile

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import snd.komelia.db.localfile.LocalFileReadProgressRepository
import snd.komelia.komga.api.KomgaBookApi
import snd.komelia.komga.api.LocalFileApiProvider
import snd.komelia.komga.api.model.KomeliaBook
import snd.komga.client.book.KomgaBookId
import java.util.concurrent.ConcurrentHashMap

class LocalFileApiProviderImpl(
    private val context: Context,
    private val incomingUriFlow: SharedFlow<String>,
    private val readProgressRepo: LocalFileReadProgressRepository,
    scope: CoroutineScope,
) : LocalFileApiProvider {

    private val registry = ConcurrentHashMap<KomgaBookId, LocalFileBookApi>()

    private val _processedBooksFlow = MutableSharedFlow<KomeliaBook>(replay = 1)
    override val processedBooksFlow: SharedFlow<KomeliaBook> = _processedBooksFlow.asSharedFlow()

    init {
        scope.launch {
            incomingUriFlow.collect { uriString ->
                val api = LocalFileBookApi(context, uriString, readProgressRepo)
                registry[api.virtualBookId] = api
                val book = api.getOne(api.virtualBookId)
                _processedBooksFlow.emit(book)
            }
        }
    }

    override fun getApiForBook(bookId: KomgaBookId): KomgaBookApi? = registry[bookId]
}
