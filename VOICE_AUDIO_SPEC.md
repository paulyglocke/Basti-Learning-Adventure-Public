# VOICE_AUDIO_SPEC.md

# Basti’s Learning Adventure — Voice & Audio Specification

## 1. Purpose

Basti’s Learning Adventure is an audio-first app. The spoken voice is therefore a core part of the product experience, not a minor accessibility feature.

The voice should sound:

- warm
- calm
- friendly
- encouraging
- natural
- clear for a young child
- consistent across devices
- appropriate in both English and German

This document defines:

1. the immediate fixes required for the current Android TTS implementation,
2. the long-term speech architecture,
3. the plan for evaluating higher-quality offline neural voices,
4. content and testing rules for all spoken text.

Read this together with:

- `AGENTS.md`
- `MASTER_PRODUCT_LEARNING_ROADMAP.md`
- `PROGRESS_TRACKER_SPEC.md`
- `GAME_DESIGN_SPEC.md`
- `ART_DIRECTION.md`

---

# 2. Original problem and current boundary

The original legacy app sent visible feedback strings directly to Android TextToSpeech. The P0 cleanup now gives all eight praise entries explicit `displayText` / `speechText` fields and sanitizes every legacy speech request in `app.js` → `speak()` before either `Android.speak()` or browser speech synthesis. Decoration-only requests are skipped.

This is the single current speech-safety boundary. Kotlin forwards its result unchanged; no duplicate Kotlin sanitizer was added because all current narration enters through this JavaScript path. Future native speech callers must establish the equivalent shared native boundary before bypassing it. Semantic icons still require authored speech (for example Crocodile/Krokodil), not Unicode names.

The legacy P0 reliability pass implements the following policy. Manual means an explicit Replay, option/object speaker or lesson Listen action; it never overrides Sound Off.

| Mode | Automatic speech | Manual speech | Balloon/completion tones |
| --- | --- | --- | --- |
| Read Everything | Questions, instructions, tutorials, lessons and relevant feedback/completion | Enabled | Enabled |
| Questions / Instructions Only | Questions, instructions, instructional tutorials and lesson explanations | Enabled | Enabled |
| Sound Off | None | None, even while controls remain visible | None |

Questions Only does not automatically narrate praise, correct feedback, generic wrong-answer encouragement or the completion summary. Future necessary corrective *instruction* must be explicitly classified as instruction; generic feedback must not be relabelled to bypass this rule. Completion Replay reads the current summary. Existing display/speech praise pairs and `sanitizeForSpeech()` remain the final legacy speech-safety boundary.

### Current system-TTS lifecycle

`LegacySpeech` is a small main-thread-confined helper for the existing bridge, not the future shared native engine architecture. It has INITIALISING / READY / FAILED states. Initialisation retains only the latest relevant request; replacement, navigation, backgrounding and disposal invalidate it. Failure rejects pending/future requests while visuals and navigation remain usable. Options provides bilingual guidance to check installed offline EN/DE voices and restart after changing device voice data. There is no network or alternate-language fallback.

Offline voices are enumerated once per engine initialization and cached by language, preferring en-GB / de-DE and otherwise another installed offline voice in the requested language, with stable name ordering. Network-required and not-installed voices are excluded. Every utterance explicitly sets its cached matching voice and only speaks when selection succeeds. Missing languages produce silence/failure, never the previous language's voice. Restart refreshes the cache. Browser development fallback likewise requires a local voice in the requested language. This filtering follows the [Android engine contract](https://developer.android.com/reference/android/speech/tts/TextToSpeech.Engine); real Samsung/Fire engine behavior still requires physical checks.

A cancellable timer and request IDs own JavaScript narration. Every new speech action, including policy-muted feedback, cancels obsolete delayed speech. New questions, completion, language changes, Home, Back, lesson navigation and backgrounding stop pending/active narration. Native playback uses QUEUE_FLUSH; repeated controls replace rather than queue. Engine completion/error callbacks are matched to the current native request and its originating WebView. Leaving the native learning surface destroys its WebView; returning from native Options currently starts from Home (full destination/session recovery remains wider P0 work). Native Back delegates to the existing lesson-aware web Back path.

In the legacy generated quiz, changing language explicitly regenerates the current question without an attempt or score change; an already answered question remains locked. Quiz-linked lessons refresh their return question too. Completion retains its summary and can be replayed. This is a bounded legacy behavior, not the future semantic task-identity migration.

### Tutorial completion and reset

Tutorials are persisted only after successful completion of their entire current utterance (tutorial plus instruction/lesson where combined). Requests, Sound Off, engine failure, interruption, Replay replacement and stale callbacks do not mark a tutorial heard. Keys are `bastiTutorial_v2_<language>_<activity>`; legacy unversioned marks are deliberately not trusted, so each language receives one successful new presentation. Increment the version when tutorial meaning changes. Reset clears all tutorial versions/languages and invalidates outstanding callbacks. Native Options persists a reset epoch even before any WebView exists; each WebView applies a new epoch once, before starting an activity.

Balloon tones share one lazily created AudioContext per web surface. Off/background cancellation stops live oscillators and suspends it; enabled interactions may resume it. No context is created while Off, and celebration remains usable silently.

The following examples describe the original bug and the continuing content rule.

Some visible feedback strings contain emoji, for example:

```text
Great! 🌟
Yes! 🎉
Correct! ⭐
Well done! 🐉
```

and German:

```text
Super! 🌟
Ja! 🎉
Richtig! ⭐
Sehr gut! 🐉
```

Some Android TTS voices verbalise emoji names.

For example, the visible string:

```text
Ja! 🎉
```

may be spoken as something like:

```text
Ja! Konfetti-Bombe.
```

This is not acceptable for production audio.

The underlying rule is:

> **Visible text and spoken text must not automatically be assumed to be identical.**

---

# 3. Immediate P0 — never speak emoji accidentally

## Required behavior

Before any text reaches a speech engine, it must be converted to deliberate speech-safe text.

At minimum:

- emoji must not be spoken unless explicitly intended,
- decorative symbols must not be spoken,
- visual star/reward glyphs must not be spoken,
- UI icons must not leak into narration,
- accessibility labels and TTS text must be deliberate rather than derived blindly from decorated UI strings.

Example:

```text
displayText = "Ja! 🎉"
speechText = "Ja!"
```

English:

```text
displayText = "Great! 🌟"
speechText = "Great!"
```

---

# 4. Preferred implementation — structured display and speech content

Do not rely only on a broad regex cleanup forever.

Preferred content model:

```kotlin
data class SpokenText(
    val displayText: String,
    val speechText: String
)
```

or equivalent bilingual content:

```kotlin
data class LocalizedSpeech(
    val displayEn: String,
    val speechEn: String,
    val displayDe: String,
    val speechDe: String
)
```

For content with no visual decoration, display and speech may share the same plain string.

For decorated content, explicitly separate them.

Example:

```text
displayEn: "Yes! 🎉"
speechEn:  "Yes!"

displayDe: "Ja! 🎉"
speechDe:  "Ja!"
```

---

# 5. Defensive speech sanitiser

Even with structured content, the final speech boundary should contain a defensive sanitisation step.

Purpose:

- prevent accidental emoji leakage,
- strip decorative symbols,
- normalise whitespace,
- protect future dynamically generated content.

Conceptually:

```kotlin
fun sanitizeForSpeech(text: String): String
```

It should:

- remove emoji presentation characters,
- remove zero-width emoji joiners/modifiers when relevant,
- remove decorative star/celebration symbols,
- collapse repeated spaces,
- preserve ordinary punctuation,
- preserve German umlauts/ß,
- preserve numbers,
- preserve meaningful mathematical language only when intentionally authored.

Do not strip meaningful letters or punctuation blindly.

Structured `speechText` remains the primary solution; sanitisation is the safety net.

---

# 6. Immediate regression tests

Add tests proving that these display strings never send emoji names to speech:

English:

```text
Great! 🌟
Yes! 🎉
Correct! ⭐
Well done! 🐉
```

German:

```text
Super! 🌟
Ja! 🎉
Richtig! ⭐
Sehr gut! 🐉
```

Expected speech requests:

```text
Great!
Yes!
Correct!
Well done!

Super!
Ja!
Richtig!
Sehr gut!
```

Also test:

- replay,
- tutorials,
- correct feedback,
- wrong feedback,
- answer pronunciation,
- completion narration,
- future rewards.

No speech path should bypass the speech-safe transformation.

---

# 7. Long-term architecture — SpeechEngine abstraction

Shared lifecycle/ownership follows [NATIVE_ARCHITECTURE_SPEC.md](NATIVE_ARCHITECTURE_SPEC.md), authored speech data follows [CONTENT_DATA_SPEC.md](CONTENT_DATA_SPEC.md), and cross-device acceptance follows [TESTING_QA_SPEC.md](TESTING_QA_SPEC.md). This document remains authoritative for speech policy, engines and voice quality.

The app should not make learning screens depend directly on Android `TextToSpeech`.

Use a shared interface conceptually like:

```kotlin
interface SpeechEngine {
    suspend fun speak(request: SpeechRequest)
    fun stop()
    fun isReady(): Boolean
}
```

Example request:

```kotlin
data class SpeechRequest(
    val text: String,
    val language: AppLanguage,
    val type: SpeechType,
    val style: SpeechStyle = SpeechStyle.NEUTRAL,
    val interrupt: Boolean = true
)
```

Possible speech types:

```text
INSTRUCTION
QUESTION
OPTION
FEEDBACK
TUTORIAL
EXPLANATION
DISCOVERY_FACT
CHARACTER_LINE
```

Possible styles are semantic hints, not guarantees:

```text
NEUTRAL
WARM
ENCOURAGING
CALM
EXCITED
```

The rest of the app should talk to the shared controller, not to a specific TTS implementation.

---

# 8. Speech engine implementations

Target architecture:

```text
SpeechEngine
├── AndroidSystemSpeechEngine
├── NeuralOfflineSpeechEngine
└── RecordedPhraseEngine / hybrid layer
```

`AndroidSystemSpeechEngine` remains a fallback.

The higher-quality default should be selected only after physical-device evaluation.

---

# 9. Candidate A — Supertonic via sherpa-onnx

Primary candidate to evaluate.

Reasons:

- offline neural synthesis,
- English and German support,
- potentially more consistent voice across devices,
- same speech stack can be used on Samsung and Fire if performance is acceptable,
- sherpa-onnx provides an Android-friendly inference route.

Evaluation questions:

- How natural is British English?
- How natural is German?
- Does the voice sound friendly for a child?
- Does it pronounce animal names correctly?
- Does it handle mixed numbers/instructions naturally?
- Start-up latency on S24?
- Start-up latency on Fire Max?
- RAM usage?
- APK/model size?
- time-to-first-audio?
- synthesis speed for short dynamic instructions?

Do not make it production default before physical tests.

---

# 10. Candidate B — Piper/VITS via sherpa-onnx

Fallback/alternative candidate.

Potential advantages:

- relatively lightweight models,
- strong offline support,
- multiple English/German voices,
- simpler device requirements.

Potential disadvantages:

- voice quality varies significantly by model,
- English and German may require separate voice models,
- each selected voice/model licence must be checked before distribution.

Evaluate selected voices rather than judging Piper as a whole.

---

# 11. Android system TTS fallback

Keep Android system TTS available.

Reasons:

- tiny app footprint,
- useful fallback if neural model fails,
- useful during development,
- may be acceptable on some devices with high-quality installed voices.

However:

- voice differs by device,
- Fire and Samsung may not have the same voices,
- some voices sound robotic,
- language availability cannot be assumed.

System TTS should therefore not be the only long-term production audio solution.

---

# 12. Hybrid recorded + generated speech

A hybrid approach is encouraged.

Use high-quality pre-generated or recorded clips for common personality lines:

English examples:

```text
Great job!
You found it!
Let’s try again.
Ready for an adventure?
Well done!
Listen carefully.
Your turn!
Adventure complete!
```

German examples:

```text
Super gemacht!
Du hast es gefunden!
Versuchen wir es noch einmal.
Bereit für ein Abenteuer?
Sehr gut!
Hör gut zu.
Du bist dran!
Abenteuer geschafft!
```

Use dynamic TTS for variable content:

```text
Put the red egg between the two rocks.
Touch the green crocodile, then the snake.
Which number comes after seven?
```

This gives recurring feedback more personality without requiring every possible sentence to be stored as audio.

---

# 13. Character voice philosophy

The app should not use exaggerated cartoon voices for core instructions.

Core narrator should be:

- friendly
- warm
- calm
- expressive but not theatrical
- easy to understand
- consistent

Optional character voices may eventually be used sparingly for:

- dragon greeting,
- crocodile snap reaction,
- Discovery Book character moments,
- celebration lines.

Learning instructions should remain exceptionally clear.

---

# 14. English voice target

Preferred English character:

- natural British English if a strong voice is available,
- warm rather than formal,
- clear consonants,
- not overly fast,
- not overly synthetic,
- child-friendly without sounding babyish.

Test phrases should include:

```text
Touch the green crocodile.
Put the red egg between the two rocks.
Which animal can slither?
What number comes after seven?
Great job!
Let’s hear that again.
The Mosasaurus lived in the sea.
Can you tell me what you can see?
```

---

# 15. German voice target

Preferred German character:

- natural Standard German,
- warm and conversational,
- no exaggerated announcer style,
- clear articles/cases,
- comfortable pacing for a young child.

Test phrases:

```text
Tippe auf das grüne Krokodil.
Lege das rote Ei zwischen die beiden Steine.
Welches Tier kann schlängeln?
Welche Zahl kommt nach sieben?
Super gemacht!
Hör dir das noch einmal an.
Der Mosasaurus lebte im Meer.
Kannst du mir sagen, was du siehst?
```

German content must be reviewed as German, not generated mechanically from English.

---

# 16. Voice audition screen

Before changing production audio, build a small **parent/developer-only Voice Lab**.

It should allow:

- select English/German,
- choose available candidate voice,
- play a fixed audition set,
- play a dynamic custom app sentence if useful,
- compare system voice vs neural candidate,
- show synthesis latency,
- optionally show model/engine name for development.

Do not expose technical voice/model names on Basti’s child-facing home screen.

Candidate parent-facing labels may later be:

```text
Friendly
Warm
Clear
System voice
```

---

# 17. Physical evaluation matrix

Test each candidate on:

## Samsung S24

Measure:

- first-use model load
- time to first spoken phrase
- subsequent phrase latency
- memory use
- interruptions
- orientation/navigation behavior
- English quality
- German quality

## Amazon Fire Max

Measure the same items.

The Fire test is particularly important because neural inference may be more constrained there.

---

# 18. Voice selection criteria

Score candidate voices internally on:

- warmth
- naturalness
- clarity
- English pronunciation
- German pronunciation
- child friendliness
- short-instruction quality
- long-sentence quality
- latency
- memory usage
- storage cost
- offline reliability
- licence/distribution suitability

Do not select purely based on benchmark speed.

For this product, a slightly slower voice may be preferable if it sounds substantially more natural, provided interaction remains responsive.

---

# 19. Pacing and speech rate

The current rate is approximately slower than default.

Do not assume one global rate works for every engine.

Tune by listening.

Guidelines:

- instructions: clear and calm,
- option labels: short and neutral,
- feedback: slightly warmer,
- Discovery facts: conversational,
- tutorials: slower if necessary.

Avoid unnaturally slow speech.

---

# 20. Speech queue behavior

Required behavior:

- a new question may interrupt stale narration,
- leaving a screen stops narration,
- replay intentionally restarts the current instruction,
- tapping an option’s speaker should not submit the option,
- feedback should not be unexpectedly interrupted by a delayed question,
- repeated taps should not create a long audio queue.

Use cancellable/owned speech requests.

---

# 21. TTS readiness

Current speech requests should not silently disappear because the TTS engine is still initialising.

The shared controller should have explicit states such as:

```text
INITIALISING
READY
FAILED
```

The current legacy implementation retains one latest request during initialisation, drops it on context cancellation, and rejects requests on failure. It already uses system TTS; there is no further engine fallback. The future enhanced engine may fall back to system TTS under the same offline and ownership rules. See section 2 for the implemented policy.

---

# 22. Offline requirement

Production learning audio should work without internet access.

Do not depend on:

- cloud speech APIs,
- Google Play Services,
- continuous network access.

Model downloads may be considered only if deliberately designed later, but the preferred child experience is that required core voices are already available offline.

---

# 23. Storage strategy

If neural models are too large to bundle comfortably:

Possible options to evaluate:

1. bundle one bilingual model,
2. bundle compressed models,
3. ship core recorded phrases + smaller neural engine,
4. optional one-time parent-triggered voice pack installation.

Do not choose this until real model size/performance is measured.

The app must remain usable if the enhanced voice model is unavailable.

---

# 24. Pronunciation dictionary / overrides

Animal and specialist words may need explicit handling.

Examples:

- Mosasaurus
- Pteranodon
- Velociraptor
- Tyrannosaurus
- Komodo
- Ankylosaurus

The future audio layer should support pronunciation overrides where an engine consistently mispronounces a key word.

Overrides should be language-specific.

Do not silently alter displayed spelling.

---

# 25. Numbers, symbols and generated text

Generated speech must be authored semantically.

Avoid sending raw decorated strings such as:

```text
3 + 2 = ?
⭐⭐⭐⭐⭐
D → Dinosaur
```

Prefer deliberate spoken equivalents:

```text
What is three plus two?
You earned five stars.
D. Dinosaur.
```

Likewise in German:

```text
Was ist drei plus zwei?
Du hast fünf Sterne gesammelt.
D. Dinosaurier.
```

---

# 26. Emoji and icon policy

Production rule:

> **Decorative emoji belong to presentation, never automatically to speech.**

This includes:

- stars
- balloons
- celebration emoji
- animals used as decorative icons
- speaker emoji
- eye emoji
- party emoji
- arrows where they are decorative

If an emoji represents actual learning content, use explicit semantic speech.

Example:

Visible:

```text
🐊
```

Spoken deliberately:

```text
Crocodile
```

Never rely on the TTS engine’s Unicode emoji name.

---

# 27. Accessibility distinction

Screen-reader semantics and narration are related but not identical.

For example:

A decorative celebration icon may have no accessibility description.

A crocodile image used as an answer must have a meaningful semantic label.

The app’s teaching narration should not simply read the entire visual interface.

---

# 28. Recommended implementation phases

## Phase V0 — immediate P0

- remove emoji/decorative symbols from current TTS output,
- separate feedback display text from speech text where necessary,
- add final speech sanitisation,
- add regression tests.

The legacy V0 implementation is now in place at the JavaScript boundary described in section 2. Automated results belong in BUILD_NOTES.md; physical S24/Fire confirmation remains required.

---

## Phase V1 — native audio foundation

Create:

- `SpeechEngine`
- `SpeechRequest`
- `AudioController`
- cancellation/queue policy
- bilingual speech handling
- Android system implementation
- test doubles

Migrate native activities to this API.

---

## Phase V2 — Voice Lab

Create parent/developer-only comparison screen.

Test:

- current Android voice
- Supertonic candidate voices
- Piper candidate voices if required

---

## Phase V3 — physical audition

Evaluate on:

- Samsung S24
- Amazon Fire Max

Use the same fixed English/German audition script.

---

## Phase V4 — choose enhanced engine

If Supertonic performs and sounds good:

- use it as preferred enhanced/default engine.

If not:

- evaluate selected Piper voices.

Always preserve system TTS fallback.

---

## Phase V5 — hybrid personality audio

Add high-quality fixed clips for repeated encouragement/personality phrases where worthwhile.

---

## Phase V6 — parent voice settings

Possible future options:

```text
Voice
● Friendly
○ Warm
○ System
```

Only expose options that have actually been tested.

---

# 29. First Astra/Luna audio task

Do **not** ask an agent to integrate a neural TTS engine immediately.

First task should be bounded:

1. audit every current speech path,
2. ensure emojis/decorative glyphs are never accidentally spoken,
3. separate display/speech feedback strings,
4. implement speech sanitisation at the final boundary,
5. add English/German regression tests,
6. preserve existing functionality,
7. document the future `SpeechEngine` migration.

Then handle neural voice experimentation as a separate task.

---

# 30. Definition of success

The voice system is successful when:

- Basti never hears accidental Unicode/emoji names,
- English and German sound natural and friendly,
- instructions remain clear,
- speech works offline,
- S24 and Fire provide a consistent experience,
- audio stops/restarts predictably,
- no stale speech overlaps new tasks,
- visual decoration cannot leak into narration,
- specialist vocabulary is pronounced acceptably,
- the app can change speech engines without rewriting learning screens.
