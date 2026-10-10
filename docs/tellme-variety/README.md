# Tell Me content variety — complete draft

Based on main `8c48de18cec5183dfb136414d7bddf502b087c14`; owner authorized committing/pushing before Android validation.
The owner's request to improve the remaining 79 scenes is now included: all 81
existing pictures have two explicit EN/DE prompt/support bundles. This replaces
the previous two-scene scope. See [REVIEW.md](REVIEW.md) for every prompt, theme
counts, unchanged artwork assumptions and review limits; [CONTENT_CHANGES.json](CONTENT_CHANGES.json)
records every starter, word, model, child example, parent note and picture-evidence link.

## Behavior

Each actual scene entry selects the next Activity-lifetime slot: A, B, A.
The chosen slot survives Help/model reveal, disclosure, language/Options, recreation
and background return. Unvisited pictures consume nothing. Home resets conversation
but retains the bounded per-scene cursors until ViewModel disposal; process restart
resets both. The nine-category/nine-picture order and optional reward remain.
One “Next picture” / “Nächstes Bild” press advances immediately, with no required
model phase. Help reveals the selected sentence model along with its starter and words. All 81 scenes now offer bounded Help/model support in both slots; the grown-up
example and guidance also match that slot. No random difficulty, database, speech,
scores, answer capture or artwork is added.

## Clock feedback included

The shared Explore/Practice clock now shows minute numbers 0–59, with larger bold
multiples of five. Hours/digital hours/short-hand legend and line use dinosaur green;
minute equivalents use dragon red. Colours match the darker outlines in the existing
vector files for readable contrast. Practice digital time remains hidden until solved.
This is presentation only; angles, dragging, audio, checking and persistence are unchanged.
Rendered minute-ring readability, especially on narrow screens, needs visual QA.

## Evidence

26 isolated Python metadata/authoring checks passed, with one actual-PNG review/hash
check explicitly skipped. Generated Kotlin and the original English reference
fingerprint pass their offline fixture checks. Fixtures are outside production paths.
All 162 prompt strings are distinct in EN and DE. That is an editorial measure,
not proof of learning quality, native-speaker acceptance or visual correctness.

Android validation was attempted but blocked before Gradle could start: the wrapper
cannot obtain Gradle 8.9; the latest attempt failed at socket creation with
“Operation not permitted”. Earlier proxy checks also returned connection refused. JVM/Android/lint/
instrumentation therefore remain unrun. The text-source snapshot is not
a full checkout, has no production PNGs, and the environment cannot obtain SDK/Gradle
through its failed proxy. No APK exists for this draft. German and current-image
review remain pending, including the owner-replacement Park .09.

## Resume in a complete checkout with SDK and working network

1. Check main is still the pinned revision; review/rebase if it changed.
2. Apply the complete saved patch; retain all original artwork.
3. Run `python3 -m unittest discover -s scripts -p 'test_*.py'` and
   `python3 scripts/generate_scene_descriptions.py --check` against real assets.
4. Run `./gradlew testDebugUnitTest assembleDebug lintDebug compileDebugAndroidTestKotlin`.
5. Run Tell Me instrumentation with actual images, EN/DE, large text, repeat visits,
   one-press Next, optional Help/model, Home/Again, Options/language, recreation and background return.
   Run Clock screen/practice/route tests and visually review minute labels at all
   representative hand positions in narrow portrait, short landscape and large text.
6. Review German and question/support meaning against each current picture.
7. Once required checks pass, commit and push under the owner's existing authorization.

Owner subsequently authorized publication before Android checks. This remains
unvalidated on Android; do not describe it as a successful build or validated
device experience until CI and visual review establish that evidence.

## Publication attempt — 2026-10-10

The owner authorized pushing before Android validation. GitHub rejected both
blob/tree creation with HTTP 403 “Resource not accessible by integration”.
Nothing was committed or pushed, and no signed workflow was started. CLI
authentication also failed. Restore repository write and Actions dispatch access;
GitHub is already installed. The existing workflow signs only a manual dispatch
on main with `distribution=true`, after its build/browser checks pass.
