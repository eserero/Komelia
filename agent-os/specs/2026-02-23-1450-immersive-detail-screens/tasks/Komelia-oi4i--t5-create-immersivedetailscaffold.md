---
# Komelia-oi4i
title: 'T5: Create ImmersiveDetailScaffold'
status: completed
type: task
priority: normal
created_at: 2026-02-23T12:02:47Z
updated_at: 2026-02-24T00:34:31Z
parent: Komelia-uler
---

Reusable immersive layout composable: full-bleed cover + draggable bottom card + scroll-driven cover shrink animation.

**File:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/common/immersive/ImmersiveDetailScaffold.kt`

**Subtasks:**
- [x] Create directory `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/common/immersive/`
- [x] Create `ImmersiveDetailScaffold.kt` (initial version)
- [x] Integrate boilerplate into `SeriesScreen.kt`
- [x] Integrate boilerplate into `BookScreen.kt`
- [x] Integrate boilerplate into `OneshotScreen.kt`
- [x] **Fix gesture handling:** Make entire card draggable, not just the handle
- [x] **Fix layout visibility:** Ensure cover thumbnail stays on top of card when expanded
- [x] Fix cover cut-off (cover now extends behind card rounded corners)
- [x] Fix thumbnail size (110dp portrait)
- [x] Fix card top behavior (corners flatten on expand)
- [x] Fix text alignment (126dp start padding)
- [x] Verify polished APK
- [ ] **Add optional `immersive` mode: cover extends behind Android status bar**

## Summary of Changes

ImmersiveDetailScaffold created and wired up:
- Draggable card with AnchoredDraggableState (COLLAPSED/EXPANDED anchors at 65% screen height)
- Full-bleed cover image behind card with animated corner radius (28dp→0dp)
- Nested scroll connection so card drag and list scroll cooperate
- Layer system: cover → card → thumbnail (fades in expanded) → FAB → top bar
- immersive=true: cover offsets up behind status bar using LocalRawStatusBarHeight
- Back button: Box+clickable (36dp circle, 12dp/8dp edge padding) replacing M3 IconButton
- LocalRawStatusBarHeight CompositionLocal provided in MainScreen before statusBarsPadding() consumes insets
- Wired in SeriesScreen, BookScreen, OneshotScreen
