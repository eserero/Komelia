---
# Komelia-tjq1
title: 'Fix: EpubFragment crash on device rotation'
status: completed
type: bug
priority: normal
created_at: 2026-03-18T11:58:10Z
updated_at: 2026-03-18T15:58:00Z
---

Add android:configChanges to MainActivity to prevent activity recreation on rotation, avoiding EpubFragment InstantiationException

## Summary of Changes\n\nAdded  to  in .\n\nThis prevents Android from destroying/recreating the activity on rotation, so  (which has no no-arg constructor) is never re-instantiated via reflection. Compose handles recomposition automatically when configuration changes.
