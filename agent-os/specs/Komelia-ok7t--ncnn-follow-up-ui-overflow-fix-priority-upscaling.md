---
# Komelia-ok7t
title: 'NCNN follow-up: UI overflow fix, priority upscaling, panel activity registration'
status: completed
type: feature
priority: normal
created_at: 2026-03-07T20:23:36Z
updated_at: 2026-03-07T23:39:55Z
---

Three follow-up fixes for the NCNN upscaling indicator:
1. Move UpscaleActivityIndicator below header row (prevents overflow)
2. Priority worker queue so current page upscales before backlog
3. Register upscale activity in loadImage() for panel navigation pages
