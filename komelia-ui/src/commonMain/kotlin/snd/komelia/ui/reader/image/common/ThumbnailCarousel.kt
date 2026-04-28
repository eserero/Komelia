package snd.komelia.ui.reader.image.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import snd.komelia.ui.reader.image.PageMetadata

@Composable
fun ThumbnailCarousel(
    pages: List<PageMetadata>,
    currentPageIndex: Int,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = currentPageIndex)

    val flingBehavior = ScrollableDefaults.flingBehavior()

    LazyRow(
        state = lazyListState,
        flingBehavior = flingBehavior,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = modifier.height(200.dp)
    ) {
        itemsIndexed(
            items = pages,
            key = { _, page -> page.toPageId().toString() }
        ) { index, page ->
            BookPageThumbnail(
                page = page,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(0.7f)
                    .clickable { onPageChange(index) }
            )
        }
    }

    LaunchedEffect(currentPageIndex) {
        if (lazyListState.firstVisibleItemIndex != currentPageIndex) {
            lazyListState.scrollToItem(currentPageIndex)
        }
    }
}

