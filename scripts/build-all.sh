#!/usr/bin/env bash
set -euo pipefail

./gradlew :app:assembleDebug

xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -destination 'generic/platform=iOS Simulator' \
  -configuration Debug \
  build
