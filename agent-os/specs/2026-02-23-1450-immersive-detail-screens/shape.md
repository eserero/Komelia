# Immersive Detail Screens — Shaping Notes

## Scope

New immersive Series, Book, and Oneshot detail screens for Android mobile only, gated behind the existing `LocalUseNewLibraryUI` flag. The existing screens remain unchanged for desktop, tablet, and when the new UI is off.

The new layout:
- Full-bleed cover image (~65% of screen height, edge-to-edge width, `ContentScale.FillWidth` — no zoom)
- Transparent top bar overlay: back button (top-left), 3-dot menu (top-right, includes "Edit")
- Draggable bottom card overlaying the lower portion of the cover
- As user scrolls card upward: cover image shrinks/morphs to small thumbnail in top-left (scroll-driven animation)
- Card background = dominant color extracted from cover image (always on for new UI, no separate setting)
- Fluid thumbnail → detail screen transition (M3 Container Transform / shared element via `SharedTransitionLayout`)
- Book swipe: cover image is a `HorizontalPager` for sibling books in series; card crossfades to new book's info

FAB:
- Split pill button: "Read Now" (2/3 width) | "Incognito" (1/3 width)
- Round Download button to the right (series: download series; book/oneshot: download book)

## Decisions

- **Mobile only**: `LocalPlatform.current == PlatformType.MOBILE && LocalUseNewLibraryUI.current`
- **No new settings**: Card color extraction from cover is always on when new UI is enabled
- **Shared element**: Use `SharedTransitionLayout` + custom `AnimatedContent` wrapping Voyager's `CurrentScreen` — expose scopes via `LocalSharedTransitionScope` + `LocalAnimatedVisibilityScope` CompositionLocals
- **Book pager**: `HorizontalPager` for cover images only (not full screen) — card updates via `AnimatedContent` crossfade when page changes; swiping settles and navigates to the target book (via `navigator.replace`)
- **Color extraction**: `expect/actual` pattern — Android uses `androidx.palette:palette-ktx`; JVM/WASM use pixel sampling fallback. Triggered via Coil's `onSuccess` state callback
- **Fluid transitions override standards**: Where M3 animation guidelines conflict with existing Voyager navigation standards, M3 takes precedence
- **Reuse all existing logic**: All ViewModel functionality, menu actions, download logic, metadata composables are reused as-is. Only the layout and navigation wrapper change.

## Context

- **Visuals:** Reference mockup shows: immersive cover, teal card with stats bar + title + description + FAB
- **References:**
  - `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/series/SeriesScreen.kt` + `view/SeriesContent.kt`
  - `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/book/BookScreen.kt` + `BookScreenContent.kt` + `BookInfoContent.kt`
  - `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/oneshot/OneshotScreen.kt` + `OneshotScreenContent.kt`
  - `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/CompositionLocals.kt` — existing locals
  - `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/platform/PlatformType.kt` — `MOBILE` enum
  - `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/settings/appearance/AppearanceSettingsContent.kt`
- **Product alignment:** Directly supports roadmap Phase 2 "Enhanced library UI" — polished, fluid browsing experience

## Standards Applied

- compose-ui/navigation — Voyager factory functions + domain-keyed screens; fluid navigation overrides where conflicting
- compose-ui/theming — Existing theme system; card color derived from cover palette, not from settings enum
- compose-ui/view-models — StateScreenModel pattern reused; no new ViewModels needed for layout
- compose-ui/dependency-injection — New CompositionLocals for SharedTransitionScope follow existing pattern
