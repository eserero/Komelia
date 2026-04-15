---
# Komelia-8dk9
title: 'feat(settings): add Crash Logs button in NCNN settings'
status: completed
type: feature
priority: normal
created_at: 2026-03-09T00:39:46Z
updated_at: 2026-03-09T00:45:56Z
---

Add a Crash Logs viewer button in Settings → Image Reader → NCNN, mirroring the existing View Logs button pattern

## Summary of Changes\n\n- Added  declaration in \n- Added  state and a "Crash Logs"  in the same Row as "View Logs"\n- Created  with Android actual: reads , ,  (last 300 lines each) from external storage\n- Added empty stubs in  and \n- Build:  passes
