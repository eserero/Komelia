# Build Standards — Komelia

Komelia is a **Kotlin Multiplatform** project targeting Android, JVM (desktop), and WASM (web).
The build system is **Gradle with Kotlin DSL**, run from the repo root.

All `./gradlew` commands are run from the **repository root** (`/home/eyal/Komelia/`).

---

## Step 1: Compile Check (fast, no APK)

Before building, verify the code compiles without errors.
This is fast — it only compiles Kotlin, no packaging.

### Android debug compile (most common)
```bash
./gradlew :komelia-ui:compileDebugKotlinAndroid
```
- Compiles `commonMain` + `androidMain` sources in `komelia-ui`
- Most new UI code lives here; this catches ~95% of compile errors

### Full Android debug compile (catches all modules)
```bash
./gradlew compileDebugSources
```
- Compiles all modules for Android debug
- Use this if you touched files outside `komelia-ui`

### JVM compile check
```bash
./gradlew :komelia-ui:compileKotlinJvm
```
- Use if you changed `commonMain` code and want to verify JVM target too

### Success output looks like:
```
BUILD SUCCESSFUL in Xs
N actionable tasks: N executed
```

### Failure output looks like:
```
> Task :komelia-ui:compileDebugKotlinAndroid FAILED
e: file:///.../Foo.kt:42:5 Unresolved reference: 'updateAnchors'
BUILD FAILED in Xs
```
Read the `e:` lines — they show file path, line number, and the error message.

---

## Step 2: Build Debug APK

Once the compile check passes, build the full debug APK:

```bash
./gradlew assembleDebug
```

- Runs from the root project — assembles all modules
- Produces: `komelia-app/build/outputs/apk/debug/komelia-app-debug.apk`
- Includes all Android debug variant resources, assets, and compiled code

### Expected output on success:
```
BUILD SUCCESSFUL in Xs
N actionable tasks: N executed
```

---

## Typical Workflow

```bash
# 1. Quick compile check (fast feedback)
./gradlew :komelia-ui:compileDebugKotlinAndroid

# 2. If BUILD SUCCESSFUL — build the APK
./gradlew assembleDebug

# 3. APK is at:
#    komelia-app/build/outputs/apk/debug/komelia-app-debug.apk
```

---

## Module Structure

The project has these Gradle modules relevant to UI work:

| Module | Purpose |
|--------|---------|
| `komelia-ui` | All Compose UI — screens, composables, ViewModels |
| `komelia-app` | Android `Application` class, `MainActivity`, app module |
| `komelia-domain:core` | Domain models, repository interfaces |
| `komelia-domain:komga-api` | Komga API models and client |
| `komelia-infra:*` | Infrastructure: database, image decoding, JNI |

When editing only UI code, `:komelia-ui:compileDebugKotlinAndroid` is sufficient for compile checking.

---

## Kotlin Multiplatform Source Sets

`komelia-ui` has these source sets:

| Source set | Compiled by |
|-----------|-------------|
| `commonMain` | All targets (compile with any `compileKotlin*` task) |
| `androidMain` | `:komelia-ui:compileDebugKotlinAndroid` |
| `jvmMain` | `:komelia-ui:compileKotlinJvm` |
| `wasmJsMain` | `:komelia-ui:compileKotlinWasmJs` |

`expect`/`actual` declarations must have matching files in each target source set. A compile error in `androidMain` about a missing `actual` means you need a corresponding file in `src/androidMain/kotlin/...`.

---

## Known API Gotchas (Compose Multiplatform 1.11.0-alpha01)

### `AnchoredDraggableState`
- **Constructor requires `anchors`** — pass `DraggableAnchors { ... }` directly in the constructor.
- **`updateAnchors()` does NOT exist** in CMP 1.11 — do not call it. Set anchors once at construction.
- **`state.offset` is `Float.NaN`** before the first gesture. Always guard:
  ```kotlin
  val offsetPx = if (state.offset.isNaN()) collapsedOffsetPx else state.offset
  ```

### `SharedTransitionScope` / `AnimatedContent`
- Requires `@OptIn(ExperimentalSharedTransitionApi::class)` on any composable using `sharedElement()`
- `LocalSharedTransitionScope.current` can be null when not inside a `SharedTransitionLayout` — always null-check before calling `with(scope) { ... }`

---

## Gradle Configuration Cache

The project uses Gradle's configuration cache. If you see cache-related errors:
```bash
./gradlew assembleDebug --no-configuration-cache
```

The cache is usually fine — only disable it if you get explicit configuration cache errors.

---

## Clean Build

If you get strange errors that don't match the code (stale cache):
```bash
./gradlew clean
./gradlew assembleDebug
```
