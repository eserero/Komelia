package snd.komelia.annotations

import kotlinx.coroutines.flow.Flow
import snd.komga.client.book.KomgaBookId

interface BookAnnotationRepository {
    fun getAnnotations(bookId: KomgaBookId): Flow<List<BookAnnotation>>
    suspend fun saveAnnotation(annotation: BookAnnotation)
    suspend fun deleteAnnotation(id: String)
}
