# The Floor — Android

> The global home of the people behind every customer conversation.

Kotlin + Jetpack Compose (Material 3) app. Phase-1 scope: auth & onboarding,
profiles with privacy controls, community discovery (The Floor), Talk,
Invite & Earn (server-authoritative referrals), Rewards, notifications.
Jobs / Academy / Marketplace / Radio ship behind server feature flags.

## Build

Requirements: JDK 17, Android Studio Ladybug+ (or just Gradle + Android SDK 35).

```bash
./gradlew assembleDebug        # APK → app/build/outputs/apk/debug/
./gradlew testDebugUnitTest    # JVM unit tests
```

Debug builds target `http://10.0.2.2:8080` (a locally running Floor backend —
see the companion backend repo). Release configuration reads signing
credentials from `~/.gradle/gradle.properties` (never committed):
`FLOOR_STORE_FILE`, `FLOOR_STORE_PASSWORD`, `FLOOR_KEY_ALIAS`, `FLOOR_KEY_PASSWORD`.

## CI

Every push to `main` runs unit tests, assembles a debug APK, and uploads it
as a build artifact (Actions tab → latest run → `the-floor-debug-apk`).

## Architecture

UI → ViewModel → UseCase/Repository → Remote (Retrofit) / Local (Room, DataStore).
Single module, package-per-layer (`core/*`, `feature/*`); design tokens live in
`core/designsystem`. Money, referral counts, reward balances and fraud state are
server-authoritative — the app renders, never computes them. Full architecture
docs live in the project's docs repository.
