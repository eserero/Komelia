# Immersive Screen New UI V2 Improvements

## Objective
Implement 5 requested improvements to the "New UI V2" immersive screens for Books, Series, and Oneshots to enhance legibility, layout, and interaction parity with "New UI" (v1).

## Changes

1.  **Series Name Tappable Link (`ImmersiveHeroText.kt` & `ImmersiveBookContent.kt`)**
    *   Add an `onSeriesClick: (() -> Unit)? = null` parameter to `ImmersiveHeroText`.
    *   Apply a `.clickable(onClick = onSeriesClick)` modifier to the series title `Text` if the callback is provided.
    *   In `ImmersiveBookContent.kt`, pass `onSeriesClick = { onSeriesClick(pageBook.seriesId) }` to `ImmersiveHeroText` in both `heroTextContent` and `cardContent` closures.

2.  **Author Name Legibility (`ImmersiveHeroText.kt`)**
    *   **Keep the accent color** as requested.
    *   To increase legibility over the hero image, we will significantly strengthen the text shadow for the author name. Currently, it uses a soft blur (`alpha = 0.5f`, `blurRadius = 8f`).
    *   We will apply a stronger, more defined drop-shadow (e.g., `Color.Black.copy(alpha = 0.8f)` with `blurRadius = 12f` and a slight `offset = Offset(0f, 2f)`) mimicking the robust legibility techniques used in high-contrast web designs.

3.  **Auto-Shrink Large Series Names (`ImmersiveHeroText.kt`)**
    *   Introduce `var titleMultiplier by remember(seriesTitle) { mutableFloatStateOf(1f) }` to scale the font size and line height.
    *   Add an `onTextLayout` callback to the series title `Text` component. If `textLayoutResult.hasVisualOverflow` is true, reduce the multiplier (e.g., `titleMultiplier *= 0.9f`) until the text fits within the allowed 2 lines.

4.  **Bottom-Aligned Text Layout (`ImmersiveDetailScaffold.kt`)**
    *   Change the logic of `startTextY` for `heroTextContent`. Instead of a hardcoded `collapsedOffset - 120.dp` (which anchors the *top* of the text block and leaves empty space at the bottom if the series name is only 1 line), we will track the measured height of the entire text block via `onGloballyPositioned`.
    *   Calculate `startTextY` dynamically: `collapsedOffset - with(density) { textHeight.toDp() } - 24.dp`.
    *   **Note on spacing:** Because the `ImmersiveHeroText` internally uses a `Column` that tightly packs its children, the author name will naturally stay immediately above the series name without any artificial gap. Sliding the entire block down ensures the bottom of the series name is always cleanly anchored near the image edge.

5.  **Thumbnail Top Margin Overlap Fix (`ImmersiveBookContent.kt`, `ImmersiveSeriesContent.kt`, `ImmersiveOneshotContent.kt`)**
    *   Currently, the thumbnail has a fixed `thumbnailTopGap = 20.dp`. In "New UI" (v1), the card included a 28dp drag handle which naturally pushed the thumbnail below the top bar/back button. In "New UI V2", the drag handle is removed, causing the thumbnail to overlap with the back button.
    *   Change `val thumbnailTopGap = 20.dp` to `val thumbnailTopGap = if (useNewUi2) 48.dp else 20.dp` in all three files to explicitly provide the extra spacing in V2, perfectly replicating the exact top position from V1.

## Verification
*   Verify tapping the series name in a book's immersive screen navigates to the series.
*   Verify the author/year text over the hero image uses the accent color but has a strong enough shadow to be easily readable against varied backgrounds.
*   Verify titles with long text scale down slightly to fit within 2 lines.
*   Verify 1-line series names appear anchored closer to the bottom of the hero image, with the author name immediately above it (no gap).
*   Verify expanding the card (scrolling down) positions the thumbnail correctly below the back button in "New UI V2" without overlap.
