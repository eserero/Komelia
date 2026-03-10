ALTER TABLE ImageReaderSettings ADD COLUMN ncnn_upscaler_url TEXT NOT NULL DEFAULT 'https://github.com/eserero/Komelia/releases/download/model/NcnnUpscalerModels.zip';
ALTER TABLE ImageReaderSettings ADD COLUMN panel_detection_url TEXT NOT NULL DEFAULT 'https://github.com/eserero/Komelia/releases/download/model/rf-detr-med.onnx.zip';
