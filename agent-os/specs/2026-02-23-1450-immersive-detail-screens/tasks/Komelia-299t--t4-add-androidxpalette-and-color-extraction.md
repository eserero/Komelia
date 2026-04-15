---
# Komelia-299t
title: 'T4: Add androidx.palette and color extraction'
status: in-progress
type: task
priority: normal
created_at: 2026-02-23T12:02:46Z
updated_at: 2026-03-07T23:53:51Z
parent: Komelia-uler
---

Card background uses dominant color extracted from cover image (always on for new UI).

**Files:**
- `gradle/libs.versions.toml` — add `androidx-palette = "1.0.0"` under `[versions]` and library alias under `[libraries]`
- `komelia-ui/build.gradle.kts` — add `implementation(libs.androidx.palette)` to `androidMain.dependencies`
- `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/common/immersive/ColorExtraction.kt` (expect)
- `komelia-ui/src/androidMain/kotlin/snd/komelia/ui/common/immersive/ColorExtraction.android.kt`
- `komelia-ui/src/jvmMain/kotlin/snd/komelia/ui/common/immersive/ColorExtraction.jvm.kt`
- `komelia-ui/src/wasmJsMain/kotlin/snd/komelia/ui/common/immersive/ColorExtraction.wasmjs.kt`

**expect signature (commonMain):**
```kotlin
expect suspend fun extractDominantColor(painter: AsyncImagePainter): Color?
```

**Android actual:**
```kotlin
actual suspend fun extractDominantColor(painter: AsyncImagePainter): Color? {
    val state = painter.state.first { it is AsyncImagePainter.State.Success || it is AsyncImagePainter.State.Error }
    val bitmap = (state as? AsyncImagePainter.State.Success)
        ?.result?.image?.toBitmap() ?: return null
    val palette = Palette.from(bitmap).generate()
    val argb = palette.getDominantColor(0)
    return if (argb == 0) null else Color(argb)
}
```
Needs: `import androidx.palette.graphics.Palette`, `import coil3.toBitmap`

**JVM actual:** `actual suspend fun extractDominantColor(painter: AsyncImagePainter): Color? = null`

**WASM actual:** same as JVM.

**Wiring into immersive screens (T5/T7/T8/T9):**
After T4 is done, update the `ImmersiveXxxContent` composables to:
```kotlin
val dominantColor = remember(coverKey) { mutableStateOf<Color?>(null) }
LaunchedEffect(coverKey) {
    dominantColor.value = extractDominantColor(painter)
}
ImmersiveDetailScaffold(cardColor = dominantColor.value, ...)
```
This requires the painter to be accessible at the screen level, so we need a rememberAsyncImagePainter for the cover.

**Subtasks:**
- [ ] Add `androidx-palette = "1.0.0"` to `[versions]` in `gradle/libs.versions.toml`
- [ ] Add `androidx-palette = { module = "androidx.palette:palette-ktx", version.ref = "androidx-palette" }` to `[libraries]` in `libs.versions.toml`
- [ ] Add `implementation(libs.androidx.palette)` to `androidMain.dependencies` in `komelia-ui/build.gradle.kts`
- [ ] Create `ColorExtraction.kt` (expect declaration) in `commonMain/immersive/`
- [ ] Create `ColorExtraction.android.kt`: extract bitmap from `AsyncImagePainter.State.Success`, run `Palette.from(bitmap).generate()`
- [ ] Create `ColorExtraction.jvm.kt`: return null
- [ ] Create `ColorExtraction.wasmjs.kt`: return null
- [ ] Wire `extractDominantColor` into immersive screen composables as `cardColor` parameter
