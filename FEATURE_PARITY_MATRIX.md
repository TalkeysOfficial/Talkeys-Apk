# Feature Parity Matrix — Phase 0 Snapshot

Status of every user-visible flow on branch `kmp/ios-migration`, audited against source (not against README claims). Legend:

- ✅ **Client-wired** - source targets a real backend or platform API; runtime/backend success is not proven by this audit.
- ⚠️ **Partial** - UI exists, some logic works, but a critical path is local-only, stubbed, or has a hard `TODO`.
- ❌ **Mock / TODO only** - placeholder behaviour, no real implementation.

| Area | Flow | Status | Notes / citations |
|---|---|---|---|
| Auth | Landing screen | ✅ | [screens/authentication/LandingScreen.kt](app/src/main/java/com/example/talkeys_new/screens/authentication/LandingScreen.kt) |
| Auth | Google Sign-In + JWT verify | ✅ | `LoginScreenUI` → Google → `AuthService.verifyToken` → `TokenManager.saveToken` ([LoginScreenUI.kt:71-135](app/src/main/java/com/example/talkeys_new/screens/authentication/loginScreen/LoginScreenUI.kt:71)). Uses Android-app `AuthService`, **not** the shared `AuthRepository`. |
| Auth | Signup screen | ⚠️ | UI exists ([signupScreen/SignupScreenUI.kt](app/src/main/java/com/example/talkeys_new/screens/authentication/signupScreen/SignupScreenUI.kt)) — actual sign-up flow piggybacks on Google Sign-In; no separate signup endpoint. |
| Auth | Token persistence | ✅ | `TokenManager` uses `SecureStorage` (AES-256-GCM via Keystore) with 24h expiry ([TokenManager.kt](app/src/main/java/com/example/talkeys_new/screens/authentication/TokenManager.kt)). |
| Auth | Logout / revoke | ⚠️ | `ProfileScreen` invokes local token/profile clearing plus `GoogleAuthClient.revokeAccess()` and `signOut()`; no backend token revocation or account deletion client call was found ([screens/profile/ProfileScreen.kt](app/src/main/java/com/example/talkeys_new/screens/profile/ProfileScreen.kt), [GoogleAuthClient.kt:49](app/src/main/java/com/example/talkeys_new/screens/authentication/GoogleAuthClient.kt:49)). |
| Auth | Shared KMP auth (`AuthRepository`) | ❌ | Defined but not wired into the running app: Phase 0 removes its Koin registration ([SharedModule.kt](shared/src/commonMain/kotlin/com/talkeys/shared/di/SharedModule.kt)); its `/api/auth/google-signin` endpoint differs from active Android login ([AuthRepository.kt:23](shared/src/commonMain/kotlin/com/talkeys/shared/auth/AuthRepository.kt:23)); iOS bridge remains unfinished ([GoogleSignInProvider.ios.kt](shared/src/iosMain/kotlin/com/talkeys/shared/auth/GoogleSignInProvider.ios.kt)). |
| Events | List (Explore + Home) | ✅ | `EventsRepository.getAllEvents` → `GET getEvents` with LRU cache. |
| Events | Detail | ✅ | `EventsRepository.getEventById` → `GET getEventById/{id}`. |
| Events | Like / unlike | ❌ | Local-set only; `TODO: Implement API call for liking event` ([EventMediatorImpl.kt:209,229](app/src/main/java/com/example/talkeys_new/screens/events/mediator/EventMediatorImpl.kt:209)). Likes also tracked by Android-local `LikedEventsManager` ([utils/LikedEventsManager.kt](app/src/main/java/com/example/talkeys_new/utils/LikedEventsManager.kt)). |
| Events | Register for event | ❌ | `TODO: Implement API call for event registration` ([EventMediatorImpl.kt:244](app/src/main/java/com/example/talkeys_new/screens/events/mediator/EventMediatorImpl.kt:244)). |
| Events | Share | ✅ | Android `Intent.ACTION_SEND` ([EventMediatorImpl.kt:259-275](app/src/main/java/com/example/talkeys_new/screens/events/mediator/EventMediatorImpl.kt:259)). |
| Events | Creation wizard (6 steps UI) | ⚠️ | All 6 step screens exist ([screens/events/createEvent/CreateEvent1Screen.kt …CreateEvent6Screen.kt](app/src/main/java/com/example/talkeys_new/screens/events/createEvent)). Submission is a no-op: `TODO: Implement API call to create event` ([EventMediatorImpl.kt:331](app/src/main/java/com/example/talkeys_new/screens/events/mediator/EventMediatorImpl.kt:331)); `saveEventDraft` also TODO ([:326](app/src/main/java/com/example/talkeys_new/screens/events/mediator/EventMediatorImpl.kt:326)). |
| Events | "Registration successful" screen | ✅ | Static screen ([RegistrationSuccessScreen.kt](app/src/main/java/com/example/talkeys_new/screens/events/RegistrationSuccessScreen.kt)). Reached only via payment flow today. |
| Profile | View profile | ⚠️ | Screen exists ([ProfileScreen.kt](app/src/main/java/com/example/talkeys_new/screens/profile/ProfileScreen.kt)); reads `DashboardRepository.getUserProfile` → `GET dashboard/profile`. Backend wiring present. |
| Profile | Avatar customisation | ⚠️ | Avatar UI present ([avatar/AvatarCustomizerScreen.kt](app/src/main/java/com/example/talkeys_new/avatar/AvatarCustomizerScreen.kt)); `AvatarManager` is local-only — no upload endpoint. |
| Profile | Registered / Liked / Hosted events tabs | ⚠️ | Pulls `GET dashboard/events?type=registered\|bookmarked\|hosted`. Works for read; depends on whether backend has data, since like/register are not actually sent. |
| Dashboard | Organizer dashboard | ⚠️ | Screen + `DashboardViewModel` exist ([screens/dashboard/DashboardViewModel.kt](app/src/main/java/com/example/talkeys_new/screens/dashboard/DashboardViewModel.kt)). Activity endpoint returns weakly-typed `Map<String, Any>` ([DashboardApiService.kt:46](app/src/main/java/com/example/talkeys_new/api/DashboardApiService.kt:46)). |
| Payment | Book ticket | ✅ | `PaymentRepository.bookTicket` → `POST /api/book-ticket-app` (shared Ktor). |
| Payment | PhonePe checkout | ⚠️ | `PhonePePaymentManager.startCheckout` calls real `PhonePeKt.startCheckoutPage`. However `MockPaymentHandler` is present in `:utils`, while `MainActivity.getCurrentMerchantOrderId()` returns a hard-coded test order ID and `getCurrentAuthToken()` returns `null` ([PhonePePaymentManager.kt](app/src/main/java/com/example/talkeys_new/utils/PhonePePaymentManager.kt), [MainActivity.kt](app/src/main/java/com/example/talkeys_new/MainActivity.kt)). |
| Payment | Verification | ✅ | `PaymentVerificationScreen` → shared `PaymentApiService.checkPaymentStatus` → `GET /api/payment/app-status-check/{id}`. |
| Payment | WebView fallback | ✅ | `WebViewPaymentScreen` handles redirect-based PhonePe flow ([screens/payment/WebViewPaymentScreen.kt](app/src/main/java/com/example/talkeys_new/screens/payment/WebViewPaymentScreen.kt)). |
| Payment | Post-payment UI feedback | ❌ | `MainActivity.showPayment{Cancelled,Failed,Pending,Error}Message` are all `TODO: Implement UI` ([MainActivity.kt:622-648](app/src/main/java/com/example/talkeys_new/MainActivity.kt:622)). |
| Notifications | FCM service + channel | ✅ | `MyFirebaseMessagingService` creates channel and displays notifications. |
| Notifications | Consent flow | ✅ | `ConsentDialogHelper` + `FCMInitializationManager`. Note: `handleFCMConsent` currently auto-grants in `NOT_SET` branch with "TESTING MODE" comment ([MainActivity.kt:232-241](app/src/main/java/com/example/talkeys_new/MainActivity.kt:232)). |
| Notifications | Token registration with backend | ❌ | `sendRegistrationToServer` and `sendTokenToServer` are `TODO`s ([MyFirebaseMessagingService.kt](app/src/main/java/com/example/talkeys_new/MyFirebaseMessagingService.kt), [MainActivity.kt](app/src/main/java/com/example/talkeys_new/MainActivity.kt)). |
| Navigation | Routes wired in `AppNavigation` | ✅ | All non-payment routes registered ([navigation/AppNavigation.kt](app/src/main/java/com/example/talkeys_new/navigation/AppNavigation.kt)). |
| Navigation | `event_registration` route | ❌ | Composable body is empty/commented out ([AppNavigation.kt:94-97](app/src/main/java/com/example/talkeys_new/navigation/AppNavigation.kt:94)). |
| Navigation | Deep links | ❌ | No `<intent-filter>` for deep links observed; no `deepLinks` arg on any composable. |
| Static | About / Privacy / T&C / Contact | ✅ | Static composables under `screens/common/` and `com.example.talkeysapk.screensUI.home`. |
| Static | "Screen under construction" | ✅ | Placeholder reachable via `screen_not_found` route. |

## Cross-cutting notes

- **`com.example.shared` package** in `:shared` is generator-template scaffolding; not used by either platform. Deleted in this phase.
- **`com.talkeys.shared.Greeting`** is the KMP starter "hello world", invoked only by a debug log line in `MainActivity.onCreate`. Removed in this phase along with its `MainActivity` reference.
- **Two competing auth implementations** exist (Android-app `AuthService` vs shared `AuthRepository`). Only the Android one is wired. The shared one carries placeholder/mock behaviour and is unwired here.
- **PhonePe credential exposure:** a production `CLIENT_SECRET` was removed from Android source in Phase 0. It must still be rotated or revoked outside the repository because it was already committed on `master`.
