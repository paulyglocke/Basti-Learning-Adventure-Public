# Session handoff — 2026-09-20

## Resume here

### Update: first P0 implementation

User explicitly requested the first P0 step: answer/audio activation isolation. The shared `choicesHtml()` now renders two sibling HTML buttons (selection and full-width audio below it), removing nested interactive controls and manual keyboard event handling. Correct answers disable selection only; audio remains available. Tests added for English/German across all seven quiz modes, pointer/Enter/Space audio, Tab order, and repeat scoring. Compose shell untouched. Build/lint passed; all 12 browser tests passed (2.8 minutes), including the three new regression tests. Remaining P0 tasks from the audit are still outstanding.

User requested a senior technical/product audit before further migration, then explicitly asked to save memory in case of disconnect. The audit was presented. No app code was changed during the audit. Next recommended work is a bounded correctness patch, followed by a reusable native quiz framework and native Prepositions.

Repository: https://github.com/paulyglocke/Basti-Learning-Adventure-Public
Local path: `/home/paul/Projects/Basti_Learning_Adventure_V1_For_Codex`
Audited revision: `290ff0381e1208d512cd06e08131568fca5aee5b`. Confirmed identical to remote main. GitHub CI succeeded for that revision.

Read this file and `TECHNICAL_AUDIT.md` before resuming. Existing documentation is partly stale; source and audit evidence take precedence over old implementation claims. Check current git status and remote revision before edits: another task may handle Compose migration. Do not overwrite concurrent changes.

## User direction and constraints

- Fully native Android is the eventual goal: Compose runtime UI, native quiz logic/audio/rewards/animations/progress, no WebView/HTML/JavaScript dependency.
- Do not chase GitHub language percentages or delete legacy code before equivalent native functionality is tested.
- Incremental migration; no broad rewrite before presenting findings. Audit has now been presented, and user allowed a conservative implementation phase if the priority is clear.
- Child is 6–7 and cannot reliably read: activities must be completable through visual/audio guidance.
- Preserve English/German, dinosaur/dragon identity, bright colors, large targets, existing educational logic, forgiving retries.
- Fully offline; no accounts, analytics, personal-data collection, cloud speech or Google Play Services requirement.
- Samsung Galaxy S24 and Amazon Fire Max; phone/tablet, portrait/landscape, cutouts/system bars.
- Future games use layered 2D/2.5D, not Unity/heavy 3D/video.
- Game order: Follow the Instructions, Memory Pairs, Dragon Treasure Hunt, Dinosaur Rescue, Crocodile Snap.
- Prior user instruction: a separate task handles Compose shell/home/Options/insets. Coordinate scope; do not redo that work blindly.

## Current architecture

One Kotlin `MainActivity.kt` owns Compose home, Options, top bar, screen enum navigation, SharedPreferences, WebView creation, and Android TTS. All seven quiz modes, 53 Verb Explorer lessons, CSS emoji animations, audio policy/tutorial flags, scoring/completion and balloons still run in `app/src/main/assets/index.html` and `app.js`.

No persistent learning progress exists. Vocabulary Booster incorrectly routes to Animal Actions. All five game cards are placeholders. Native home is always two columns with fixed 142dp card heights, not a complete adaptive design.

## Validation actually performed

- `JAVA_HOME=/home/paul/.local/share/basti-tools/jdk17 ./gradlew assembleDebug lintDebug --console=plain`: successful, 0 lint errors / 6 warnings. First sandboxed attempt failed on network-interface initialization; escalated run passed in 2m41s.
- Existing Playwright suite: 9/9 passed. Uses mocked Android speech; does not establish actual device audio or Compose behavior.
- Focused bilingual matrix: seven quiz modes × English/German × all/questions/off. Automatic speech present in all/questions and absent in off; four answer speakers rendered.
- Tested scores 0/5, 2/5, 5/5, 10/10: generated star count and text agree. Double correct tap awards only once; Continue resets session. Ten-star visual layout still clips on phones.
- Confirmed keyboard speaker activation submits an answer; correct speaker + Enter sets score=1 and answered=true.
- Confirmed manual speech bypasses Sound Off; tutorials marked heard while off; fetch/chase select old animation family; preposition sentences reference wrong objects.
- `adb devices` outside sandbox: no devices attached. No physical S24/Fire validation.
- Latest published release is v1.0.0, an older artifact; current debug APK ~8.5MB versus tracked V1 APK ~48KB. Version remains code 1/name 1.0.

## Next ten tasks

1. P0: Separate answer selection and speaker buttons; test pointer/Enter/Space/accessibility activation.
2. P0: Correct Prepositions scene-linked prompts, choices and EN/DE feedback.
3. P0: Render individually wrapping stars; ensure completion actions visible on short/narrow screens.
4. P0: Define/enforce audio policy; cancel stale speech; handle TTS readiness and missing offline voices.
5. P0: Repair native Back/WebView lifecycle and durable tutorial reset; verify device insets.
6. P1: Native settings/session repositories and restoration tests.
7. P1: Reusable native quiz/audio/completion components.
8. P1: Migrate Prepositions end-to-end as first native activity.
9. P1: Stable content models and articulated native verb-animation prototype.
10. P1: Correct docs/release identity and add native CI tests.

## Tooling

- JDK: `/home/paul/.local/share/basti-tools/jdk17`
- Node tools: `/home/paul/.local/share/basti-tools/node-v22.14.0-linux-x64/bin`
- Browser tests: prepend Node tools to PATH, then `npm test -- --reporter=line`. Chromium needs execution outside the restricted sandbox.
- GitHub CLI: `/home/paul/.local/share/basti-tools/gh_2.101.0_linux_amd64/bin/gh`
- SDK: `/home/paul/Android/Sdk`
- Audit diagnostic scripts: `/tmp/basti-audit.cjs`, `/tmp/basti-audit-extra.cjs`; screenshot `/tmp/basti-completion-audit.png`. These are temporary evidence, not maintained tests; they may disappear.
- Sandbox DNS blocked GitHub; escalated read-only remote/CI checks succeeded. Do not mistake that for repository failure.

The audit was a review, not a completed fix pass. Do not repeat earlier claims that all animations teach their actions, all audio policies work, or all system-bar/device cases are verified.
