package snd.komelia.ui.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import snd.komelia.ui.LoadState
import snd.komelia.ui.LocalPlatform
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.ReloadableScreen
import snd.komelia.ui.book.bookScreen
import snd.komelia.ui.common.components.ErrorContent
import snd.komelia.ui.common.components.LoadingMaxSizeIndicator
import snd.komelia.ui.platform.BackPressHandler
import snd.komelia.ui.platform.PlatformType
import snd.komelia.ui.platform.ScreenPullToRefreshBox
import snd.komelia.ui.series.seriesScreen

class SearchScreen(
    private val initialQuery: String?,
) : ReloadableScreen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel(initialQuery) {
            viewModelFactory.getSearchViewModel()
        }
        LaunchedEffect(initialQuery) { vm.initialize(initialQuery) }

        val navigator = LocalNavigator.currentOrThrow

        ScreenPullToRefreshBox(screenState = vm.state, onRefresh = vm::reload) {
            if (LocalPlatform.current == PlatformType.MOBILE) {
                val state by vm.state.collectAsState()
                SearchBarWithResults(
                    query = vm.query,
                    onQueryChange = { vm.query = it },
                    isLoading = state == LoadState.Loading,
                    onBack = { navigator.pop() },
                    startExpanded = true,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    when (state) {
                        is LoadState.Error -> ErrorContent(
                            (state as LoadState.Error).exception.message ?: "Error",
                            onReload = vm::reload
                        )

                        LoadState.Uninitialized, LoadState.Loading -> LoadingMaxSizeIndicator()
                        is LoadState.Success -> SearchContent(
                            query = vm.query,
                            searchType = vm.currentTab,
                            onSearchTypeChange = vm::onSearchTypeChange,

                            seriesResults = vm.seriesResults,
                            seriesCurrentPage = vm.seriesCurrentPage,
                            seriesTotalPages = vm.seriesTotalPages,
                            onSeriesPageChange = vm::onSeriesPageChange,
                            onSeriesClick = { navigator.push(seriesScreen(it)) },

                            bookResults = vm.bookResults,
                            bookCurrentPage = vm.bookCurrentPage,
                            bookTotalPages = vm.bookTotalPages,
                            onBookPageChange = vm::onBookPageChange,
                            onBookClick = { navigator.push(bookScreen(it)) },
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (val state = vm.state.collectAsState().value) {
                        is LoadState.Error -> ErrorContent(
                            state.exception.message ?: "Error",
                            onReload = vm::reload
                        )

                        LoadState.Uninitialized, LoadState.Loading -> LoadingMaxSizeIndicator()
                        is LoadState.Success -> {
                            SearchContent(
                                query = vm.query,
                                searchType = vm.currentTab,
                                onSearchTypeChange = vm::onSearchTypeChange,

                                seriesResults = vm.seriesResults,
                                seriesCurrentPage = vm.seriesCurrentPage,
                                seriesTotalPages = vm.seriesTotalPages,
                                onSeriesPageChange = vm::onSeriesPageChange,
                                onSeriesClick = { navigator.push(seriesScreen(it)) },

                                bookResults = vm.bookResults,
                                bookCurrentPage = vm.bookCurrentPage,
                                bookTotalPages = vm.bookTotalPages,
                                onBookPageChange = vm::onBookPageChange,
                                onBookClick = { navigator.push(bookScreen(it)) },
                            )
                        }
                    }
                }
            }
            BackPressHandler { navigator.pop() }
        }
    }
}
