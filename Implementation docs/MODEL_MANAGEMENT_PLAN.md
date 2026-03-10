# Plan: Model Management and UI Improvements

This document outlines the strategy for moving NCNN upscaler models to on-demand downloads, implementing configurable model URLs, and refining the Image Settings UI.

---

## 1. Preparation: Model Bundling and Hosting

This phase focuses on preparing the model assets for remote hosting.

*   **Upscaler Model Bundling**:
    *   Bundle all contents of `komelia-infra/ncnn-upscaler/src/main/assets/` (including all `models-*` directories) into a single zip file named `NcnnUpscalerModels.zip`.
    *   Command: `cd komelia-infra/ncnn-upscaler/src/main/assets/ && jar -cMf NcnnUpscalerModels.zip models-* && mv NcnnUpscalerModels.zip ../../../../../`
    *   Result: `NcnnUpscalerModels.zip` will be located at the project root (`/home/eyal/Komelia/NcnnUpscalerModels.zip`).
*   **Panel Model Migration**:
    *   Download the existing panel model zip: `https://github.com/Snd-R/komelia-onnxruntime/releases/download/model/rf-detr-med.onnx.zip`.
    *   Command: `curl -L -o rf-detr-med.onnx.zip https://github.com/Snd-R/komelia-onnxruntime/releases/download/model/rf-detr-med.onnx.zip` (executed at project root).
    *   Result: `rf-detr-med.onnx.zip` will be located at the project root (`/home/eyal/Komelia/rf-detr-med.onnx.zip`).
*   **GitHub Hosting**:
    *   Upload both `NcnnUpscalerModels.zip` and the panel model zip to a release in the current project under the `model` tag.
    *   Target Repository: `https://github.com/eserero/Komelia`
    *   Final URLs:
        1.  `https://github.com/eserero/Komelia/releases/download/model/NcnnUpscalerModels.zip`
        2.  `https://github.com/eserero/Komelia/releases/download/model/rf-detr-med.onnx.zip`

---

## 2. Domain and Configuration Changes

### 2.1 Settings Model Updates (`komelia-domain`)
*   **`NcnnUpscalerSettings.kt`**:
    *   Add `ncnnUpscalerUrl: String` with the default pointing to the new GitHub-hosted `NcnnUpscalerModels.zip`.
    *   Add an `isDownloaded: Boolean` check to verify if `context.filesDir/ncnn_models` contains valid model files.
*   **`ImageReaderSettings.kt`**:
    *   Add `panelDetectionUrl: String`.
    *   Define constants for the default URLs:
        1.  **Original**: `https://github.com/Snd-R/komelia-onnxruntime/releases/download/model/rf-detr-med.onnx.zip`
        2.  **GitHub Managed**: The new URL identified in Section 1.

### 2.2 Repository Updates (`komelia-infra`)
*   Update `ImageReaderSettingsRepository` and `ReaderSettingsRepositoryWrapper` to persist `ncnnUpscalerUrl` and `panelDetectionUrl`.

---

## 3. Infrastructure and Downloading

### 3.1 `OnnxModelDownloader` Interface Update
*   Modify the interface to support URL-based downloads:
    *   `fun panelDownload(url: String): Flow<UpdateProgress>`
    *   `fun ncnnDownload(url: String): Flow<UpdateProgress>`

### 3.2 `AndroidOnnxModelDownloader.kt` Implementation
*   **`ncnnDownload(url: String)`**:
    *   Download the zip from the provided URL.
    *   Extract contents to `context.filesDir/ncnn_models`. Ensure the directory structure (e.g., `models-cunet/`) is correctly recreated in the filesystem.
*   **`panelDownload(url: String)`**:
    *   Update to accept and use the `url` parameter instead of the hardcoded constant.
    *   Retain extraction logic to the app's local storage.

### 3.3 `AndroidNcnnUpscaler.kt` Updates
*   **FileSystem Loading**: Modify `reinit()` to load models from the filesystem.
    *   Call `newNcnn.load(null, paramPath, binPath)` (passing `null` to indicate no `AssetManager`).
    *   Construct absolute paths: `"${context.filesDir.absolutePath}/ncnn_models/$paramPath"`.
*   **Feature Gate**: Prevent initialization if the local model files are missing.

---

## 4. UI Enhancements (`komelia-ui`)

### 4.1 `ImageReaderSettingsContent.kt` Refactor
*   **Log Buttons Relocation**:
    *   Remove "View Logs" and "Crash Logs" from `NcnnSettingsContent`.
    *   Add a new `Row` at the very bottom of the main `ImageReaderSettingsContent` column.
    *   Place both log buttons in this row, ensuring they occupy a single line at the bottom of the screen.

### 4.2 `NcnnSettingsContent.kt` Updates
*   **Download Management**:
    *   Display a "Download Models" button if `isDownloaded` is false.
    *   Disable the upscaler toggle and dropdowns until the download is complete.
    *   Show download progress using the `DownloadDialog`.
*   **URL Selection**:
    *   Add a dropdown for "Model URL Source" with options: "Default (GitHub)" and "Custom".
    *   If "Custom" is selected, show a text field for the URL.

### 4.3 `PanelDetectionSettings.kt` Updates
*   **URL Selection**:
    *   Implement a dropdown with three options: "Default (Original)", "Default (GitHub)", and "Custom".
    *   If "Custom" is selected, provide a text input field.
*   Pass the selected URL to the download flow.

---

## 5. Cleanup
*   Once the downloading mechanism is fully verified on Android, remove the model files from `komelia-infra/ncnn-upscaler/src/main/assets/` to reduce APK size.

---

---

## 7. Problems to Resolve

### 7.1 APK Size Issues
*   **Issue**: APK size remains high (~160MB), indicating models are still being bundled.
*   **RCA**: Previous builds populated `build/intermediates` with the assets. Even after deleting source assets, the build system may be reusing cached intermediates.
*   **Fix**: Perform a mandatory `./gradlew clean` before the next release build.

### 7.2 Download Failures and Initialization Errors
*   **Issue**: NCNN download fails with "file not found", and subsequent attempts throw `java.lang.IllegalStateException: onnx model downloader is not initialized`.
*   **RCA (IllegalStateException)**: In `ReaderViewModel.kt`, `NcnnSettingsState` is being instantiated with `onnxModelDownloader = null` because the downloader is not currently passed into the `ReaderViewModel` constructor.
*   **RCA (Download Error)**: Likely a URL mismatch or network issue during the initial flow.
*   **Fix**: Update `ReaderViewModel` constructor to accept `OnnxModelDownloader` and pass it through.

### 7.3 Custom URL UI Logic
*   **Issue**: Selecting the "Custom" option in the NCNN settings dialog does not reveal the custom URL text field.
*   **RCA**: In `NcnnSettingsContent.kt`, the `onOptionChange` block specifically ignores the "Custom" value (`if (it.value != "Custom")`). Because the underlying `settings.ncnnUpscalerUrl` never changes, the `remember(settings.ncnnUpscalerUrl)` block keeps the selection on "Default", and the text field's visibility condition (`selectedUrlOption.value == "Custom"`) remains false.
*   **Fix**: Implement local state in the Composable to track "Custom" selection independently of the persisted URL, or update the logic to allow switching.

*   **UX Consistency**: Ensure the new row uses `horizontalArrangement = Arrangement.spacedBy(10.dp)` and `verticalAlignment = Alignment.CenterVertically` for visual parity with the ONNX model settings.

---

## 9. Summary of New Regressions and Stability Issues

### 9.1 Panel Model Settings UI Regression
*   **Issue**: The Panel Model settings UI is currently broken.
*   **Symptoms**:
    *   The download button lacks the "Re-download" dynamic labeling logic.
    *   The "Installed" text label and green checkmark icon are missing even when the model is downloaded.
    *   The panel detection option itself remains non-functional even after a successful download.
*   **Required Action**: Revert or fix `PanelDetectionSettings.kt` to restore its original working behavior and visual state.

### 9.2 Upscaler Redownload Destabilization
*   **Issue**: Redownloading NCNN upscaler models while the app is running or after a successful initial download causes system instability.
*   **Symptoms**: Redownloading seems to "destabilize the code," potentially due to race conditions or file locks when overwriting models that might be in use or initialized.
*   **Required Action**: Investigate safe model replacement strategies or prevent redownload while the upscaler is active/initialized.


*   **UX Consistency**: Ensure the new row uses `horizontalArrangement = Arrangement.spacedBy(10.dp)` and `verticalAlignment = Alignment.CenterVertically` for visual parity with the ONNX model settings.

---

## 9. Summary of New Regressions and Stability Issues

### 9.1 Panel Model Settings UI Regression
*   **Issue**: The Panel Model settings UI is currently broken.
*   **Symptoms**:
    *   The download button lacks the "Re-download" dynamic labeling logic.
    *   The "Installed" text label and green checkmark icon are missing even when the model is downloaded.
    *   The panel detection option itself remains non-functional even after a successful download.
*   **Required Action**: Revert or fix `PanelDetectionSettings.kt` to restore its original working behavior and visual state.

### 9.2 Upscaler Redownload Destabilization
*   **Issue**: Redownloading NCNN upscaler models while the app is running or after a successful initial download causes system instability.
*   **Symptoms**: Redownloading seems to "destabilize the code," potentially due to race conditions or file locks when overwriting models that might be in use or initialized.
*   **Required Action**: Investigate safe model replacement strategies or prevent redownload while the upscaler is active/initialized.

