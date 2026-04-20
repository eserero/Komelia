package snd.komelia.audiobook

import snd.komga.client.book.KomgaBookId

data class AudioChapterEntry(
    val bookId: KomgaBookId,
    val chapterIndex: Int,       // position in the unified flat list (0-based)
    val fileIndex: Int,          // which audio file (matches ExoPlayer track index)
    val title: String,
    val fileOffsetSeconds: Double, // start position within that file
    val durationSeconds: Double,
)
