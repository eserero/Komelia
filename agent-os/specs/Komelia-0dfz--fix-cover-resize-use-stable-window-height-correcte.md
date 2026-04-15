---
# Komelia-0dfz
title: 'Fix cover resize: use stable window height (corrected)'
status: completed
type: bug
priority: normal
created_at: 2026-03-01T22:57:16Z
updated_at: 2026-03-01T23:11:48Z
---

The first fix added systemNavBarHeight back to screenHeight, but the actual instability comes from appNavBarHeight (Material NavigationBar ~80dp) that disappears during transition. The correct fix uses LocalWindowInfo.current.containerSize to get the real window height, invariant to whether the app nav bar is shown.

## Summary of Changes

- : Replaced  with  using  for a height value invariant to the app nav bar showing/hiding.
- Added .
- Moved  declaration up to the new calculation block and removed duplicate declaration ~100 lines lower.
