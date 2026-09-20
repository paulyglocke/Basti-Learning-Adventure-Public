# Follow-up backlog for the stronger review

This is a review queue for the next model pass. Items are ordered by risk and
usefulness; none are required to call the current source a working V1.

## P0 — verify on real hardware

- Install and launch the APK on a Samsung S24-series phone in airplane mode.
- Install and launch it on an Amazon Fire Max tablet in airplane mode.
- Check Android Back from home, game, Verb Explorer, and a verb lesson.
- Confirm English and German offline TTS voices speak, and that switching
  language changes the selected voice without requiring network access.
- Check Android 15 edge-to-edge insets, rotation, touch targets, and emoji
  rendering on both portrait and landscape screens.
- Exercise a 5-question and 10-question round on every activity, including
  Sunday-to-Monday and number maximum 9,999 cases.

## P1 — correctness and maintainability

- Replace inline `onclick` handlers and raw `innerHTML` rendering with event
  listeners and DOM helpers. Current content is bundled and static, but this
  would reduce future injection and quoting risk and remove the intentional
  WebView JavaScript lint warning.
- Add a small content validator that checks every quiz verb has a lesson,
  creature IDs resolve, bilingual fields exist, and answer generators always
  include the correct answer.
- Revisit the number level semantics: `1–10` currently permits zero in several
  recognition/order exercises, while visual counting starts at one. Decide
  whether zero should be taught explicitly or excluded consistently.
- Review German grammar for all generated sentences with a native speaker,
  especially articles, plural forms, and verb inflection.
- Move the APK out of the Git repository if release size or signing policy
  changes; retain checksum and GitHub release assets instead.

## P2 — product improvements

- Add a parent-only option for an independent maths difficulty ceiling.
- Add progress or completion history without collecting identifying data.
- Improve animation art beyond emoji/CSS while preserving offline packaging and
  Fire OS compatibility.
- Add a quiet audio fallback state that explains when no local voice is
  installed, while keeping visual questions fully usable.
- Add a release build/signing path separate from the debug sideload APK.
- Add accessibility review with TalkBack, larger text settings, reduced motion,
  and colour-contrast tooling.

## CI / release follow-up

- Keep `android-actions/setup-android` constrained to `platform-tools`; the
  obsolete default `tools` package caused the first GitHub run to fail.
- After CI is green, publish the existing `v1.0.0` draft and verify its APK
  checksum against `verification/apk-sha256.txt`.
- Consider pinning third-party Actions to commit SHAs for a hardened release
  workflow.
