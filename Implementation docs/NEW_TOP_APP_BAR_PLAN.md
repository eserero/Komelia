# New UI2 Top App Bar & Screen Redesign Plan

## Context

Implements a comprehensive top app bar and screen layout redesign for the Komelia app, gated behind the `LocalUseNewLibraryUI2` flag. Reference designs: `Implementation docs/stitch ui/library_dark_mode.html` and `library_light_mode.html`. Targets mobile layout only (`MobileLayout` in `MainScreen.kt`).

---

## Design Overview

### New `NewTopAppBar` composable
Replaces per-screen toolbars on Library, Home, and Search screens.

- **Height:** ~45dp content height (≈30% less than M3 TopAppBar's 64dp)
- **Left:** Hamburger icon (accent color) + "Komelia" in NotoSerif Bold Italic (not accented)
- **Right:** Theme toggle icon (accent color) + optional 3-dots MoreVert (library screen only)
- **Background:** Frosted glass via `hazeEffect` on all themes when newUI2 is active
  - Modern themes (`transparentBars = true`): extends visually into Android status bar area
  - Non-modern themes: floats over content that starts below the status bar
- **Gating:** Only shown on Library, Home, and Search screens when `LocalUseNewLibraryUI2 = true`

### Library screen changes (newUI2)
- Remove `LibraryToolBar` (count chip + PageSizeDropdown + 3-dots)
- Replace `LibrarySegmentedButtons` (segmented buttons) with `LibraryTabChips` (FilterChip style matching home screen)
- Add `LibraryHeaderSection` as `beforeContent` in the lazy grid:
  - Left: library name (large, NotoSerif Bold) + item count below (small, muted, uppercase)
  - Right: `PageSizeSelectionDropdown`

### Home screen changes (newUI2)
- Add floating `NewTopAppBar`
- Add `HomeHeaderSection` ("Home" in large NotoSerif Bold) as first scrollable item in the lazy layout
- Move `Toolbar()` filter chips inside the lazy layout (so they scroll away, no longer pinned at top)

### Search screen changes (newUI2)
- Add floating `NewTopAppBar`
- Push `SearchBarWithResults` down by `LocalFloatingToolbarPadding` to avoid overlap

### Theme toggle behavior
- DARK ↔ LIGHT
- DARK_MODERN ↔ LIGHT_MODERN (also toggles accent color: LIGHT_MODERN → 0xFF6A1CF6, DARK_MODERN → 0xFFBA9EFF)
- DARKER → LIGHT (no DARKER equivalent)

### Status bar frosted glass (modern themes only — requirement 8)
- When `useNewLibraryUI2 && theme.transparentBars`: content box in `MobileLayout` starts at y=0 (no status bar padding), so `NewTopAppBar` can visually extend into the status bar area
- `NewTopAppBar` adds `windowInsetsTopHeight(WindowInsets.statusBars)` as internal spacer on modern themes
- Status bar insets are NOT consumed in this mode → `PlatformTitleBar` in settings and other non-targeted screens still works correctly

---

## Files to Create

| # | File |
|---|------|
| 1 | `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/topbar/NewTopAppBar.kt` |

## Files to Modify

| # | File | Changes |
|---|------|---------|
| 2 | `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/MainScreenViewModel.kt` | Add `toggleTheme()` |
| 3 | `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/MainScreen.kt` | Extend hazeState creation; status bar handling for modern+newUI2 |
| 4 | `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/library/LibraryScreen.kt` | Replace toolbar + segmented buttons; add header section + tab chips |
| 5 | `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/home/HomeContent.kt` | Add `HomeHeaderSection`; move chips inside lazy layout |
| 6 | `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/home/HomeScreen.kt` | Wrap with floating `NewTopAppBar` when newUI2 |
| 7 | `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/search/SearchScreen.kt` | Wrap with floating `NewTopAppBar`; push search content down |

---

## Detailed Implementation

### Task 1 — `MainScreenViewModel.kt`: Add `toggleTheme()`

`MainScreenViewModel` already has `private val settingsRepository: CommonSettingsRepository`. Add:

```kotlin
fun toggleTheme(currentTheme: Theme) {
    screenModelScope.launch {
        val (newAppTheme, newAccent) = when (currentTheme) {
            Theme.LIGHT        -> AppTheme.DARK         to null
            Theme.DARK         -> AppTheme.LIGHT        to null
            Theme.DARKER       -> AppTheme.LIGHT        to null
            Theme.LIGHT_MODERN -> AppTheme.DARK_MODERN  to Color(0xFFBA9EFF.toInt())
            Theme.DARK_MODERN  -> AppTheme.LIGHT_MODERN to Color(0xFF6A1CF6.toInt())
        }
        settingsRepository.putAppTheme(newAppTheme)
        if (newAccent != null) settingsRepository.putAccentColor(newAccent.toArgb().toLong())
    }
}
```

New imports: `snd.komelia.settings.model.AppTheme`, `androidx.compose.ui.graphics.Color`, `androidx.compose.ui.graphics.toArgb`.

---

### Task 2 — `MainScreen.kt`: MobileLayout hazeState & status bar

#### 2a. Extend hazeState creation to cover newUI2 (not only transparent bars)

```kotlin
// Current:
val transparentBars = useNewLibraryUI && theme.transparentBars
val hazeState = if (transparentBars) rememberHazeState() else null

// New:
val transparentBars = useNewLibraryUI && theme.transparentBars
val useNewTopBar = useNewLibraryUI2 && useNewLibraryUI
val hazeState = if (transparentBars || useNewTopBar) rememberHazeState() else null
```

#### 2b. For modern + newUI2: set top=0 and don't consume status bar insets

Find the content `Box` inside `MobileLayout` with `.padding(top = paddingValues.calculateTopPadding(), ...)`. Replace with:

```kotlin
val isModernNewTopBar = useNewTopBar && theme.transparentBars
val topPadding = if (isModernNewTopBar) 0.dp else paddingValues.calculateTopPadding()

Box(
    Modifier
        .fillMaxSize()
        .padding(
            start = paddingValues.calculateStartPadding(layoutDirection),
            end = paddingValues.calculateEndPadding(layoutDirection),
            top = topPadding,
            bottom = bottomPadding,
        )
        .then(
            if (isModernNewTopBar)
                // Only consume bottom inset so PlatformTitleBar in other screens still works
                Modifier.consumeWindowInsets(PaddingValues(bottom = paddingValues.calculateBottomPadding()))
            else
                Modifier.consumeWindowInsets(paddingValues)
        )
        .statusBarsPadding()  // remains no-op in both cases
        .then(if (hazeState != null) Modifier.hazeSource(hazeState) else Modifier)
)
```

---

### Task 3 — `NewTopAppBar.kt`: Create the shared bar

```kotlin
package snd.komelia.ui.topbar

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.HazeMaterials
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.NotoSerif_Bold
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.Font
import snd.komelia.ui.*
import snd.komelia.ui.common.menus.LibraryActionsMenu
import snd.komelia.ui.common.menus.LibraryMenuActions

@Composable
fun NewTopAppBar(
    libraryActions: LibraryMenuActions? = null,
    modifier: Modifier = Modifier,
) {
    val theme = LocalTheme.current
    val hazeState = LocalHazeState.current
    val accentColor = LocalAccentColor.current
    val mainScreenVm = LocalMainScreenViewModel.current
    val coroutineScope = rememberCoroutineScope()
    var showOptionsMenu by remember { mutableStateOf(false) }

    val isAdmin = LocalKomgaState.current.authenticatedUser.collectAsState().value?.roleAdmin() ?: true
    val isOffline = LocalOfflineMode.current.collectAsState().value
    val showThreeDotsMenu = libraryActions != null && (isAdmin || isOffline)

    val iconColor = accentColor ?: theme.colorScheme.primary
    val notoSerif = FontFamily(Font(Res.font.NotoSerif_Bold, FontWeight.Bold))

    // Semi-opaque fallback when hazeEffect is not available (non-modern themes)
    val solidBg = theme.colorScheme.surface.copy(alpha = 0.85f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (hazeState != null)
                    Modifier.hazeEffect(hazeState) {
                        style = HazeMaterials.thin(theme.colorScheme.surface)
                    }
                else
                    Modifier  // background drawn by the Scaffold's surface
            )
    ) {
        // Modern themes only: spacer so bar visually covers the status bar area
        if (theme.transparentBars) {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        }

        // Actual bar row — ~45dp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Hamburger
            IconButton(onClick = { coroutineScope.launch { mainScreenVm.toggleNavBar() } }) {
                Icon(Icons.Rounded.Menu, contentDescription = null, tint = iconColor)
            }

            // App name
            Text(
                "Komelia",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = notoSerif,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    letterSpacing = (-0.5).sp,
                ),
                modifier = Modifier.weight(1f),
            )

            // Theme toggle
            val toggleIcon = when (theme) {
                Theme.LIGHT, Theme.LIGHT_MODERN -> Icons.Rounded.DarkMode  // show DarkMode icon to switch to dark
                else                            -> Icons.Rounded.LightMode  // show LightMode icon to switch to light
            }
            IconButton(onClick = { mainScreenVm.toggleTheme(theme) }) {
                Icon(toggleIcon, contentDescription = "Toggle theme", tint = iconColor)
            }

            // 3-dots (library screen only)
            if (showThreeDotsMenu && libraryActions != null) {
                Box {
                    IconButton(onClick = { showOptionsMenu = true }) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = null, tint = iconColor)
                    }
                    LibraryActionsMenu(
                        library = libraryActions.library,  // ⚠ verify this field exists on LibraryMenuActions
                        actions = libraryActions,
                        expanded = showOptionsMenu,
                        onDismissRequest = { showOptionsMenu = false }
                    )
                }
            }
        }
    }
}
```

> **⚠ Implementation note:** Check `LibraryMenuActions` data class — it currently takes `library: KomgaLibrary` as a separate parameter in `LibraryActionsMenu`. You may need to pass library separately to `NewTopAppBar` or add it as a field on `LibraryMenuActions`.

> **⚠ Icon note:** `Icons.Rounded.DarkMode` and `Icons.Rounded.LightMode` may not be in the extended icon set. Alternatives: `Icons.Rounded.NightlightRound` (dark) / `Icons.Rounded.WbSunny` (light). Check available icons.

---

### Task 4 — `LibraryScreen.kt`: Replace toolbar + segmented buttons

#### 4a. New `beforeContent` logic when `useNewUI2`

In `LibraryScreen.Content()`, after computing `totalCountInfo`:

```kotlin
val useNewUI2 = LocalUseNewLibraryUI2.current
val library = vm.library.collectAsState().value

// beforeContent: header + chips (newUI2) or segmented buttons (legacy)
val beforeContent: @Composable () -> Unit = if (useNewUI2) {
    {
        LibraryHeaderSection(
            library = library,
            totalCount = totalCount,
            countLabel = countLabel,
            pageSize = pageSize,
            onPageSizeChange = onPageSizeChange,
        )
        LibraryTabChips(
            currentTab = vm.currentTab,
            collectionsCount = vm.collectionsCount,
            readListsCount = vm.readListsCount,
            onBrowseClick = vm::toBrowseTab,
            onCollectionsClick = vm::toCollectionsTab,
            onReadListsClick = vm::toReadListsTab,
        )
    }
} else {
    {
        LibrarySegmentedButtons(
            currentTab = vm.currentTab,
            collectionsCount = vm.collectionsCount,
            readListsCount = vm.readListsCount,
            onBrowseClick = vm::toBrowseTab,
            onCollectionsClick = vm::toCollectionsTab,
            onReadListsClick = vm::toReadListsTab,
        )
    }
}
```

#### 4b. New floating layout when `useNewUI2`

Replace the existing `floatToolbar` / Column block:

```kotlin
if (useNewUI2) {
    val barHeight = 45.dp
    val statusBarHeight = if (theme.transparentBars) LocalRawStatusBarHeight.current else 0.dp
    CompositionLocalProvider(LocalFloatingToolbarPadding provides barHeight + statusBarHeight) {
        Box(Modifier.fillMaxSize()) {
            tabContent()   // series/collection/readlist grid with header+chips as beforeContent
            NewTopAppBar(libraryActions = vm.libraryActions())
        }
    }
} else {
    // existing floatToolbar / Column behavior (unchanged)
    if (floatToolbar) {
        val toolbarHazeState = rememberHazeState()
        CompositionLocalProvider(
            LocalHazeState provides toolbarHazeState,
            LocalFloatingToolbarPadding provides 64.dp,
        ) {
            Box(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxSize().hazeSource(toolbarHazeState)) { tabContent() }
                toolbarContent()
            }
        }
    } else {
        Column {
            if (showToolbar) toolbarContent()
            tabContent()
        }
    }
}
```

#### 4c. New private composables for header section and tab chips

Add to `LibraryScreen.kt`:

```kotlin
@Composable
private fun LibraryHeaderSection(
    library: KomgaLibrary?,
    totalCount: Int,
    countLabel: String,
    pageSize: Int,
    onPageSizeChange: (Int) -> Unit,
) {
    val notoSerif = FontFamily(Font(Res.font.NotoSerif_Bold, FontWeight.Bold))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                library?.name ?: "All Libraries",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = notoSerif,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                ),
            )
            if (totalCount > 0) {
                Text(
                    "$totalCount ${countLabel.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp,
                )
            }
        }
        PageSizeSelectionDropdown(pageSize = pageSize, onPageSizeChange = onPageSizeChange)
    }
}

@Composable
private fun LibraryTabChips(
    currentTab: LibraryTab,
    collectionsCount: Int,
    readListsCount: Int,
    onBrowseClick: () -> Unit,
    onCollectionsClick: () -> Unit,
    onReadListsClick: () -> Unit,
) {
    if (collectionsCount == 0 && readListsCount == 0) return
    val chipColors = AppFilterChipDefaults.filterChipColors()
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(
                selected = currentTab == SERIES,
                onClick = onBrowseClick,
                label = { Text("Series") },
                colors = chipColors,
                shape = AppFilterChipDefaults.shape(),
                border = AppFilterChipDefaults.filterChipBorder(currentTab == SERIES),
            )
        }
        if (collectionsCount > 0) {
            item {
                FilterChip(
                    selected = currentTab == COLLECTIONS,
                    onClick = onCollectionsClick,
                    label = { Text("Collections") },
                    colors = chipColors,
                    shape = AppFilterChipDefaults.shape(),
                    border = AppFilterChipDefaults.filterChipBorder(currentTab == COLLECTIONS),
                )
            }
        }
        if (readListsCount > 0) {
            item {
                FilterChip(
                    selected = currentTab == READ_LISTS,
                    onClick = onReadListsClick,
                    label = { Text("Read Lists") },
                    colors = chipColors,
                    shape = AppFilterChipDefaults.shape(),
                    border = AppFilterChipDefaults.filterChipBorder(currentTab == READ_LISTS),
                )
            }
        }
    }
}
```

---

### Task 5 — `HomeContent.kt`: Add header + move chips inside lazy layout

#### 5a. Add `HomeHeaderSection`

```kotlin
@Composable
private fun HomeHeaderSection() {
    val notoSerif = FontFamily(Font(Res.font.NotoSerif_Bold, FontWeight.Bold))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            "Home",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontFamily = notoSerif,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
            ),
        )
    }
}
```

#### 5b. Update `HomeContent` composable signature and body

Add `useNewUI2 = LocalUseNewLibraryUI2.current` inside `HomeContent`. When true:
- Remove `Toolbar()` from the outer `Column`
- Pass `topContent = { HomeHeaderSection(); Toolbar(...) }` to `DisplayContent`

```kotlin
@Composable
fun HomeContent(
    filters: List<HomeFilterData>,
    activeFilterNumber: Int,
    onFilterChange: (Int) -> Unit,
    cardWidth: Dp,
    onSeriesClick: (KomgaSeries) -> Unit,
    seriesMenuActions: SeriesMenuActions,
    bookMenuActions: BookMenuActions,
    onBookClick: (KomeliaBook) -> Unit,
    onBookReadClick: (KomeliaBook, Boolean) -> Unit,
) {
    val gridState = rememberLazyGridState()
    val columnState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val useNewLibraryUI = LocalUseNewLibraryUI.current
    val useNewUI2 = LocalUseNewLibraryUI2.current

    if (useNewUI2) {
        // Header + chips are first items inside the lazy layout
        DisplayContent(
            filters = filters,
            activeFilterNumber = activeFilterNumber,
            gridState = gridState,
            columnState = columnState,
            cardWidth = cardWidth,
            onSeriesClick = onSeriesClick,
            seriesMenuActions = seriesMenuActions,
            bookMenuActions = bookMenuActions,
            onBookClick = onBookClick,
            onBookReadClick = onBookReadClick,
            topContent = {
                HomeHeaderSection()
                Toolbar(
                    filters = filters,
                    currentFilterNumber = activeFilterNumber,
                    onFilterChange = { newFilter ->
                        onFilterChange(newFilter)
                        coroutineScope.launch {
                            if (useNewLibraryUI && newFilter == 0) columnState.animateScrollToItem(0)
                            else gridState.animateScrollToItem(0)
                        }
                    },
                )
            },
        )
    } else {
        // Legacy: Toolbar pinned above DisplayContent
        Column {
            Toolbar(
                filters = filters,
                currentFilterNumber = activeFilterNumber,
                onFilterChange = { newFilter ->
                    onFilterChange(newFilter)
                    coroutineScope.launch {
                        if (useNewLibraryUI && newFilter == 0) columnState.animateScrollToItem(0)
                        else gridState.animateScrollToItem(0)
                    }
                },
            )
            DisplayContent(
                filters = filters,
                activeFilterNumber = activeFilterNumber,
                gridState = gridState,
                columnState = columnState,
                cardWidth = cardWidth,
                onSeriesClick = onSeriesClick,
                seriesMenuActions = seriesMenuActions,
                bookMenuActions = bookMenuActions,
                onBookClick = onBookClick,
                onBookReadClick = onBookReadClick,
            )
        }
    }
}
```

#### 5c. Update `DisplayContent` to accept `topContent` and add top padding

Add `topContent: (@Composable () -> Unit)? = null` parameter to `DisplayContent`. Add it as the first item in both `LazyColumn` and `LazyVerticalGrid`:

```kotlin
val toolbarPadding = LocalFloatingToolbarPadding.current

// In LazyColumn:
LazyColumn(
    contentPadding = PaddingValues(top = toolbarPadding, bottom = 15.dp + extraBottomPadding),
    ...
) {
    if (topContent != null) {
        item { topContent() }
    }
    // existing items...
}

// In LazyVerticalGrid:
LazyVerticalGrid(
    contentPadding = PaddingValues(
        top = toolbarPadding,
        bottom = 15.dp + extraBottomPadding,
    ),
    ...
) {
    if (topContent != null) {
        item(span = { GridItemSpan(maxLineSpan) }) { topContent() }
    }
    // existing items...
}
```

---

### Task 6 — `HomeScreen.kt`: Add floating `NewTopAppBar`

In `HomeScreen.Content()`, inside the `is Success` state block:

```kotlin
val useNewUI2 = LocalUseNewLibraryUI2.current
val theme = LocalTheme.current
val barHeight = 45.dp
val statusBarHeight = if (theme.transparentBars) LocalRawStatusBarHeight.current else 0.dp
val floatingPadding = if (useNewUI2) barHeight + statusBarHeight else 0.dp

CompositionLocalProvider(LocalFloatingToolbarPadding provides floatingPadding) {
    Box(Modifier.fillMaxSize()) {
        ScreenPullToRefreshBox(screenState = vm.state, onRefresh = vm::reload) {
            // existing FAB and HomeContent calls (unchanged)
        }
        if (useNewUI2) {
            NewTopAppBar()  // no libraryActions for home
        }
    }
}
```

---

### Task 7 — `SearchScreen.kt`: Add floating `NewTopAppBar`

In `SearchScreen.Content()`, inside the `MOBILE` platform branch:

```kotlin
val useNewUI2 = LocalUseNewLibraryUI2.current
val theme = LocalTheme.current
val barHeight = 45.dp
val statusBarHeight = if (theme.transparentBars) LocalRawStatusBarHeight.current else 0.dp
val floatingPadding = if (useNewUI2) barHeight + statusBarHeight else 0.dp

if (LocalPlatform.current == PlatformType.MOBILE) {
    if (useNewUI2) {
        CompositionLocalProvider(LocalFloatingToolbarPadding provides floatingPadding) {
            Box(Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(top = floatingPadding)
                ) {
                    SearchBarWithResults(
                        // existing params unchanged
                    ) {
                        // existing content unchanged
                    }
                }
                NewTopAppBar()
            }
        }
    } else {
        SearchBarWithResults(
            // existing (unchanged)
        ) { ... }
    }
}
```

---

## Key Reused Patterns

| Component | File | Notes |
|-----------|------|-------|
| `AppFilterChipDefaults` | `common/components/DescriptionChips.kt` | Chip styling for `LibraryTabChips` |
| `PageSizeSelectionDropdown` | `common/components/Pagination.kt` | Moved into `LibraryHeaderSection` |
| `LibraryActionsMenu` | `common/menus/LibraryActionsMenu.kt` | Used in `NewTopAppBar` 3-dots |
| `HazeMaterials.thin()` | haze library | Frosted glass style |
| `LocalHazeState` | `CompositionLocals.kt` | Global hazeSource from MobileLayout |
| `LocalFloatingToolbarPadding` | `CompositionLocals.kt` | Top content padding for all three screens |
| `LocalRawStatusBarHeight` | `CompositionLocals.kt` | Status bar height for modern mode |
| `Res.font.NotoSerif_Bold` | `composeResources/font/NotoSerif_Bold.ttf` | Pattern from `ItemCard.kt:77` |

---

## Implementation Notes / Gotchas

1. **`LibraryActionsMenu` signature**: Currently `LibraryActionsMenu(library, actions, expanded, onDismissRequest)`. `LibraryMenuActions` may or may not carry the `library` field. Check the data class definition and pass `library` separately to `NewTopAppBar` if needed.

2. **Icon names**: `Icons.Rounded.DarkMode` / `Icons.Rounded.LightMode` — verify availability. Alternatives: `Icons.Rounded.NightlightRound` (dark mode) / `Icons.Rounded.WbSunny` (light mode).

3. **NotoSerif italic**: Only `NotoSerif_Bold.ttf` exists; italic is synthesized (letters are sheared). Acceptable for now. A `NotoSerif_BoldItalic.ttf` would improve quality in a follow-up.

4. **Bar height = 45.dp (hardcoded)**: Consistent with `LocalRawStatusBarHeight` approach. If bar content ever wraps, this may need to be measured. Keep fixed for now.

5. **`Theme.DARKER` toggle**: Mapped to `AppTheme.LIGHT` since there is no DARKER-equivalent light theme.

6. **Legacy behavior**: ALL changes are strictly conditional on `useNewUI2 = LocalUseNewLibraryUI2.current`. When false, every screen reverts to its current behavior exactly.

7. **`HomeScreen.kt` CompositionLocalProvider placement**: Wrap the entire `is Success` block (including the FAB) so the `LocalFloatingToolbarPadding` is available to all children.

---

## Verification Checklist

### With newUI2 = ON, theme = DARK_MODERN
- [ ] Library: new top bar visible, NotoSerif italic "Komelia", accent-colored icons
- [ ] Library: bar height visibly shorter than standard M3 TopAppBar
- [ ] Library: frosted glass covers status bar area (icons visible through glass)
- [ ] Library: library name + count + page-size picker in content header
- [ ] Library: Series / Collections / ReadLists chips visible (style matches home chips)
- [ ] Library: content scrolls behind bar correctly; no items cut off at bottom
- [ ] Home: "Home" header in NotoSerif visible; filter chips scroll with content
- [ ] Home: new top bar floats above correctly
- [ ] Search: search bar starts below the new top bar (not overlapping)
- [ ] Theme toggle: DARK_MODERN → LIGHT_MODERN → accent color changes to purple

### With newUI2 = ON, theme = DARK (non-modern)
- [ ] Bar floats over content with semi-opaque background (no full glass blur)
- [ ] Status bar area NOT covered by bar (bar starts below status bar)

### With newUI2 = OFF
- [ ] Library, Home, Search: completely unchanged from before
- [ ] All other screens: unaffected

### Other screens (settings, series detail, etc.)
- [ ] Status bar padding correct (PlatformTitleBar still works)
- [ ] No visual regressions

### Build
- [ ] `./gradlew :komelia-app:assembleDebug` succeeds, no new warnings
