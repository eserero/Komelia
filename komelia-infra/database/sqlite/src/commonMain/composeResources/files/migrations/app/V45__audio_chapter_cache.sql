CREATE TABLE IF NOT EXISTS audio_chapter_cache (
    book_id TEXT NOT NULL,
    chapter_index INTEGER NOT NULL,
    file_index INTEGER NOT NULL,
    title TEXT NOT NULL,
    file_offset_seconds REAL NOT NULL DEFAULT 0.0,
    duration_seconds REAL NOT NULL DEFAULT 0.0,
    PRIMARY KEY (book_id, chapter_index)
);
CREATE INDEX IF NOT EXISTS idx_audio_chapter_cache_book_id ON audio_chapter_cache(book_id);
