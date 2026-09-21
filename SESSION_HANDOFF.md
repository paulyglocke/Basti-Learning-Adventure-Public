# Session handoff — 2026-09-21

## Current session

Documentation Pass 2 adds NATIVE_ARCHITECTURE_SPEC.md, CONTENT_DATA_SPEC.md, UX_NAVIGATION_SPEC.md and TESTING_QA_SPEC.md and integrates their reading order. No runtime code changed and no Compose migration started. Read AGENTS.md and the master specs first; BACKLOG.md is the current action queue. Historical audit recommendations and earlier task permissions do not override the current task or master specs.

Current source: Kotlin MainActivity owns Compose home/Options/top bar, shell navigation, SharedPreferences, Android TTS and WebView hosting. All seven quiz modes, 53 Verb Explorer lessons, CSS animations, tutorials, scoring/completion and balloons remain legacy web code. All five Play cards are placeholders; Vocabulary Booster falls back to Animal Actions. No persistent skill-level progress exists.

The prior P0 answer/audio activation isolation is complete: choicesHtml uses sibling selection/audio buttons and retains audio after a correct answer. Its recorded build/lint and 12 browser-test results are historical (2026-09-20); see BUILD_NOTES.md. Remaining correctness work is in BACKLOG.md, including Prepositions, speech glyph leakage, completion layouts, Letters case, audio policy, navigation/lifecycle and physical device validation.

Pass 2 verification is documentation-only: git diff, whitespace, Markdown/local file references and cross-spec consistency. No new device/build/browser validation is implied. The repository is ready to return to bounded implementation work from BACKLOG.md when requested. The detailed all/questions/off audio policy, mastery thresholds and parent-entry interaction still need decisions in their owning specs when those features are implemented; do not treat conceptual schemas as existing code.

## Historical session context (2026-09-20)

Audit revision: `290ff0381e1208d512cd06e08131568fca5aee5b`; remote/CI identity was checked then, not reverified in this pass. See TECHNICAL_AUDIT.md for detailed findings. No physical S24/Fire device was attached; browser mocks did not establish real audio/native behavior.

The earlier session used `/home/paul/Projects/Basti_Learning_Adventure_V1_For_Codex`. Those paths and temporary artifacts below belong to that host and may not exist in another checkout. Prior concurrent Compose work and implementation authorization were session-specific; inspect git status/diff and source before new work.

## Historical tooling

- JDK: `/home/paul/.local/share/basti-tools/jdk17`
- Node tools: `/home/paul/.local/share/basti-tools/node-v22.14.0-linux-x64/bin`
- Browser tests: prepend Node tools to PATH, then `npm test -- --reporter=line`. Chromium needs execution outside the restricted sandbox.
- GitHub CLI: `/home/paul/.local/share/basti-tools/gh_2.101.0_linux_amd64/bin/gh`
- SDK: `/home/paul/Android/Sdk`
- Audit diagnostic scripts: `/tmp/basti-audit.cjs`, `/tmp/basti-audit-extra.cjs`; screenshot `/tmp/basti-completion-audit.png`. These are temporary evidence, not maintained tests; they may disappear.
- Sandbox DNS blocked GitHub; escalated read-only remote/CI checks succeeded. Do not mistake that for repository failure.

The audit was a review, not a completed fix pass. Do not repeat earlier claims that all animations teach their actions, all audio policies work, or all system-bar/device cases are verified.
