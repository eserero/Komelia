---
# Komelia-uler
title: 2026-02-23-1450-immersive-detail-screens
status: in-progress
type: epic
priority: normal
created_at: 2026-02-23T12:00:53Z
updated_at: 2026-02-23T13:01:27Z
---

New immersive Series, Book, and Oneshot detail screens for Android mobile (gated by LocalUseNewLibraryUI flag).

Full-bleed cover image (~65% height), draggable bottom card with cover-extracted dominant color, M3 Container Transform shared element transitions from library grid, and book cover carousel with crossfade card updates.

Spec: agent-os/specs/2026-02-23-1450-immersive-detail-screens/plan.md

## Tasks
- [ ] T1: Save spec documentation
- [ ] T2: Add SharedTransition infrastructure (Komelia-3q5z)
- [ ] T3: Add sharedElement modifiers (Komelia-korg)
- [ ] T4: Add androidx.palette + color extraction (Komelia-299t)
- [ ] T5: Create ImmersiveDetailScaffold (Komelia-oi4i)
- [ ] T6: Create ImmersiveDetailFab (Komelia-gg0h)
- [ ] T7: Create ImmersiveSeriesContent (Komelia-ibgq)
- [ ] T8: Create ImmersiveBookContent with pager (Komelia-rhyj)
- [ ] T9: Create ImmersiveOneshotContent (Komelia-wq73)
