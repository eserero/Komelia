---
# Komelia-0a5a
title: 'Fix: Back button circle size + immersive cover behind status bar'
status: completed
type: bug
priority: normal
created_at: 2026-02-23T23:27:59Z
updated_at: 2026-02-24T00:34:31Z
---

Two bugs from the previous immersive implementation:
1. Back button circle unchanged visually — M3 IconButton enforces internal .size(40.dp) for state layer, overriding external Modifier.size(29.dp).
2. Cover not extending behind status bar — statusBarsPadding() in MainScreen.kt consumes WindowInsets.statusBars for entire subtree.

Fix 1: Replace IconButton with Box + clickable in SeriesScreen, BookScreen, OneshotScreen.
Fix 2: Capture raw status bar height before statusBarsPadding() consumes it, pass via LocalRawStatusBarHeight CompositionLocal.

- [ ] Add LocalRawStatusBarHeight to CompositionLocals.kt
- [ ] Provide it in MainScreen.kt before statusBarsPadding()
- [ ] Use LocalRawStatusBarHeight in ImmersiveDetailScaffold.kt
- [ ] Replace IconButton with Box+clickable in SeriesScreen.kt
- [ ] Replace IconButton with Box+clickable in BookScreen.kt
- [ ] Replace IconButton with Box+clickable in OneshotScreen.kt
- [ ] Verify build compiles

## Summary of Changes

- Replaced IconButton with Box+clickable in all 3 screens (true 36dp circle, no M3 40dp override)
- Added padding(start=12.dp, top=8.dp) so circle is not flush with screen edges
- Added LocalRawStatusBarHeight to capture status bar height before statusBarsPadding() consumes WindowInsets.statusBars
- ImmersiveDetailScaffold now reads LocalRawStatusBarHeight instead of WindowInsets.statusBars.getTop() (which returned 0)
- Root cause of status bar issue: immersive=true was never passed — fixed in all 3 screens
