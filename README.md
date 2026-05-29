# Talkeys

Talkeys is an event discovery, registration, profile, and payment app. The
Android app is the production client today, and the repository is being migrated
to Kotlin Multiplatform so Android and iOS can share business logic while
keeping platform-native UI.

## Current Status

The codebase is in an active KMP migration branch.

- Android remains the primary runnable app.
- `:shared` contains the cross-platform Kotlin code used by Android and exposed
  to Swift.
- `iosApp` is a SwiftUI app wired to the shared framework. It currently proves
  framework loading, Swift interop, and shared ViewModel observation. Full iOS UI
  parity is still in progress.
- Shared logic currently covers events, profile/dashboard data, authentication
  storage/repository foundations, payment checkout state, and common networking.

For detailed migration decisions, see:

- [TOOLCHAIN_DECISION.md](TOOLCHAIN_DECISION.md)
- [INTEROP_DECISION.md](INTEROP_DECISION.md)
- [FEATURE_PARITY_MATRIX.md](FEATURE_PARITY_MATRIX.md)
- [CURRENT_CLIENT_API_AUDIT.md](CURRENT_CLIENT_API_AUDIT.md)

## Repository Layout

```text
.
├── app/                 Android app: Jetpack Compose UI and Android platform integrations
├── shared/              Kotlin Multiplatform module used by Android and iOS
├── iosApp/              SwiftUI app that consumes the shared framework
├── scripts/             Local build scripts
├── gradle/              Version catalog and Gradle wrapper configuration
└── *.md                 Architecture, audit, and migration notes
```

## Architecture

The target architecture is native UI on each platform with shared Kotlin for
data, domain, and presentation state.

```text
Android Compose UI              SwiftUI UI
       │                            │
       ├──────── platform adapters ─┤
       │                            │
shared Kotlin ViewModels / StateFlow / validators
       │
shared repositories
       │
Ktor API clients, serialization, storage interfaces
       │
Platform implementations where needed
```

### Shared Module

The `:shared` module is Kotlin Multiplatform and currently includes:

- Ktor networking and JSON serialization
- Koin dependency injection
- AndroidX Lifecycle ViewModel KMP
- SKIE for Swift-friendly Flow/ViewModel interop
- Kermit logging facade
- Events API, repository, UI state, detail/list ViewModels, and creation state
- Profile/dashboard API, repository, and ViewModels
- Auth repository, token storage interfaces, and platform storage foundations
- Payment checkout state and PhonePe checkout URL preparation

Android still contains platform UI and integrations such as Compose screens,
Google Sign-In UI, PhonePe WebView handling, Firebase messaging, Android
navigation, and Android-specific sharing/image picking.

## Toolchain

Key versions are managed in [gradle/libs.versions.toml](gradle/libs.versions.toml).

- Kotlin: `2.2.21`
- Android Gradle Plugin: `8.10.1`
- Gradle wrapper: `8.11.1`
- Ktor: `3.0.1`
- SKIE: `0.10.12`
- AndroidX Lifecycle ViewModel KMP: `2.9.0`

## Prerequisites

- macOS for iOS builds
- Xcode with iOS 16+ SDK
- Android Studio or IntelliJ IDEA
- JDK 17+
- Android SDK 35

## Build

Build Android only:

```bash
./gradlew :app:assembleDebug
```

Build the shared iOS simulator framework:

```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

Build Android and iOS together:

```bash
./scripts/build-all.sh
```

Run shared tests:

```bash
./gradlew :shared:allTests
```

From Phase 2 onward, `./scripts/build-all.sh` is the local gate for migration
work.

## Running the Apps

### Android

Open the repository in Android Studio and run the `app` configuration on an
emulator or device.

Google Sign-In requires the app package and signing certificate SHA fingerprint
to be registered in the Google Cloud OAuth client. A Google Sign-In `ApiException:
10` usually means the SHA fingerprint or OAuth client configuration does not
match the build being installed.

### iOS

Open:

```text
iosApp/iosApp.xcodeproj
```

The Xcode project uses Kotlin Multiplatform direct integration. Its build phase
invokes `:shared:embedAndSignAppleFrameworkForXcode`, so Xcode builds and embeds
the Kotlin framework before compiling Swift.

## Security Notes

- Do not commit production secrets.
- Payment client secrets, signing secrets, and private credentials must live on
  the backend only. Mobile binaries are extractable.
- The mobile app may contain only public identifiers required by an SDK, such as
  a public client ID when the provider documents it as safe for clients.
- Auth tokens must not be logged.
- Payment request bodies, redirect tokens, order tokens, and callback payloads
  must not be logged.

PhonePe order creation and transaction verification should be performed by the
backend. The client should receive only client-consumable checkout data and then
ask the backend to verify transaction status.

## Backend/API Notes

The app currently talks to:

```text
https://api.talkeys.xyz
```

Endpoint inventory and known contract gaps are documented in:

- [CURRENT_CLIENT_API_AUDIT.md](CURRENT_CLIENT_API_AUDIT.md)
- [BACKEND_API_SPEC.md](BACKEND_API_SPEC.md)

Do not invent endpoint paths or response types during migration. Use the audit
documents and the existing client code as the source of truth, then update the
docs when the backend contract changes.

## Development Rules

- Keep Android building after each migration slice.
- Keep shared code platform-neutral unless the file is under `androidMain` or
  `iosMain`.
- Prefer shared repositories and shared ViewModels for business logic.
- Keep Compose and SwiftUI platform-native.
- Add `commonTest` coverage for new shared logic, especially Ktor API and
  serialization behavior.
- Run `./scripts/build-all.sh` before reporting migration work as complete.

## Git Workflow

The KMP migration work lives on:

```text
kmp/ios-migration
```

Recommended flow:

```bash
git checkout kmp/ios-migration
git pull --ff-only
./scripts/build-all.sh
git add <changed files>
git commit -m "type: concise description"
git push
```

Use focused commits. Avoid mixing feature migration, toolchain changes, and
cleanup unless they are directly connected.

## License

License information has not been finalized in this repository. Add a `LICENSE`
file before public distribution.
