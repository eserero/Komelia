# Audio Player Non-Immersive Theme Fix — Plan

## Task 1: Save Spec Documentation ✅

Created this spec folder.

## Task 2: Fix AudioMiniPlayer ✅

**File:** `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/audio/AudioMiniPlayer.kt`

Implemented: non-immersive path uses a `Box` with `NonImmersiveAudioBackground` + shadow/clip
instead of `Surface(color = Transparent)`. Immersive path unchanged (`color = backgroundColor`,
`tonalElevation = 6.dp`).

## Task 3: Fix AudioFullScreenPlayer ✅

**File:** `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/reader/epub/audio/AudioFullScreenPlayer.kt`

Implemented: inner overlay is now conditional — immersive ON keeps
`background(backgroundColor.copy(alpha = 0.72f))`, immersive OFF uses `NonImmersiveAudioBackground`.

## Task 4: Extract NonImmersiveAudioBackground ✅

**File:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/reader/epub/audio/AudioPlayerBackground.kt`

Shared composable in commonMain renders haze effect (modern themes) or solid surface (classic themes),
matching `ReaderControlsCard` exactly.
