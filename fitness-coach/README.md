# Fitness Coach — AI readiness lifecycle

Part of the **On-Device Fitness Coach** series on [Mobile With Me](https://www.divyajain.dev/blog).
This milestone adds the lifecycle *before* inference: checking whether Gemini Nano can
run, downloading it when needed, warming it up, and showing each of those states in the
UI, without the UI ever knowing which AI SDK is underneath.

**Companion post (Part 5):** *Your AI Model Is Not Always Ready: Managing On-Device AI
Lifecycle on Android* (link added on publish)

**Previous milestone (Part 4):**
[A Production-Ready Architecture for On-Device AI on Android](https://www.divyajain.dev/blog/posts/on-device-fitness-coach/production-architecture)

## What this demonstrates

> The model being on the device does not mean the model is ready.

Two seams, two responsibilities:

```
                     FitnessAiReadinessManager (interface)   ← availability, download, warm-up, close
                              │
Compose UI ← FitnessCoachViewModel ← AiReadinessState

Compose UI → FitnessCoachViewModel → GenerateFitnessInsightUseCase → FitnessInsightEngine (interface)  ← inference
```

| Seam | Implementations |
|------|-----------------|
| `FitnessInsightEngine` | `GeminiNanoInsightEngine`, `FakeInsightEngine`, `UnavailableInsightEngine` |
| `FitnessAiReadinessManager` | `GeminiNanoAiReadinessManager`, `FakeAiReadinessManager`, `UnavailableAiReadinessManager` |

In Gemini mode both Gemini classes share **one** `GenerativeModel`, so the client that
was checked and warmed up is the same one that runs inference.

## The readiness lifecycle

```
Checking ──► DownloadRequired ──► Downloading ──► WarmingUp ──► Ready ──► (inference) ──► close()
   │                                                  │
   ├──► Downloading (already running in AICore)       └──► Failed ──► Try again
   ├──► Unavailable
   └──► Failed (status check)
```

`AiReadinessState` is Fitness Coach's own product state, not a copy of ML Kit's
`FeatureStatus`. Each state has a visible consequence:

| State | What the user sees |
|-------|--------------------|
| `Checking` | "Preparing on-device coaching…" |
| `DownloadRequired` | One-time download explanation + **Download** |
| `Downloading` | Progress bar (determinate when the size is known; otherwise **Check again**) |
| `WarmingUp` | "Almost ready…" |
| `Ready` | **Generate insight** enabled |
| `Unavailable` | Explanation; Generate still gives a rule-based suggestion |
| `Failed` | "We couldn't finish setting up…" + **Try again** |

A few deliberate choices:

- **A completed download still warms up.** `DownloadCompleted` leads to `WarmingUp`,
  then `Ready`. Downloaded is not the same as loaded.
- **`UNAVAILABLE` is "not right now", not "never".** AICore can report it on a supported
  device that hasn't fetched its configuration yet after setup or reset.
- **Readiness starts when the coaching screen opens**, in `viewModelScope`, so warm-up
  happens before the first tap without eagerly initializing AI across the app.
- **No WorkManager.** Status checks, download progress, and warm-up only matter while
  the screen is alive, so they're ordinary coroutines. AICore owns the model download
  itself. WorkManager is for work that must survive the app leaving the screen.
- **Nothing persists `modelReady = true`.** The source of truth for "right now" is
  always `checkStatus()`. Persisted settings (like the debug engine mode) are context,
  never readiness.
- **`close()` is part of the lifecycle.** `onCleared()` closes the readiness manager
  and the engine; ML Kit documents `close()` as safe to call more than once.

## Versioning what Fitness Coach owns

Gemini Nano is managed by AICore, so there's no model file version to point at.
`FitnessAiConfiguration` tracks the pieces this app controls: prompt version, summary
schema version, output validation version, and the requested model **release stage**
(`STABLE`) and **preference** (`FULL`). The client is created with that configuration,
and once ready the app logs it alongside `getBaseModelName()`.

## EngineMode (live demo)

Use the on-screen **Engine (debug)** selector to switch without rebuilding:

| Mode | Behavior |
|------|----------|
| **Gemini** | Real ML Kit path: real status check, download, warm-up, inference |
| **Fake** | Checking → WarmingUp → Ready, then the deterministic Demo Part 1 insight |
| **Fake (needs download)** | Starts at DownloadRequired; simulated download → warm-up → Ready. Shows the full lifecycle on an emulator |
| **Unavailable** | Readiness Unavailable; Generate gives the notice + rule-based suggestion |

Selection is persisted in SharedPreferences and applied via Activity recreate.

## Why there are multiple implementations

Gemini Nano on-device generative AI needs specific supported hardware (Pixel 9/10,
or another device on ML Kit's supported list). **It does not run on an emulator.**
The Fake and Unavailable implementations of both seams make every lifecycle state
visible, previewable, and unit-testable without one.

## Running it

1. Open the `fitness-coach` folder in Android Studio (Ladybug or newer recommended).
2. Pick **Fake** for the Demo Part 1 happy path on any device / emulator.
3. Pick **Fake (needs download)** to walk through download → warm-up → ready.
4. Pick **Unavailable** to see the unavailable notice + rule-based suggestion.
5. On a Gemini Nano-supported device (Pixel 9/10+), pick **Gemini** for the real path.

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

## What this milestone does NOT do yet

- **Performance (Part 6):** first-insight latency, cancelling or de-duplicating
  concurrent requests, memory use, and behavior on slower supported devices.
- **Output quality (Part 7):** what to do when the model runs fine but the answer still
  isn't good enough to show.
- No cloud fallback, and no real `FitnessSummaryBuilder`; the ViewModel uses a fixed
  demo summary.

## Dependency note

Uses `com.google.mlkit:genai-prompt:1.0.0-beta4`. The Prompt API is beta and not covered
by an SLA or deprecation policy, so the version is pinned; check the
[ML Kit release notes](https://developers.google.com/ml-kit/release-notes) before bumping.

## Project structure

```
app/src/main/java/com/divya/fitnesscoach/
├── domain/
│   ├── ActivitySummary.kt
│   ├── AiReadinessState.kt
│   ├── EngineMode.kt
│   ├── FitnessAiReadinessManager.kt
│   ├── FitnessInsightEngine.kt
│   ├── FitnessInsightFailure.kt
│   ├── FitnessInsightResult.kt
│   ├── GenerateFitnessInsightUseCase.kt
│   ├── ReadinessFailure.kt
│   └── RuleBasedFitnessInsight.kt
├── data/
│   ├── EngineModeStore.kt
│   ├── FakeAiReadinessManager.kt
│   ├── FakeInsightEngine.kt
│   ├── FeatureStatusMapping.kt
│   ├── FitnessAiConfiguration.kt
│   ├── GeminiNanoAiReadinessManager.kt
│   ├── GeminiNanoInsightEngine.kt
│   ├── InsightEngineFactory.kt
│   ├── UnavailableAiReadinessManager.kt
│   └── UnavailableInsightEngine.kt
├── ui/
│   ├── AiReadinessMessages.kt
│   ├── FitnessCoachScreen.kt
│   ├── FitnessCoachViewModel.kt
│   ├── FitnessCoachViewModelFactory.kt
│   └── FitnessInsightFailureMessages.kt
└── MainActivity.kt
```

## Series context (blog)

- Part 1: [On-Device AI Is a Decision, Not a Trend](https://www.divyajain.dev/blog/posts/on-device-fitness-coach/architecture-decision)
- Part 2: On-Device AI Decision Scorecard
- Part 3: Too Many On-Device AI Options? Here's How to Actually Choose
- Part 4: [A Production-Ready Architecture for On-Device AI on Android](https://www.divyajain.dev/blog/posts/on-device-fitness-coach/production-architecture)
- **Part 5 (this milestone):** Your AI Model Is Not Always Ready: Managing On-Device AI Lifecycle on Android
- Part 6 (next): responsiveness, cancellation, concurrency, and measuring inference performance
