ALTER TABLE ImageReaderSettings ADD COLUMN ncnn_upscale_on_load BOOLEAN NOT NULL DEFAULT 0;
ALTER TABLE ImageReaderSettings ADD COLUMN ncnn_upscale_threshold INTEGER NOT NULL DEFAULT 1200;
