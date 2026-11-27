# Consolidation and release-readiness plan

Approved by the owner on 2026-11-26 after the project-management and senior-engineering
review of baseline `8e3407f7f22eec7e1622855214c2bf2e58a19990`.

The product is a substantial private beta: broad native curriculum and strong
state/audio/progress foundations, with uneven release acceptance. The next delivery
sequence prioritizes reliability, curriculum clarity and refinement of existing work.
The [master roadmap](../MASTER_PRODUCT_LEARNING_ROADMAP.md) remains the product
authority; specialist specifications retain their existing contracts.

## Delivery checkpoints

| Checkpoint | Work | Completion condition |
| --- | --- | --- |
| 1. Trusted baseline — active | Diagnose hosted Android failures, reconcile implementation/acceptance records, define current regression coverage, prepare one batched device review. Keep first-error evidence for the separate unresolved Compose disposal crash. | Reliable hosted checks at the candidate revision; a concise inventory distinguishes implementation, automated evidence, physical acceptance and blocked checks. |
| 2. Refined daily experience | Clarify Home learning paths, especially the 20 Maths entries; improve first-use instructions and optional demonstrations; extend the established visual foundation into Options and selected learning screens; refine recovery. | Basti can choose, understand, resume and leave representative activities comfortably. Findings from the signed-candidate device review are recorded and prioritized. |
| 3. Sustainable engineering | Extract shell route/lifecycle wiring incrementally; review repeated owner/journal operations; automate recurring validation/evidence steps; budget assets/performance; define progress-history capacity/recovery and local export. | Changes need less repeated wiring/manual administration; measured budgets and recovery behavior are documented and tested. |
| 4. Connected learning | Bring forward a small curated Today’s Adventure using existing confidence, communication and manipulation activities. Select later curriculum additions from observed needs. | A short understandable journey is validated in use. Progress-informed recommendations wait for trustworthy evidence and explicit rules. |

Checkpoint 4 advances only the small curated version relative to its former P3
position. It does not authorize a full story engine, adaptive classifier, dashboard,
or speculative learning framework. A child can stop after any module; no streaks,
penalties, timers or forced completion.

## First work package

1. Preserve both failed hosted Android reports and classify their first failures.
   Run 212 failed Same Amount; run 213 failed a German Wash Hands component case.
   Passing one later run does not establish a fix for an earlier timeout.
2. Identify whether each failure is application behavior, test synchronization,
   environmental readiness or still unresolved. Correct the demonstrated cause;
   preserve assertions, bounded waits and original reports.
3. Validate any correction locally against the failed cases and relevant neighboring
   routes/layouts; run the configured hosted selection when publication is authorized.
4. Publish a source-reconciled [activity and acceptance inventory](ACTIVITY_ACCEPTANCE.md).
   Retain existing physical evidence, distinguish untested modes and avoid inferring
   physical acceptance from emulator tests.
5. Repair stale current summaries in README, the brief and backlog. Keep historical
   evidence intact; link to it rather than appending task history to every spec.
6. Prepare the [batched device review](DEVICE_ACCEPTANCE_BATCH.md), identifying
   owner-managed work and any inspection blockers before scheduling execution.

Current investigation/evidence and exact progress are recorded in
[the checkpoint review](consolidation-2026-11-26/README.md).

## Checkpoint 1 progress — 2026-11-27

- Completed: saved plan/roadmap linkage, source-reconciled 36-entry inventory,
  current-summary corrections, batched device-review preparation, original hosted
  report preservation and exact failure-location triage.
- Verified: original report digests/XML/counts, strict report-inspector positive and
  negative cases, unchanged app/test source identities, documentation links and
  restoration of all 88 original emulator data files/settings followed by shutdown.
- Follow-up complete locally: first-error diagnostics reproduced real missing artwork;
  tracing and a fail-before/pass-after regression identified missing main-thread
  publication confinement. The candidate correction passed the affected 5/5, full
  configured Android 40/40, JVM 751/751, assemblies/lint and seven wrapper checks.
  Original bounds/assertions remain. [Diagnosis and evidence](artwork-ci-diagnostics-2026-11-27/README.md).
- Open: hosted confirmation of the candidate after an authorized push. Earlier
  emulator-system failure and historical Compose disposal remain separate; no claim
  that every previous timeout shares this cause. Review existing screenshot-capture
  timing before accepting future galleries (one passing-run English chooser file
  actually shows Home). No new curriculum work before this reliability gate.
- Pending owner-managed work: signed-candidate physical/audio/accessibility/child batch.

## Existing-product refinement priorities

- **Home/curriculum:** distinguish recognition, construction and application; explain
  the roles of Counting Tray, Make 5, Build 5/10, One More/Less and Number Stories.
  Preserve activity IDs, saved rounds and independent skills when regrouping.
- **Presentation:** reuse the established design tokens and native controls; review
  typography, headings, disabled/focus states, spacing and reachable actions per screen.
- **Audio/support:** review short first-use prompts, optional models, EN/DE wording,
  offline voices, missing-voice recovery and interruption behavior with Basti.
- **Generalisation:** vary objects/layouts within existing concepts and identify
  missing next steps before adding more standalone cards.
- **Language:** finish independent German review of Tell Me and observe prompt
  independence. Keep written-initial matching separate from reviewed phonics/audio.
- **Recovery/accessibility:** cover process termination as well as Activity recreation,
  large text, actual TalkBack/switch use, offline use and same-key upgrades.

## Engineering constraints and measured baseline

- Preserve the pure learning reducers, deterministic frozen plans, stable semantic
  IDs, compatible journals, deduplicated progress, silent restoration and owned audio.
- MainActivity is 1,489 lines and initializes 43 ViewModels at this baseline. Extract
  responsibilities incrementally; these counts alone do not establish a performance bug.
- Progress is a whole-file snapshot bounded to 10,000 events / 16 MiB. Benchmark
  realistic histories and define capacity recovery/retention/export before changing
  storage. Do not silently prune history or merely raise bounds.
- The locally built debug APK is approximately 399 MiB; bundled assets approximately
  365 MiB. Measure installation/cold start/decoding/memory on Samsung and Fire, then
  optimize the largest reviewed assets. Debug size is not a release-size measurement.
- Reuse shared operations only where real consumers justify them; learning mechanics
  and existing wire formats remain explicit. Broad rewrites and dependency upgrades
  require demonstrated need.

## Evidence and acceptance rules

- Record tested revision, artifact identity, scope, result, failures and limits.
- Keep local, hosted, component, production-route and physical evidence distinct.
- A failed hosted test needs a diagnosed cause or an explicitly open investigation;
  retries and larger timeouts alone do not close it.
- Never infer generalised mastery from repeated app answers or nonvisual descriptions.
- Preserve owner-deferred hardware acceptance as pending, while independent automated
  consolidation continues. Do not touch stable device data, uninstall or downgrade.
- Owner approval of this plan authorizes these first steps, not an automatic commit,
  push, distribution or a redesign of all activities.
