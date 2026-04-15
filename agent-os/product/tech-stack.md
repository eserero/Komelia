# Tech Stack

## Language

- **Kotlin** (Kotlin Multiplatform — shared code across all targets)

## UI

- **Compose Multiplatform** (JetBrains) — shared UI across Android, Desktop, and Web
- **Material 3** components
- **Voyager** — navigation/routing

## Platform Targets

- **Android** (minSdk 26)
- **Desktop JVM** — Linux x86_64, Windows x86_64
- **Web** — WASM (WebAssembly via Kotlin/Wasm)

## Networking

- **Ktor** — HTTP client (KMP-compatible)
- **komga-client** — Komga REST API client library
- **komf-client** — Komf metadata extension client

## Database

- **SQLite** via sqlite-jdbc
- **Exposed** — Kotlin SQL framework / ORM
- **Flyway** — database migrations

## Image Loading & Processing

- **Coil** — async image loading (KMP-compatible)
- **libvips** — native high-performance image processing (via JNI/CMake)
- **ONNX Runtime** — AI-based image upscaling

## Async & Concurrency

- **kotlinx.coroutines** — structured concurrency
- **kotlinx.atomicfu** — atomic operations

## Serialization & Data

- **kotlinx.serialization** — JSON serialization
- **kotlinx.datetime** — date/time utilities
- **kotlinx.io** — I/O abstractions
- **AndroidX DataStore** — settings/preferences persistence (Android)
- **Protobuf** — binary serialization for settings (Desktop/Web)

## Build

- **Gradle** (Kotlin DSL)
- **Android Gradle Plugin (AGP)**
- **CMake + Ninja** — native C++ library builds (libvips, ONNX, WebView)

## Other

- **logback / kotlin-logging** — logging
- **Coil** — image loading with caching
- **cache4k** — in-memory caching
- **FileKit** — file picker / file system access

## Backend

None — Komelia is a pure client application. It connects to a user-operated [Komga](https://komga.org/) server via its REST API.
