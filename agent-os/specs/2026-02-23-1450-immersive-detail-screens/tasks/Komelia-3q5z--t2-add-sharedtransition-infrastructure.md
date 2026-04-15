---
# Komelia-3q5z
title: 'T2: Add SharedTransition infrastructure'
status: completed
type: task
priority: normal
created_at: 2026-02-23T12:02:12Z
updated_at: 2026-03-07T23:38:59Z
parent: Komelia-uler
---

Add SharedTransitionLayout + AnimatedContent wrapper around Voyager Navigator.

**Files:**
- `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/CompositionLocals.kt`
- `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/MainView.kt`

**CompositionLocals.kt additions:**
```kotlin
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }
```

**MainView.kt change** (in `MainContent`, mobile path only):

Current:
```kotlin
Navigator(screen = loginScreen, ...) { navigator ->
    CurrentScreen()
}
```

New (mobile path):
```kotlin
SharedTransitionLayout {
    Navigator(screen = loginScreen, ...) { navigator ->
        AnimatedContent(targetState = navigator.lastItem, label = "nav") { screen ->
            CompositionLocalProvider(
                LocalSharedTransitionScope provides this@SharedTransitionLayout,
                LocalAnimatedVisibilityScope provides this@AnimatedContent,
            ) {
                screen.Content()
            }
        }
    }
}
```

Keep `CurrentScreen()` for non-mobile — `LocalAnimatedVisibilityScope` stays null so all `sharedBounds` guards no-op safely. `SharedTransitionLayout` wraps ALL platforms so the scope is available everywhere; `AnimatedContent` is mobile-only.

**Voyager compatibility (resolved):**
-  replaces  **on mobile only**
- Desktop keeps  — sharedElement modifiers no-op safely (LocalAnimatedVisibilityScope is null)
- ALL platforms get  +  so T3's sharedElement guards work everywhere
-  stays **outside** AnimatedContent (it's not inside CurrentScreen anyway)
- The  gate must wrap  (currently wraps  at line 200)
- Persistent overlays (FABs, pill nav bar) that float above transitions: add `renderInSharedTransitionScopeOverlay(zIndexInOverlay = 1f)` using `LocalSharedTransitionScope.current`
- Add `@OptIn(ExperimentalSharedTransitionApi::class)` to `MainContent`

**Subtasks:**
- [ ] Add `LocalSharedTransitionScope` and `LocalAnimatedVisibilityScope` to `CompositionLocals.kt`
- [ ] In `MainView.kt MainContent`, add `SharedTransitionLayout` + `AnimatedContent` wrapper for mobile
- [ ] Provide `LocalSharedTransitionScope` and `LocalAnimatedVisibilityScope` via `CompositionLocalProvider` inside `AnimatedContent`
- [ ] Verify no regression on desktop (keep `CurrentScreen()` for non-mobile)
- [ ] Check that Voyager screen lifecycle is not broken by `AnimatedContent` wrapping
