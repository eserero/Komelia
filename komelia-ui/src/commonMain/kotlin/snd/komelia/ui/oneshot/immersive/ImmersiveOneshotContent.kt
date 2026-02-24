package snd.komelia.ui.oneshot.immersive

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import snd.komelia.image.coil.SeriesDefaultThumbnailRequest
import kotlin.math.roundToInt
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.ui.book.BookInfoColumn
import snd.komelia.ui.common.immersive.ImmersiveDetailFab
import snd.komelia.ui.common.immersive.ImmersiveDetailScaffold
import snd.komelia.ui.common.menus.BookMenuActions
import snd.komelia.ui.common.menus.OneshotActionsMenu
import snd.komelia.ui.dialogs.ConfirmationDialog
import snd.komelia.ui.dialogs.permissions.DownloadNotificationRequestDialog
import snd.komelia.ui.library.SeriesScreenFilter
import snd.komelia.ui.readlist.BookReadListsContent
import snd.komelia.ui.collection.SeriesCollectionsContent
import snd.komelia.ui.series.view.SeriesDescriptionRow
import snd.komga.client.collection.KomgaCollection
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.readlist.KomgaReadList
import snd.komga.client.series.KomgaSeries

@Composable
fun ImmersiveOneshotContent(
    series: KomgaSeries,
    book: KomeliaBook,
    library: KomgaLibrary,
    accentColor: Color?,
    onLibraryClick: (KomgaLibrary) -> Unit,
    onBookReadClick: (markReadProgress: Boolean) -> Unit,
    oneshotMenuActions: BookMenuActions,
    collections: Map<KomgaCollection, List<KomgaSeries>>,
    onCollectionClick: (KomgaCollection) -> Unit,
    onSeriesClick: (KomgaSeries) -> Unit,
    readLists: Map<KomgaReadList, List<KomeliaBook>>,
    onReadListClick: (KomgaReadList) -> Unit,
    onReadlistBookClick: (KomeliaBook, KomgaReadList) -> Unit,
    onFilterClick: (SeriesScreenFilter) -> Unit,
    onBookDownload: () -> Unit,
    cardWidth: Dp,
    onBackClick: () -> Unit,
) {
    var showDownloadConfirmationDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {

        ImmersiveDetailScaffold(
            coverData = SeriesDefaultThumbnailRequest(series.id),
            coverKey = series.id.value,
            cardColor = accentColor,
            immersive = true,
            topBarContent = {},   // Fixed overlay handles this
            fabContent = {},      // Fixed overlay handles this
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
                            BookStatsLine(book, Modifier
                                .padding(start = 16.dp, end = 16.dp, top = 4.dp)
                                .graphicsLayer { this.alpha = alpha })
                    }

                    // Header: book title + writers (year)
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
                                // Book title (2/3 headlineMedium, bold)
                                Text(
                                    text = book.metadata.title,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontSize = (headlineFs * 2f / 3f).sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                )
                                // Writers (year) — 10 sp
                                val writers = remember(book.metadata.authors) {
                                    book.metadata.authors
                                        .filter { it.role.lowercase() == "writer" }
                                        .joinToString(", ") { it.name }
                                }
                                val year = book.metadata.releaseDate?.year
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
                            BookStatsLine(book, Modifier
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .graphicsLayer { this.alpha = alpha })
                    }

                    // SeriesDescriptionRow (library, status, age rating, etc.)
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        SeriesDescriptionRow(
                            library = library,
                            onLibraryClick = onLibraryClick,
                            releaseDate = null,
                            status = null,
                            ageRating = series.metadata.ageRating,
                            language = series.metadata.language,
                            readingDirection = series.metadata.readingDirection,
                            deleted = series.deleted || library.unavailable,
                            alternateTitles = series.metadata.alternateTitles,
                            onFilterClick = onFilterClick,
                            showReleaseYear = false,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }

                    // Summary
                    if (book.metadata.summary.isNotBlank()) {
                        item {
                            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                Text(
                                    text = book.metadata.summary,
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
                                publisher = series.metadata.publisher,
                                genres = series.metadata.genres,
                                authors = book.metadata.authors,
                                tags = book.metadata.tags,
                                links = book.metadata.links,
                                sizeInMiB = book.size,
                                mediaType = book.media.mediaType,
                                isbn = book.metadata.isbn,
                                fileUrl = book.url,
                                onFilterClick = onFilterClick,
                            )
                        }
                    }

                    // Reading lists
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        BookReadListsContent(
                            readLists = readLists,
                            onReadListClick = onReadListClick,
                            onBookClick = onReadlistBookClick,
                            cardWidth = cardWidth,
                        )
                    }

                    // Collections
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        SeriesCollectionsContent(
                            collections = collections,
                            onCollectionClick = onCollectionClick,
                            onSeriesClick = onSeriesClick,
                            cardWidth = cardWidth,
                        )
                    }
                }
            }
        )

        // Fixed overlay: back button + 3-dot menu
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
                OneshotActionsMenu(
                    series = series,
                    book = book,
                    actions = oneshotMenuActions,
                    expanded = expandActions,
                    onDismissRequest = { expandActions = false },
                )
            }
        }

        // Fixed overlay: FAB
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 16.dp)
        ) {
            ImmersiveDetailFab(
                onReadClick = { onBookReadClick(true) },
                onReadIncognitoClick = { onBookReadClick(false) },
                onDownloadClick = { showDownloadConfirmationDialog = true },
                accentColor = accentColor,
                showReadActions = true,
            )
        }
    }

    if (showDownloadConfirmationDialog) {
        var permissionRequested by remember { mutableStateOf(false) }
        DownloadNotificationRequestDialog { permissionRequested = true }
        if (permissionRequested) {
            ConfirmationDialog(
                body = "Download \"${book.metadata.title}\"?",
                onDialogConfirm = {
                    onBookDownload()
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
