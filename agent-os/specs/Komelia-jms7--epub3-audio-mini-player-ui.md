---
# Komelia-jms7
title: EPUB3 Audio Mini-Player UI
status: completed
type: feature
priority: normal
created_at: 2026-03-18T11:14:33Z
updated_at: 2026-03-18T11:25:15Z
parent: Komelia-ecr6
---

Add play/pause mini-player and SMIL-driven paragraph highlighting for epub3 books with media overlays

## Implementation

- [ ] Build passes
- [x]  — manages AudiobookPlayer, drives EpubView highlight
- [x]  — M3 floating play/pause button
- [x]  — mediaOverlayController field, initialize/attach/release wiring
- [x]  — renders AudioMiniPlayer outside showControls block

## Summary of Changes

- Added  — wraps AudiobookPlayer, builds Track list from OverlayPar clips, drives EpubView highlight via onClipChanged/onIsPlayingChanged callbacks
- Added  — Material 3 circle play/pause button collecting isPlaying state
- Modified  — mediaOverlayController StateFlow, init/attach/release wiring
- Modified  — renders AudioMiniPlayer at BottomCenter outside showControls block
- Modified  — added media3-common and media3-session as direct androidMain dependencies (needed to resolve Player.Listener/MediaController.Listener supertypes of AudiobookPlayer)
