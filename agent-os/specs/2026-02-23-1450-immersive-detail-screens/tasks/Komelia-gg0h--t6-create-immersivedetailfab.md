---
# Komelia-gg0h
title: 'T6: Create ImmersiveDetailFab'
status: completed
type: task
priority: normal
created_at: 2026-02-23T12:02:47Z
updated_at: 2026-02-24T08:48:13Z
parent: Komelia-uler
---

Split pill FAB: Read Now (2/3) | Incognito (1/3) + separate round Download button.

**File:** `komelia-ui/src/commonMain/kotlin/snd/komelia/ui/common/immersive/ImmersiveDetailFab.kt`

**Signature:**
```kotlin
fun ImmersiveDetailFab(
    onReadClick: () -> Unit,
    onReadIncognitoClick: () -> Unit,
    onDownloadClick: () -> Unit,
)
```

**Layout:**
```
Row(horizontalArrangement = spacedBy(12.dp), verticalAlignment = CenterVertically) {
    Surface(shape=CircleShape, color=primaryContainer) {
        Row(height=56.dp, verticalAlignment=CenterVertically) {
            [clickable Row, padding 20dp H] Icon(MenuBook) + Text("Read Now")
            VerticalDivider(fillMaxHeight(0.6f))
            [clickable Box, padding 16dp H] Icon(VisibilityOff)
        }
    }
    FloatingActionButton(modifier=size(56.dp), shape=CircleShape, containerColor=secondaryContainer) {
        Icon(Download)
    }
}
```

**Icons used:**
- `Icons.AutoMirrored.Rounded.MenuBook` (read)
- `Icons.Default.VisibilityOff` (incognito — from material-icons-extended)
- `Icons.Filled.Download` (download — already used in BookScreenContent)

**Subtasks:**
- [ ] Create `ImmersiveDetailFab.kt` in `common/immersive/`
- [ ] Read Now + Incognito pill: `Surface(CircleShape, primaryContainer)` containing a `Row(height=56.dp)`
- [ ] Read Now: clickable Row with `MenuBook` icon + "Read Now" LabelLarge text, `padding(horizontal=20.dp)`
- [ ] Incognito: `VerticalDivider` separator + clickable Box with `VisibilityOff` icon, `padding(horizontal=16.dp)`
- [ ] Download: `FloatingActionButton(Modifier.size(56.dp), CircleShape, secondaryContainer)` with Download icon
- [ ] Verify `Icons.Default.VisibilityOff` is importable (needs extended icons)

## Summary of Changes

- ImmersiveDetailFab: accentColor param + luminance-based content color (light/dark cover adaptation); showReadActions param
- Series: showReadActions=false → download FAB only, right-aligned
- Book/Oneshot: full pill (Read Now + Incognito) + download FAB
- ImmersiveDetailScaffold: replaced floating fade-out FAB layer with fixed Alignment.BottomCenter slot + windowInsetsPadding(navigationBars), always visible
- MainScreen: PillBottomNavigationBar hidden when navigator.lastItem is SeriesScreen/BookScreen/OneshotScreen
