# Current Client API Audit - Phase 0 Snapshot

Snapshot of every network call observed in source on branch `kmp/ios-migration`. Endpoints are quoted exactly as they appear in code. **No backend contract has been inferred; only what is wired today.** Citations are `path:line`.

Base hosts observed:
- `https://api.talkeys.xyz/` — Android Retrofit clients (auth + events + dashboard)
- `ProductionConfig.getApiBaseUrl()` — shared Ktor `ApiClient` (used by payments)
  - [shared/src/commonMain/kotlin/com/talkeys/shared/network/ApiClient.kt:31](shared/src/commonMain/kotlin/com/talkeys/shared/network/ApiClient.kt:31)

> The two base URL sources are not unified. The Android auth/events/dashboard clients hard-code `https://api.talkeys.xyz/` ([app/src/main/java/com/example/talkeys_new/screens/authentication/AuthService.kt:25](app/src/main/java/com/example/talkeys_new/screens/authentication/AuthService.kt:25), [app/src/main/java/com/example/talkeys_new/api/RetrofitClient.kt:8](app/src/main/java/com/example/talkeys_new/api/RetrofitClient.kt:8), [app/src/main/java/com/example/talkeys_new/screens/events/RetrofitClient.kt:19](app/src/main/java/com/example/talkeys_new/screens/events/RetrofitClient.kt:19)); only `PaymentApiService` reads from `ProductionConfig`. Reconcile this during feature/network migration; Phase 1 is toolchain-only.

---

## 1. Authentication (Android-only, Retrofit)

### `POST verify`
- File: [app/src/main/java/com/example/talkeys_new/screens/authentication/AuthService.kt:17-20](app/src/main/java/com/example/talkeys_new/screens/authentication/AuthService.kt:17)
- Full path: `https://api.talkeys.xyz/verify`
- Method: `POST`
- Headers: `Authorization: Bearer <google_id_token>` (passed as `@Header("Authorization")`)
- Body: none
- Response: `Response<UserResponse>` — Gson-deserialised. `UserResponse` shape lives in [app/src/main/java/com/example/talkeys_new/dataModels/DataClasses.kt](app/src/main/java/com/example/talkeys_new/dataModels/DataClasses.kt). The login flow reads `body.name` and `body.accessToken` ([app/src/main/java/com/example/talkeys_new/screens/authentication/loginScreen/LoginScreenUI.kt:131-135](app/src/main/java/com/example/talkeys_new/screens/authentication/loginScreen/LoginScreenUI.kt:131)).
- Consumer: `LoginScreenUI` — Google Sign-In flow.
- Status: Android-only. **Not** in `:shared`. The `AuthRepository` in `:shared` references a different endpoint `/api/auth/google-signin` ([shared/src/commonMain/kotlin/com/talkeys/shared/auth/AuthRepository.kt:23](shared/src/commonMain/kotlin/com/talkeys/shared/auth/AuthRepository.kt:23)) that is **never invoked** from the running app.

---

## 2. Events (shared read path; legacy Android actions retained)

Service: [app/src/main/java/com/example/talkeys_new/screens/events/EventApiService.kt](app/src/main/java/com/example/talkeys_new/screens/events/EventApiService.kt)
Repository: [app/src/main/java/com/example/talkeys_new/screens/events/EventsRepository.kt](app/src/main/java/com/example/talkeys_new/screens/events/EventsRepository.kt)
Auth: `AuthInterceptor` adds `Authorization: Bearer <jwt>` ([app/src/main/java/com/example/talkeys_new/screens/events/AuthInterceptor.kt:35-38](app/src/main/java/com/example/talkeys_new/screens/events/AuthInterceptor.kt:35)).

Phase 4 migration note: event list and event detail reads are now implemented in `:shared` with Ktor, typed models, a shared repository, and shared ViewModels. Android `ExploreEventsScreen`, `HomeScreen`, and `EventDetailScreen` observe the shared ViewModels through a temporary DTO rendering adapter; iOS consumes the same ViewModels from SwiftUI. The Retrofit service and Android event classes remain in the tree for unmigrated mediator/action code and are not the active read source for these three screens.

### `GET getEvents`
- File: [app/src/main/java/com/example/talkeys_new/screens/events/EventApiService.kt:12-13](app/src/main/java/com/example/talkeys_new/screens/events/EventApiService.kt:12)
- Full path: `https://api.talkeys.xyz/getEvents`
- Response: `Response<EventListResponse>` → `.data.events: List<EventResponse>`
- Current shared consumer: `com.talkeys.shared.data.events.EventsApi`, `EventsRepository`, `EventsListViewModel`; rendered by Android `ExploreEventsScreen` / `HomeScreen` and iOS `EventsListView`.

### `GET getEventById/{id}`
- File: [app/src/main/java/com/example/talkeys_new/screens/events/EventApiService.kt:15-18](app/src/main/java/com/example/talkeys_new/screens/events/EventApiService.kt:15)
- Full path: `https://api.talkeys.xyz/getEventById/{id}`
- Path params: `id: String`
- Response: `Response<EventDetailResponse>`
- Current shared consumer: `com.talkeys.shared.data.events.EventsApi`, `EventsRepository`, `EventDetailViewModel`; rendered by Android `EventDetailScreen` and iOS `EventDetailView`.

> No current client calls were found for likes, registration, event creation, or event submission. Those flows are local-only in this app - see [app/src/main/java/com/example/talkeys_new/screens/events/mediator/EventMediatorImpl.kt:209](app/src/main/java/com/example/talkeys_new/screens/events/mediator/EventMediatorImpl.kt:209) (`TODO: Implement API call`).

Weakly typed fields: the legacy Android DTOs still expose `EventResponse.ticketPrice` and `EventResponse.totalSeats` as `Any`. The shared read path handles the observed number-or-string input explicitly, exposing `ticketPrice: String` and `totalSeats: Int`; malformed values fail parsing rather than silently becoming zero. A temporary Android DTO rendering adapter remains until the surrounding Android-only event actions are migrated.

---

## 3. Dashboard (Android-only, Retrofit)

Service: [app/src/main/java/com/example/talkeys_new/api/DashboardApiService.kt](app/src/main/java/com/example/talkeys_new/api/DashboardApiService.kt)
Repository: [app/src/main/java/com/example/talkeys_new/api/DashboardRepository.kt](app/src/main/java/com/example/talkeys_new/api/DashboardRepository.kt)
Auth: `Authorization: Bearer <jwt>` passed per-call by repository.

### `GET dashboard/profile`
- File: [DashboardApiService.kt:24-27](app/src/main/java/com/example/talkeys_new/api/DashboardApiService.kt:24)
- Full path: `https://api.talkeys.xyz/dashboard/profile`
- Headers: `Authorization`
- Response: `Response<UserProfileResponse>` — fields: `_id, name, email, displayName?, about?, pronouns?, avatarUrl?, likedEvents`.

### `PATCH dashboard/profile`
- File: [DashboardApiService.kt:29-33](app/src/main/java/com/example/talkeys_new/api/DashboardApiService.kt:29)
- Body: `Map<String, String>` ← **weakly typed.** Backend contract for accepted keys is undocumented in code.
- Response: `Response<UserProfileResponse>`.

### `GET dashboard/events`
- File: [DashboardApiService.kt:35-41](app/src/main/java/com/example/talkeys_new/api/DashboardApiService.kt:35)
- Query: `type` (`"registered" | "bookmarked" | "hosted"` — string literals only, no enum), `status` (`"past" | "upcoming"`), `period` (`"1m" | "6m" | "1y"`).
- Response: `Response<UserEventsResponse>` → `events: List<EventResponse>`.

### `GET dashboard/activity`
- File: [DashboardApiService.kt:43-47](app/src/main/java/com/example/talkeys_new/api/DashboardApiService.kt:43)
- Query: `range?` (default `"1m"`).
- Response: `Response<Map<String, Any>>` ← **weakly typed.** No model; consumer caches the raw map. See [DashboardRepository.kt:174-218](app/src/main/java/com/example/talkeys_new/api/DashboardRepository.kt:174).

---

## 4. Payments (Shared / Ktor)

Service: [shared/src/commonMain/kotlin/com/talkeys/shared/network/PaymentApiService.kt](shared/src/commonMain/kotlin/com/talkeys/shared/network/PaymentApiService.kt)
Repository: [shared/src/commonMain/kotlin/com/talkeys/shared/data/payment/PaymentRepository.kt](shared/src/commonMain/kotlin/com/talkeys/shared/data/payment/PaymentRepository.kt)
Auth: optional `Authorization: Bearer <jwt>` per call.

### `POST /api/book-ticket-app`
- File: [PaymentApiService.kt:19](shared/src/commonMain/kotlin/com/talkeys/shared/network/PaymentApiService.kt:19)
- Full path: `${BASE_URL}/api/book-ticket-app`
- Headers: `Content-Type: application/json`, optional `Authorization: Bearer <token>`.
- Body: `BookTicketRequest { eventId, passType, friends: List<Friend{name,email}> }` — [PaymentModels.kt:6-16](shared/src/commonMain/kotlin/com/talkeys/shared/data/payment/PaymentModels.kt:6).
- Response: `BookTicketResponse { success, message, data: PaymentOrderData? }`. `PaymentOrderData` includes `passId, merchantOrderId, orderId, amount, amountInPaisa, totalTickets, token, event, qrStrings, friends` — [PaymentModels.kt:26-37](shared/src/commonMain/kotlin/com/talkeys/shared/data/payment/PaymentModels.kt:26).
- Timeout: 30s.
- Consumers: `PhonePePaymentManager.bookTicketAndPay` ([PhonePePaymentManager.kt:104](app/src/main/java/com/example/talkeys_new/utils/PhonePePaymentManager.kt:104)), `EventPaymentScreen` ([EventPaymentScreen.kt:386](app/src/main/java/com/example/talkeys_new/screens/payment/EventPaymentScreen.kt:386)).

### `GET /api/payment/app-status-check/{merchantOrderId}`
- File: [PaymentApiService.kt:70](shared/src/commonMain/kotlin/com/talkeys/shared/network/PaymentApiService.kt:70)
- Path params: `merchantOrderId`.
- Headers: optional `Authorization: Bearer <token>`.
- Response: `PaymentStatusResponse { success, status, data: PaymentStatusData? }` where `status ∈ {"COMPLETED","FAILED","PENDING"}` per comment.
- Consumer: `PhonePePaymentManager.verifyPaymentStatusOnServer`, `PaymentVerificationScreen`.

---

## Discrepancies with `BACKEND_API_SPEC.md`

[`BACKEND_API_SPEC.md`](BACKEND_API_SPEC.md) documents a proposed PhonePe contract, not the active client contract observed in source:

| Operation | Backend spec | Current shared client |
|---|---|---|
| Create order | `POST /api/payment/create-order` | `POST /api/book-ticket-app` |
| Status check | `GET /api/payment/order-status/{orderId}` | `GET /api/payment/app-status-check/{merchantOrderId}` |

These contracts must be reconciled with the backend before the payment migration is treated as production-ready.

---

## Cross-cutting observations

- **Security incident requiring out-of-repo action:** a production PhonePe `CLIENT_SECRET` was embedded in tracked Android source and removed in Phase 0. Because it already existed on `master`, the credential must be rotated or revoked with PhonePe/backend owners.
- **Payment routing risk left for migration:** the current Android navigation route carries a payment URL/token and order/pass IDs as route arguments. It no longer logs them in Phase 0, but this should be redesigned during payment migration so secrets are held in transient state rather than navigation strings.
- **Two HTTP stacks today:** Retrofit/OkHttp/Gson (Android `:app`) and Ktor (`:shared`). Consolidate this as features move into shared code, not during the toolchain-only Phase 1.
- **Two `Authorization` flows:** Android requests pipe the JWT through an interceptor; shared Ktor calls require the caller to pass the token. `EventPaymentScreen` reaches into Android `TokenManager` and then calls a shared service ([EventPaymentScreen.kt:351-386](app/src/main/java/com/example/talkeys_new/screens/payment/EventPaymentScreen.kt:351)).
- **No FCM-token-to-backend client call exists.** `MyFirebaseMessagingService.sendRegistrationToServer` is a stub ([MyFirebaseMessagingService.kt](app/src/main/java/com/example/talkeys_new/MyFirebaseMessagingService.kt) - `TODO: Implement`).
- **No current client calls were found for:** like/unlike, event registration, event creation, profile-image upload, FCM token registration, or backend logout/revoke.
