# Immersive UI 2 — Single-Pane Image Merge + Fixed Morph Target

## Goal

Fix two issues with the New UI 2 immersive screen:

1. **Single-pane merge**: The cover image and the card below should look like one continuous surface, not two separate floating elements. The image should bleed into the card via a gradient with no visible seam (no shadow, no rounded top edge on the card).

2. **Fixed morph target**: When the card expands, the cover image should animate directly to the thumbnail's *final* resting position — not chase the thumbnail as the card travels upward. Currently the image tracks the thumbnail's moving absolute position across 65% of the screen height.

## Reference

See `Implementation docs/stitch ui/immersive_light.html` for the target visual. The key pattern: a full-width hero image with a `bg-gradient-to-t` overlay that fades to the page background color, making the image flow seamlessly into the content below as a single surface.

---

## All Changes — Single File

**File**: `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/common/immersive/ImmersiveDetailScaffold.kt`

### Change 1 — Fix morph target Y (around line 411)

The `onThumbnailPositioned` lambda currently captures the thumbnail's position relative to the scaffold root. This absolute position includes `cardOffsetPx`, so as the card moves up during expansion, `targetThumbnailOffset.y` changes from ~408dp (collapsed) to ~48dp (expanded). The lerp `lerp(0, targetThumbnailOffset.y, expandFraction)` then makes the image chase the card over the full travel distance.

Fix: subtract `cardOffsetPx` from the captured y so the stored value always represents the thumbnail's position at full expansion (card y=0).

```kotlin
// BEFORE
val onThumbnailPositioned: (LayoutCoordinates) -> Unit = { coords ->
    scaffoldCoordinates?.let { root ->
        targetThumbnailOffset = root.localPositionOf(coords, Offset.Zero)
    }
}

// AFTER
val onThumbnailPositioned: (LayoutCoordinates) -> Unit = { coords ->
    scaffoldCoordinates?.let { root ->
        val absPos = root.localPositionOf(coords, Offset.Zero)
        // Normalize: subtract the card's current offset so we store the thumbnail's
        // position as it will be when the card is fully expanded (cardOffsetPx = 0).
        targetThumbnailOffset = Offset(absPos.x, absPos.y - cardOffsetPx)
    }
}
```

**Why this works**: During any frame of the animation, `absPos.y = cardOffsetPx + thumbnailInCard.y`. Subtracting `cardOffsetPx` always gives `thumbnailInCard.y` (~36–48dp), which is the stable offset of the thumbnail from the card top. Since at full expansion `cardOffsetPx = 0`, this equals the final absolute position. The morph image now travels only ~48dp vertically (it mostly just shrinks in place) instead of chasing the card across the screen.

---

### Change 2 — Remove card top corner radius when morphing (line ~328)

When `useMorphingCover = true`, the card's 28dp rounded top corners create a visible curved bump right at the boundary where the morphing image ends and the card begins. Remove the corner radius while the morphing cover is active.

```kotlin
// BEFORE
val topCornerRadiusDp = lerp(28f, 0f, expandFraction).dp

// AFTER
val topCornerRadiusDp = if (useMorphingCover) 0.dp else lerp(28f, 0f, expandFraction).dp
```

---

### Change 3 — Remove card shadow when morphing (line ~378)

The `shadow(elevation = 6.dp)` on the card renders a drop shadow that makes the card appear to float above the image, breaking the single-pane illusion.

```kotlin
// BEFORE
.shadow(elevation = 6.dp, shape = cardShape)

// AFTER
.shadow(elevation = if (useMorphingCover) 0.dp else 6.dp, shape = cardShape)
```

---

### Change 4 — Remove morphing cover bottom corner radius at collapsed state (line ~459)

Currently the morphing cover has 28dp bottom corners at collapsed state. Combined with the card's 28dp top corners, these two opposing curves create a visible gap/discontinuity at the boundary. Setting this to 0 makes both surfaces meet as a flat horizontal line.

```kotlin
// BEFORE
val currentBottomRadius = lerp(28f, 8f, expandFraction).dp

// AFTER
val currentBottomRadius = lerp(0f, 8f, expandFraction).dp
```

---

### Change 5 — Larger gradient for smoother merge (line ~488)

Increase the gradient area from 40% to 50% of the image height for a more gradual, less abrupt fade into the card color.

```kotlin
// BEFORE
.fillMaxHeight(0.4f)

// AFTER
.fillMaxHeight(0.5f)
```

---

## Expected Result

**Collapsed state**: The full-screen cover image flows into the card with no visible boundary. The gradient at the bottom of the image fades to `backgroundColor`, which matches the card surface — one continuous pane.

**Morph animation on expand**: The cover image shrinks from full-screen to thumbnail size, moving only ~48dp vertically (directly toward the thumbnail's final position near the top of the card). No more image chasing the card upward across the screen.

**Snap to expanded**: The morphing cover disappears at `expandFraction >= 0.99`, exactly when the real thumbnail fades in at that position. Seamless handoff.

**Collapse**: Reverse animation is equally clean.

---

## Context: What NOT to change

- The drag handle pill at the top of the card — keep as-is.
- The `cardContent` lambdas in `ImmersiveSeriesContent.kt`, `ImmersiveBookContent.kt`, `ImmersiveOneshotContent.kt` — no changes needed.
- The shared element transition setup (`coverSharedModifier`, `SharedTransitionLayout`) — no changes needed.
- The non-morphing code path (`useMorphingCover = false`) — should remain completely unaffected.
