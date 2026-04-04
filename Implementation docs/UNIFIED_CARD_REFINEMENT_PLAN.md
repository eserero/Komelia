# Plan: Unified `LibraryItemCard` Component (Refined)

## Objective
Address discrepancies found in the initial unification pass by strictly adhering to the legacy visual behaviors where requested, while maintaining a single, unified `LibraryItemCard` component. 

The three modes to support are:
1. **Text Below Card** (`cardLayoutBelow = true`)
2. **Text Overlay with Transparent Background** (`overlayBackground = true`, `cardLayoutBelow = false`)
3. **Text Overlay without Background** (`overlayBackground = false`, `cardLayoutBelow = false`)

## Required Fixes & Specifications

### 1. Typography & Ordering Unification
Across **ALL three modes**, the text styling must be consistent:
* **Book / Primary Title:** Must ALWAYS be **Bold**.
* **Series / Secondary Text:** Must ALWAYS be **Regular** weight.
* **Font Sizes:** Use `bodySmall` for the primary title and `labelSmall` for the secondary text universally, ensuring consistency.
* **Order:** When a Series name is displayed alongside a Book name, the Series name must appear **above** the Book name. (Achieved by passing `secondaryTextTop = true` from `BookItemCard`).

### 2. Shading & Gradient Rules
The rendering of gradients behind the text must perfectly respect the active mode:
* **Mode 1 (Below Card):** NO text gradients on the image.
* **Mode 2 (Overlay with Background):** NO gradients. The solid overlay card provides all necessary contrast. We must ensure `CardTopGradient` and `CardBottomGradient` are completely hidden.
* **Mode 3 (Overlay without Background):** Gradients must be **EXACTLY** as they were originally. `CardBottomGradient` (80.dp height) must be rendered behind the text, and `CardTopGradient` should be visible.

### 3. Overlay Dimensions & Placement
* **Mode 2 & 3 Placement:** The text block must remain glued to the absolute **bottom** of the thumbnail (fixing the legacy floating bug).
* **Mode 2 (Overlay with Background) Height Adjustment:** Reduce the height of the solid overlay background by ~20%. It was previously `48.dp`; it should be reduced to `38.dp`. The text inside it must be glued to the top of this reduced overlay (e.g., `padding(top = 2.dp)`).
* **Mode 3 (Overlay without Background) Height:** Keep the text wrapper height at `48.dp` so it sits nicely within the 80.dp gradient.

## Implementation Steps

### Step 1: Update `CardTextBackground`
Modify `CardTextBackground` in `ItemCard.kt` so the solid overlay is 38.dp high.
```kotlin
@Composable
fun CardTextBackground(modifier: Modifier = Modifier) {
    val overlayBackground = LocalCardLayoutOverlayBackground.current
    if (overlayBackground) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(38.dp) // Reduced by 20%
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
        )
    } else {
        CardBottomGradient(modifier)
    }
}
```

### Step 2: Update `LibraryItemCard`
Refine the typography and layout logic inside `LibraryItemCard`:
```kotlin
// ... inside the Box image scope
if (!cardLayoutBelow) {
    Box(
        contentAlignment = Alignment.BottomStart,
        modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth()
    ) {
        CardTextBackground()
        Column(
            modifier = Modifier
                .height(if (overlayBackground) 38.dp else 48.dp)
                .padding(
                    horizontal = 8.dp, 
                    top = if (overlayBackground) 2.dp else 4.dp, 
                    bottom = 4.dp
                ),
            verticalArrangement = Arrangement.Top
        ) {
            // Apply uniform styles
            val primaryStyle = MaterialTheme.typography.bodySmall.copy(
                shadow = shadow,
                fontWeight = FontWeight.Bold // Always bold
            )
            val secondaryStyle = MaterialTheme.typography.labelSmall.copy(
                shadow = shadow,
                fontWeight = FontWeight.Normal // Always regular
            )
            // ... render text using these styles
        }
    }
}
// ...
if (cardLayoutBelow) {
    // Apply exact same unified styles
    val primaryStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
    val secondaryStyle = MaterialTheme.typography.labelSmall
    // ...
}
```

### Step 3: Fix Gradient Visibility in Badges
In `BookItemCard.kt` and `SeriesItemCard.kt`, ensure `CardTopGradient` is only drawn when appropriate.
```kotlin
@Composable
private fun BookImageBadges(
    book: KomeliaBook,
    showGradients: Boolean,
) {
    // showGradients should be passed as (!cardLayoutBelow && !overlayBackground)
    if (showGradients) CardTopGradient()
    // ...
}
```
Update the invocation in `BookImageCard` and `SeriesImageCard`:
```kotlin
val overlayBackground = LocalCardLayoutOverlayBackground.current
// ...
BookImageBadges(
    book = book,
    libraryIsDeleted = libraryIsDeleted,
    showGradients = !cardLayoutBelow && !overlayBackground
)
```

### Step 4: Ensure Correct Parameter Passing
In `BookItemCard.kt`:
* Ensure `secondaryTextTop = true` is passed to `LibraryItemCard` so the Series name renders above the Book name.

## Verification
1. **Mode "Text below card"**: Check that the Book name is Bold, Series name is Regular.
2. **Mode "Overlay with Background"**: Check that the height is smaller (38.dp), text sits near the top of the background, Book name is Bold, Series is Regular, and Series is ON TOP of Book name. Verify NO top/bottom gradients are rendered over the image.
3. **Mode "Overlay without Background"**: Check that the text is anchored to the bottom, the 80.dp black bottom gradient is clearly visible behind the text, and top gradients appear. Book is Bold, Series is Regular.