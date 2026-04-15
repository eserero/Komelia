---
# Komelia-korg
title: 'T3: Add sharedElement modifiers to source thumbnails'
status: completed
type: task
priority: normal
created_at: 2026-02-23T12:02:46Z
updated_at: 2026-03-07T23:39:17Z
parent: Komelia-uler
---

Add Modifier.sharedElement on book/series cover images in library and home screens.

**Files:**
- `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/common/images/SeriesThumbnail.kt`
- `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/common/images/BookThumbnail.kt`

**Pattern for SeriesThumbnail:**
```kotlin
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SeriesThumbnail(
    seriesId: KomgaSeriesId,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current
    val sharedModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Modifier.sharedBounds(
                rememberSharedContentState(key = "cover-${seriesId.value}"),
                animatedVisibilityScope = animatedVisibilityScope,
                enter = fadeIn(),
                exit = fadeOut(),
                resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(),
            )
        }
    } else Modifier
    // ... rest unchanged, but add sharedModifier to ThumbnailImage modifier
}
```

Same pattern for `BookThumbnail` with key `"cover-${bookId.value}"`.

**ImmersiveDetailScaffold cover (T5):**
The scaffold's cover `ThumbnailImage` also needs the same sharedElement modifier:
```kotlin
val sharedModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
    with(sharedTransitionScope) {
        Modifier.sharedBounds(
            rememberSharedContentState(key = "cover-$coverKey"),
            animatedVisibilityScope = animatedVisibilityScope,
            enter = fadeIn(),
            exit = fadeOut(),
            resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(),
        )
    }
} else Modifier
```
Apply sharedModifier BEFORE graphicsLayer so both the source and destination share the same element.

**Subtasks:**
- [ ] Add `thumbnailKey` read from `LocalSharedTransitionScope.current` in `SeriesThumbnail.kt`
- [ ] Apply `Modifier.sharedElement(key="cover-${seriesId.value}", ...)` to the inner `ThumbnailImage` modifier when scopes are non-null
- [ ] Same for `BookThumbnail.kt` with key `"cover-${bookId.value}"`
- [ ] Apply matching sharedElement modifier to the cover in `ImmersiveDetailScaffold.kt` using `coverKey`
- [ ] Import `LocalSharedTransitionScope`, `LocalAnimatedVisibilityScope` from `snd.komelia.ui`
- [ ] Add `@OptIn(ExperimentalSharedTransitionApi::class)` to affected composables

## Implementation note (refined)

Use `sharedBounds` (not `sharedElement`) everywhere — the container changes size and shape between grid thumbnail and Layer 1 full-width cover.

**Easing must match the scaffold motion system:**
- Scaffold expand: `tween(500ms, CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f))` — M3 Emphasized
- Scaffold collapse: `tween(200ms, CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f))` — M3 Emphasized Accelerate

`sharedBounds` has two independent channels: `boundsTransform` (rectangle morph — the main hero motion, defaults to spring if not set = wrong feel) and `enter`/`exit` (content cross-fade inside the bounds). Set `boundsTransform = { _, _ -> tween(500, emphasizedEasing) }` on enter and the accelerate variant on exit. Keep `fadeIn`/`fadeOut` short (150 ms) so they don't compete with the bounds morph.

**Modifier order in ImmersiveDetailScaffold:** `.then(coverSharedModifier)` must come **before** `.graphicsLayer { alpha = ... }` so the shared transition composites before the alpha fade is applied.
