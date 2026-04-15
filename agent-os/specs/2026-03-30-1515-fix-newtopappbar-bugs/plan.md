# Fix NewTopAppBar Bugs — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix 4 regressions introduced by the NewTopAppBar feature: frosted glass transparency, status bar immersion, and chip layout overlaps on Library and Home screens.

**Architecture:** Two root causes — (A) hazeEffect/hazeSource architectural violation where NewTopAppBar is a *descendant* of the hazeSource Box instead of a sibling overlay; (B) missing `Column` wrapper around multi-composable beforeContent/topContent lambdas used inside LazyVerticalGrid items (Box context).

**Tech Stack:** Kotlin, Jetpack Compose Multiplatform, Haze 1.7.2

---

## Root Cause Analysis

### Issues 1 & 2 — Frosted glass not working / Status bar not frosted

**Problem A — Wrong hazeSource placement:**
In `MainScreen.kt`, the outer content `Box` is marked `.hazeSource(hazeState)`. The `NewTopAppBar` (with `.hazeEffect(hazeState)`) lives *inside* this Box as a descendant (via HomeScreen/LibraryScreen). Haze requires the hazeEffect to be a **sibling overlay** of hazeSource, not a child of it. Result: blur capture fails → bar appears fully transparent.

**Problem B — statusBarsPadding always applied:**
The outer Box in `MainScreen.kt` always applies `.statusBarsPadding()`, even when `isModernNewTopBar=true`. This offsets all content (including NewTopAppBar) *below* the status bar. NewTopAppBar can never visually cover the status bar area. Its internal `Spacer(windowInsetsTopHeight(statusBars))` becomes 0 because insets are already consumed.

**Fix:**
1. Conditionally skip `.statusBarsPadding()` when `isModernNewTopBar=true` (insets flow to NewTopAppBar).
2. Each screen using NewTopAppBar creates its own `screenHazeState`, applies `hazeSource(screenHazeState)` to the **content Box** (sibling of NewTopAppBar), and provides it via `CompositionLocalProvider(LocalHazeState provides screenHazeState)`. This mirrors the existing working `floatToolbar` pattern in LibraryScreen.

### Issues 3 & 4 — Chips overlapping header

**Problem:** The `newUI2BeforeContent` (LibraryScreen) and `topContent` (HomeContent) lambdas call multiple composables (`Header + Chips`) without a `Column` wrapper. When rendered inside a `LazyVerticalGrid` item — whose content scope is effectively `BoxScope` — the composables stack at `(0, 0)` and visually overlap instead of being laid out vertically.

**Fix:** Wrap the composables inside each lambda in a `Column`.

---

## Files Modified

| File | Change |
|------|--------|
| `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/MainScreen.kt` | Conditionally skip `.statusBarsPadding()` when `isModernNewTopBar` |
| `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/library/LibraryScreen.kt` | Add local hazeState + hazeSource; wrap `newUI2BeforeContent` in `Column` |
| `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/home/HomeScreen.kt` | Add local hazeState + hazeSource for NewTopAppBar |
| `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/home/HomeContent.kt` | Wrap `topContent` lambda in `Column` |

---

## Task 1 — MainScreen.kt: Conditional statusBarsPadding

**File:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/MainScreen.kt`

Current code (around line 252):
```kotlin
.statusBarsPadding()
.then(if (hazeState != null) Modifier.hazeSource(hazeState) else Modifier)
```

- [ ] **Step 1: Make statusBarsPadding conditional**

Replace:
```kotlin
.statusBarsPadding()
.then(if (hazeState != null) Modifier.hazeSource(hazeState) else Modifier)
```
With:
```kotlin
.then(if (!isModernNewTopBar) Modifier.statusBarsPadding() else Modifier)
.then(if (hazeState != null) Modifier.hazeSource(hazeState) else Modifier)
```

**Why this is safe:** When `isModernNewTopBar=false`, behaviour is unchanged. When `isModernNewTopBar=true`, the status bar insets are NOT consumed here, so they flow to NewTopAppBar's internal `Spacer(windowInsetsTopHeight(statusBars))` which then correctly occupies the status bar area.

The `hazeSource` remains on the outer Box — this is still needed for the bottom navigation bar frosted glass, which works correctly as a sibling. The NewTopAppBar's per-screen hazeState (Task 2) is separate.

- [ ] **Step 2: Verify imports** — no new imports needed.

---

## Task 2 — LibraryScreen.kt: Local hazeState + Column wrapper

**File:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/library/LibraryScreen.kt`

### Part A — Add local hazeState for frosted glass

Current code (around lines 216–224):
```kotlin
if (useNewUI2) {
    val barHeight = 45.dp
    val statusBarHeight = if (theme.transparentBars) LocalRawStatusBarHeight.current else 0.dp
    CompositionLocalProvider(LocalFloatingToolbarPadding provides barHeight + statusBarHeight) {
        Box(Modifier.fillMaxSize()) {
            tabContent()
            NewTopAppBar(library = library, libraryActions = vm.libraryActions())
        }
    }
}
```

- [ ] **Step 1: Rewrite the useNewUI2 branch to use a local hazeState**

Replace the entire `if (useNewUI2)` block with:
```kotlin
if (useNewUI2) {
    val barHeight = 45.dp
    val statusBarHeight = if (theme.transparentBars) LocalRawStatusBarHeight.current else 0.dp
    val screenHazeState = if (theme.transparentBars) rememberHazeState() else null
    CompositionLocalProvider(
        LocalFloatingToolbarPadding provides barHeight + statusBarHeight,
        LocalHazeState provides screenHazeState,
    ) {
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .then(if (screenHazeState != null) Modifier.hazeSource(screenHazeState) else Modifier)
            ) {
                tabContent()
            }
            NewTopAppBar(library = library, libraryActions = vm.libraryActions())
        }
    }
}
```

**Why:** The inner `Box` with `hazeSource` is now a **sibling** of `NewTopAppBar`, not an ancestor. Haze can correctly capture and blur the content behind the bar.

- [ ] **Step 2: Check imports** — `rememberHazeState` is already imported (used in the `floatToolbar` branch below). `LocalHazeState` is already imported.

### Part B — Wrap newUI2BeforeContent in Column

Current code (around lines 189–205):
```kotlin
val newUI2BeforeContent = @Composable {
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
```

- [ ] **Step 3: Wrap in Column**

Replace with:
```kotlin
val newUI2BeforeContent = @Composable {
    Column {
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
}
```

- [ ] **Step 4: Add Column import** if not already present:
```kotlin
import androidx.compose.foundation.layout.Column
```

---

## Task 3 — HomeScreen.kt: Local hazeState

**File:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/home/HomeScreen.kt`

Current code (around lines 82–141):
```kotlin
val barHeight = 45.dp
val statusBarHeight = if (theme.transparentBars) LocalRawStatusBarHeight.current else 0.dp
val floatingPadding = if (useNewUI2) barHeight + statusBarHeight else 0.dp
CompositionLocalProvider(LocalFloatingToolbarPadding provides floatingPadding) {
Box(Modifier.fillMaxSize()) {
ScreenPullToRefreshBox(screenState = vm.state, onRefresh = vm::reload) {
    // ...content...
}
if (useNewUI2) {
    NewTopAppBar()
}
}
}
```

- [ ] **Step 1: Add local hazeState and hazeSource sibling box**

Replace the layout structure:
```kotlin
val barHeight = 45.dp
val statusBarHeight = if (theme.transparentBars) LocalRawStatusBarHeight.current else 0.dp
val floatingPadding = if (useNewUI2) barHeight + statusBarHeight else 0.dp
val screenHazeState = if (useNewUI2 && theme.transparentBars) rememberHazeState() else null
CompositionLocalProvider(
    LocalFloatingToolbarPadding provides floatingPadding,
    LocalHazeState provides screenHazeState,
) {
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .then(if (screenHazeState != null) Modifier.hazeSource(screenHazeState) else Modifier)
        ) {
            ScreenPullToRefreshBox(screenState = vm.state, onRefresh = vm::reload) {
                when (val state = vm.state.collectAsState().value) {
                    is LoadState.Error -> ErrorContent(
                        message = state.exception.message ?: "Unknown Error",
                        onReload = vm::reload
                    )
                    else ->
                        HomeContent(
                            filters = vm.currentFilters.collectAsState().value,
                            activeFilterNumber = vm.activeFilterNumber.collectAsState().value,
                            onFilterChange = vm::onFilterChange,
                            cardWidth = vm.cardWidth.collectAsState().value,
                            onSeriesClick = { navigator push seriesScreen(it) },
                            seriesMenuActions = vm.seriesMenuActions(),
                            bookMenuActions = vm.bookMenuActions(),
                            onBookClick = { navigator push bookScreen(it) },
                            onBookReadClick = { book, markProgress ->
                                navigator.parent?.push(
                                    readerScreen(
                                        book = book,
                                        markReadProgress = markProgress,
                                        onExit = { lastReadBook ->
                                            if (lastReadBook.id != book.id) {
                                                vm.reload()
                                            }
                                        }
                                    )
                                )
                            },
                        )
                }
                val extraBottomPadding = LocalTransparentNavBarPadding.current
                FloatingActionButton(
                    onClick = { navigator.replaceAll(FilterEditScreen(vm.currentFilters.value)) },
                    containerColor = accentColor ?: MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (accentColor != null) {
                        if (accentColor.luminance() > 0.5f) Color.Black else Color.White
                    } else MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .then(if (extraBottomPadding == 0.dp) Modifier.windowInsetsPadding(WindowInsets.navigationBars) else Modifier)
                        .padding(bottom = 16.dp + extraBottomPadding, end = 16.dp)
                ) {
                    Icon(Icons.Rounded.Edit, null)
                }
            }
        }
        if (useNewUI2) {
            NewTopAppBar()
        }
    }
}
```

- [ ] **Step 2: Check imports** — add if missing:
```kotlin
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import snd.komelia.ui.LocalHazeState
```

---

## Task 4 — HomeContent.kt: Wrap topContent in Column

**File:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/home/HomeContent.kt`

The `topContent` lambda (passed from the `useNewUI2` branch in `HomeContent`, around lines 95–108) renders `HomeHeaderSection()` then `Toolbar(...)` without a container. When used in a `LazyVerticalGrid` item, they overlay.

Current code (around lines 95–108):
```kotlin
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
```

- [ ] **Step 1: Wrap in Column**

Replace with:
```kotlin
topContent = {
    Column {
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
    }
},
```

- [ ] **Step 2: Verify Column import** — add if missing:
```kotlin
import androidx.compose.foundation.layout.Column
```

---

## Task 5 — Build and verify

- [ ] **Step 1: Build debug APK**

```bash
cd /home/eyal/Komelia
./gradlew :komelia-app:assembleDebug 2>&1 | tail -20
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 2: Install and manual test**

```bash
adb install -r komelia-app/build/outputs/apk/debug/komelia-app-debug.apk
```

Verify:
1. Open app on a modern theme (LIGHT_MODERN or DARK_MODERN)
2. Library screen → top bar shows frosted glass blur (not transparent)
3. Status bar area is also frosted/immersive — no hard line between status bar and top bar
4. Switch to Collections tab → chips (Series/Collections/ReadLists) appear **below** the library name, not overlapping
5. Switch to ReadLists tab → same
6. Go to Home screen → all good
7. On Home, select a non-All filter → chips and "Home" header properly stacked, no overlap

- [ ] **Step 3: Commit**

```bash
git add \
  komelia-ui/src/commonMain/kotlin/snd/komelia/ui/MainScreen.kt \
  komelia-ui/src/commonMain/kotlin/snd/komelia/ui/library/LibraryScreen.kt \
  komelia-ui/src/commonMain/kotlin/snd/komelia/ui/home/HomeScreen.kt \
  komelia-ui/src/commonMain/kotlin/snd/komelia/ui/home/HomeContent.kt
git commit -m "fix(ui): frosted glass, status bar immersion, and chip layout in NewTopAppBar screens"
```
