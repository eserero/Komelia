---
# Komelia-xdaf
title: 'fix(reader): upscaler queue bug fixes'
status: in-progress
type: bug
created_at: 2026-03-08T16:40:24Z
updated_at: 2026-03-08T16:40:24Z
---

Fix 3 upscaler queue bugs: (1) all cached pages upscale when toggling upscaleOnLoad, (2) queued jobs not cancelled when upscaler disabled, (3) old engine requests run against new engine on switch
