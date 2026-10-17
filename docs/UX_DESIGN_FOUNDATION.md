# UX Refresh Phase 1 — opt-in visual foundation

Baseline: `dac2fbb1ca68fb80691d2e22cb908cfa0b634919`. The owner authorised Phase 1
finalisation as the prerequisite to the isolated Phase 2 Home prototype. This
foundation is not a production Home redesign or curriculum regrouping.

## Boundary

`ui/design/BastiDesignTokens.kt` holds the proposed discovery palette, explicit
foregrounds, typography, spacing and shapes. `BastiDesignTheme` is an ordinary
Material 3 theme wrapper; it is **not applied by MainActivity**. All existing
screens, shared action/choice/support/completion implementations, activity-specific
colours, routes, content, audio and persistence remain unchanged. Phase 0's 32sp /
40sp greeting and Home-only header reflow remain intact.

Six `BastiCategoryTreatment` values are presentation swatches, not navigation,
curriculum or progress identities. No dynamic colour or automatic dark-mode palette
is introduced. This bounded light palette requires separate review before rollout.

## Tokens and reuse

The proposed colours match the owner brief: cream `#FBF8F0`, white `#FFFFFF`, ink
`#203747`, primary green `#2E6B50`, secondary container `#DDEDE2`; category containers
`#E3F0FC`, `#EEE9F8`, `#E7F2DC`, `#DFF1E9`, `#FFF0D0`, `#F9E6DC`.
Containers use explicit ink foregrounds; primary uses white. Outline `#65756E`
distinguishes interactive boundaries. JVM checks cover normal-text contrast ≥4.5:1
and outline/selection contrast ≥3:1. Material's disabled-action styling is retained;
disabled controls are not included in the enabled text-pair contrast claim.
Measured baseline pairs: ink on the listed neutral/category surfaces ≥10.17:1,
white on primary 6.30:1, outline on those surfaces ≥3.99:1.

| Typography role | Size / line height (sp) |
|---|---|
| Home heading | 32 / 40, bold |
| Screen/section heading | 28 / 36, bold |
| Learning question | 24 / 32, semibold |
| Answer label | 20 / 28, semibold |
| Instruction and support | 20 / 28 |
| Parent information | 16 / 24 |
| Action label | 18 / 26, semibold |

All use the system font and system scaling. Text is not capped or ellipsized.
Spacing roles are 8/16/24dp; page padding 24dp; minimum target 56dp. Controls retain
16dp corners; new category cards use 24dp corners. These are opt-in values, not a
mechanical replacement of semantic colours or existing screen geometry.

`BastiSectionHeading` adds heading semantics to caller-owned text.
`BastiCategoryCard` is one Material clickable Surface with a visible label,
decorative Fit illustration, caller-owned callback/enabled/selected values, and
selected semantics. A checkmark plus thicker outline avoids colour-only selection.
It owns no routing, speech, learning state or progress. Labels can grow vertically;
only decorative artwork has a bounded height.

There is **no second action-button API**. Existing `NativeActionButton` roles take
their colours/label style from the opt-in Material theme. Existing
`NativeSupportMessage` remains the support component; its larger-text setting and
semantics are unchanged. No shared completion or answer behaviour is refactored.

## Inspecting the isolated showcase

The showcase Activity and copy live exclusively in `src/debug`. There is no launcher
entry, Home link or release Activity. It is exported in debug builds solely for
explicit developer launch; it does not read or change app preferences or sessions.

After installing the debug APK:

```sh
adb shell am start -n com.bellfamily.bastischool/.ui.design.DesignShowcaseActivity --es language de --es page categories
```

Languages: `en`, `de`. Pages: `categories`, `type`, `actions`, `all` (default).
Set system font scale/orientation normally; the demo follows Android configuration.
Compose `@Preview` examples also cover English 1×/1.5× and German 2× portrait/landscape.
Existing six canonical animal illustrations are sampled only for the showcase,
explicitly labelled as sample mascots, **not approved category artwork**.
Demo actions increment a local counter and never navigate or speak. Selection is
local saveable demo state. The gallery is not a proposal to make unfinished content
playable. No bitmap, font, dependency or learning asset was added or modified.

## Validation and next slice

Exact run results, screenshots and limitations are recorded in SESSION_HANDOFF.md.
Automated coverage checks palette/contrast, typography, theme isolation, selection,
disabled activation, touch, keyboard, and EN/DE × 1×/1.5×/2× × portrait/short landscape.
Existing Home/action/choice/support/completion tests remain regression gates.
Physical-device and actual TalkBack/switch acceptance remain separate.

## Scoped file inventory

All paths are relative to the repository; no existing production screen is edited.

| File | Change |
|---|---|
| `app/src/main/java/com/bellfamily/bastischool/ui/design/BastiDesignTokens.kt` | New palette, typography, spacing and shapes |
| `app/src/main/java/com/bellfamily/bastischool/ui/design/BastiDesignTheme.kt` | New explicitly opt-in Material theme |
| `app/src/main/java/com/bellfamily/bastischool/ui/design/BastiDiscoveryComponents.kt` | New section heading and illustrated category card |
| `app/src/debug/AndroidManifest.xml` | Debug-only explicit showcase entry |
| `app/src/debug/java/com/bellfamily/bastischool/ui/design/DesignShowcaseActivity.kt` | Isolated developer Activity |
| `app/src/debug/java/com/bellfamily/bastischool/ui/design/BastiDesignShowcase.kt` | Bilingual component samples and Compose previews |
| `app/src/test/java/com/bellfamily/bastischool/ui/design/BastiDesignTokensTest.kt` | Four palette/contrast/typography tests |
| `app/src/androidTest/java/com/bellfamily/bastischool/ui/design/BastiDesignFoundationTest.kt` | Twelve layout cases plus theme/disabled isolation test |
| `NATIVE_ARCHITECTURE_SPEC.md` | Foundation boundary and cross-reference |
| `SESSION_HANDOFF.md` | Current validation evidence and review status |
| `docs/UX_DESIGN_FOUNDATION.md` | Token/component contract, inspection instructions and file inventory |

## Recommended next slice

Next slice, after Phase 1 is committed/pushed and required CI passes: an isolated
illustrated Home prototype using these tokens and the existing 20-entry catalogue.
Confirm the complete Phase 2 brief before implementation; the received message
ends at “Learning categories — Display”. Do not replace production Home or migrate
learning-screen colours.
