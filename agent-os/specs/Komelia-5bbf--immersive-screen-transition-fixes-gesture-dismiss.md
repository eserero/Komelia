---
# Komelia-5bbf
title: Immersive screen transition fixes + gesture dismiss
status: completed
type: task
priority: normal
created_at: 2026-03-09T11:24:06Z
updated_at: 2026-03-09T15:22:31Z
---

Implement full gesture-scrubbed shared element transition on swipe-down dismiss.

Steps:
- [x] Step 1: Add LocalDismissCallbacks to CompositionLocals.kt
- [x] Step 2: Replace AnimatedContent with SeekableTransitionState in MainScreen.kt
- [x] Step 3: Replace dismissAnimatable with gestureProgress + DismissCallbacks in ImmersiveDetailScaffold.kt

## Summary of Changes

- **CompositionLocals.kt**: Added  data class and  composition local to wire gesture progress into the navigation layer.
- **MainScreen.kt**: Replaced  with  backed by . Added  that call  on drag,  on commit, and  on cancel. A  keeps seekableState in sync for normal (non-gesture) navigation.
- **ImmersiveDetailScaffold.kt**: Replaced  with  (state) +  (shared between outer scope and BoxWithConstraints via ). All gesture paths (nestedScroll onPostScroll/onPostFling and dismissPointerInput) now call . Card offset no longer has a dismiss component — the exit animation on  uses  with the full dismiss distance so it tracks the finger 1:1 through the seekable transition.
