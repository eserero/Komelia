---
# Komelia-1n3r
title: Fix cover resize on library/home→book transition
status: completed
type: bug
priority: normal
created_at: 2026-03-01T21:40:12Z
updated_at: 2026-03-01T22:19:58Z
---

Cover image briefly appears at correct size then shrinks when navigating from library/home to book. Root cause: collapsedOffset = screenHeight * 0.65f inside BoxWithConstraints. When isImmersiveScreen flips false→true, the Scaffold's bottom padding changes (bottom nav bar hides), BoxWithConstraints.maxHeight changes, collapsedOffset changes, cover height changes → sharedBounds re-targets mid-animation. Fix: provide LocalRawNavBarHeight (like LocalRawStatusBarHeight) so ImmersiveDetailScaffold can compute a stable stableScreenHeight = maxHeight + navBarDp that doesn't include the bottom nav bar.

## Summary of Changes

- Added  to 
- In  (MainScreen.kt): read  bottom padding and provide it as  alongside the existing 
- In : compute  and use that for , so the 65% anchor is based on  — invariant whether the bottom nav bar is shown or not
