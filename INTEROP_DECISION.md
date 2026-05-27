# Swift/Kotlin Interop Decision — Phase 3

## Selected strategy

**SKIE** (Swift Kotlin Interface Enhancer) by Touchlab.

## Versions

| Component | Version |
|---|---|
| SKIE plugin | `0.10.12` |
| Kotlin | `2.2.21` |

## Compatibility evidence

SKIE is compatible with Kotlin versions from `2.0.0` through `2.3.10` ([source](https://skie.touchlab.co/intro)). Kotlin `2.2.21` falls within this range.

## AndroidX lifecycle-viewmodel pin

AndroidX ViewModel supports KMP from version `2.8.0+`. The official current documentation demonstrates `2.10.0`. This project pins `androidx.lifecycle:lifecycle-viewmodel` to **`2.9.0`** because testing `2.10.0` changed resolved Compose dependencies (transitively upgrading `foundation` from 1.7.x to 1.9.x) and caused the existing Android production usages of `animateItemPlacement()` to fail compilation in `ExploreEventscreenUI.kt`, `HomeScreen.kt`, and `LikedEventsScreen.kt`.

This is an intentional compatibility pin; do not modify production Android screens during Phase 3 merely to absorb that dependency update. The pin can be revisited when the Compose BOM is upgraded and the deprecated `animateItemPlacement` call sites are migrated to their replacement API.

## Why SKIE over alternatives

### vs. handwritten Swift wrappers around Objective-C-exposed Flows

Kotlin/Native exports `Flow` to Objective-C as an opaque interface without generic type information. Consuming it from Swift requires manual callback bridging, loses element types, and forces every ViewModel to carry boilerplate wrapper code. SKIE automatically converts `StateFlow<T>` into a typed Swift `AsyncSequence`, eliminating this entire category of glue code.

### vs. KMP-NativeCoroutines

KMP-NativeCoroutines is a viable alternative but requires annotating every exposed coroutine/flow property with `@NativeCoroutines` and generates companion wrapper functions. SKIE operates at the framework level with no per-property annotations, producing a cleaner Kotlin API surface. SKIE also preserves generic type arguments through the Swift export.

## Intended production pattern

1. **commonMain ViewModels** extend AndroidX `ViewModel` and expose `StateFlow<UiState>`.
2. **SwiftUI** observes those flows as Swift `AsyncSequence` via SKIE using `for await` loops.
3. **SwiftUI** owns lifecycle and display concerns; shared ViewModels own state and business logic.
4. **Android Compose** observes the same `StateFlow` instances via `collectAsState()`.

## Key SKIE limitation preserved in architecture

Do not throw exceptions through UI Flows consumed by Swift. Exceptions crossing the Kotlin/Native boundary in a `for await` loop crash the app. Instead, expose errors as part of the UI state model (e.g., `ApiResult.Failure`) so Swift always receives a value, never an exception.

## SKIE preview SwiftUI helpers

SKIE offers preview SwiftUI observation helpers (`Observing`, `.collect`). These are **not enabled** in this phase. The stable `for await` AsyncSequence collection pattern documented at [SKIE Flow interop](https://skie.touchlab.co/features/flows) is used instead.

## Official sources

- [SKIE overview and compatibility](https://skie.touchlab.co/intro)
- [SKIE installation](https://skie.touchlab.co/Installation)
- [SKIE Flow interop](https://skie.touchlab.co/features/flows)
- [AndroidX KMP ViewModel guide](https://developer.android.com/kotlin/multiplatform/viewmodel)
