# Publisher Logos in Immersive Screens

## Context

Users want to see publisher branding in the immersive series/book/oneshot detail screens. The app already stores `publisher` in `KomgaSeriesMetadata` (accessible as `series.metadata.publisher: String`). Publisher logos are bundled as app assets from the ComicRackCE open-source logo pack (PNGs with transparency), matched to publisher names via filename normalization.

Two logo placements per screen:
1. **Hero badge** (collapsed state): white-tinted logo on dark rounded pill, bottom-left of the cover image area, fades out as the card expands
2. **Card logo** (expanded state): natural-color logo, right-aligned below the title/writers row in the card, fades in as the card expands

Fallback when no logo file matches the publisher: nothing shown (publisher name is already visible in the Tags tab).

---

## Bean: Komelia-ylo4

## Steps

### Step 1: Download & normalize logo assets ✓

Downloaded all PNGs from ComicRackCE repository:
`https://github.com/maforget/ComicRackCE/tree/master/ComicRack/Output/Resources/Icons/Publishers`

856 files downloaded to: `komelia-ui/src/commonMain/composeResources/files/publishers/`

Normalization rule: `name.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')`

Files with `#` separator (e.g., `DC Comics#DC.png`) are saved under **both** normalized names to support alias matching (e.g., `dc_comics.png` and `dc.png`).

### Step 2: New file — `PublisherLogoLoader.kt` ✓

**Path**: `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/common/immersive/PublisherLogoLoader.kt`

Provides:
- `normalizePublisherName(name: String): String`
- `@Composable fun rememberPublisherLogo(publisher: String?): ImageBitmap?`

### Step 3: Modify `ImmersiveDetailScaffold` ✓

Added `publisherLogo: ImageBitmap? = null` parameter.

Added Layer 2.5: Publisher hero badge between the card (Layer 2) and top bar (Layer 3). Shows white-tinted logo on dark pill at bottom-left of cover, fades out as card expands.

### Step 4: Modify `ImmersiveSeriesContent` ✓

Added `rememberPublisherLogo(series.metadata.publisher)`, passes to scaffold, adds card logo grid item.

### Step 5: Modify `ImmersiveOneshotContent` ✓

Same treatment as Series. `series.metadata.publisher` is available.

### Step 6: Modify `ImmersiveBookContent` ✓

Added `publisher: String? = null` parameter. Added `rememberPublisherLogo`, passes to scaffold, adds card logo grid item.

### Step 7: Thread publisher from BookViewModel ✓

- Added `KomgaSeriesApi` to `BookViewModel`
- Added `publisher: MutableStateFlow<String?>`
- Added `loadPublisher()` that fetches series metadata once during initialization
- Updated `ViewModelFactory.getBookViewModel` to pass `komgaApi.seriesApi`
- Updated `BookScreen.kt` to pass `vm.publisher.collectAsState().value`

### Step 8: Create spec doc ✓

This file.

---

## File summary

| Action | File |
|--------|------|
| NEW    | `komelia-ui/src/commonMain/composeResources/files/publishers/*.png` (856 files) |
| NEW    | `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/common/immersive/PublisherLogoLoader.kt` |
| MODIFY | `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/common/immersive/ImmersiveDetailScaffold.kt` |
| MODIFY | `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/series/immersive/ImmersiveSeriesContent.kt` |
| MODIFY | `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/oneshot/immersive/ImmersiveOneshotContent.kt` |
| MODIFY | `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/book/immersive/ImmersiveBookContent.kt` |
| MODIFY | `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/book/BookViewModel.kt` |
| MODIFY | `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/ViewModelFactory.kt` |
| MODIFY | `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/book/BookScreen.kt` |
