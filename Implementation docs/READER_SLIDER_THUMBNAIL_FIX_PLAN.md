# Fix Image Slider Thumbnail in New UI 2 Control Panel

The image viewer's new control panel (UI 2) currently hides the thumbnail preview when dragging the navigation slider, even if the "load small previews" setting is enabled. This is because the slider is used in "bare" mode, which explicitly disables previews and labels. This plan restores the thumbnail functionality by decoupling the "bare" styling from the preview logic and allowing the preview to float above the control card.

## Objective
Restore the thumbnail preview when dragging the navigation slider in the image reader's new control panel, while respecting the user's preference for loading previews.

## Key Files & Context
- `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/reader/image/ReaderState.kt`: Manages the state for the image reader, including settings.
- `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/reader/image/common/ProgressSlider.kt`: The slider component that displays the thumbnail preview.
- `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/reader/image/settings/BottomSheetSettingsOverlay.kt`: Contains the new UI control card implementation.
- `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/reader/ReaderControlsCard.kt`: The container for the control card, which currently clips its content.
- `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/reader/image/settings/CommonImageSettings.kt`: Shared image settings component for both bottom sheet and side menu.

## Implementation Steps

### 1. Update `ReaderState` to Manage `loadThumbnailPreviews`
- In `ReaderState.kt`, add a `loadThumbnailPreviews` `MutableStateFlow`.
- Initialize it in `initialize(bookId: KomgaBookId)` from `readerSettingsRepository.getLoadThumbnailPreviews()`.
- Add a method `onLoadThumbnailPreviewsChange(load: Boolean)` to update both the internal state and the repository.

### 2. Update `CommonImageSettings` to include the Toggle
- In `CommonImageSettings.kt`, add `loadThumbnailPreviews: Boolean` and `onLoadThumbnailPreviewsChange: (Boolean) -> Unit` parameters.
- Add a `SwitchWithLabel` to display this setting, matching the one in the global image reader settings.

### 3. Pass the setting through the UI layers
- Update `SettingsOverlay` in `SettingsContent.kt` to extract `loadThumbnailPreviews` from `commonReaderState`.
- Pass it down to `BottomSheetSettingsOverlay`, `SettingsSideMenuOverlay`, and `BottomSheetImageSettings`.
- Pass it to `ImageReaderControlsCardNewUI` in `BottomSheetSettingsOverlay.kt`.

### 4. Enhance `ProgressSlider` to support floating previews
- In `ProgressSlider.kt`, update `ProgressSlider` and `PageSpreadProgressSlider` to accept a `loadThumbnailPreviews` parameter.
- Update the internal `Slider` component:
    - Change the preview visibility logic to use `loadThumbnailPreviews` (and only when `showPreview` is true during dragging).
    - Allow the label to also be shown during dragging even if `isBare` is true.
    - Modify the `Layout` placement logic:
        - If `isBare` is true, place `previewPlaceable` and `labelPlaceable` at negative Y offsets so they float above the slider's reported area.
        - Ensure `totalHeight` remains only `sliderPlaceable.height` when `isBare` is true, so it doesn't push other elements in the card.

### 5. Prevent Clipping in `ReaderControlsCard`
- In `ReaderControlsCard.kt`, remove the `.clip(RoundedCornerShape(28.dp))` modifier. This allows the floating preview from the slider to be drawn outside the card's boundaries instead of being cut off.
- For the `Surface` branch (when `hazeState` is null), ensure the `Surface` doesn't clip its children.

## Verification & Testing
1. **Settings Consistency**: Verify that changing the "Load small previews" toggle in the reader's settings overlay updates the global setting and vice-versa.
2. **Dragging Preview (New UI)**: With "UI 2" enabled and "Load small previews" ON, drag the navigation slider in the image reader and verify that the thumbnail preview and page number label appear floating above the control card.
3. **Dragging Preview (Old UI)**: Verify that the preview still works correctly in the original UI (bottom slider).
4. **Setting OFF**: Verify that no preview appears when the setting is OFF, even while dragging.
5. **Clipping Check**: Ensure that the floating preview is not clipped by the top edge of the control card.
