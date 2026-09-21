# Agent Instructions — Basti’s Learning Adventure

## Read first
Before making product, architecture or learning-content decisions, read:
1. MASTER_PRODUCT_LEARNING_ROADMAP.md
2. PROGRESS_TRACKER_SPEC.md
3. GAME_DESIGN_SPEC.md
4. VOICE_AUDIO_SPEC.md
5. ART_DIRECTION.md
6. PROJECT_BRIEF.md
7. BACKLOG.md
8. SESSION_HANDOFF.md if present

The master roadmap and domain specs above are authoritative for long-term direction. Source code determines current implementation reality; older briefs, plans, audits and handoffs must not override the master specs.

## Product principles
- audio-first
- calm and pressure-free
- short predictable sessions
- custom images instead of emoji in final UI
- native 2D/2.5D, not true 3D
- fully offline
- no Google Play Services dependency
- long-term target: native Compose with no runtime WebView/HTML/JavaScript dependency
- migrate incrementally; never delete working legacy functionality before native feature parity and tests

## Learning principles
- speaking/listening are as important as letters and maths
- encourage communication, not just multiple-choice answering
- use open-ended prompts where appropriate
- do not make automatic speech scoring a dependency
- generalise concepts across multiple contexts
- explain mistakes constructively
- teach help-seeking and self-advocacy
- use Basti’s interests: dinosaurs, dragons, snakes, Komodo dragons, crocodiles, sharks, whales

## Progress
Design all new activities so they can report skill-level progress into the shared Progress Tracker.
Avoid isolated activity-specific progress systems.

## Shared data
Prefer native shared models for:
- animals
- vocabulary
- skills
- questions
- progress
- learning content

Avoid duplicating facts/words across unrelated arrays/files.

## Development behaviour
- inspect git status/diff, current source and handoff notes before editing
- preserve interrupted work
- work in checkpoints
- test before broadening scope
- keep repo in a known-good state
- update docs when architecture changes
- leave a concise handoff when work is incomplete
