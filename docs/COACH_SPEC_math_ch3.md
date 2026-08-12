# Math Chapter 3 — Decimals · Coach Spec

Reference spec for the per-simulation coach, in the same shape as the Chapter 2 spec. All 15 English
sims now carry the coach: `edu-coach.js` was wired in (it was previously absent for the whole
chapter) and each sim publishes native `window.__eduRound` hooks. This document captures, for each
sim, the **concept**, the coach **text**, the **explanation** (Explain-panel steps), the **glow**
behaviour, and the **unit tests**.

> **Source of truth:** this file is the single sign-off catalogue for the Ch 3 English coach. Do not
> duplicate per-sim copy into `COACH_EXPLANATIONS.md` — that file links here to avoid a dual source.

## How the coach works (all sims)

Each round the sim publishes a `window.__eduRound` object the shared engine renders. Full contract:

| Field | Purpose |
|---|---|
| `native:true` | marks this as a sim-authored round (engine prefers it over scraping) |
| `hint` | the question shown first — states the full problem |
| `hintVoice` | short spoken form of the hint (TTS) |
| `why` | one line of reasoning (the rule) |
| `detail` | Photomath steps (`Step 1 … / Step 2 … / Why it works / Tip`) for the Explain panel |
| `line` | the reveal text shown on the coach card |
| `voice` | short spoken form of the reveal (TTS) |
| `glow` | element (or selector) of the correct control — the **answer glow** |
| `glowKind` | optional: `answer` (red) vs default hint (orange) |
| `input` / `inputHint` | element of a field to fill + a placeholder hint (the **input glow**) |
| `submit` | element of the advance button (Check / Next) — the **action glow** |
| `key` | round identity; changing it resets the hint-disclosure level |

**Multi-phase states.** Mission/round sims publish a *different* round object per phase, keyed so the
engine re-emits cleanly:

- **start / idle** (`run:false` before Start): `line` = "Tap Start…", `glow` = the Start button, `key` = `start-<idx>`.
- **active** (mid-round): the full `hint`/`why`/`detail`/`glow`/`submit`, `key` = `<round>-<idx>` (plus live sub-state, e.g. the number of tiles placed, so the glow re-targets as the learner progresses).
- **resolved** (after Check): `line` = the sim's feedback message, `submit` = Next, `key` = `res-<idx>`.
- **done** (series complete): `line` = completion message, `submit` = Restart, `key` = `done-<idx>`.

Because `glow` is read fresh every engine tick, re-publishing on each render (including timer or
build-step re-renders) keeps the highlight pointed at the current correct control.

## Glow & disclosure by teaching model

Two glow types: the **answer glow** (the correct control for the round) and the **action/input glow**
(Check / Next, or a field that needs typing). The answer glow only appears at "reveal," and when
that happens depends on the model:

| Model | Round 1 shows | Answer glow appears | Taps |
|---|---|---|---|
| **Step-by-step** (`guided`) | the answer immediately | from the start | 0 |
| **Try first** (`ask`) | the hint question only | after one tap of Hint | 1 |
| **Answer on tap** (`ondemand`) | the hint question only | after one tap of Show answer | 1 |
| **Self-explain** (`self`) | the question, then the *why* as a nudge | after a second tap | 2 |

The action/input glow always shows when the round needs it; only the answer glow is gated by the
model. Concept-explorer views (number-line Explore, place-value Explore, sequence browsing) have no
single tap-target, so they narrate + carry the Explain writeup but have no answer glow.

### Glow target per simulation

| Sim | Answer glow | Action / input glow |
|---|---|---|
| 3_1 Number Line (Explore/Sequence) | — (explorer) | — |
| 3_1 Number Line (Quiz) | the correct option | Next |
| 3_1_new Borrow Bridge | — (input answer) | input (the difference) → Check |
| 3_2 Place Value (Explore) | — (explorer) | — |
| 3_2 Place Value (Quiz) | the correct option | Next |
| 3_2_new Closest Target Hunt | the closest option | Check |
| 3_3 Unit Converter (Explore) | — (explorer) | — |
| 3_3 Unit Converter (Quiz) | the correct option | Next |
| 3_3_new Context Decoder | the correct interpretation | Check |
| 3_4 Add & Subtract (Explore) | — (explorer) | — |
| 3_4 Add & Subtract (Quiz) | the correct option | Next |
| 3_4_new Disaster Dispatch | the correct value (option A/B) | — (resolves on tap) |
| 3_5 Compare & Order (Compare) | the correct `<` / `=` / `>` | Next |
| 3_5 Compare & Order (Closest) | the closest option | Next |
| 3_5_new Order Rescue | the smallest remaining token | Check (when all placed) |
| 3_6 Place Value Builder | the digit for the next place | Check (when all placed) |
| 3_7 Place Value Gridlock | the needed digit tile, then its slot | Check (when all placed) |
| 3_8 Sequence Pulse Lab | — (input answer) | input (next term) → Check |
| 3_9 Place Value Shift Lab | ×10 or ÷10 (whichever moves toward target) | Next — only after win/lose (during play only ×10/÷10 glows) |
| 3_10 Unit Conversion Tower | — (input answer) | input (converted value) → Check |

---

## Per-simulation spec

Each entry: concept · coach text (hint → line) · explanation (Explain-panel steps) · unit tests.

### 3_1 · Decimal Number Line — Zoom & Explore (Explore / Sequences / Quiz)
- **Concept:** decimals sit between whole numbers; place value continues as tenths, hundredths, thousandths.
- **Coach (Quiz):** hint "Which is greater: 1.23 or 1.32?" → line "Answer: 1.32." · **Sequence:** "The step is +0.4 each time. What are the next terms?" · **Explore:** narrates `1.4 = 1 unit, 4 tenths`, between 1 and 2.
- **Explanation (Quiz):** Step 1 read the question · Step 2 the sim's `ex` · Step 3 tap the answer · Why — decimals compare by place value; trailing zeros don't change value · Tip — more digits ≠ bigger (0.2 > 0.02).
- **Unit tests:** every QUIZ `ans` is one of its `opts`.

### 3_1_new · Decimal Borrow Bridge
- **Concept:** subtract decimals by aligning points and borrowing.
- **Coach:** hint "Subtract 12.4 − 6.7. Line up the decimal points and borrow." → line "12.4 − 6.7 = 5.7." · glows the input with the answer as placeholder + Check.
- **Explanation:** Step 1 align points, pad with zeros · Step 2 the round's borrow hint · Step 3 subtract column by column to the answer · Why — borrowing moves 1 from the next place (worth 10) · Tip — pad the top number with zeros first.
- **Unit tests:** every round `a − b == ans`.

### 3_2 · Decimal Place Value Explorer (Explore / Quiz)
- **Concept:** a digit's value is its face value × its place (tenths, hundredths, thousandths).
- **Coach (Quiz):** hint "2 ones, 3 tenths and 5 hundredths = ? Which option is right?" → line "Answer: 2.35." · **Explore:** narrates each digit of the browsed number by place.
- **Explanation (Quiz):** Step 1 read the question · Step 2 `ex` (e.g. 2 + 3/10 + 5/100) · Step 3 tap the answer · Why — value = face × place · Tip — a missing place is a zero (4 ones + 6 hundredths = 4.06).
- **Unit tests:** every QUIZ `ans ∈ opts`.

### 3_2_new · Decimal Closest Target Hunt
- **Concept:** compare decimals place by place to find the smallest distance to a target.
- **Coach:** hint "Which option is closest to 1.000?" → line "Closest to 1: 1.010."
- **Explanation:** Step 1 write each option to the same places as the target · Step 2 measure each distance · Step 3 the smallest gap wins · Why — compared left to right, the first differing place decides nearness · Tip — pad with trailing zeros.
- **Unit tests:** the declared `ans` is the option minimising `|opt − target|` (fixed 2 wrong rounds: 0.509, 9.909).

### 3_3 · Decimal Unit Converter (Explore / Quiz)
- **Concept:** metric conversions are powers of ten — a decimal-point shift.
- **Coach (Quiz):** hint "12 mm = ? cm. Which conversion is right?" → line "Answer: 1.2 cm." · **Explore:** narrates "convert by powers of 10 — move the decimal point."
- **Explanation (Quiz):** Step 1 read the conversion · Step 2 `ex` (12 ÷ 10 = 1.2) · Step 3 tap the answer · Why — ÷10/100/1000 shifts the point 1/2/3 places left · Tip — count the zeros in the factor.
- **Unit tests:** every QUIZ `ans ∈ opts`.

### 3_3_new · Decimal Context Decoder
- **Concept:** a decimal's meaning depends on its unit (time, cricket, money, place value).
- **Coach:** hint "4.5 hours means…? Which interpretation is right?" → line "It means '4 hours 30 minutes'."
- **Explanation:** Step 1 read the quantity in context · Step 2 convert the decimal into the context's units · Step 3 tap the answer · Why — the same digits mean different amounts when the unit splits into 10, 60, 100 or 6 · Tip — 0.20 = 0.2, but 0.2 = 10 × 0.02.
- **Unit tests:** every `ans ∈ opts`.

### 3_4 · Decimal Addition & Subtraction (Explore / Quiz)
- **Concept:** line up the decimal points, then add/subtract column by column.
- **Coach (Quiz):** hint "5.3 + 2.6 = ? Line up the decimal points and compute." → line "Answer: 7.9." · **Explore:** narrates the align-and-compute rule.
- **Explanation (Quiz):** Step 1 align points, pad with zeros · Step 2 `ex` (3+6=9 tenths, 5+2=7) · Step 3 tap the answer · Why — aligning keeps tenths under tenths · Tip — 18 is 18.0.
- **Unit tests:** every QUIZ `ans ∈ opts`.

### 3_4_new · Decimal Disaster Dispatch
- **Concept:** real-world unit conversion by powers of ten (choose the safe value).
- **Coach:** hint states the mission context → line "Correct: 0.00005 g." · glows the correct option (A/B).
- **Explanation:** Step 1 read the context · Step 2 the mission's `why` (e.g. 0.05 mg = 0.00005 g) · Step 3 tap the correct value · Why — each unit step is ×/÷ 10, 100 or 1000 · Tip — bigger unit divides, smaller unit multiplies.
- **Unit tests:** every mission `ans ∈ {a, b}`.

### 3_5 · Compare & Order Decimals (Compare / Closest)
- **Concept:** compare decimals place by place; "closest" means smallest distance.
- **Coach (Compare):** hint "Compare 1.23 and 1.32 — which sign fits?" → line "1.23 < 1.32." · **(Closest):** "Which is closest to 1?" → "Closest to 1: 1.01."
- **Explanation:** Step 1 pad to equal places · Step 2 compare left to right / measure distances · Step 3 tap the sign or closest option · Why — the first differing place decides · Tip — trailing zeros don't change value.
- **Unit tests:** every COMPARE `ans == sign(a − b)` and every CLOSEST `ans` is the nearest option (fixed 2 wrong rounds: compare `1.23 < 1.32`, closest `25 → 25.148`).

### 3_5_new · Decimal Order Rescue
- **Concept:** order decimals ascending by comparing place by place.
- **Coach:** hint "Place the decimals in ascending order — which unplaced value is smallest?" → line "Place 0.002 next (smallest remaining)." · glows the smallest tray token, then Check when all placed.
- **Explanation:** Step 1 pad to equal places · Step 2 the smallest unplaced value · Step 3 tap it, repeat smallest → largest, then Check · Why — decimals order by their first differing place · Tip — 0.2 = 0.20; trailing zeros never change value.
- **Unit tests:** shim confirms the smallest-remaining glow logic; every round's tokens are orderable.

### 3_6 · Decimal Place Value Builder Bay
- **Concept:** build a target decimal by placing each digit in its correct place.
- **Coach:** hint "Build 705.34 — which digit goes in the next slot?" → line "Place a 7 in the hundreds slot." · glows the needed digit tile, then Check when full.
- **Explanation:** Step 1 read the target place by place · Step 2 the next empty slot's place needs digit X · Step 3 tap that digit; repeat, then Check · Why — a digit's position sets its meaning (ones vs tenths vs hundredths) · Tip — a 0 still holds a place.
- **Unit tests:** every target's digits are exactly the round's tiles (fixed 2 unbuildable targets: 705.30, 700.05).

### 3_7 · Decimal Place Value Gridlock
- **Concept:** select a digit tile, then lock it into the slot whose place needs it.
- **Coach:** with nothing selected → "Select a 3 for the hundreds place" (glows the tile); with a digit selected → "Tap the hundreds slot to place your 3" (glows the slot).
- **Explanation:** Step 1 the target place needs digit X · Step 2 select that tile · Step 3 tap its slot; repeat, then Check · Why — each place holds exactly one digit; matching digit to place builds the decimal · Tip — a 0 still holds its place.
- **Unit tests:** every target's digits match the round's tiles (fixed 1 unbuildable target: 702.96).

### 3_8 · Decimal Sequence Pulse Lab
- **Concept:** find the constant step, add it to the last term.
- **Coach:** hint "The sequence is 4.40, 4.45, 4.50, … What is the next term?" → line "Next term: 4.55." · glows the input with the answer + Check.
- **Explanation:** Step 1 find the step (+0.05) · Step 2 add it to the last term · Step 3 the next term · Why — an arithmetic sequence adds the same amount each step · Tip — a decreasing sequence subtracts.
- **Unit tests:** every round `last(seq) + step == next`.

### 3_9 · Place Value Shift Lab
- **Concept:** ×10 shifts the decimal point one place right; ÷10 one place left.
- **Coach:** hint "Turn 7.05 into 70.5 using ×10 and ÷10 — which shift moves the right way?" → line "Tap ×10 to grow toward 70.5." · glows ×10 or ÷10 based on current vs target.
- **Explanation:** Step 1 compare current to target · Step 2 ×10 if smaller, ÷10 if bigger · Step 3 repeat until you reach the target · Why — each ×10/÷10 moves every digit one place · Tip — the round's move hint.
- **Unit tests:** every mission's target is reachable from start by whole ×10/÷10 shifts within the move limit.

### 3_10 · Unit Conversion Precision Tower
- **Concept:** convert by the power-of-ten factor, shifting the decimal point.
- **Coach:** hint "Convert 750 cm to m. cm to m: divide by 100." → line "750 cm = 7.5 m." · glows the input with the answer + Check.
- **Explanation:** Step 1 the conversion rule · Step 2 multiply/divide by the factor — shift the point N places · Step 3 the converted value · Why — the metric system scales by tens · Tip — count the zeros in the factor.
- **Unit tests:** every mission `src × factor` is finite and matches the stated conversion.

---

## Scope

English only. The Kannada twins (`math_3_1_kn.html … math_3_5_kn.html`) exist but are **not** wired
to the coach — Kannada coaching is deferred (tracked separately). Everything in this spec applies to
the English `math_3_*.html` sims.

## Unit test approach

Two harnesses run offline (no device needed). They are not committed with the sims; recreate them
from the definitions below (the exact scripts used for this pass live under the working `/tmp`):

1. **Math harness** (`/tmp/v3ch3.js`) — extracts each sim's `ROUNDS/MISSIONS/QUIZ/CASES/COMPARE/
   CLOSEST` array and re-derives every declared answer and computed value: closest-value distances,
   compare signs, decimal subtraction (`a−b==ans`), sequence steps (`last+step==next`), unit
   conversions (`src×factor`), build feasibility (sorted target digits == sorted tiles), ×10/÷10
   reachability within the move limit, and MCQ answer-in-options. **Ch 3: 109 checks, 0 failures**
   after fixing 6 data bugs (3_5 ×2, 3_2_new ×2, 3_6 ×2, 3_7 ×1).
2. **DOM shim** (`/tmp/shim.js`) — a minimal `document`/element stub that runs each sim's inline
   `<script>` render loop headless and asserts `window.__eduRound` is published with `hint`/`why`/
   `detail`. **All 15 sims OK.**

Re-run: `node /tmp/v3ch3.js` and `node /tmp/shim.js math_3_1.html … math_3_10.html` from
`EduAI_app/Simulations`. Also `node --check` each extracted `<script>` for syntax.

## Acceptance / sign-off checklist (per sim)

- [ ] Loads `edu-coach.js` (engine present).
- [ ] Publishes a native `__eduRound` in every phase (start / active / resolved / done).
- [ ] `glow` points at the sim's own correct control (correct by construction).
- [ ] `hint` states the full problem; `why` gives the rule; `detail` has `Step 1/2/3 + Why + Tip`.
- [ ] `voice`/`hintVoice` present; numbers/operators read correctly (no "oh oh oh").
- [ ] Input rounds set `input` + `inputHint`; build rounds re-target `glow` as slots fill.
- [ ] Math harness passes for the sim's data; DOM shim runs without throwing.
- [ ] Live check on the deployed sim across ≥2 rounds and all teaching models.

### Data bugs found and fixed in Ch 3
- `3_5` Compare: `1.23 ? 1.32` answer was `>`, corrected to `<`.
- `3_5` Closest: target 25 answer was `24.815`, corrected to `25.148` (the actual nearest).
- `3_2_new`: closest answers for targets 0.5 and 9.9 were off by one option → `0.509`, `9.909`.
- `3_6`: targets `70.53` and `7.05` can't fit the fixed 3-whole + 2-decimal builder → `705.30`, `700.05`.
- `3_7`: target `72.96` (4 digits) can't fill 5 place slots → `702.96`.
