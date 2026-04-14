CREATE TABLE IF NOT EXISTS local_file_read_progress (
    virtual_book_id TEXT PRIMARY KEY,
    page INTEGER NOT NULL DEFAULT 1,
    completed INTEGER NOT NULL DEFAULT 0,
    readium_progression TEXT
);
