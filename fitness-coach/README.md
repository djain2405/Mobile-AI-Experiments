# Fitness Coach — clean architecture

Part of the **On-Device Fitness Coach** series on [Mobile With Me](https://www.divyajain.dev/blog).
This milestone builds the architectural seam between the app and Gemini Nano, so the
runtime underneath can change without touching the UI or domain layers.

**Companion post (Part 4):**
[A Production-Ready Architecture for On-Device AI on Android](https://www.divyajain.dev/blog/posts/on-device-fitness-coach/production-architecture)

## What this demonstrates

> The UI and domain layers do not know which AI SDK is executing the request.

```
Compose UI → FitnessCoachViewModel → GenerateFitnessInsightUseCase → FitnessInsightEngine (interface)
                                                                            ├── GeminiNanoInsightEngine (real, ML Kit GenAI Prompt API)
                                                                            └── FakeInsightEngine (previews, tests, emulator)
```

## Why there are two engines

Gemini Nano on-device generative AI needs specific supported hardware (Pixel 9/10,
or another device on ML Kit's supported list). **It does not run on an emulator.**

- `GeminiNanoInsightEngine` is the real ML Kit GenAI Prompt API implementation used by
  this sample. Run the app on a supported device and it works. Run it anywhere else
  (including any emulator) and it safely reports `FitnessInsightResult.Unavailable`,
  it does not crash.
- `FakeInsightEngine` is a deterministic stand-in used for Compose previews, unit
  tests, and day-to-day development. Without it, nothing above the engine layer could
  be built or tested honestly before getting hold of real hardware.

## Running it

1. Open the `fitness-coach` folder in Android Studio (Ladybug or newer recommended).
2. Run on an emulator to see the honest Unavailable path from the real engine (expected).
3. Run on a Gemini Nano-supported device to see the real Gemini Nano response.
   First run may trigger a model/adapter download over the network; subsequent runs
   are fully offline.

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

## What this milestone does NOT do yet

- No retries, cloud fallback, or confidence scoring on the result — that belongs in a
  later post on confidence and fallbacks.
- No real `FitnessSummaryBuilder`; the ViewModel uses a fixed sample summary for now.
- No model download UI/progress state — that belongs in a later post on model lifecycle.

## Architecture review notes

- **Lifecycle-aware state collection.** `FitnessCoachRoute` uses
  `collectAsStateWithLifecycle()`, not `collectAsState()`.
- **A real, lifecycle-managed ViewModel.** `MainActivity` obtains the ViewModel via
  `by viewModels { FitnessCoachViewModelFactory(...) }`.
- **A genuinely pure screen.** `FitnessCoachScreen` takes `uiState` and
  `onRequestInsight` as plain parameters. Previews need nothing but a hardcoded state.
- **A bounded input type.** `ActivitySummary` replaces a raw `String` on the engine API.
- **No raw SDK exception text in the UI.** `FitnessInsightFailure` maps to user copy.
- **Invalid input is state, not a thrown exception.**

## Project structure

```
app/src/main/java/com/divya/fitnesscoach/
├── domain/
│   ├── ActivitySummary.kt
│   ├── FitnessInsightEngine.kt
│   ├── FitnessInsightFailure.kt
│   ├── FitnessInsightResult.kt
│   └── GenerateFitnessInsightUseCase.kt
├── data/
│   ├── GeminiNanoInsightEngine.kt
│   └── FakeInsightEngine.kt
├── ui/
│   ├── FitnessCoachViewModel.kt
│   ├── FitnessCoachViewModelFactory.kt
│   ├── FitnessInsightFailureMessages.kt
│   └── FitnessCoachScreen.kt
└── MainActivity.kt
```

## Series context (blog)

- Part 1: On-Device AI Is a Decision, Not a Trend
- Part 2: On-Device AI Decision Scorecard
- Part 3: Too Many On-Device AI Options? Here's How to Actually Choose
- **Part 4 (this milestone):** production-ready architecture with `FitnessInsightEngine`
