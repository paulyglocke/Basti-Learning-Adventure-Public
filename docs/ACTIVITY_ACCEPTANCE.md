# Current activity and acceptance inventory

Reconciled on 2026-11-26 against source baseline `8e3407f7f22eec7e1622855214c2bf2e58a19990`.
This is the current roll-up; the [QA specification](../TESTING_QA_SPEC.md) and
linked reviews retain detailed historical evidence and acceptance contracts.

## Current implementation

36 live Home entries: 33 native and three retained web. Maths contains 20 entries.
These are Home entry counts, not screen, mode, skill or curriculum-unit counts.
Choosers such as Days & Seasons, Colours and Follow Instructions contain several
native activities. Three future-game placeholders are filtered from the live catalogue.

Sources: [Home cards](../app/src/main/java/com/bellfamily/bastischool/MainActivity.kt),
[category/dispatch map](../app/src/main/java/com/bellfamily/bastischool/ui/home/HomeCatalogue.kt),
[independent route expectations](../app/src/androidTest/java/com/bellfamily/bastischool/HomeRouteTest.kt).
The table was reconciled against all three; no content or identity change is implied.

| Stable Home ID | English / German entry | Browse category | Runtime / initial destination |
| --- | --- | --- | --- |
| `tellme` | Tell Me! / Erzähl mal! | Words & Talking | Native / `TELL_ME` |
| `vocabulary` | Vocabulary Booster / Wortschatz | Words & Talking | Native / `VOCABULARY_CHOOSER` |
| `verbExplorer` | Learn Verbs / Verben lernen | Words & Talking | Native / `VERB_LIBRARY` |
| `letters` | Animal First Letters / Tier-Anfangsbuchstaben | Words & Talking | Native / `LETTERS` |
| `count` | Numbers / Zahlen | Numbers & Maths | Web / `WEB` |
| `time` | Days & Seasons / Tage & Jahreszeiten | Time & Seasons | Native / `DAYS_SEASONS` |
| `verbs` | Animal Actions / Tier-Aktionen | Animals & Nature | Native / `VERB_QUIZ` |
| `subitising` | How many? / Wie viele? | Numbers & Maths | Native / `SUBITISING` |
| `quantityMatch` | Numbers & Groups / Zahlen & Mengen | Numbers & Maths | Native / `QUANTITY_MATCH` |
| `compareQuantity` | More or Fewer / Mehr oder weniger | Numbers & Maths | Native / `COMPARE_QUANTITY` |
| `numberOrder` | Number Order / Zahlenfolge | Numbers & Maths | Native / `NUMBER_ORDER` |
| `clock` | Clock & Time / Uhr & Zeit | Time & Seasons | Native / `CLOCK` |
| `addTogether` | Add Together / Zusammenzählen | Numbers & Maths | Native / `ADD_TOGETHER` |
| `addChallenge` | Add Bigger Numbers / Größere Zahlen addieren | Numbers & Maths | Native / `ADD_CHALLENGE` |
| `subtraction` | Take Away / Wegnehmen | Numbers & Maths | Native / `SUBTRACTION` |
| `mixedAddition` | Mixed Addition / Gemischte Plusaufgaben | Numbers & Maths | Native / `MIXED_ADDITION` |
| `buildFive` | Build 5 / Fünf legen | Numbers & Maths | Native / `BUILD_FIVE` |
| `buildTen` | Build 10 / Zehn legen | Numbers & Maths | Native / `BUILD_TEN` |
| `makeFive` | Make 5 / Fünf ergänzen | Numbers & Maths | Native / `MAKE_FIVE` |
| `countingTray` | Build a Dinosaur Group / Dinosauriergruppe legen | Numbers & Maths | Native / `COUNTING_TRAY` |
| `math` | Easy Maths / Einfache Mathe | Numbers & Maths | Web / `WEB` |
| `positions` | Prepositions / Präpositionen | Words & Talking | Native / `PREPOSITIONS` |
| `mixed` | Mixed Adventure / Gemischtes Abenteuer | All Activities only | Web / `WEB` |
| `memoryPairs` | Memory Pairs / Paare merken | Animals & Nature | Native / `MEMORY_CHOOSER` |
| `washHands` | Wash Hands / Hände waschen | Everyday Skills | Native / `WASH_HANDS` |
| `exploreHabitats` | Explore Habitats / Lebensräume entdecken | Animals & Nature | Native / `HABITATS_CHOOSER` |
| `matchHabitats` | Match Animals to Habitats / Tiere und Lebensräume zuordnen | Animals & Nature | Native / `HABITAT_MATCH` |
| `animalGroups` | Animal Groups / Tiere zuordnen | Animals & Nature | Native / `ANIMAL_GROUPS` |
| `colourSort` | Colour Sort / Farben sortieren | Colours | Native / `COLOURS_CHOOSER` |
| `follow` | Follow the Instructions / Anweisungen folgen | Everyday Skills | Native / `FOLLOW_LEVELS` |
| `patternBuilder` | Pattern Builder / Muster bauen | Numbers & Maths | Native / `PATTERN_CHOOSER` |
| `quantityChange` | One More / One Less / Eins mehr / eins weniger | Numbers & Maths | Native / `QUANTITY_CHANGE_CHOOSER` |
| `numberStories` | Number Stories / Rechengeschichten | Numbers & Maths | Native / `NUMBER_STORIES_CHOOSER` |
| `conservation` | Same Amount / Gleich viele | Numbers & Maths | Native / `CONSERVATION_CHOOSER` |
| `lengthComparison` | Longer / Shorter / Länger / kürzer | Numbers & Maths | Native / `LENGTH_COMPARE` |
| `lengthOrder` | Order by Length / Nach Länge ordnen | Numbers & Maths | Native / `LENGTH_ORDER` |

## Verification layers at this baseline

| Layer | Recorded evidence | Limit / remaining work |
| --- | --- | --- |
| Full JVM, app/test APKs, lint | 751/751; lint 0 errors/7 warnings/2 information in [Order by Length review](length-order-2026-11-25/README.md) | Domain/storage/content coverage; not physical or full-screen acceptance. |
| All Home entry dispatch/recreation | EN36 + DE36, 72 actual framework recreations in the same review | Entry navigation and declared legacy question restoration; not every mode or full round. |
| Scoped latest phone/tablet | New route/owner/layout 6, comparison/Home/startup 9; tablet layouts 4 in the same review | Component and production captures are separate; tablet repeats four phone cases. |
| Hosted run 213 / 8e3407f | Build/browser passed; Android 35/36, Wash Hands German portrait artwork wait failed | Hosted Android gate OPEN. Same Amount passed this run; earlier failure remains under diagnosis. [Investigation](consolidation-2026-11-26/README.md). |
| Hosted run 212 / 8703c4e | Build/browser passed; Android 33/34, Same Amount final chooser artwork wait failed | Historical failure is not closed by a later pass. |
| S24 physical | Prior selected Seasons/Wilma flows, celebration-art review and same-key upgrade are recorded in [QA](../TESTING_QA_SPEC.md#stable-apk-upgrade-acceptance-2026-09-23) | Preserve those passes; current-baseline broader modes/audio/accessibility remain pending. |
| Fire Max physical | Acceptance remains owner-managed/pending | Emulator tablet layouts and dependency inspection do not establish Fire compatibility. |
| Child / actual TalkBack/switch / offline audible voices | Partial historical owner observations only; no blanket current-baseline pass | Review per activity/mode/language in [the device batch](DEVICE_ACCEPTANCE_BATCH.md). |

## Deeper family evidence and refinements

Follow-up candidate (2026-11-27): [artwork publication and diagnostics](artwork-ci-diagnostics-2026-11-27/README.md)
passed JVM 751/751 and the full configured Android selection 40/40 locally, including
Same Amount, four Wash Hands layouts, lifecycle and neighboring route/owner cases.
This supplements the baseline table; it does not replace hosted or physical evidence.
Hosted confirmation remains pending publication.

These links identify scoped/historical reviews, not a claim every current mode has
just been rerun. New current-baseline hardware results must record exact mode,
revision, device/OS, language, voice and artifact identity.

| Family | Existing evidence | Main acceptance/refinement remaining |
| --- | --- | --- |
| Tell Me | [Pilot validation](tellme-variety/PILOT_VALIDATION.md) | Independent German review, prompt independence and child/physical review; open-ended speech stays ungraded. |
| Vocabulary / written initial letters | [Sweep review](app-sweep-implementation-2026-11-05/REVIEW.md), [Vocabulary presentation](vocabulary-phase4d-review/REVIEW.md) | Current category/mode device matrix; written initials are not phonics evidence. |
| Verbs / Animal Actions / Build 5/10 | [Native combined review](native-verbs-bonds-2026-11-07/README.md) | Audible offline EN/DE and action clarity/physical acceptance; preserve 53 lessons and old WEB compatibility. |
| Earlier number/quantity choice and tray activities | [QA ledger](../TESTING_QA_SPEC.md), [sweep](app-sweep-implementation-2026-11-05/REVIEW.md) | Explicit recognition→construction progression, current deep-mode/hardware matrix. |
| Memory / Patterns / One More-Less | [Memory](memory-pairs-2026-11-09/README.md), [Patterns](pattern-builder-2026-11-10/README.md), [Quantity change](one-more-less-2026-11-11/README.md) | Physical comprehension/accessibility and broader-context use; starter modes already implemented. |
| Number Stories / Same Amount / Length | [Stories](number-stories-2026-11-16/README.md), [Same Amount](same-amount-2026-11-20/README.md), [comparison](length-comparison-2026-11-23/README.md), [ordering](length-order-2026-11-25/README.md) | Hosted timeout diagnosis, physical/audio acceptance and later generalisation. |
| Prepositions | [Scene review](prepositions-art/README.md), [QA](../TESTING_QA_SPEC.md) | Current revision 4/content 1.4, exact older recovery; device distinction/touch/audio acceptance. |
| Days / Seasons / Months / Clock | [QA](../TESTING_QA_SPEC.md), [Months](months-phase4a-review/REVIEW.md), [Wilma](wilma-phase4c-review/REVIEW.md) | Preserve named older S24 passes; newer modes, wheel, calendar transfer and Fire/audio acceptance remain separate. |
| Colours / Rainbow | [Colour presentation](colours-phase5a1-review/REVIEW.md), [Rainbow](rainbow-phase5br3b-review/REVIEW.md) | Current mode/large-text/colour recognition device matrix; avoid colour-only cues. |
| Habitats / Animal Groups | [Habitat matching](animals-phase5c6a-matching-polish-review/REVIEW.md), [QA](../TESTING_QA_SPEC.md) | Current factual/art clarity, independent navigation and device acceptance. |
| Follow / School Skills / Wash Hands | [QA](../TESTING_QA_SPEC.md), [lifecycle investigation](compose-disposal-2026-11-24/README.md) | Hosted artwork wait, child action flow/tap targets, offline voices and hardware review; historical disposal cause OPEN. |
| Retained Numbers / Easy Maths / Mixed | [Home verification](home-legacy-timeout-2026-11-24/README.md) | Keep compatibility/recovery/browser checks until tested native parity and physical retirement gates pass. |

## Status rules

Implementation, local automated evidence, hosted results, source-art review and
physical acceptance are independent columns. Pending owner-managed hardware work
is not a failed automated test. Blocked inspection is not a pass from UI alone.
No activity counts or successful app checks establish generalised mastery.

The [consolidation plan](CONSOLIDATION_PLAN.md) owns the next delivery sequence.
