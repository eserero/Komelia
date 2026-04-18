ALTER TABLE book_annotations ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0;
UPDATE book_annotations SET updated_at = created_at;
