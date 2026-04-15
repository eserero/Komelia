# Immersive Detail Screens — Implementation Plan

## Context

Komelia's current series/book/oneshot detail screens use a standard card layout that doesn't feel native or immersive on Android. The user wants new detail screens gated behind the existing `LocalUseNewLibraryUI` flag (mobile only) that feature a full-bleed cover image, a scrollable bottom card with dominant-color background extracted from the cover, fluid M3 Container Transform transitions from the library grid, and a book image carousel with crossfade card updates for browsing siblings within a series.

The existing screens remain unchanged for desktop, tablet, and when the new UI flag is off. All existing ViewModel logic, metadata composables, and action handlers are reused inside the new layout.

---

## Execution Order

Tasks are executed in this sequence — screens first, then polish layers:

| Step | Bean | What gets delivered |
|------|------|---------------------|
| 1 | T1 ✅ | Spec docs saved |
| 2 | T5 | Immersive layout scaffold (card uses theme color, no transition yet) |
| 3 | T6 | FAB (Read Now / Incognito / Download) |
| 4 | T7 | Series detail screen — immersive, fully functional on Android |
| 5 | T8 | Book detail screen — immersive + sibling pager |
| 6 | T9 | Oneshot detail screen — feature complete |
| 7 | T4 | Card colors now match cover art (Palette API) |
| 8 | T2 | Navigation transitions wired up (SharedTransitionLayout) |
| 9 | T3 | Thumbnails tagged — "tap → cover flies to detail" goes live |

Tasks T5–T9 are self-contained and don't require T2/T3/T4 to function. The scaffold accepts `cardColor: Color?` (null = surfaceVariant fallback) and `coverKey: String` (used later by T2/T3 — harmless until then).

---

## Task 1: Save Spec Documentation

**Deliverable:** All planning and context documents saved in the spec folder.
**What it fulfills:** Ensures the feature intent, decisions, and references are preserved for future reference — months later, anyone (or an AI agent) can find this spec and understand what was built and why.

Create `agent-os/specs/2026-02-23-1450-immersive-detail-screens/` with shape.md, standards.md, references.md, and plan.md.

**Status:** ✅ Already complete.

---

## Task 2: Add SharedTransition Infrastructure

**Deliverable:** The app's navigation layer is aware of shared elements. Compose can now track the same image across two different screens and animate between them.
**What it fulfills:** Powers every cover-image transition in the app — tapping a series/book anywhere (library, home, series grid) will animate its cover expanding into the detail screen, and tapping back will animate it shrinking back. This is the foundation that Tasks 3, 5, 7, 8, and 9 all depend on for fluid navigation.

**Why technically:** M3 Container Transform (thumbnail → full cover) requires `SharedTransitionLayout` wrapping both source and destination screens.

**Files to modify:**
- `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/CompositionLocals.kt`
  ```kotlin
  @OptIn(ExperimentalSharedTransitionApi::class)
  val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
  val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }
  ```
  Imports: `SharedTransitionScope`, `AnimatedVisibilityScope`, `ExperimentalSharedTransitionApi`

- `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/MainView.kt`
  - `SharedTransitionLayout` wraps the whole `Navigator` block (all platforms get the scope)
  - `AnimatedContent(navigator.lastItem)` replaces `CurrentScreen()` on **mobile only**;
    desktop/web keep `CurrentScreen()` — `LocalAnimatedVisibilityScope` stays null there → all
    `sharedBounds` guards no-op safely
  - `navigator.clearEvent()` and `LaunchedEffect` blocks stay **outside** `AnimatedContent`
  - `@OptIn(ExperimentalSharedTransitionApi::class)` on `MainContent`

  ```kotlin
  @OptIn(ExperimentalSharedTransitionApi::class)
  SharedTransitionLayout {
      CompositionLocalProvider(LocalSharedTransitionScope provides this) {
          Navigator(screen = loginScreen, ...) { navigator ->
              // ... LaunchedEffect / clearEvent blocks stay here, unchanged ...
              if (canProceed) {
                  when (platformType) {
                      MOBILE -> AnimatedContent(
                          targetState = navigator.lastItem,
                          label = "nav",
                      ) { screen ->
                          CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                              screen.Content()
                          }
                      }
                      else -> CurrentScreen()  // desktop/web: sharedBounds no-ops
                  }
              }
          }
      }
  }
  ```

  Imports: `SharedTransitionLayout`, `AnimatedContent`, `ExperimentalSharedTransitionApi`,
  `LocalSharedTransitionScope`, `LocalAnimatedVisibilityScope`

---

## Task 3: Add `sharedBounds` Modifiers to Cover Thumbnails

**Deliverable:** Every cover thumbnail in the library grid is tagged so Compose can animate it
into the full-width cover in `ImmersiveDetailScaffold`. Back-navigation reverses the animation.
**What it fulfills:** Together, Tasks 2 + 3 = the "tap → cover flies to detail screen" effect.

**Key design decision — `sharedBounds` not `sharedElement`:**
Layer 1 of `ImmersiveDetailScaffold` (the full-width cover) is the animation *destination*.
Because the container changes size and possibly clip shape (small card → full-width), use
`sharedBounds` (bounds + content cross-fade) rather than `sharedElement` (pixel-perfect match).
`SharedTransitionScope.ResizeMode.ScaleToBounds()` keeps aspect ratio during the morph.

**Key string:** `"cover-${id}"` must match exactly between source thumbnail and destination Layer 1.

**Files to modify:**

- `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/common/images/SeriesThumbnail.kt`
**Animation specs — must match the scaffold's motion system:**

The scaffold uses:
- Expand: `tween(500ms, CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f))` — M3 Emphasized
- Collapse: `tween(200ms, CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f))` — M3 Emphasized Accelerate

`sharedBounds` has two independent animation channels:
- `boundsTransform` — animates the rectangle morph (position + size). **This is the main hero motion.** Defaults to a spring if not set — will feel bouncy and inconsistent with the scaffold.
- `enter`/`exit` — animate the content cross-fade *inside* the bounds. Keep these short so they don't compete with the bounds morph.

Shared easing constants (define once at the top of each file or in a shared constants file):
```kotlin
private val emphasizedEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
private val emphasizedAccelerateEasing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
```

  ```kotlin
  @OptIn(ExperimentalSharedTransitionApi::class)
  @Composable
  fun SeriesThumbnail(seriesId: KomgaSeriesId, modifier: Modifier = Modifier, ...) {
      val sharedTransitionScope = LocalSharedTransitionScope.current
      val animatedVisibilityScope = LocalAnimatedVisibilityScope.current
      val sharedModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
          with(sharedTransitionScope) {
              Modifier.sharedBounds(
                  rememberSharedContentState(key = "cover-${seriesId.value}"),
                  animatedVisibilityScope = animatedVisibilityScope,
                  enter = fadeIn(tween(150)),
                  exit = fadeOut(tween(150, easing = emphasizedAccelerateEasing)),
                  boundsTransform = { _, _ ->
                      tween(durationMillis = 500, easing = emphasizedEasing)
                  },
                  resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(),
              )
          }
      } else Modifier

      ThumbnailImage(
          modifier = modifier.then(sharedModifier),  // caller clip/size first, then sharedBounds
          ...
      )
  }
  ```
  Same pattern for `BookThumbnail.kt` with key `"cover-${bookId.value}"`.

- `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/common/immersive/ImmersiveDetailScaffold.kt`
  - Add `@OptIn(ExperimentalSharedTransitionApi::class)` to the file
  - Apply `sharedBounds` to **Layer 1** (full-width cover) — the animation destination
  - Apply to both the immersive and non-immersive Layer 1 paths
  - **Modifier order matters:** `sharedBounds` must come **before** `graphicsLayer`
    (so the shared transition composites the element before the alpha fade is applied):
    ```kotlin
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current
    val coverSharedModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Modifier.sharedBounds(
                rememberSharedContentState(key = "cover-$coverKey"),
                animatedVisibilityScope = animatedVisibilityScope,
                enter = fadeIn(tween(150)),
                exit = fadeOut(tween(150, easing = emphasizedAccelerateEasing)),
                boundsTransform = { _, _ ->
                    tween(durationMillis = 500, easing = emphasizedEasing)
                },
                resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(),
            )
        }
    } else Modifier

    // Layer 1 modifier:
    Modifier
        .fillMaxWidth()
        .offset { IntOffset(0, -statusBarPx.roundToInt()) }
        .height(collapsedOffset + topCornerRadiusDp + statusBarDp)
        .then(coverSharedModifier)          // sharedBounds before graphicsLayer ✓
        .graphicsLayer { alpha = 1f - expandFraction }
    ```

  Imports: `ExperimentalSharedTransitionApi`, `SharedTransitionScope`,
  `rememberSharedContentState`, `fadeIn`, `fadeOut`, `tween`,
  `LocalSharedTransitionScope`, `LocalAnimatedVisibilityScope`

**`AnimatedContent` transition spec (MainView.kt):**
By default `AnimatedContent` cross-fades the whole screen content behind the shared element.
This is generally fine — the grid fades out while the detail screen fades in. If it looks
wrong in practice, add:
```kotlin
AnimatedContent(
    targetState = navigator.lastItem,
    transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
    label = "nav",
) { ... }
```

**Verification:**
```bash
./gradlew :komelia-app:assembleDebug
```
Visual checks:
1. Tap a series card → cover hero-animates to full-width with M3 Emphasized deceleration (500 ms)
2. Back → cover returns to card with M3 Emphasized Accelerate (200 ms) — feels swift, not bouncy
3. Motion feels continuous with card expand/collapse (same easing family)
4. Desktop/web: no regression, no crash (guards no-op when scope is null)
5. Series + book + oneshot all work

---

## Task 4: Add `androidx.palette` Dependency + Color Extraction expect/actual

**Deliverable:** A function `extractDominantColor(painter)` that returns the dominant colour from any loaded cover image — on Android using the real Palette API, on desktop/web returning null (falls back to the theme surface colour).
**What it fulfills:** The bottom card's background colour automatically matches the cover art (the teal card in the reference mockup). No user setting needed — it just works. This function is called by Tasks 5, 7, 8, and 9 to colour their cards.

**Why technically:** Card background uses dominant color from cover image, always on for new UI. No new settings required.

**Files to modify:**
- `gradle/libs.versions.toml` — add:
  ```toml
  [versions]
  androidx-palette = "1.0.0"

  [libraries]
  androidx-palette = { module = "androidx.palette:palette-ktx", version.ref = "androidx-palette" }
  ```
- `komelia-ui/build.gradle.kts` — add `androidx-palette` to `androidMain` dependencies

**New files to create:**
- `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/common/immersive/ColorExtraction.kt`
  ```kotlin
  expect suspend fun extractDominantColor(painter: AsyncImagePainter): Color?
  ```
- `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/common/immersive/ColorExtraction.android.kt`
  - Use `Palette.from(bitmap).generate()` → `getDominantColor(fallback)` → convert to `Color`
  - Get bitmap from painter's `State.Success` state
- `komelia-ui/src/jvmMain/kotlin/snd/komelia/ui/common/immersive/ColorExtraction.jvm.kt`
  - Fallback: return null (card uses theme surfaceVariant)
- `komelia-ui/src/wasmJsMain/kotlin/snd/komelia/ui/common/immersive/ColorExtraction.wasmjs.kt`
  - Fallback: return null

---

## Task 5: Create `ImmersiveDetailScaffold` — Shared Layout Composable

**Deliverable:** A reusable layout composable that any detail screen (series, book, oneshot) can plug into. It renders the full-bleed cover image on top, a draggable card anchored at the bottom, and handles the scroll animation where the cover shrinks to a small thumbnail in the top-left as the user scrolls the card up.
**What it fulfills:** This is the visual foundation of the immersive experience — the "wow" layout described in the requirements. Tasks 7, 8, and 9 all use this as their outer shell. Building it once here means all three screens share identical motion and layout behaviour.

**Location:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/common/immersive/ImmersiveDetailScaffold.kt`

**Layout:**
```
Box(fillMaxSize) {
  AsyncImage(cover)              ← full width, ~65% height, FillWidth, sharedElement modifier
  TopBarOverlay                  ← back button + 3-dot menu, transparent bg, top of screen
  BottomCard (anchoredDraggable) ← anchored at ~35% from bottom, draggable upward
  FabRow                         ← split pill + download round button, above card
}
```

**Scroll-driven cover animation:**
- Track `offsetFraction` from `AnchoredDraggableState` (0f = card at rest, 1f = card fully expanded)
- As card moves up: use `graphicsLayer { scaleX/Y, translationX/Y }` to shrink the cover to a ~56dp thumbnail in the top-left corner
- Interpolate using `lerp(offsetFraction)` for scale and translation

**Signature:**
```kotlin
@Composable
fun ImmersiveDetailScaffold(
    coverData: Any,
    coverKey: String,            // for sharedElement key = "cover-$coverKey"
    cardColor: Color?,           // null = use MaterialTheme.colorScheme.surfaceVariant
    topBarContent: @Composable () -> Unit,
    fabContent: @Composable () -> Unit,
    cardContent: @Composable ColumnScope.() -> Unit,
)
```

**Immediate Integration (Boilerplate):**
To verify Task 5 visually before moving to T6/T7, integrate a boilerplate version into `SeriesScreen.kt`:
- **Branching logic:** `if (LocalPlatform.current == PlatformType.MOBILE && LocalUseNewLibraryUI.current)`
- **Cover:** `SeriesDefaultThumbnailRequest(seriesId)`
- **Card Color:** `LocalAccentColor.current`
- **Content:** Placeholder text and basic navigation (back button) to verify the "wow" layout and shrink animation.

---

## Task 6: Create `ImmersiveDetailFab` Composable

**Deliverable:** The floating action button row that sits above the bottom card on all immersive detail screens: a pill-shaped "Read Now / Incognito" split button plus a separate circular Download button to the right.
**What it fulfills:** Replaces the current separate read, incognito, and download buttons with a compact, visually distinctive FAB matching the reference mockup. Built once here and reused by Tasks 7, 8, and 9.

**Location:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/common/immersive/ImmersiveDetailFab.kt`

**Design:** (based on mockup + user spec)
```
Row {
    [Read Now (2/3) | divider | Incognito (1/3)]  ← pill shape, split by VerticalDivider
    [Round Download Button]                        ← ~56dp circular FAB on right
}
```
- "Read Now" and "Incognito" share one pill container; vertical `VerticalDivider` separates them
- Download button is a separate `FloatingActionButton` with circular shape
- Sizes match existing library/home FABs

**Signature:**
```kotlin
@Composable
fun ImmersiveDetailFab(
    onReadClick: () -> Unit,
    onReadIncognitoClick: () -> Unit,
    onDownloadClick: () -> Unit,
)
```

---

## Task 7: Create `ImmersiveSeriesContent`

**Deliverable:** The new immersive Series detail screen is live on Android when "New UI" is enabled. Opening a series shows the cover full-bleed, a coloured card with the series name, metadata, and books grid below, the new FAB, and the back/menu top bar. The existing series screen is unchanged for desktop/tablet/old UI.
**What it fulfills:** Delivers the immersive experience for the most common entry point — browsing a series. All existing functionality (book grid, collections tab, filters, bulk actions, download, edit) is preserved inside the new layout.

**Location:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/series/immersive/ImmersiveSeriesContent.kt`

**Uses:** `ImmersiveDetailScaffold` + `ImmersiveDetailFab`

**Card content (reusing existing composables):**
- Series name (large title)
- `SeriesDescriptionRow(...)` — status, age rating, language, reading direction
- `SeriesChipTags(...)` — publisher, genres, tags, author chips
- `TabRow` — Books / Collections tabs (pill-shaped, existing)
- `SeriesBooksContent(...)` or `SeriesCollectionsContent(...)` based on active tab

**Top bar:** Back button (`←`) + 3-dot `SeriesActionsMenu` with Edit added to menu items (move from separate button to menu)

**FAB:** `ImmersiveDetailFab(onReadClick, onReadIncognitoClick, onDownloadClick = vm::onDownload)`

**Modify `SeriesScreen.Content()`:**
```kotlin
val platform = LocalPlatform.current
val useNewUI = LocalUseNewLibraryUI.current
if (platform == PlatformType.MOBILE && useNewUI) {
    ImmersiveSeriesContent(...)
} else {
    SeriesContent(...)   // unchanged
}
```

---

## Task 8: Create `ImmersiveBookContent` (with sibling pager)

**Deliverable:** The new immersive Book detail screen is live on Android when "New UI" is enabled. It shows the book cover full-bleed with left/right edges of adjacent book covers peeking in — swiping the cover image moves to the prev/next book in the series, and the card below crossfades to show that book's metadata. All existing book functionality (read, download, metadata, read lists) is preserved.
**What it fulfills:** Delivers the immersive experience for individual books, plus the "swipe to browse siblings" interaction — the M3 lateral navigation pattern where the cover image acts as a carousel anchored to the series.

**Location:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/book/immersive/ImmersiveBookContent.kt`

**Sibling loading:** Add `val siblingBooks: StateFlow<List<KomeliaBook>>` to `BookViewModel` — fetches the series book list using `seriesId` when `bookSiblingsContext == BookSiblingsContext.Series`

**Pager design (M3 Shared Axis — lateral):**
- `HorizontalPager(siblingBooks)` for cover image only (not the full screen)
- Shows prev/next book covers peeking at edges (`contentPadding` on pager + `pageSpacing`)
- When `pagerState.settledPage` changes: `vm.loadBook(siblingBooks[page].id)` updates card content
- Card uses `AnimatedContent(targetState = currentBook)` with crossfade
- Swiping uses Compose pager's default snap animation; card crossfades simultaneously

**Card content (reusing existing composables):**
- Series name + book number
- `BookInfoRow(...)` — pages, release date, read progress
- `BookInfoColumn(...)` — authors, genres, tags, publishers, links, ISBN, file info
- `BookReadListsContent(...)`

**Top bar:** Back button + `BookActionsMenu` (with Edit moved into menu)

**FAB:** `ImmersiveDetailFab(onReadClick, onReadIncognitoClick, onDownloadClick)`

**Modify `BookScreen.Content()`:**
```kotlin
if (platform == PlatformType.MOBILE && useNewUI) {
    ImmersiveBookContent(...)
} else {
    BookScreenContent(...)   // unchanged
}
```

---

## Task 9: Create `ImmersiveOneshotContent`

**Deliverable:** The new immersive Oneshot detail screen is live on Android when "New UI" is enabled. Oneshots (standalone books that are also a series) get the same immersive treatment as series and books, with their hybrid metadata (series-level status + book-level page count/progress) displayed in the card.
**What it fulfills:** Completes the immersive experience across all three detail screen types. Every item in the library — series, book, oneshot — now has the same fluid, polished entry/exit experience when "New UI" is on.

**Location:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/oneshot/immersive/ImmersiveOneshotContent.kt`

**Uses:** `ImmersiveDetailScaffold` + `ImmersiveDetailFab`

**Card content (reusing existing composables):**
- Series name (oneshot title)
- `SeriesDescriptionRow(...)` — status, age rating, language, reading direction
- `BookInfoRow(...)` — page count, release date, read progress
- `BookInfoColumn(...)` — metadata, tags, authors
- `BookReadListsContent(...)`, `SeriesCollectionsContent(...)`

**Top bar:** Back button + `OneshotActionsMenu` (with Edit in menu)
**FAB:** `ImmersiveDetailFab` (download = oneshot/book download)

**Modify `OneshotScreen.Content()`:**
```kotlin
if (platform == PlatformType.MOBILE && useNewUI) {
    ImmersiveOneshotContent(...)
} else {
    OneshotScreenContent(...)   // unchanged
}
```

---

---

## Implementation Notes (Added During Execution)

### Codebase findings that affect the plan

**Cover images are NOT URL strings.** The codebase uses Coil request objects:
- Series: `SeriesDefaultThumbnailRequest(seriesId)` from `snd.komelia.image.coil`
- Book: `BookDefaultThumbnailRequest(bookId)` from `snd.komelia.image.coil`
- `ThumbnailImage(data: Any, cacheKey: String)` accepts these as its `data` parameter

→ **T5 scaffold signature change:** `coverUrl: String` → `coverData: Any`

**Sibling books API.** `KomgaBookApi` has `getBookSiblingPrevious`/`getBookSiblingNext` (one at a time). For the pager we need the full list. Use `bookApi.getBookList(BookConditionBuilder { seriesId { isEqualTo(book.seriesId) } }, KomgaPageRequest(size=500, sort="metadata.numberSort,asc"))`.

**T8 pager design change.** Rather than a full-screen `HorizontalPager` for covers (which would require scaffold redesign), use a **sibling thumbnail strip** (a `LazyRow` of small cover thumbnails) at the top of the card content. The main cover in the scaffold shows the currently selected book. Tapping a thumbnail calls `vm.loadBook(sibling.id)`, which updates `vm.book` and triggers `AnimatedContent` crossfade in the card.

**T7 card content scroll.** `SeriesBooksContent` uses `LazyVerticalGrid` internally. The immersive card's `cardContent` slot uses a `Column(weight=1f)` — the caller must ensure scrollable content fills this space. For series, pass a content that wraps the book grid inside a `fillMaxSize()` composable. The grid's own scroll handles content scrolling; the drag handle handles card expansion.

**SeriesActionsMenu** already has `showEditOption: Boolean` and `showDownloadOption: Boolean` parameters — no changes to that composable needed. Just call it with `showEditOption=true` and `showDownloadOption=false` (download goes to FAB).

**BookActionsMenu** also has `showEditOption: Boolean` and `showDownloadOption: Boolean` — same pattern.

**Voyager + AnimatedContent (T2).** Replacing `CurrentScreen()` with `AnimatedContent(navigator.lastItem)` is a significant change that may break Voyager's screen lifecycle. Needs careful testing. Start with the MOBILE platform path only.

### AnchoredDraggableState API (CMP 1.11)

CMP 1.11-alpha01 uses Compose Foundation ~1.8+. Constructor requires `anchors`:
```kotlin
AnchoredDraggableState(
    initialValue = CardDragValue.COLLAPSED,
    anchors = DraggableAnchors { COLLAPSED at collapsedPx; EXPANDED at 0f },
    positionalThreshold = { it * 0.5f },
    velocityThreshold = { with(density) { 100.dp.toPx() } },
    snapAnimationSpec = spring(),
    decayAnimationSpec = exponentialDecay(),
)
```
Use `SideEffect { state.updateAnchors(...) }` to sync anchors on orientation changes.

---

## Key Files Summary

| File | Action |
|------|--------|
| `komelia-ui/.../ui/CompositionLocals.kt` | Add `LocalSharedTransitionScope`, `LocalAnimatedVisibilityScope` |
| `komelia-ui/.../ui/MainView.kt` | Wrap Navigator in `SharedTransitionLayout + AnimatedContent` |
| `komelia-ui/.../ui/common/images/ThumbnailImage.kt` | Add `sharedElement` modifier on covers |
| `gradle/libs.versions.toml` | Add `androidx.palette` version + library alias |
| `komelia-ui/build.gradle.kts` | Add palette dependency to androidMain |
| `komelia-ui/.../common/immersive/ColorExtraction.kt` | New: expect/actual color extraction |
| `komelia-ui/.../common/immersive/ImmersiveDetailScaffold.kt` | New: reusable immersive layout |
| `komelia-ui/.../common/immersive/ImmersiveDetailFab.kt` | New: split pill + download FAB |
| `komelia-ui/.../series/immersive/ImmersiveSeriesContent.kt` | New: series immersive layout |
| `komelia-ui/.../book/immersive/ImmersiveBookContent.kt` | New: book immersive layout + pager |
| `komelia-ui/.../oneshot/immersive/ImmersiveOneshotContent.kt` | New: oneshot immersive layout |
| `komelia-ui/.../series/SeriesScreen.kt` | Conditional: mobile+newUI → immersive |
| `komelia-ui/.../book/BookScreen.kt` | Conditional: mobile+newUI → immersive |
| `komelia-ui/.../oneshot/OneshotScreen.kt` | Conditional: mobile+newUI → immersive |
| `komelia-ui/.../book/BookViewModel.kt` | Add `siblingBooks` flow |

---

## Verification

1. **Mobile new UI on:** Open library on Android → tap a series → cover animates expanding from thumbnail → cover fills top ~65% of screen → coloured card with metadata appears below
2. **Scroll interaction:** Scroll card upward → cover shrinks to top-left thumbnail with smooth animation
3. **Back transition:** Press back → cover animates back to the grid thumbnail position
4. **Book pager:** Open a series → tap a book → swipe the cover → adjacent book cover slides in → card crossfades to new book's metadata
5. **Existing UI preserved:** Toggle "New Library UI" off → detail screens revert to existing layout exactly
6. **Desktop/tablet unchanged:** Non-MOBILE platform always uses existing screens
7. **FAB:** "Read Now" → reader; "Incognito" → incognito reader; Download → download with confirmation
8. **3-dot menu:** Edit appears in menu (not separate button)
9. **Card colour:** Card background matches dominant colour from cover art; non-Android = theme surfaceVariant
