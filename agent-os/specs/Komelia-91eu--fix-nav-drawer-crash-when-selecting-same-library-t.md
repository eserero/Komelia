---
# Komelia-91eu
title: Fix nav drawer crash when selecting same library twice
status: completed
type: bug
priority: normal
created_at: 2026-03-03T00:21:30Z
updated_at: 2026-03-03T00:22:21Z
---

When navigating to the same library from the drawer a second time, AnimatedContent composes both the outgoing and incoming LibraryScreen simultaneously during the fade. Since LibraryScreen has a deterministic key based on libraryId, both screens share the same SaveableState key → crash: Key 3_0:screen was used multiple times.

Fix: guard replaceAll calls in LibrariesNavBar.onLibraryClick (and onLibrariesClick) to skip navigation when already on the same destination.

- [x] Guard onLibraryClick in LibrariesNavBar
- [x] Guard onLibrariesClick in LibrariesNavBar
- [x] Guard NavBar equivalents if at risk

## Summary of Changes

Guarded `replaceAll` calls in both `LibrariesNavBar` and `NavBar` (desktop) `onLibraryClick` and `onLibrariesClick` lambdas. When the user taps a destination they are already on, navigation is skipped — the drawer still closes. This prevents `AnimatedContent` from composing two `LibraryScreen` instances with the same Voyager key simultaneously, which was the cause of the `Key 3_0:screen was used multiple times` crash.
