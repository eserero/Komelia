package snd.komelia.ui

import snd.komelia.ui.book.BookFilter
import snd.komelia.ui.platform.ScreenSerializable
import snd.komga.client.readlist.KomgaReadListId
import kotlin.jvm.JvmInline

sealed interface BookSiblingsContext : ScreenSerializable {
    data class Series(val filter: BookFilter? = null) : BookSiblingsContext

    @JvmInline
    value class ReadList(val id: KomgaReadListId) : BookSiblingsContext
}