package snd.komelia.ui.series

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import snd.komelia.ui.BookSiblingsContext
import snd.komelia.ui.LoadState.Error
import snd.komelia.ui.LocalReloadEvents
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.ReloadableScreen
import snd.komelia.ui.book.bookScreen
import snd.komelia.ui.collection.CollectionScreen
import snd.komelia.ui.common.components.ErrorContent
import snd.komelia.ui.library.LibraryScreen
import snd.komelia.ui.oneshot.OneshotScreen
import snd.komelia.ui.platform.BackPressHandler
import snd.komelia.ui.platform.ScreenPullToRefreshBox
import snd.komelia.ui.reader.readerScreen
import snd.komelia.ui.series.SeriesViewModel.SeriesTab
import snd.komelia.ui.series.view.SeriesContent
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesId
import kotlin.jvm.Transient

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import snd.komelia.image.coil.SeriesDefaultThumbnailRequest
import snd.komelia.ui.LocalAccentColor
import snd.komelia.ui.LocalPlatform
import snd.komelia.ui.LocalUseNewLibraryUI
import snd.komelia.ui.common.immersive.ImmersiveDetailScaffold
import snd.komelia.ui.platform.PlatformType

fun seriesScreen(series: KomgaSeries): Screen =
    if (series.oneshot) OneshotScreen(series, BookSiblingsContext.Series)
    else SeriesScreen(series)

class SeriesScreen(
    val seriesId: KomgaSeriesId,
    @Transient
    private val series: KomgaSeries? = null,
    @Transient
    private val startingTab: SeriesTab? = SeriesTab.BOOKS,
) : ReloadableScreen {

    constructor(series: KomgaSeries, startingTab: SeriesTab = SeriesTab.BOOKS) : this(
        series.id,
        series,
        startingTab
    )

    override val key: ScreenKey = seriesId.toString()

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel(seriesId.value) {
            viewModelFactory.getSeriesViewModel(seriesId, series, startingTab)
        }
        val reloadEvents = LocalReloadEvents.current
        LaunchedEffect(seriesId) {
            vm.initialize()
            val series = vm.series.value
            if (series != null && series.oneshot) {
                navigator.replace(OneshotScreen(series, BookSiblingsContext.Series))
                return@LaunchedEffect
            }
            reloadEvents.collect { vm.reload() }
        }

        DisposableEffect(Unit) {
            vm.startKomgaEventHandler()
            onDispose { vm.stopKomgaEventHandler() }
        }

        val platform = LocalPlatform.current
        val useNewUI = LocalUseNewLibraryUI.current
        if (platform == PlatformType.MOBILE && useNewUI) {
            ImmersiveDetailScaffold(
                coverData = SeriesDefaultThumbnailRequest(seriesId),
                coverKey = "series-$seriesId",
                cardColor = LocalAccentColor.current,
                immersive = true,
                topBarContent = {
                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp, top = 8.dp)
                            .size(36.dp)
                            .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                            .clickable { onBackPress(navigator, vm.series.value?.libraryId) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                fabContent = {
                    Button(
                        onClick = {},
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text("Boilerplate FAB")
                    }
                },
                cardContent = { expandFraction ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .padding(start = (126.dp * expandFraction).coerceAtLeast(0.dp))
                    ) {
                        Text(
                            text = vm.series.collectAsState().value?.metadata?.title ?: "Loading...",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("Immersive Detail Boilerplate")
                        Text("Scroll anywhere on the card to see the cover shrink animation.")

                        // Add some height to enable scrolling/dragging if needed
                        Spacer(Modifier.height(1000.dp))
                    }
                }
            )

            BackPressHandler {
                vm.series.value?.let { onBackPress(navigator, it.libraryId) }
            }
            return
        }

        ScreenPullToRefreshBox(screenState = vm.state, onRefresh = vm::reload) {
            when (val state = vm.state.collectAsState().value) {
                is Error -> ErrorContent(
                    message = state.exception.message ?: "Unknown Error",
                    onReload = vm::reload
                )

                else -> {
                    SeriesContent(
                        series = vm.series.collectAsState().value,
                        library = vm.library.collectAsState().value,
                        onLibraryClick = { navigator.push(LibraryScreen(it.id)) },
                        seriesMenuActions = vm.seriesMenuActions(),
                        onFilterClick = { filter ->
                            val series = requireNotNull(vm.series.value)
                            navigator.push(LibraryScreen(series.libraryId, filter))
                        },

                        currentTab = vm.currentTab,
                        onTabChange = vm::onTabChange,

                        booksState = vm.booksState,
                        onBookClick = { navigator push bookScreen(it) },
                        onBookReadClick = { book, markProgress ->
                            navigator.parent?.push(readerScreen(book, markProgress))
                        },

                        collectionsState = vm.collectionsState,
                        onCollectionClick = { collection -> navigator.push(CollectionScreen(collection.id)) },
                        onSeriesClick = { series ->
                            navigator.push(
                                if (series.oneshot) OneshotScreen(series, BookSiblingsContext.Series)
                                else SeriesScreen(series, vm.currentTab)
                            )
                        },
                        onDownload = vm::onDownload
                    )
                }
            }

            BackPressHandler {
                vm.series.value?.let { onBackPress(navigator, it.libraryId) }
            }
        }
    }

    private fun onBackPress(navigator: Navigator, libraryId: KomgaLibraryId?) {
        if (navigator.canPop) {
            navigator.pop()
        } else {
            libraryId?.let { navigator replaceAll LibraryScreen(it) }
        }
    }
}
