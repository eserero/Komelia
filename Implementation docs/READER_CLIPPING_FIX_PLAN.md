# Fix Thumbnail Clipping

## Objective
Fix the clipping of floating thumbnails in the new UI control card.

## Implementation Steps
1. Modify `ReaderControlsCard.kt` to separate the background shape/shadow from the content container.
2. Use `matchParentSize()` for the background to retain the shadow and haze effects without clipping the overflowing content of the slider (thumbnails and labels).

## Verification
Ensure thumbnails are fully visible above the control card when dragging the slider.