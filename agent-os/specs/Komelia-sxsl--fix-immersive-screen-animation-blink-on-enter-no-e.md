---
# Komelia-sxsl
title: 'fix: immersive screen animation — blink on enter + no exit animation'
status: completed
type: bug
priority: normal
created_at: 2026-03-09T23:57:02Z
updated_at: 2026-03-09T23:57:43Z
---

Fix two visual bugs with ImmersiveDetailScaffold: blink on enter (cover pops in instantly) and no exit animation (screen just disappears on back).

## Summary of Changes

- **ImmersiveDetailScaffold.kt**: Replaced `EnterTransition.None`/`ExitTransition.None` on `coverSharedModifier` with `fadeIn(400ms)`/`fadeOut(300ms)` — fixes the blink/pop-in on enter and instant disappearance on exit
- **ImmersiveDetailScaffold.kt**: Changed card exit in `cardOverlayModifier` from `fadeOut(200ms)` to `slideOutVertically(400ms) + fadeOut(300ms)` — adds a visible slide-down exit
- **MainScreen.kt**: Made `AnimatedContent` transition spec immersive-aware: `EnterTransition.None` when entering immersive (individual elements handle their own entry), `fadeOut(450ms)` when exiting immersive (keeps content alive long enough for card/cover exit animations to complete), unchanged crossfade for all other navigations
