# Phase 5C.6A — Habitat Matching Educational Polish

## Starting state

- Branch: `main`.
- Local HEAD and `origin/main`: `e0791f12cf3e075a2a86068960b920b3efd938a7`.
- Working tree: clean at start.
- The seven-question matching activity and its complete Phase 5C.6 evidence were already committed. No Animal Groups or other journal data was changed in this review.

## Question-by-question review

All seven questions retain their canonical IDs, animal IDs, choices, accepted answers, scene/fact rationale, assessment policy and progress context. The full current bank is in [QUESTION_BANK.md](../animals-phase5c6-habitat-matching-review/QUESTION_BANK.md).

| Question | Finding | Before (EN / DE) | After (EN / DE) | Acceptance rationale |
|---|---|---|---|---|
| Dolphin scene | KEEP | “Where did we explore the dolphin?” / “Wo haben wir den Delfin entdeckt?” | Unchanged | Refers to the taught ocean scene; it does not state an exclusive biological habitat. |
| Rabbit scenes | IMPROVE | “Choose a place where we met rabbits.” / “Wähle einen Ort, an dem wir Kaninchen getroffen haben.” | “Where did we see rabbits?” / “Wo haben wir Kaninchen gesehen?” | Removes the unnatural “met rabbits / Kaninchen getroffen” phrasing. Both Woodland and Underground remain accepted, and shared feedback says both places were explored. |
| Mole tunnels | KEEP | “Where does the mole dig tunnels?” / “Wo gräbt der Maulwurf seine Gänge?” | Unchanged | Short, concrete and directly supported by the reviewed tunnel fact. |
| Earthworm scene | IMPROVE | “Where did we explore the earthworm moving through soil?” / “Wo haben wir den Regenwurm entdeckt, der sich durch den Boden bewegt?” | “Where did we see the earthworm?” / “Wo haben wir den Regenwurm gesehen?” | The earlier prompt was long and repeated the soil relation. The concise version is an explicit scene-recall question; the soil fact remains in correct feedback. German correct feedback is now “Ja! Den Regenwurm haben wir unter der Erde entdeckt.” |
| Frog scene | KEEP | “Where did we explore the frog?” / “Wo haben wir den Frosch entdeckt?” | Unchanged | Scene recall avoids saying frogs live only in water. The reviewed fact retains use of water and damp land. |
| Duck scene | KEEP | “Where did we explore the duck?” / “Wo haben wir die Ente entdeckt?” | Unchanged | Refers to the pictured pond lesson, not an exclusive freshwater classification. |
| Rabbit shelter | IMPROVE | “Where can wild rabbits use a warren for shelter?” / “Wo können Wildkaninchen einen Bau als Schutz nutzen?” | “Where can wild rabbits find shelter underground?” / “Wo finden Wildkaninchen unter der Erde Schutz?” | Keeps the wild-rabbit shelter relationship while removing the less familiar English word “warren” and making the German sentence direct. The accepted answer remains Underground; this is not a claim that rabbits live only there. |

Correct feedback changes are limited to Earthworm (short, natural scene/fact wording in both languages) and Rabbit shelter (English “underground burrows” instead of “underground warrens”; German unchanged). Dolphin/Rabbit/Mole/Frog/Duck correct feedback and all retry feedback remain unchanged. Incorrect feedback still says “Not that place. Try another one.” / “Dieser Ort passt nicht. Probiere einen anderen.” It is neutral, keeps the current question and allows retry without losing earlier progress.

## Help and child comprehension

The previous generic Help (“Think about the place we explored.” / “Denk an den Ort, den wir entdeckt haben.”) did not direct attention to the visual choices. It now says:

- EN: “Look at the animal and the pictures. What do you remember?”
- DE: “Schau dir das Tier und die Bilder an. Woran erinnerst du dich?”

This suggests a concrete comparison and recall strategy without identifying an answer. It is one fixed bilingual prompt shared across all seven questions, not an adaptive tutor. Help stays optional, spoken only on request, and retains existing support/attempt semantics. Correct answers still show their feedback and require explicit Continue.

Emulator inspection can establish that the animal, question and choices are displayed together and that Help/feedback fit or scroll; it cannot establish that a six-year-old understands the concepts. The difference between recalling the Rabbit scene and answering its shelter question should be observed with Basti in owner-managed child-use review.

## Science, language and audio

- Rabbit scene recall still accepts both `habitat.woodland` and `habitat.underground`. Its feedback is true for either choice.
- Rabbit shelter remains a shelter relation. No universal single animal-to-habitat mapping was added.
- Frog and Duck questions remain scene-specific; no water-only claim was introduced.
- Mole remains associated with digging tunnels underground; Earthworm remains associated with movement through soil.
- Dolphin remains a recall of the pictured marine scene.
- Prompt, Help, correct feedback and retry text are the exact displayed and spoken `ContentText` for each language. Existing Replay/Help/feedback speech effects and Sound Off policy are unchanged. Actual installed offline voice quality is not asserted.

## Checkpoint and progress compatibility

The activity ID `activity.discover.habitat_matching`, activity revision 1, content version 1.1, skill, question/task IDs, answer sets, question ordering policy, journal namespace, checkpoint schema and progress behavior are unchanged. This is copy-only polish: question meaning and accepted-answer sets did not change. Validation accepts the exact frozen strings from the committed version for the edited prompt/feedback/Help fields, while new sessions use the polished copy. Thus an older in-progress checkpoint can keep its original wording and restore normally; it is not silently rewritten. A JVM regression encodes/restores a checkpoint with the original wording and validates its unchanged task IDs and answer sets.

Existing attempt semantics, Continue idempotency and completion delivery are unchanged. No Animal Groups AGR1 journal/progress, historic four-animal pool or other activity identity is touched. The focused route flow exercises a restored session, then starts a new session through the real Again control to capture the new wording.

## Visual and responsive findings

The previous [Phase 5C.6 gallery](../animals-phase5c6-habitat-matching-review/gallery.html) was inspected. Habitat thumbnails remain distinguishable by their imagery and labels; the woodland preview's distant lake does not override its explicit “Woodland / Wald” label. Existing portrait, 1.5× and 2× layouts remain unchanged.

The former short-landscape capture showed only the prompt, not the animal. The question and animal now sit side by side in landscape. On short landscape (height below 400 dp), the prompt uses the project's smaller scalable information text style; ordinary landscape keeps the larger instruction style. The system font scale still applies. The final 1280×720 capture shows both the full prompt and named animal before scrolling; answer cards, Replay and Help remain scrollable below. This is an emulator viewport, not physical-device evidence. German 2× remains one-column, with a longer scroll.

The new [genuine emulator gallery](gallery.html) contains **25 final-source screenshots**: 11 focused copy/layout captures and 14 additional route, interaction and responsive captures. Portrait captures are 1080×2400; phone landscape is 2400×1080; short landscape is 1280×720; tablet emulator viewport overrides are 1600×2560 and 2560×1600. The images show the changed Rabbit, Earthworm and shelter prompts, Help, Earthworm correct feedback, wrong/retry and Continue, Activity recreation, Options return, German 1.5×/2×, phone landscape, short landscape and tablet viewports. Screenshots were visually inspected individually. No physical tablet claim is made. Prior short-landscape image is shown for before/after comparison from the committed 5C.6 gallery.

Visual review found all four habitat choices remain distinguishable through imagery and labels. The Earthworm is prominent beside its name; selected/correct feedback uses a dark outline and text in addition to color. At German 2× the habitat cards stack vertically and the lower choice remains reachable by scrolling. In phone landscape, the full prompt and animal/name are visible together; choices and Replay/Help are below the first viewport but scrollable. The tablet landscape emulator viewport shows both choices and actions together. These captures establish emulator layout only, not child comprehension or physical-device acceptance.

## Validation

Baseline evidence from 5C.6 was 607/607 JVM and 603/603 complete API 35, and does not validate these edits. Phase 5C.6A fresh runs are recorded here after final implementation:

- Focused matching JVM content/host tests: **11/11 passed**, 0 failures/errors/skips.
- Fresh full JVM on final source: **609/609 passed**, 0 failures/errors/skips. XML suites are retained under [`jvm/final/`](jvm/final/).
- Focused API 35 route test on final source: **1/1 passed** on disposable `emulator-5554`, including Help/EN-DE, Rabbit/Earthworm new-copy states, Activity recreation flow, and short-landscape prompt plus animal visibility. XML is under [`api35/focused-final/`](api35/focused-final/).
- Complete unfiltered API 35 run #1: **603 tests executed, 1 failed, 0 errors, 0 skipped** in 39m 15s. The sole failure was unrelated `SeasonsProgressionRouteTest.newModesKeepExactStateAcrossOptionsLanguageRecreationAndCompleteOrdering`, a byte-array checkpoint comparison during Activity recreation (encoded length differed by one byte). Full XML, HTML report and failing test log are retained under [`api35/full-run/`](api35/full-run/).
- The Seasons test passed alone **1/1**. Complete unfiltered API 35 rerun #2: **603/603 passed**, 0 failures/errors/skips; Gradle reported `BUILD SUCCESSFUL`. XML and HTML are under [`api35/full-run-rerun/`](api35/full-run-rerun/). The initial failure remains in the record.
- `assembleDebug`, `assembleDebugAndroidTest`, and `lintDebug` passed on final source. Lint reported **0 errors and 5 warnings** (two manifest attributes are newer than minSdk, two existing vector assets exceed the suggested size, and the existing WebView enables JavaScript); no new warning was attributed to this change. Lint reports are in [`jvm/final/`](jvm/final/).
- `git diff --check`: passed after the final documentation and evidence updates.

Early focused UI attempts failed on test assertions: the test first resumed an older checkpoint (whose copy must remain frozen), then Compose substring assertions did not match despite the exact expected text appearing in node semantics. The flow now completes the restored session, uses Again for the new-copy captures, and asserts exact text. No product failure was observed in those attempts. The one-off Seasons checkpoint assertion is outside Habitat Matching and passed in isolation and in the final full rerun.

## Known limitations

- No real child comprehension or preference claim is made.
- Physical-device ergonomics, actual TalkBack/switch access, and installed offline voice quality remain owner-managed.
- Activity recreation is not OS process-death acceptance.
- Real child comprehension has not been tested. Physical-device ergonomics, actual TalkBack/switch access, and installed offline voice quality remain owner-managed.
- Activity recreation is not OS process-death acceptance.
- The randomized bank is not shown in every language/font/view combination; the gallery gives representative final-source coverage, while the prior 5C.6 matrix remains the broader baseline.

## Changed-file inventory

- `MASTER_PRODUCT_LEARNING_ROADMAP.md`
- `SESSION_HANDOFF.md`
- `TESTING_QA_SPEC.md`
- `UX_NAVIGATION_SPEC.md`
- `app/src/main/java/com/bellfamily/bastischool/learning/animals/HabitatMatchingContent.kt`
- `app/src/main/java/com/bellfamily/bastischool/ui/habitats/HabitatMatchingScreen.kt`
- `app/src/test/java/com/bellfamily/bastischool/learning/animals/HabitatMatchingContentTest.kt`
- `app/src/androidTest/java/com/bellfamily/bastischool/ui/habitats/HabitatMatchingRouteTest.kt`
- `docs/animals-phase5c6-habitat-matching-review/QUESTION_BANK.md`
- `docs/animals-phase5c6a-matching-polish-review/REVIEW.md`
- `docs/animals-phase5c6a-matching-polish-review/gallery.html`
- Focused screenshots: `screenshots/earthworm-correct-de.png`, `earthworm-scene-help-de.png`, `earthworm-scene-question-de.png`, `earthworm-scene-question-en.png`, `rabbit-scenes-help-de.png`, `rabbit-scenes-question-de.png`, `rabbit-scenes-question-en.png`, `rabbit-shelter-help-de.png`, `rabbit-shelter-question-de.png`, `rabbit-shelter-question-en.png`, `short-landscape-after.png`.
- Final-flow screenshots: `screenshots/final-flow/01-animals-category-en.png`, `02-first-question-en.png`, `03-first-question-de.png`, `04-wrong-retry-feedback-de.png`, `05-correct-feedback-de.png`, `06-continue-2-of-7-de.png`, `07-activity-recreated-session-resumed.png`, `08-german-1_5x.png`, `09-german-2x.png`, `10-completion-and-progress-result.png`, `11-options-language-return-en.png`, `12-phone-landscape.png`, `14-tablet-portrait-emulator-viewport.png`, `15-tablet-landscape-emulator-viewport.png`.
- `jvm/final/`: 111 full-suite JVM XML reports, JUnit binary result files, and final lint text/HTML reports.
- `api35/focused-final/`: focused matching XML and UTP result binary.
- `api35/full-run/`: first full-suite XML and UTP result, full HTML report, and the Seasons failure logcat.
- `api35/full-run-rerun/`: clean complete-suite XML and UTP result plus full HTML report.
- `api35/seasons-isolated/`: isolated passing Seasons XML and UTP result.

No commit or push was made for Phase 5C.6A.

## Recommendation

**READY TO COMMIT** for owner review. The initial full API run's single
unrelated Seasons recreation assertion is documented; its isolated rerun and
the final complete unfiltered 603-test run passed. No Habitat Matching
blocker remains. Physical-device use, real-child comprehension, TalkBack,
switch access and installed offline voice quality remain owner-managed and
were not inferred from emulator tests.
