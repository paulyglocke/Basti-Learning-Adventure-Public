# Compose migration, phase 1

The app previously used one Java `Activity` that hosted a full-screen local WebView. The HTML and JavaScript owned the home screen, options, game navigation, settings, language switching, speech, and learning logic.

This phase moves the Android shell to Kotlin and Jetpack Compose Material 3. `MainActivity` now owns the native top app bar, home screen, options screen, Android back handling, preferences, and system-bar policy. The existing WebView remains the learning surface for migrated games and keeps the current offline content and educational logic working while each learning screen is moved incrementally.

`WindowCompat.setDecorFitsSystemWindows(window, false)` is paired with `WindowInsets.statusBars` on the native top bar and `WindowInsets.safeDrawing` on the scaffold. The WebView receives the scaffold content padding, so content clears status bars, display cutouts, and navigation bars in portrait and landscape on phones and tablets.

The native home is grouped into Learn, Practice, and Play. Existing modes launch through the WebView bridge; future Play cards are visible but intentionally inactive until their content exists. Native settings are mirrored into the WebView so language, sound, round length, and number level continue to affect the existing games.

Known follow-up work for the next migration pass:

- Replace the temporary emoji card art with the project’s dinosaur and dragon asset system.
- Migrate individual learning screens from WebView to Compose one at a time.
- Add automated Compose UI tests for inset handling, compact phone layouts, tablets, and landscape.
- Validate on a physical Galaxy S24 and an Amazon Fire tablet; the build itself has no Google Play Services dependency.
