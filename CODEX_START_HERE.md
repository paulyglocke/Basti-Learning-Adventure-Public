# Codex: Start Here

This repository/project is **Basti's Learning Adventure**, an offline bilingual Android learning app for a 6–7 year old preparing for school.

Before editing anything, read:

1. `PROJECT_BRIEF.md`
2. `README.md`
3. the existing Android/Gradle files
4. `app/src/main/assets/index.html`
5. `app/src/main/java/com/bellfamily/bastischool/MainActivity.java`
6. `.github/workflows/build-apk.yml`

## Your first task

1. Inspect the entire project and summarise the current architecture and features.
2. Run/build the Android project.
3. Fix any Gradle, Android SDK, Java, manifest, WebView, asset, or GitHub Actions build errors you encounter.
4. Do not redesign or replace working functionality just for architectural preference.
5. Preserve the product requirements in `PROJECT_BRIEF.md`.
6. Verify the app remains fully offline and does not depend on Google Play Services.
7. Verify English/German switching and persisted settings.
8. Verify responsive phone/tablet behaviour.
9. Verify number levels (10/20/50/100/custom) and large-number exercise logic.
10. Verify all verb quiz entries have usable Verb Explorer lessons.
11. Verify the "Learn this verb" flow returns to the same quiz question.
12. Improve obvious animation/UI problems where safe, while keeping animations local/offline.
13. Run a final build and report exactly what changed, any remaining issues, and the APK output path.

## Important

The child-facing app must remain age-appropriate. Do not turn explanations into dictionary definitions or make exercises harder simply because the code supports larger values.
