CREATE TABLE IF NOT EXISTS audio_position (
    book_id TEXT PRIMARY KEY,
    track_index INTEGER NOT NULL DEFAULT 0,
    position_seconds REAL NOT NULL DEFAULT 0.0,
    saved_at INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS audio_bookmarks (
    id TEXT PRIMARY KEY,
    book_id TEXT NOT NULL,
    track_index INTEGER NOT NULL DEFAULT 0,
    position_seconds REAL NOT NULL DEFAULT 0.0,
    track_title TEXT NOT NULL DEFAULT '',
    created_at INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_audio_bookmarks_book_id ON audio_bookmarks(book_id);
