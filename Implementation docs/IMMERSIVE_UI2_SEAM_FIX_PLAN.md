# Immersive UI 2 — Seamless Image-Card Merge

## Goal

Eliminate the visible seam, shadow, and drag handle at the boundary between the morphing cover image and the card below it, so the two surfaces appear as one continuous plane (like the reference in `stitch ui/immersive_light.html`).

## Root Causes

1. **Color mismatch at boundary**: The morphing cover gradient fades to `backgroundColor` (opaque). The card immediately below starts with `scrimColor = backgroundColor.copy(alpha = 0.72f)` composited over a blurred cover image — visually different, causing a color jump at y = `collapsedOffset`.
2. **Material3 Card surface tint**: The `Card` composable in Layer 2.75 applies a surface background and tonal tint even at 0dp elevation, which can produce subtle edge artifacts.
3. **Drag handle pill**: Visible at the card top in morphing mode; user wants it hidden.
4. **Hard clip edge**: The Card's clip creates a 1-pixel antialiased line at the boundary.

Reference: `Implementation docs/stitch ui/immersive_light.html` — gradient fades to page background; main content uses the same color; zero visible boundary.

---

## File

`komelia-ui/src/commonMain/kotlin/snd/komelia/ui/common/immersive/ImmersiveDetailScaffold.kt`

---

## Changes

### Change 1 — Replace Card with Box in Layer 2.75 (line ~465)

The `Card` composable adds a Material3 surface background and tonal elevation tint. Replace with a plain `Box` using explicit `shadow` + `clip` modifiers:

```kotlin
// BEFORE
Card(
    shape = RoundedCornerShape(
        topStart = currentTopRadius, topEnd = currentTopRadius,
        bottomStart = currentBottomRadius, bottomEnd = currentBottomRadius,
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = currentElevation),
    modifier = Modifier
        .offset(x = currentX, y = currentY)
        .size(width = currentWidth, height = currentHeight)
        .then(coverSharedModifier)
) {
    Box(modifier = Modifier.fillMaxSize()) { ... }
}

// AFTER
val morphShape = RoundedCornerShape(
    topStart = currentTopRadius, topEnd = currentTopRadius,
    bottomStart = currentBottomRadius, bottomEnd = currentBottomRadius,
)
Box(
    modifier = Modifier
        .offset(x = currentX, y = currentY)
        .size(width = currentWidth, height = currentHeight)
        .shadow(elevation = currentElevation, shape = morphShape, clip = false)
        .clip(morphShape)
        .then(coverSharedModifier)
) {
    // ThumbnailImage + gradient (same as before, just moved out of nested Box)
}
```

Remove `Card(` and `CardDefaults.cardElevation(...)` — also remove the `Card` and `CardDefaults` imports if unused elsewhere.

---

### Change 2 — Hide drag handle when useMorphingCover = true (line ~399)

```kotlin
// BEFORE
Box(modifier = Modifier.fillMaxWidth().height(28.dp), contentAlignment = Alignment.Center) {
    Box(modifier = Modifier.size(width = 32.dp, height = 4.dp)
        .clip(RoundedCornerShape(2.dp))
        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)))
}

// AFTER
if (!useMorphingCover) {
    Box(modifier = Modifier.fillMaxWidth().height(28.dp), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size(width = 32.dp, height = 4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)))
    }
}
```

---

### Change 3 — Add top-fade gradient inside the card when useMorphingCover = true (line ~393)

Inside the card's outer `Box` (after the `scrimColor` background box, before the content Column), add a `backgroundColor` → `Color.Transparent` overlay. This visually continues the morphing cover's gradient into the card interior, eliminating the color mismatch at the boundary:

```kotlin
// Add after the scrimColor Box:
if (useMorphingCover) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .align(Alignment.TopCenter)
            .background(
                Brush.verticalGradient(
                    listOf(backgroundColor, Color.Transparent)
                )
            )
    )
}
```

---

### Change 4 — Increase morphing cover gradient height to 0.65f (line ~490)

```kotlin
// BEFORE
.fillMaxHeight(0.5f)

// AFTER
.fillMaxHeight(0.65f)
```

---

## Non-morphing path

All changes are guarded by `useMorphingCover` checks or are inside the morphing-only block (`if (useMorphingCover && expandFraction < 0.99f)`). The `useMorphingCover = false` path is completely unaffected.

## Expected Result

- **Collapsed state**: Image flows into card with no visible line, no shadow, no drag handle. The gradient + card top-fade produces a single continuous surface.
- **With immersive colors enabled**: No seam even when `backgroundColor` differs from pure surface.
- **With immersive colors disabled**: Same result.
- **Expand/collapse animation**: Unaffected — morph target fix from the previous plan still applies.
