---
# Komelia-gcn2
title: Enable ONNX Upscaling on Android
status: completed
type: feature
priority: normal
created_at: 2026-03-03T18:52:36Z
updated_at: 2026-03-07T23:39:25Z
---

Wire up ONNX Runtime upscaling (MangaJaNai ESRGAN models) on Android. The native libraries are already bundled. Steps:
- [x] Step 1: Add jvmAndAndroidMain source set in komelia-domain/core/build.gradle.kts
- [x] Step 2: Create OnnxRuntimeUpscalerImpl.kt in jvmAndAndroidMain, delete DesktopOnnxRuntimeUpscaler.kt
- [x] Step 3: Update DesktopAppModule.kt imports/constructor call
- [x] Step 4: Implement mangaJaNaiDownload() in AndroidOnnxModelDownloader.kt
- [x] Step 5: Add upscaler to AndroidReaderImage.kt and AndroidReaderImageFactory.kt
- [x] Step 6: Implement createUpscaler() in AndroidAppModule.kt, wire factory
- [x] Step 7: Remove platform guard in SettingsContent.kt

## Reasons for Scrapping

ONNX upscaling on Android CPU is too slow to be practical. Even after fixing the initial-load hang (by not upscaling in resizeImage), zoomed-in upscaling would still take 30s–several minutes per page on CPU. NNAPI/GPU support is not available in the current native library enum. Reverted all changes.
