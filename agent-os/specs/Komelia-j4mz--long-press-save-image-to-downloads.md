---
# Komelia-j4mz
title: Long-press save image to Downloads
status: completed
type: feature
priority: normal
created_at: 2026-03-04T22:32:43Z
updated_at: 2026-03-04T23:05:14Z
---

Add long-press gesture on reader images to save current page to Android Downloads folder. Includes platform-agnostic expect/actual pattern, context menu with AnimatedDropdownMenu, and AppNotification feedback.

## Summary of Changes

- Added  (common expect) with , , and  helpers
- Added Android actual using  (API 29+, no permissions needed) via 
- Added JVM actual saving to  using 
- Added wasmJs no-op stub
- Added  to  — fetches page bytes via , builds filename, saves, shows success notification
- Added  parameter to  (wired into )
- Propagated  through , , 
- Added context menu state +  with "Save image" item in 
