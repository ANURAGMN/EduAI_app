# Coach UI revamp — review notes (for Cursor)

Everything below is **new since `CHANGES_V5_COACH.md`**. Two repos: **EduAI_app** (sims) and
**Eduapp** (Android). App-side Kotlin was written pattern-matched but **NOT compiled here** — please
build and fix any signature mismatches. Some Eduapp files were also edited by Cursor in parallel.

Current build marker: **`20260808u`** (grep logcat `CoachBuild`).

---

## A. Engine — `EduAI_app/Simulations/edu-coach.js`
- **Hint models (no more 7-second timer).** Reads `window.__eduHintMode` = `ask | guided | self | ondemand`; `window.__eduHint()` advances disclosure; `window.__eduSetHintMode(m)` persists to localStorage.
  - `ask` and `ondemand`: **one tap reveals** move + why (no separate nudge).
  - `self`: problem → nudge (why) → reveal.
  - `guided`: reveals immediately.
- **Explain panel** = Photomath-style: numbered **step cards** parsed from `detail` ("Step 1 — …"), plus Why/Tip blocks; `window.__eduExplain()` / `window.__eduCloseExplain()`; Listen/Stop/Replay via **native TTS in-app** (`AndroidBridge.coachSpeak` / `coachStop`), speechSynthesis standalone. Shows only when there's `why`/`detail`.
- **Per-round detail** built from the round's real numbers for compare, rounding, ratio, build, pattern, calculator, target. **Brief why** appended to the coach line at reveal.
- **Numbers spoken as words** (`speakNums`): "+10000" → "ten thousand".
- Standalone-only: renders its own mode chips + Hint button (browser testing); hidden in-app (`IN_APP`), where the native coach card drives it.

## B. Sim hooks (per-round Explain / native `window.__eduRound`)
- **Math ch1 complete** (per-round Explain): `math_1_1`, `1_1_new`, `1_2`, `1_2_new`, `1_3`, `1_4`, `1_4_new`, `1_5`, `1_13` (native) + engine-covered `1_6`–`1_12`, `1_3_new`, `1_5_new`, `1_7`, `1_8`.
- **`math_1_3_new` (Digit Permutation Vault) — NEW native hook** (fixes "no voice/highlight"): glows the next digit toward a valid answer, speaks it, per-round Explain.
- Math ch2 so far: `2_2`, `2_4`, `2_1_new`, `2_5_new`.
- Demos: `Simulations/sample_coach_demo.html`, `Simulations/coach_variations.html`.

## C. App — floating coach (Layout C)
- **`SimCoachOverlay.kt` → new `SimFloatingCoach`**: sim fills the screen; a **tutor-avatar bubble** (falls back to bulb) + one-line peek + **Replay** upfront, with **Explain stacked above Hint** on the right; tap bubble → popover (full line, voice toggle, coach-style chips). Hidden while the page Explain sheet is open.
- **`ConceptSimulationViewer.kt`**: V5 coach now renders `SimFloatingCoach` **overlaying the sim** (anchored to the bottom region, not `fillMaxSize`, so the WebView stays touchable). Old bottom-card V4 branch removed. `coachAvatar` moved above the sim Box.

## D. App — Coach Settings sheet (header gear)
- **`CoachSettingsSheet.kt` — NEW** (Material3 `Dialog`, plain params): methodology, voice on/off, **voice selection**, **speed**, **avatar**.
- **`SimulationHeader.kt`**: added a **gear** (`onSettingsClick`).
- **`ConceptSimulationViewer.kt`** wiring:
  - `hintMode` (pref) + `hintSignal`; `explainOpen` + `explainSignal`/`explainDismissSignal`.
  - `ttsController: TextToSpeech = hiltViewModel()` passed into `rememberSimulationKeyConceptTts(languageCode, avatarCode, ttsController)`.
  - Speed → `ttsController.setSpeechRate`; Voice → `ttsController.setVoice`; **Avatar (Boy/Girl/None) → `ttsController.switchCharacter(code)`** + persisted via `SharedPreferenceUtils.getCoachAvatar/setCoachAvatar`.
- **`SimGuide.kt`**: `HintMode` enum. **`SharedPreferenceUtils.kt`**: `getHintMode/setHintMode`, `getCoachAvatar/setCoachAvatar`.
- **Bridge/WebView** (some by Cursor): `coachStop`, `coachExplainVisible`, `hintMode`, `hintSignal`, `explainSignal`, `explainDismissSignal` LaunchedEffects.

---

## Fixed after first review (build 20260808v)
- **Risk 7 (None still showed the tutor):** the `coachAvatar` is now gated on `useNativeAvatar && coachAvatarCode != "disable"`, so "None" hides the tutor bubble (falls back to the bulb).
- **Redundant methodology:** removed the coach-style chips from the floating popover; methodology now lives only in the Settings sheet. Dropped the now-unused `onHintModeChange` from `SimFloatingCoach`.

## Please review these risk points
1. **`CoachSettingsSheet.kt`** compiles (Dialog + verticalScroll + Slider/Switch).
2. **`SimFloatingCoach`** overlay does NOT block WebView touches (it's bottom-anchored, not full-size). Confirm the sim's bottom controls (Lock Build / Check) aren't covered on small screens — if so, add bottom padding to the sim content.
3. **`hiltViewModel<TextToSpeech>()`** returns the SAME instance the coach uses (we pass it into `rememberSimulationKeyConceptTts`). Verify `setVoice(Voice)`, `setSpeechRate(Float)`, `switchCharacter(String)` signatures.
4. **Voice-selection labels** are generic ("Voice 1..N") — friendly names still TODO.
5. **Removed the V4 bottom-card branch** in `ConceptSimulationViewer` — check no leftover refs (`coachMission`, `explainOpen`) are now unused/broken.
6. **`edu-coach.js` ↔ app**: `window.__eduHintMode/__eduHint/__eduExplain/__eduCloseExplain` names match the LaunchedEffects in `SimulationWebView.kt`.
7. Avatar: `switchCharacter` swaps the tutor character — confirm it reflects in the floating bubble (native avatar) as expected.

## Not done yet
- Friendly voice names; Math ch2 remaining hooks; Science ch2/ch3; Kannada `_kn` parity.
