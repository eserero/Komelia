package snd.komelia.annotations

import snd.komga.client.book.KomgaBookId

sealed class AnnotationLocation {
    /** EPUB3 annotation. locatorJson is a Readium Locator serialized to JSON (contains
     *  locations.fragments with CSS selectors identifying the exact text range). */
    data class EpubLocation(
        val locatorJson: String,
        val selectedText: String?, // Display only — the highlighted text snippet
    ) : AnnotationLocation()

    /** Comic/image reader annotation. x and y are 0.0–1.0 fractions of image dimensions. */
    data class ComicLocation(
        val page: Int,
        val x: Float,
        val y: Float,
    ) : AnnotationLocation()
}

data class BookAnnotation(
    val id: String,
    val bookId: KomgaBookId,
    val location: AnnotationLocation,
    /** Null means note-only (no visual highlight). Comic pins always have a color. */
    val highlightColor: Int?,
    /** Null means pure highlight with no note text. */
    val note: String?,
    val createdAt: Long,
    val updatedAt: Long = createdAt,
)
