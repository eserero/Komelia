package snd.komelia.ui.book.immersive

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import snd.komelia.DefaultDateTimeFormats.localDateTimeFormat
import snd.komelia.image.coil.BookDefaultThumbnailRequest
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.ui.book.BookInfoColumn
import snd.komelia.ui.common.immersive.ImmersiveDetailFab
import snd.komelia.ui.common.immersive.ImmersiveDetailScaffold
import snd.komelia.ui.common.menus.BookActionsMenu
import snd.komelia.ui.common.menus.BookMenuActions
import snd.komelia.ui.dialogs.ConfirmationDialog
import snd.komelia.ui.dialogs.permissions.DownloadNotificationRequestDialog
import snd.komelia.ui.library.SeriesScreenFilter
import snd.komelia.ui.readlist.BookReadListsContent
import snd.komga.client.readlist.KomgaReadList
import snd.komga.client.series.KomgaSeriesId
import kotlin.math.roundToInt

@Composable
fun ImmersiveBookContent(
    book: KomeliaBook,
    siblingBooks: List<KomeliaBook>,
    accentColor: Color?,
    bookMenuActions: BookMenuActions,
    onBackClick: () -> Unit,
    onReadBook: (KomeliaBook, Boolean) -> Unit,
    onDownload: () -> Unit,
    onFilterClick: (SeriesScreenFilter) -> Unit,
    readLists: Map<KomgaReadList, List<KomeliaBook>>,
    onReadListClick: (KomgaReadList) -> Unit,
    onReadListBookPress: (KomeliaBook, KomgaReadList) -> Unit,
    cardWidth: Dp,
    onSeriesClick: (KomgaSeriesId) -> Unit,
) {
    val initialPage = remember(siblingBooks, book) {
        siblingBooks.indexOfFirst { it.id == book.id }.coerceAtLeast(0)
    }
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { maxOf(1, siblingBooks.size) }
    )

    // Once siblings load, jump to the correct page without animation.
    // Guard with initialScrollDone so that subsequent siblingBooks emissions
    // (e.g. from read-progress updates) don't yank the pager back.
    var initialScrollDone by remember { mutableStateOf(false) }
    LaunchedEffect(siblingBooks) {
        if (!initialScrollDone && siblingBooks.isNotEmpty()) {
            val idx = siblingBooks.indexOfFirst { it.id == book.id }.coerceAtLeast(0)
            pagerState.scrollToPage(idx)
            initialScrollDone = true
        }
    }

    // selectedBook drives the FAB and 3-dot menu after each swipe settles
    val selectedBook = remember(pagerState.settledPage, siblingBooks) {
        siblingBooks.getOrNull(pagerState.settledPage) ?: book
    }

    var showDownloadConfirmationDialog by remember { mutableStateOf(false) }
    var sharedExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {

        // Outer HorizontalPager — slides the entire scaffold (cover + card) laterally
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            pageSpacing = 8.dp,
        ) { pageIndex ->
            val pageBook = siblingBooks.getOrNull(pageIndex) ?: book
            // Memoize to avoid a new Random requestCache on every recomposition, which would
            // cause ThumbnailImage's remember(data,cacheKey) to rebuild the ImageRequest and flash.
            val coverData = remember(pageBook.id) { BookDefaultThumbnailRequest(pageBook.id) }

            ImmersiveDetailScaffold(
                coverData = coverData,
                coverKey = pageBook.id.value,
                cardColor = accentColor,
                immersive = true,
                initiallyExpanded = sharedExpanded,
                onExpandChange = { sharedExpanded = it },
                topBarContent = {},  // Fixed overlay handles this
                fabContent = {},     // Fixed overlay handles this
                cardContent = { expandFraction ->
                    val thumbnailOffset = (126.dp * expandFraction).coerceAtLeast(0.dp)
                    val thumbnailTopGap = 20.dp
                    val thumbnailHeight = 110.dp / 0.703f // ≈ 156.5 dp

                    val navBarBottom = with(LocalDensity.current) {
                        WindowInsets.navigationBars.getBottom(this).toDp()
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(0.dp),
                        contentPadding = PaddingValues(bottom = navBarBottom + 80.dp),
                    ) {
                        // Collapsed stats line (fades out as card expands)
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            val alpha = (1f - expandFraction * 2f).coerceIn(0f, 1f)
                            if (alpha > 0.01f)
                                BookStatsLine(pageBook, Modifier
                                    .padding(start = 16.dp, end = 16.dp, top = 4.dp)
                                    .graphicsLayer { this.alpha = alpha })
                        }

                        // Header: thumbnail offset + series title · #N, book title, writers (year)
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = (thumbnailTopGap + thumbnailHeight) * expandFraction)
                                    .padding(
                                        start = thumbnailOffset + 16.dp,
                                        end = 16.dp,
                                        top = lerp(8f, thumbnailTopGap.value, expandFraction).dp,
                                    )
                            ) {
                                Column {
                                    val headlineFs = MaterialTheme.typography.headlineMedium.fontSize.value
                                    // Line 1: Series · #N (2/3 headlineMedium, bold) — tappable link
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable { onSeriesClick(pageBook.seriesId) }
                                            .padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    ) {
                                        Text(
                                            text = "${pageBook.seriesTitle} · #${pageBook.metadata.number}",
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontSize = (headlineFs * 2f / 3f).sp,
                                                fontWeight = FontWeight.Bold,
                                            ),
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                        )
                                    }
                                    // Line 2: Book title (bodySmall) — only if different from series title
                                    if (pageBook.metadata.title != pageBook.seriesTitle) {
                                        Text(
                                            text = pageBook.metadata.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(top = 2.dp),
                                        )
                                    }
                                    // Line 3: Writers (year) — 10 sp
                                    val writers = remember(pageBook.metadata.authors) {
                                        pageBook.metadata.authors
                                            .filter { it.role.lowercase() == "writer" }
                                            .joinToString(", ") { it.name }
                                    }
                                    val year = pageBook.metadata.releaseDate?.year
                                    val writersYearText = buildString {
                                        if (writers.isNotEmpty()) append(writers)
                                        if (year != null) {
                                            if (writers.isNotEmpty()) append(" ")
                                            append("($year)")
                                        }
                                    }
                                    if (writersYearText.isNotEmpty()) {
                                        Text(
                                            text = writersYearText,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(top = 2.dp),
                                        )
                                    }
                                }
                            }
                        }

                        // Expanded stats line (fades in as card expands)
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            val alpha = (expandFraction * 2f - 1f).coerceIn(0f, 1f)
                            if (alpha > 0.01f)
                                BookStatsLine(pageBook, Modifier
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .graphicsLayer { this.alpha = alpha })
                        }

                        // Summary
                        if (pageBook.metadata.summary.isNotBlank()) {
                            item {
                                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                    Text(
                                        text = pageBook.metadata.summary,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                        }

                        // Divider
                        item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }

                        // Book metadata (authors, tags, links, file info, ISBN)
                        item {
                            Box(Modifier.padding(horizontal = 16.dp)) {
                                BookInfoColumn(
                                    publisher = null,
                                    genres = null,
                                    authors = pageBook.metadata.authors,
                                    tags = pageBook.metadata.tags,
                                    links = pageBook.metadata.links,
                                    sizeInMiB = pageBook.size,
                                    mediaType = pageBook.media.mediaType,
                                    isbn = pageBook.metadata.isbn,
                                    fileUrl = pageBook.url,
                                    onFilterClick = onFilterClick,
                                )
                            }
                        }

                        // Reading lists
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            BookReadListsContent(
                                readLists = readLists,
                                onReadListClick = onReadListClick,
                                onBookClick = onReadListBookPress,
                                cardWidth = cardWidth,
                            )
                        }
                    }
                }
            )
        }

        // Fixed overlay: back button + 3-dot menu (stays still while pager slides)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 12.dp, end = 4.dp, top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                    .clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
            }

            var expandActions by remember { mutableStateOf(false) }
            Box {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                        .clickable { expandActions = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = null, tint = Color.White)
                }
                BookActionsMenu(
                    book = selectedBook,
                    actions = bookMenuActions,
                    expanded = expandActions,
                    showEditOption = true,
                    showDownloadOption = false,  // download is in FAB
                    onDismissRequest = { expandActions = false },
                )
            }
        }

        // Fixed overlay: FAB (stays still while pager slides)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 16.dp)
        ) {
            ImmersiveDetailFab(
                onReadClick = { onReadBook(selectedBook, true) },
                onReadIncognitoClick = { onReadBook(selectedBook, false) },
                onDownloadClick = { showDownloadConfirmationDialog = true },
                accentColor = accentColor,
                showReadActions = true,
            )
        }
    }

    // Two-step download confirmation dialog
    if (showDownloadConfirmationDialog) {
        var permissionRequested by remember { mutableStateOf(false) }
        DownloadNotificationRequestDialog { permissionRequested = true }
        if (permissionRequested) {
            ConfirmationDialog(
                body = "Download \"${selectedBook.metadata.title}\"?",
                onDialogConfirm = {
                    onDownload()
                    showDownloadConfirmationDialog = false
                },
                onDialogDismiss = { showDownloadConfirmationDialog = false },
            )
        }
    }
}

@Composable
private fun BookStatsLine(book: KomeliaBook, modifier: Modifier = Modifier) {
    val pagesCount = book.media.pagesCount
    val segments = remember(book) {
        buildList {
            add("$pagesCount page${if (pagesCount == 1) "" else "s"}")
            book.metadata.releaseDate?.let { add(it.toString()) }
            book.readProgress?.let { progress ->
                if (!progress.completed) {
                    val pagesLeft = pagesCount - progress.page
                    val pct = (progress.page.toFloat() / pagesCount * 100).roundToInt()
                    add("$pct%, $pagesLeft page${if (pagesLeft == 1) "" else "s"} left")
                }
                add(progress.readDate
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .format(localDateTimeFormat))
            }
        }
    }
    if (segments.isEmpty()) return
    Text(
        text = segments.joinToString(" | "),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}
