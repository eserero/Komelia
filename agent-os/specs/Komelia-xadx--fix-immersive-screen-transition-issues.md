---
# Komelia-xadx
title: Fix immersive screen transition issues
status: completed
type: bug
priority: normal
created_at: 2026-03-01T10:31:13Z
updated_at: 2026-03-07T23:40:13Z
---

Fix three transition issues: book image flicker (Coil re-fetch), series/oneshot unmemoized coverData, oneshot missing shared transition due to loading gate

## Tasks
- [ ] ThumbnailImage.kt: remove originalSize, add usePlaceholderKey
- [ ] ImmersiveDetailScaffold.kt: remove originalSize=true, add usePlaceholderKey=false
- [ ] ImmersiveSeriesContent.kt: replace remember with mutableStateOf + LaunchedEffect
- [ ] ImmersiveOneshotContent.kt: replace remember with mutableStateOf + LaunchedEffect

- [ ] ImmersiveBookContent.kt: add coverBook guard + mutableStateOf + LaunchedEffect for coverData

## Fix: Double Animation + Cover Flash\n- [x] Add EnterExitState, derivedStateOf, snapshotFlow imports\n- [x] Replace pager setup (hold at 1 page until transition settles)\n- [x] Guard pageBook in pager lambda to keep cover key stable
