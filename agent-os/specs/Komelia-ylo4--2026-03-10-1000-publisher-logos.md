---
# Komelia-ylo4
title: 2026-03-10-1000-publisher-logos
status: completed
type: epic
priority: normal
created_at: 2026-03-10T01:05:08Z
updated_at: 2026-03-10T10:03:01Z
---

Publisher logos in immersive screens - bundle ComicRackCE logo assets and show them in series/book/oneshot immersive detail screens

## Tasks
- [x] Step 1: Download & normalize logo assets
- [x] Step 2: Create PublisherLogoLoader.kt
- [x] Step 3: Modify ImmersiveDetailScaffold.kt
- [x] Step 4: Modify ImmersiveSeriesContent.kt
- [x] Step 5: Modify ImmersiveOneshotContent.kt
- [x] Step 6: Modify ImmersiveBookContent.kt
- [x] Step 7: Thread publisher from caller (BookViewModel)
- [x] Step 8: Create spec doc

## Summary of Changes

- Downloaded 856 publisher logo PNGs from ComicRackCE (including alias variants from filenames with `#`)
- Created `PublisherLogoLoader.kt` with `normalizePublisherName()` and `rememberPublisherLogo()` composable
- Added `publisherLogo: ImageBitmap?` to `ImmersiveDetailScaffold` with hero badge (Layer 2.5)
- Wired publisher logos into Series, Oneshot, and Book immersive screens (hero badge + card logo)
- Added `KomgaSeriesApi` + `publisher: StateFlow<String?>` to `BookViewModel` for book screens
- Build verified: `assembleDebug` passes with no errors
