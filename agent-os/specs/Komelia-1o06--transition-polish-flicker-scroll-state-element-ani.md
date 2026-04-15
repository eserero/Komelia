---
# Komelia-1o06
title: 'Transition Polish: Flicker, Scroll State, Element Animation, Speed'
status: completed
type: task
priority: normal
created_at: 2026-02-25T06:16:58Z
updated_at: 2026-02-25T06:20:58Z
parent: Komelia-uler
---

Fix transition quality issues: flicker (Coil INEXACT), crossfade during shared transition, inner sharedBounds fade, scroll position loss (saveableState), entry animation on non-shared elements, and increase duration to 600ms.

## Summary of Changes

- **ThumbnailImage.kt**: Changed `Precision.EXACT` → `Precision.INEXACT` so the full-screen cover reuses the card's cached bitmap instead of re-decoding → eliminates flicker
- **BookThumbnail.kt** + **SeriesThumbnail.kt**: Disable Coil crossfade when in shared transition (`crossfade = !inSharedTransition`); replace `fadeIn`/`fadeOut` on `sharedBounds` with `EnterTransition.None`/`ExitTransition.None`; increase `boundsTransform` from 500ms → 600ms
- **ImmersiveDetailScaffold.kt**: Same sharedBounds fade removal + 600ms duration for cover; added `uiEnterExitModifier` (fadeIn 300ms delay 150ms / fadeOut 150ms) applied to Layer 2 card, Layer 4 FAB, Layer 5 top bar
- **MainScreen.kt**: Wrapped `screen.Content()` in `navigator.saveableState("screen", screen)` to preserve LazyGrid/List scroll positions across navigation; adjusted AnimatedContent fade to 400ms enter / 250ms exit
