# Toolchain Decision — Phase 1

## Original versions (Phase 0 baseline)

| Component | Version |
|---|---|
| AGP | 8.10.1 |
| Gradle wrapper | 8.11.1 |
| Kotlin | 2.0.21 |
| `:shared` plugin | `com.android.kotlin.multiplatform.library` |

## Why the original combination is outside Kotlin's documented compatibility range

Kotlin 2.0.20–2.0.21 officially supports AGP up to **8.5** and Gradle up to **8.8** (with 8.7–8.8 having a KMP deprecation-warning caveat). The project was already running AGP **8.10.1** and Gradle **8.11.1**, which exceed both upper bounds. While the build happened to succeed at runtime, the combination is not inside the documented support matrix, meaning any build failure would be unsupported.

Source: [Kotlin Gradle compatibility table](https://kotlinlang.org/docs/gradle-configure-project.html)

## Why the Android-KMP plugin is retained

The `:shared` module uses `com.android.kotlin.multiplatform.library`, which is the officially supported AGP plugin for KMP Android library modules. Its prerequisites are AGP >= 8.10.0 and KGP >= 2.0.0; both are satisfied by the selected combination. There is no reason to migrate back to `com.android.library` + `androidTarget { }` — the project is already on the recommended path.

Source: [Android KMP plugin documentation](https://developer.android.com/kotlin/multiplatform/plugin)

## Selected combination

| Component | Version |
|---|---|
| AGP | 8.10.1 (unchanged) |
| Gradle wrapper | 8.11.1 (unchanged) |
| Kotlin | **2.2.21** (upgraded from 2.0.21) |
| `:shared` plugin | `com.android.kotlin.multiplatform.library` (unchanged) |

### Why Kotlin 2.2.21

- Kotlin 2.2.20–2.2.21 supports AGP **7.3.1–8.11.1** and Gradle **7.6.3–8.14**, so AGP 8.10.1 and Gradle 8.11.1 are squarely inside the range.
- The Android Kotlin/AGP support page confirms Kotlin 2.2 requires AGP 8.10+, which this project already has.
- 2.2.21 is the latest patch in the 2.2.x line at time of writing.

Sources:
- [Kotlin Gradle compatibility table](https://kotlinlang.org/docs/gradle-configure-project.html)
- [Android Kotlin/AGP support](https://developer.android.com/build/kotlin-support)
- [Android KMP plugin](https://developer.android.com/kotlin/multiplatform/plugin)

## AGP 9 is intentionally deferred

AGP 9 is unnecessary for this migration phase and would introduce additional unrelated changes (new DSL requirements, potential plugin compatibility issues). The current AGP 8.10.1 is inside the Kotlin 2.2.21 compatibility window and meets the KMP plugin minimum. AGP 9 can be evaluated independently in a future phase if needed.

## Files modified in this phase

- `gradle/libs.versions.toml` — Kotlin version entries aligned to 2.2.21.
- `TOOLCHAIN_DECISION.md` — this file.

No application source files, build scripts, Gradle wrapper, or AGP version were changed. Phase 0 audit and parity documents (`CURRENT_CLIENT_API_AUDIT.md`, `FEATURE_PARITY_MATRIX.md`) are unchanged.
