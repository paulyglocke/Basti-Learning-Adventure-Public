# Codex: Start Here

Basti’s Learning Adventure is an offline, audio-first English/German Android learning app for school readiness.

Start in this order:

1. [AGENTS.md](AGENTS.md) — mandatory operating rules and source-of-truth hierarchy.
2. [MASTER_PRODUCT_LEARNING_ROADMAP.md](MASTER_PRODUCT_LEARNING_ROADMAP.md) — long-term product and learning vision.
3. [PROGRESS_TRACKER_SPEC.md](PROGRESS_TRACKER_SPEC.md) — progress, adaptive learning and skill tracking.
4. [GAME_DESIGN_SPEC.md](GAME_DESIGN_SPEC.md) — mechanics and shared game systems.
5. [VOICE_AUDIO_SPEC.md](VOICE_AUDIO_SPEC.md) — speech/audio architecture and quality.
6. [ART_DIRECTION.md](ART_DIRECTION.md) — visuals and assets.
7. [BACKLOG.md](BACKLOG.md) — actionable work queue.
8. [SESSION_HANDOFF.md](SESSION_HANDOFF.md), if present — temporary session state.
9. Inspect git status/diff and current source, starting with [MainActivity.kt](app/src/main/java/com/bellfamily/bastischool/MainActivity.kt) and the legacy [app.js](app/src/main/assets/app.js) / [index.html](app/src/main/assets/index.html).

Current implementation is hybrid Compose + legacy WebView. The long-term target is fully native Compose with no runtime WebView/HTML/JavaScript. Migrate incrementally; never remove legacy functionality before tested native parity exists.

[PROJECT_BRIEF.md](PROJECT_BRIEF.md) summarises the product and constraints; [README.md](README.md) covers usage/build setup. [BUILD_NOTES.md](BUILD_NOTES.md) records test evidence. [MIGRATION_PHASE_1.md](MIGRATION_PHASE_1.md) and [TECHNICAL_AUDIT.md](TECHNICAL_AUDIT.md) preserve migration history and audit findings, not overriding specifications.
