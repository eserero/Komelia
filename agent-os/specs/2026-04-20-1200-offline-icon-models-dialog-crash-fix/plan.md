# Plan: Offline Icon, Model Download Dialog, Default Model & Crash Fix

## Context

Four related improvements to the Sipurra/Komelia Android app:

1. **Offline icon** — the mobile `NewTopAppBar` has no offline indicator. Add a WiFi icon (both online/offline states) to the left of the theme toggle. Tapping either state shows a confirmation dialog to switch mode.

2. **First-launch model dialog** — after the first successful login on a fresh install, if neither NCNN upscaler nor panel detection models are downloaded, show a one-time prompt offering to download both.

3. **Default NCNN model** — change the factory default from Waifu2x to Real-ESRGAN (affects new installs only).

4. **Crash fix (RCA confirmed)** — `ConnectTimeoutException` crashes the app almost every launch. Root cause: `initialSync()` is called from an `.onEach { }.launchIn()` event listener in `ReaderState` and `Epub3ReaderState` with no exception handling.

---

## Task 1: Save Spec Documentation ✅

Create `agent-os/specs/2026-04-20-1200-offline-icon-models-dialog-crash-fix/` — done.

---

## Task 2: Fix ConnectTimeoutException crash

**Root cause files:**
- `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/reader/image/ReaderState.kt` — lines 149–153
- `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/Epub3ReaderState.kt` — lines 630–634

**Fix:** Wrap `initialSync()` call in each event listener with `runCatching { }`:

```kotlin
komgaEvents.events.onEach { event ->
    if (event is KomgaEvent.ReadProgressChanged && event.bookId == (booksState.value?.currentBook?.id ?: bookId)) {
        runCatching { initialSync() }
    }
}.launchIn(stateScope)
```

---

## Task 3: Change default NCNN engine to Real-ESRGAN

**File:** `komelia-domain/core/src/commonMain/kotlin/snd/komelia/settings/model/NcnnUpscalerSettings.kt`

Change default `engine` from `NcnnEngine.WAIFU2X` to `NcnnEngine.REAL_ESRGAN` and `model` from `"models-cunet/scale2.0x_model"` to `"models-realesrgan"`.

---

## Task 4: Add offline WiFi icon to NewTopAppBar

**File:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/topbar/NewTopAppBar.kt`

- Online → `Icons.Rounded.Wifi` (iconColor); tapping shows `ConfirmationDialog("Go Offline?")` → `mainScreenVm.goOffline()`
- Offline → `Icons.Rounded.WifiOff` (error color); tapping shows `ConfirmationDialog("Go Online?")` → `mainScreenVm.goOnline()`
- Insert before theme toggle at line 114

Also add `goOffline()` to `MainScreenViewModel` (sets `putOfflineMode(true)`, navigates to offline login).

---

## Task 5: Add first-launch model download dialog

### 5a. Settings flag
Add `modelDownloadPromptShown: Boolean = false` to `ImageReaderSettings`. Wire through repository interface and wrapper.

### 5b. MainScreenViewModel logic
- `showModelDownloadDialog: StateFlow<Boolean>` — true when `!promptShown && !ncnnDownloaded && !panelDownloaded`
- `dismissModelDownloadDialog()` — sets flag true
- `downloadModels()` — triggers both downloads

### 5c. MainScreen dialog
Show `AlertDialog` on first login when models are missing:
- Title: "Download AI Models?"
- Body: "Download the models needed for panel-by-panel navigation and image upscaling? (~100MB)"
- Confirm: "Download" → `vm.downloadModels()`
- Dismiss: "Not now" → `vm.dismissModelDownloadDialog()`
