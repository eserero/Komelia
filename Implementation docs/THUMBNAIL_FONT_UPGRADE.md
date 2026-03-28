# Plan: Thumbnail Font Upgrade

Update the thumbnail fonts, sizes, and weights to match the "Stitch UI" example exactly when `New UI 2` is enabled.

## Objective
The goal is to align the unified thumbnail (item card) typography with the provided HTML design example. This requires downloading and bundling the exact font files used.

## Implementation Steps

### 1. Font Procurement and Setup
- Create the directory: `komelia-ui/src/commonMain/composeResources/font/`.
- Download the following font files (using `curl` or similar):
  - **Inter-SemiBold.ttf**: From Google Fonts (e.g., `https://github.com/google/fonts/raw/main/ofl/inter/static/Inter-SemiBold.ttf`)
  - **NotoSerif-Bold.ttf**: From Google Fonts (e.g., `https://github.com/google/fonts/raw/main/ofl/notoserif/static/NotoSerif-Bold.ttf`)
- Verify files are placed in `komelia-ui/src/commonMain/composeResources/font/`.

### 2. Define Font Families in Code
Modify `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/common/cards/ItemCard.kt` or `Theme.kt`:
- Add required imports for font resource loading.
- Define `Inter` and `NotoSerif` font families:
  ```kotlin
  val Inter = FontFamily(Font(Res.font.Inter_SemiBold, FontWeight.SemiBold))
  val NotoSerif = FontFamily(Font(Res.font.NotoSerif_Bold, FontWeight.Bold))
  ```

### 3. Update `LibraryItemCard` Typography
Modify `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/common/cards/ItemCard.kt`:
- Retrieve `useNewLibraryUI2` from `LocalUseNewLibraryUI2.current`.
- **When `useNewLibraryUI2` is TRUE:**
  - **Primary Style (Title):**
    - `fontFamily = NotoSerif`
    - `fontSize = 13.sp`
    - `fontWeight = FontWeight.Bold`
  - **Secondary Style (Metadata):**
    - `fontFamily = Inter`
    - `fontSize = 10.sp`
    - `fontWeight = FontWeight.SemiBold`
    - `letterSpacing = 0.5.sp`
    - Uppercase the `secondaryText` content.

## Verification & Testing
1. Confirm fonts are correctly bundled in the build.
2. Enable "New UI 2" and verify exact font matching with the design.
