---
# Komelia-5r2y
title: 'Epub3 Controls Card: Bug Fixes'
status: completed
type: bug
priority: normal
created_at: 2026-03-19T17:38:00Z
updated_at: 2026-03-19T17:39:19Z
parent: Komelia-ecr6
---

Fix 5 regressions in the epub3 controls card: missing chapter chip on first open, broken double-tap audio, slider->play jumps back, AudioMiniPlayer unclickable, missing drag handle

## Summary of Changes

- **Fix 1** (): Seed  with  in  so the slider shows the correct position immediately on open.
- **Fix 2a** (): Debounced  with 400ms delay (cancels on double-tap) so the scrim no longer intercepts the second tap of a double-tap audio trigger.
- **Fix 2b** ():  now calls  synchronously so pressing Play immediately after slider navigation plays from the correct position.
- **Fix 2c** (): Moved  to last in the  so it renders above the scrim and its buttons remain clickable.
- **Fix 3** (): Removed  constraint on the drag handle wrapper box so  renders at its natural 48dp height and the pill is fully visible.
