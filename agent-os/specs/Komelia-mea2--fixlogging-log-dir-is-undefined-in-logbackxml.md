---
# Komelia-mea2
title: 'fix(logging): LOG_DIR_IS_UNDEFINED in logback.xml'
status: completed
type: bug
priority: normal
created_at: 2026-03-09T00:50:00Z
updated_at: 2026-03-09T00:56:48Z
---

initLogging() calls getILoggerFactory() before setting LOG_DIR property, so logback initializes with undefined path. Fix: set System property before triggering logback init.

## Summary of Changes\n\nAdded  before  in . This ensures logback reads the property during auto-configuration, before  (belt-and-suspenders) takes effect.
