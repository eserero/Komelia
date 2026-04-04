# Plan: Unified `LibraryItemCard` Component

## Objective
Enhance the visual presentation of library items (Books, Series, Collections, Read Lists) by creating a single, fully unified thumbnail component (`LibraryItemCard`) that handles all layout options natively. This addresses the request to have all corners rounded for thumbnails in "Text below card" mode and to remove text indentation, while ensuring consistency across the entire app.

## Goals
1.  **Rounded Corners:** All four corners of the thumbnail should be rounded (12.dp) when "Text below card" is active.
2.  **No Text Indentation:** Text below the card should align perfectly with the horizontal edges of the thumbnail.
3.  **Smaller Text:** Text below the card should be 1 point/sp smaller than the standard overlay text.
4.  **Unified Component:** A single `LibraryItemCard` component to handle:
    *   Text Below Card (`cardLayoutBelow = true`)
    *   Text Overlay with Transparent Background (`overlayBackground = true`)
    *   Text Overlay without Background (`overlayBackground = false`)
5.  **Persistence:** Ensure progress bars (e.g., for books) remain visible on the image even in "Text Below Card" mode.

## Implementation Details

### 1. Unified `LibraryItemCard`
Create the `LibraryItemCard` in `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/common/cards/ItemCard.kt`.

```kotlin
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryItemCard(
    modifier: Modifier = Modifier,
    title: String,
    secondaryText: String? = null,
    secondaryTextTop: Boolean = false,
    isUnavailable: Boolean = false,
    titleBold: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    badges: @Composable BoxScope.() -> Unit = {},
    progress: @Composable BoxScope.() -> Unit = {},
    image: @Composable () -> Unit,
) {
    val cardLayoutBelow = LocalCardLayoutBelow.current
    val overlayBackground = LocalCardLayoutOverlayBackground.current

    val shape = if (cardLayoutBelow) RoundedCornerShape(12.dp) else RoundedCornerShape(8.dp)
    val color = if (cardLayoutBelow) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant
    val elevation = CardDefaults.cardElevation(defaultElevation = if (cardLayoutBelow) 0.dp else 2.dp)

    Card(
        shape = shape,
        modifier = modifier
            .combinedClickable(onClick = onClick ?: {}, onLongClick = onLongClick)
            .then(if (onClick != null || onLongClick != null) Modifier.cursorForHand() else Modifier),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = elevation
    ) {
        // Thumbnail Logic
        val imageShape = if (cardLayoutBelow) RoundedCornerShape(12.dp) else RoundedCornerShape(8.dp)

        Box(
            modifier = Modifier
                .aspectRatio(0.703f)
                .clip(imageShape)
        ) {
            image()
            badges()
            
            // Overlay Text Logic
            if (!cardLayoutBelow) {
                Box(
                    contentAlignment = Alignment.BottomStart, 
                    modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth()
                ) {
                    CardTextBackground()
                    Column(
                        modifier = Modifier
                            .height(48.dp)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.Top
                    ) {
                        val textColor = if (overlayBackground) MaterialTheme.colorScheme.onSurface else Color.White
                        val secondaryTextColor = if (overlayBackground) MaterialTheme.colorScheme.onSurfaceVariant else Color.White.copy(alpha = 0.8f)
                        val shadow = if (overlayBackground) null else Shadow(color = Color.Black, offset = Offset(1f, 1f), blurRadius = 4f)
                        
                        val primaryStyle = MaterialTheme.typography.bodyMedium.copy(
                            shadow = shadow,
                            fontWeight = if (titleBold) FontWeight.Bold else FontWeight.Normal,
                            fontSize = (MaterialTheme.typography.bodyMedium.fontSize.value - 1).sp
                        )
                        val secondaryStyle = MaterialTheme.typography.labelMedium.copy(
                            shadow = shadow,
                            fontSize = (MaterialTheme.typography.labelMedium.fontSize.value - 1).sp
                        )

                        if (isUnavailable) {
                            Text("Unavailable", style = primaryStyle, color = MaterialTheme.colorScheme.error, maxLines = 1)
                            Text(title, style = primaryStyle, color = textColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        } else {
                            val secondary = @Composable {
                                if (secondaryText != null) {
                                    Text(secondaryText, style = secondaryStyle, color = secondaryTextColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            val primary = @Composable {
                                Text(title, style = primaryStyle, color = textColor, maxLines = if (secondaryText == null) 2 else 1, overflow = TextOverflow.Ellipsis)
                            }

                            if (secondaryTextTop) { secondary(); primary() } else { primary(); secondary() }
                        }
                    }
                }
            }
            progress() // Always rendered on the image
        }
        
        // Below Card Text Logic
        if (cardLayoutBelow) {
            Column(
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp), // NO horizontal padding
                verticalArrangement = Arrangement.Center
            ) {
                val primaryStyle = MaterialTheme.typography.bodySmall
                val secondaryStyle = MaterialTheme.typography.labelSmall

                if (isUnavailable) {
                    Text("Unavailable", style = primaryStyle, color = MaterialTheme.colorScheme.error, maxLines = 1)
                    Text(title, style = primaryStyle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                } else {
                    val secondary = @Composable {
                        if (secondaryText != null) {
                            Text(secondaryText, style = secondaryStyle, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    val primary = @Composable {
                        Text(
                            text = title, 
                            style = primaryStyle, 
                            fontWeight = if (titleBold) FontWeight.Bold else null,
                            maxLines = if (secondaryText == null) 2 else 1, 
                            minLines = if (secondaryText == null) 2 else 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (secondaryTextTop) { secondary(); primary() } else { primary(); secondary() }
                }
            }
        }
    }
}
```

### 2. Migration Plan
1.  **`BookItemCard.kt`**:
    *   Replace `ItemCard` with `LibraryItemCard`.
    *   Pass `title = bookTitle`, `secondaryText = seriesTitle` (if applicable).
    *   Move `LinearProgressIndicator` to the `progress` slot.
    *   Delete `BookImageOverlay` and manual overlay Column logic.
2.  **`SeriesItemCard.kt`**:
    *   Replace `ItemCard` with `LibraryItemCard`.
    *   Pass `title = title`.
    *   Delete `SeriesImageOverlay`.
3.  **`CollectionItemCard.kt` / `ReadListItemCard.kt`**:
    *   Replace `ItemCard` with `LibraryItemCard`.
    *   Pass appropriate name and item counts.
4.  **`AppearanceSettingsContent.kt`**:
    *   Update preview card to use `LibraryItemCard`.

## Verification
*   **Mode "Text below card":** 12.dp rounded image corners, text flush to edges, text is `bodySmall`/`labelSmall`.
*   **Mode "Overlay":** Text is 1 point smaller than standard `bodyMedium`, correctly uses background or shadow based on settings.
*   **Book Progress:** Visible on image in both modes.
*   **Unavailable State:** Standardized across all card types.