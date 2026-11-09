# Shared verb content

`verbs.json` is the authored source for all 53 bilingual actions, 42 animal labels,
reviewed distractors, explanations, stage labels and 39 motion atlases. Lesson order
is frozen for legacy checkpoint compatibility; stable semantic IDs identify native
lessons. The native catalogue supplies the same explanation/example to Explore and
the assessed Animal Actions session. Existing canonical animal IDs are retained,
including the explicit `croc` → `animal.crocodile` adapter.

After an intentional content edit, run:

```sh
python3 tools/generate_verbs.py
node tools/verb_motion_parity.cjs
```

The generator validates references and frame bounds, then writes typed Kotlin
records and the retained web catalogue adapter. Native Android never loads or
executes JavaScript for either migrated route. CI runs the generator with `--check`.
`verb-motion-parity.tsv` records thirteen deterministic samples from the retained,
reviewed web player for each lesson. The JVM test compares native physical frames,
positions, baseline, size and direction with all 689 samples. Regenerate the fixture
only for an intentional reviewed motion change; do not change it to mask a native
port defect.

The legacy web implementation stays available for old saved WEB bundles and Mixed
Adventure. Its existing lesson indices, current question, answer locks and score
remain unchanged. There is no synthetic import of historical web scores into the
native shared Progress Tracker.
