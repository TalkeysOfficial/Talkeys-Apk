# iosApp — Minimal Smoke-Test iOS Application

This is a minimal SwiftUI iOS app that proves the Kotlin Multiplatform `:shared` module can be loaded and called from Swift. It is **not** feature parity with the Android app; it is a local smoke test only.

## Prerequisites

- **Xcode** (with iOS 16.0+ SDK)
- **Java 17+** and **Gradle** (for building the shared Kotlin framework)

## How to open

Open `iosApp/iosApp.xcodeproj` in Xcode.

## How it works

The app consumes `sharedKit` through Kotlin Multiplatform direct integration. An Xcode Run Script Build Phase invokes `:shared:embedAndSignAppleFrameworkForXcode` before Swift compilation, which builds the Kotlin/Native framework and embeds it into the app automatically.

The initial screen displays a greeting string returned from Kotlin `commonMain` code, confirming that Swift can import and call shared Kotlin APIs.

## Build command

```bash
./scripts/build-all.sh
```

This builds both the Android debug APK and the iOS simulator app in sequence.

## What is intentionally deferred

The following are **not** included in this smoke-test app and will be added in later phases:

- Authentication (Google Sign-In, Apple Sign-In)
- Networking and API calls
- Payment integration (PhonePe)
- Firebase (FCM, Analytics)
- Deep links and notifications
- Real product screens (events, profile, dashboard)
- Storage and data persistence
