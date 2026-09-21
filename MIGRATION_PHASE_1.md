# Compose migration, phase 1

Migration-plan/history document: this records the initial shell migration and remaining work, not a full architecture specification or proof of native parity. Current master specs and source take precedence.

The app previously used one Java `Activity` that hosted a full-screen local WebView. The HTML and JavaScript owned the home screen, options, game navigation, settings, language switching, speech, and learning logic.

This phase introduced the hybrid Compose + legacy WebView implementation by moving the Android shell to Kotlin and Jetpack Compose Material 3. `app/src/main/java/com/bellfamily/bastischool/MainActivity.kt` owns the native top app bar, home screen, options screen, Android back handling, preferences, and system-bar policy. The existing WebView remains the learning surface for existing learning activities and keeps the current offline content and educational logic working while each learning screen is moved incrementally.

`WindowCompat.setDecorFitsSystemWindows(window, false)` is paired with `WindowInsets.statusBars` on the native top bar and `WindowInsets.safeDrawing` on the scaffold. The WebView receives scaffold content padding. This is an inset-handling starting point, not verified coverage of all cutouts, orientations or devices; physical S24/Fire and native UI tests remain required.

The native home is grouped into Learn, Practice, and Play. Existing modes launch through the WebView bridge; future Play cards are visible but intentionally inactive until their content exists. Native settings are mirrored into the WebView so language, sound, round length, and number level continue to affect the existing games.

Long-term target: fully native Compose with no runtime WebView/HTML/JavaScript. Remove legacy routes only after tested native parity, including bilingual content, audio, navigation, completion and required data migration. Shell ownership does not imply that Back, lifecycle or settings synchronization are fully correct; see BACKLOG.md.

Known follow-up work for later migration passes:

- Replace the temporary emoji card art with custom dinosaur/dragon illustrations following ART_DIRECTION.md; a shared production asset system is not yet established.
- Migrate individual learning screens from WebView to Compose one at a time.
- Add automated Compose UI tests for inset handling, compact phone layouts, tablets, and landscape.
- Validate on a physical Galaxy S24 and an Amazon Fire tablet; the build itself has no Google Play Services dependency.
