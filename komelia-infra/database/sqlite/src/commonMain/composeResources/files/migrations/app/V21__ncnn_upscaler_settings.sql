ALTER TABLE ImageReaderSettings ADD COLUMN ncnn_enabled INTEGER NOT NULL DEFAULT 0;
ALTER TABLE ImageReaderSettings ADD COLUMN ncnn_engine TEXT NOT NULL DEFAULT 'WAIFU2X';
ALTER TABLE ImageReaderSettings ADD COLUMN ncnn_model TEXT NOT NULL DEFAULT 'models-cunet/scale2.0x_model';
ALTER TABLE ImageReaderSettings ADD COLUMN ncnn_gpu_id INTEGER NOT NULL DEFAULT 0;
ALTER TABLE ImageReaderSettings ADD COLUMN ncnn_tta_mode INTEGER NOT NULL DEFAULT 0;
ALTER TABLE ImageReaderSettings ADD COLUMN ncnn_num_threads INTEGER NOT NULL DEFAULT 4;
