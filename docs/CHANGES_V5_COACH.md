# V5 coach — change notes

A running record of everything changed while building the V5 "one-clock" coach, the Explain
panel, and the Math ch1 rollout. Two repos are involved: **EduAI_app** (the simulations site)
and **Eduapp** (the Android app).

---

## 1. What V5 is (in one line)
The simulation drives the coach. A single shared script in each sim reads the live activity,
works out the next move, and shows: an on-sim **glow**, a short **coach line** (full problem +
brief why), and an **Explain** panel (Photomath-style steps + Listen/Stop/Replay). The app is a
thin host that renders the line, speaks it, and shows the Explain chip.

---

## 2. EduAI_app (simulations) — changes

### New files
- `Simulations/edu-coach.js` — the shared V5 coach engine (see features below).
- `Simulations/sample_coach_demo.html` — a self-contained coached place-value sim (demo).
- `Simulations/coach_variations.html` — same problem shown with 4 hint styles (Try-first /
  Step-by-step / Self-explain / Answer-on-tap) to compare the feel.
- `docs/COACH_EXPLANATIONS.md` — the reviewable source-of-truth for every scenario's text.
- `docs/EDU_ROUND_CONTRACT.md`, `docs/examples/edu-round-hook-math_1_8.html` — the sim→coach
  contract and a worked hook example. NOTE: these were originally authored in the **Eduapp** repo
  (`Eduapp/docs/…`) before EduAI_app was connected; they are now also copied here so sim authors
  have them alongside the sims.

### Wiring
- `<script src="edu-coach.js"></script>` added before `</body>` in **57 English sims**
  (Math ch1 + ch2, Science ch2 + ch3). Kannada `_kn` twins are NOT wired yet.

### edu-coach.js engine — features
- **One-clock model:** one 300ms loop reads the DOM, solves, glows, and pushes the line/voice on
  the same tick, so glow + text + voice can't drift apart.
- **Native publish + fallback:** a sim can set `window.__eduRound = {native:true, …}` to drive the
  coach directly (correct by construction). If it doesn't, the engine's generic solver handles the
  known shapes (compare, rounding, ratio, speed, pattern, build, calculator, maximize, vault,
  expression, acid/base, conductor).
- **App hand-off:** sets `window.__eduV5=true` so the app's older loop yields; routes text/voice to
  native TTS in-app, self-renders a bar + browser speech standalone.
- **Numbers spoken as words** (`speakNums`): "+10000" → "ten thousand", "1,00,000" → "one lakh"
  (fixes "plus one oh oh oh").
- **Full problem quoted** in every hint (target/number/sequence with real values), not a terse ask.
- **Brief why/how** appended to the coach line at reveal.
- **Explain panel (Photomath style):** the question, an answer chip, numbered **step cards**, a
  "why it works" block and tips — built **per round from the actual numbers**. Has **Listen / Stop /
  Replay** (native TTS in-app). Only appears when there's real reasoning to show.
- **Cross-device:** responsive sizing (safe-area insets, no `vh` dependence — the earlier
  "thin strip" panel bug), bar wraps and grows instead of truncating.

### Per-sim native hooks (13 sims, hand-wired to each sim's own answer)
Math ch1: `math_1_1`, `math_1_1_new`, `math_1_2`, `math_1_2_new`, `math_1_3`, `math_1_4`,
`math_1_4_new`, `math_1_5`, `math_1_13`.
Math ch2: `math_2_1_new`, `math_2_2`, `math_2_4`, `math_2_5_new`.

### Math Chapter 1 — COMPLETE
All 18 English modules now have: full-problem hint → brief why → **per-round Photomath Explain**.
(`math_1_3` "Sense of Scale" is a pure explorer with no single answer, so it keeps a guiding prompt
instead of steps — intentional.)

---

## 3. Eduapp (Android app) — changes
- `SimulationInteractionScript.kt` — `tick4()` yields when `window.__eduV5` is set (lets the sim
  engine drive); science acid/base handler earlier became a full walkthrough of every substance.
- `SimulationWebViewBridge.kt` — added `coachSpeak`, `coachText`, and `coachStop` bridges
  (+ `onCoachSpeak` / `onCoachText` / `onCoachStop` callbacks).
- `SimulationWebView.kt` — passes `coachV4Active`, `onCoachText/Speak/Stop`, and `explainSignal`
  (a bump that calls `window.__eduExplain()` to open the panel from the coach card).
- `ConceptSimulationViewer.kt` — V5 wiring; default coach mode is the one-clock coach; **Explain
  chip** added to the coach card (opens the page panel); Stop button wired to `keyConceptTts.stop()`.
- `SimCoachOverlay.kt` — `SimAdaptiveCoachBar` gained an `onExplain` chip (next to Replay).
- Build markers progressed `20260808j → 20260808m`. **STATUS:** `20260808m` (Explain chip on the
  coach card) is currently only in the **working tree** — the last build actually committed/pushed
  and installable is `20260808l` (coachStop bridge). The Explain-chip and `coachStop` app changes go
  live only after a commit + `./gradlew installDebug`.

> App changes need a rebuild (`./gradlew installDebug`). Sim/`edu-coach.js` changes only need a
> `git push` — the app loads them from the deployed site.

---

## 4. Design decisions still open (demoed, not yet the live default)
- **Hint model:** the live in-sim coach still uses a 7-second auto-reveal. The **on-demand** models
  (progressive Hint on tap, single show-hint, help-after-wrong-try, and a blend) are built in
  `coach_variations.html` for you to choose. Once you pick, I'll replace the timer in `edu-coach.js`.
- **UI presentation:** Photomath-style sheet direction agreed; half-sheet vs full, one-step-at-a-time
  vs all steps, per-step "why" — to finalise.

---

## 5. Not done yet (planned next)
- Swap the chosen on-demand hint model into the live engine (remove the 7s timer).
- Math ch2: native hooks for the remaining ~14 bespoke expression games + per-round Explain.
- Science ch2 (neutralisation, intro) and ch3 (electricity) native hooks.
- Kannada `_kn` parity for the coach + translated explanations.
- Optional: per-student coach mode + adaptivity; hint-usage analytics; minor sim UI polish
  (tap-target size, bottom spacing, scroll glowed control into view).

---

## 6. How to test
1. `cd EduAI_app && git add -A && git commit -m "V5 coach" && git push` (deploys the sims).
2. `cd Eduapp && ./gradlew installDebug` (for the app-side Explain chip + Stop).
3. On device: open a Math ch1 sim, play a couple of rounds, check the glow + coach line, tap
   **Explain** and confirm the steps use the current round's numbers, and try Listen/Stop/Replay.
4. `adb logcat -s CoachBuild:D` shows the *installed* build — it will read `20260808l` until you
   commit + rebuild the app changes, after which it reads `20260808m`.
