package snd.komelia.ui.reader.epub.audio

data class AudioMetadataInfo(
    val tags: Map<String, String>,
    val chapters: List<AudioChapter>
)

data class AudioChapter(
    val title: String,
    val startTimeMs: Long,
    val endTimeMs: Long
)
