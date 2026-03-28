# Immersive Hero Text Redesign Plan

## Objective
Update the New UI 2 Immersive View for Series, Oneshot, and Books to match the layout and typography of the provided `immersive_light.html` reference. The title and metadata text will be repositioned to overlay the hero image when collapsed, and morph into its final position in the card header when expanded.

## Visual Changes: Current vs. Proposed State

### How it looks today
- **Collapsed View (User at the top):** The hero image is full-width and full-height down to the card. The title text (Series Name, Book Title, Authors) is drawn *inside* the white/dark card, sitting immediately *below* the hero image.
- **Expanded View (User scrolls up):** The card covers the screen, the hero image morphs into a small thumbnail in the top-left, and the text sits to the right of the thumbnail inside the card.
- **Book Title Format:** Includes a `#` (e.g. `Series Name · #1`). The book chapter title is shown as standard text.

### How it will look (Proposed Changes)
1. **Collapsed State (`expandFraction = 0`):**
   - **Location:** The text is completely removed from the card and placed directly **on top of the hero image**, aligned to the bottom-left of the image overlay.
   - **Styling:** The text will have a white color with a drop shadow (for legibility against the image).
   - **Layout Stack (bottom to top):**
     - **Chapter Name** (for books): Bottom-aligned, uppercase, tracking wide, sans-serif.
     - **Series Title**: Serif font (`FontFamily.Serif`), bold, uppercase, up to 2 lines (bottom-aligned). For books, this includes the book number without the `#` (e.g., `SERIES NAME 1`).
     - **Author & Year**: Top-aligned, uppercase, tracking wide, using the app's accent color.
2. **End State (`expandFraction = 1`):**
   - **Location:** As the user scrolls up, the text will seamlessly transition into the card header to sit exactly to the right of the shrunk thumbnail (the same final location as today).
   - **Styling:** The text color fades to standard Surface text colors (`onSurface`, `onSurfaceVariant`), and the font sizes shrink to fit the card header.
   - **Scroll Behavior:** Once fully expanded, the text becomes part of the scrollable grid and scrolls away naturally as the user reads down the page.
3. **The Transition (The Driver):**
   - The transition is driven by the `expandFraction` (0 to 1) provided by the `AnchoredDraggableState` as the user drags the card up. 
   - While dragging, the text's `X` and `Y` coordinates, font sizes, line heights, and font colors are linearly interpolated (`lerp`) from their starting positions on the image to their target positions inside the card header.

## Implementation Steps

### 1. Create `ImmersiveHeroText.kt`
Create a new reusable composable `ImmersiveHeroText` in `snd.komelia.ui.common.immersive`:
- Accept `expandFraction` to dynamically interpolate font sizes, line heights, and colors.
- Layout the Author, Title, and Chapter using the exact font families and spacing prescribed by the HTML reference.
- Use a drop shadow and white text when `expandFraction < 0.5f` to ensure visibility against the background image.

### 2. Update `ImmersiveDetailScaffold.kt` to Handle Morphing Overlay
- **Add Parameter:** `heroTextContent: (@Composable (expandFraction: Float) -> Unit)? = null`.
- **Target Tracking:** Update the `cardContent` lambda to expose `onTextPositioned: (LayoutCoordinates) -> Unit`. The scaffold will use this to remember the exact `targetTextOffset` where the text must end up.
- **Overlay Rendering:** In Layer 2.75 (the Morphing Cover Overlay), invoke `heroTextContent` inside a custom `Layout` modifier. This layout will calculate `currentX` and `currentY` by interpolating between the start position (bottom of the hero image) and the `targetTextOffset` based on `expandFraction`.

### 3. Update `ImmersiveSeriesContent.kt` & `ImmersiveOneshotContent.kt`
- Construct the new text properties (Series Title, Author+Year).
- Pass `ImmersiveHeroText` to the `ImmersiveDetailScaffold` via `heroTextContent`.
- In the `LazyVerticalGrid`'s first item, render the exact same `ImmersiveHeroText` (with `expandFraction = 1f`) wrapped in a `Box`. This Box will call `onTextPositioned` so the Scaffold knows the target location. It will also use `alpha = if (expandFraction > 0.99f) 1f else 0f` so it seamlessly takes over from the floating overlay text once fully expanded.

### 4. Update `ImmersiveBookContent.kt`
- Construct the `title` as `"$seriesTitle ${book.metadata.number}"` (dropping the `· #`).
- Pass the book's specific title as the `chapter` parameter to match the styling requirements.
- Apply the same dual-rendering logic (Scaffold overlay + Grid item) as Series.

## Verification & Testing
- Swipe up and down on the immersive view to verify the text smoothly morphs in position, scale, and color without jumping or flashing.
- Verify text is securely anchored to the bottom-left of the image when fully collapsed.
- Verify that the text inside the card header accurately scrolls away when the user scrolls the grid.