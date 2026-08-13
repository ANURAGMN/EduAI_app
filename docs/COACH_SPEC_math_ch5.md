# Math Chapter 5 — Parallel & Intersecting Lines · Coach Plan

**Status: PLAN → building.** Design spec to sign off before/with implementation. None of the 7 Ch 5
sims load `edu-coach.js` yet, so the build has two parts per sim: (1) wire the engine
(`<script src="edu-coach.js"></script>` before `</body>`), and (2) add the native `window.__eduRound`
hooks below. Same shape as Ch 2 / 3 / 4.

**This revision corrects the spec to match the actual sims** (per review): 5_1 is set-only (no
classify), 5_5 locks the *corresponding* relation only, 5_6 needs true dual-glow, and the slider/drag
sims use a **stable round key** (not a per-degree key). See "Corrections applied" at the end.

The chapter is **geometry** (angles, angle relations, parallel lines cut by a transversal). Interaction
mix: **slider / drag-to-target** angle setting (5_1, 5_2, 5_4, 5_5), one **build-then-classify MCQ**
(5_3), one **multi-select chip pair** (5_6), and one **place-value click race** (5_7, off-theme —
Number Play). No free-text, no token assembly.

## How the coach works (contract)

Each round the sim publishes `window.__eduRound`. Field reference is
[`docs/EDU_ROUND_CONTRACT.md`](./EDU_ROUND_CONTRACT.md); that doc still describes the older V4 scrape
fields, so treat **the V5 extended fields below as authoritative — they are what `edu-coach.js`
actually reads** (`present()` / `renderReveal()`):

| Field | Purpose |
|---|---|
| `native:true` | sim-authored round (engine prefers it over scraping) |
| `hint` / `hintVoice` | the question (full problem) + its spoken form |
| `why` | one-line rule being used (the angle relation) |
| `detail` | Photomath steps (`Step 1/2/3 · Why it works · Tip`) for the Explain panel |
| `line` / `voice` | the reveal + its spoken form; **`line` may update live each tick** (bar re-renders on text change) |
| `glow` (+ `glowKind`) | correct control. **Accepts a single element _or an array_** (`glow:[elA, elB]`) — engine glows each (added for 5_6) |
| `submit` | Check / Next button (action glow) |
| `input` / `inputHint` | field + placeholder (unused in Ch 5 — no free-text) |
| `key` | round identity; changing it resets disclosure **and** re-speaks. Keep it **stable per round.** |

## Key policy for live-manipulation sims (slider / drag / place-value)

`emit(text, voice, vkey)` sets the bar whenever `text` changes but only speaks when `vkey` changes; the
reveal uses `vkey = 'R:'+key`. Therefore:

- Use a **stable per-round key** — `r-<idx>` (or `m-<idx>`). Do **not** fold the live angle into the key.
- Put the live nudge ("a few degrees more / less", current value) in **`line`** only. The bar updates
  live as the learner drags; the voice does **not** re-speak every degree, and disclosure does not reset.
- The glow is re-applied every tick from the current `__eduRound`, so the glow can follow the live state
  (e.g. flip slider → Check once inside tolerance) without touching the key.

## The four teaching models ("hint for 4 variations")

Only the **reveal + answer glow** are gated; the **action glow** (Check / Next) and the **manipulation
glow** (slider handle, draggable point) show whenever the round needs the learner to act.

| Model (`key`) | Round shows first | When the answer + glow appear | Taps |
|---|---|---|---|
| **Try first** (`ask`) | the hint question only | after one tap of **Hint** | 1 |
| **Step-by-step** (`guided`) | the target/answer + glow immediately | from the start | 0 |
| **Self-explain** (`self`) | the question, then the *why* (the relation) as a nudge | after a second tap | 2 |
| **Answer on tap** (`ondemand`) | the hint question only | after one tap of **Show answer** | 1 |

Worked example (5_2, round `corresponding = 46°`):
- **Try first** — "Slide the transversal angle so the corresponding angle is 46°."; Hint reveals "46°, corresponding angles are equal" and glows the slider + Check.
- **Step-by-step** — reveal + glow immediately.
- **Self-explain** — question → nudge ("corresponding angles are *equal*") → reveal.
- **Answer on tap** — question with a "Show answer" chip → one tap reveals.

---

## Per-simulation plan

### 5_1 · Angle Chase Bridge — set the angle (slider, no classify)
- **Actual interaction:** slider `#slider` + ±1/±5 nudge; move the red bridge angle to `t` within `tol` = 2°. `ROUNDS = [45, 68, 92, 125, 138]` (no 90°). **No acute/right/obtuse UI** — it is set-only.
- **Concept:** read a protractor-style angle and set it precisely to a target measure.
- **Coach:** hint "Swing the red bridge to 45°." → line (live) "You're at 52° — nudge down about 7°." → at target "45° — locked. Tap Check."
- **Explanation:** Step 1 read the current angle off the beam · Step 2 use ±5 to get close, ±1 to fine-tune · Step 3 stop within 2° of 45° · Why — a full turn is 360°, a straight angle 180°, a right angle 90°; here you're matching an exact measure · Tip — the ±1 buttons land you inside the 2° window.
- **Glow:** the slider handle; **Check** glows once within tolerance. Stable key `r-<idx>`; live nudge in `line`.
- **Unit tests:** each `t ∈ [5,175]` (slider range) and reachable; **no** acute/obtuse assertion.

### 5_2 · Angle Relation Signal Tower — transversal by relation (slider + drag)
- **Actual interaction:** drag on the diagram + slider `#xSlider` + nudge; set the transversal angle so the named relation equals `target`. `MISSIONS = [corresponding 46, alternate 72, interior 112, corresponding 128, alternate 94]`, `tol` = 2°.
- **Concept:** across a transversal, **corresponding** and **alternate** angles are *equal*; **co-interior** (same-side interior) angles are *supplementary* (sum 180°).
- **Coach:** hint "Slide so the *corresponding* angle reads 46°." → line "Set it to 46° — corresponding angles are equal."
- **Explanation:** Step 1 identify the relation (corresponding / alternate / co-interior) · Step 2 apply it — equal → set the angle to the target; co-interior → set it so the same-side pair sums to 180 (angle = 180 − target) · Step 3 fine-tune within 2° · Why — parallel lines force corresponding/alternate equal and co-interior supplementary · Tip — "interior, same side" is the only one that adds to 180.
- **Glow:** slider handle + Check. Stable key `m-<idx>`; live nudge in `line`.
- **Target mapping (confirm against `check()` at build):** corresponding/alternate → x = target; interior (112) → x = 68 so the same-side pair = 112.
- **Unit tests:** each required angle is inside `[10,170]`; the co-interior case sums to 180°.

### 5_3 · Line Relation Sort Yard — build, then classify (drag + MCQ)
- **Actual interaction:** **two phases.** (1) **Build** — drag the `.drag` endpoints to create the target relation. (2) **Classify** — tap one of three `.choice` buttons (`parallel` / `intersecting` / `perpendicular`); `check()` grades `st.pick`. `ROUNDS` targets `[parallel, intersecting, perpendicular, parallel, perpendicular, intersecting]`.
- **Concept:** two lines are **parallel** (never meet), **perpendicular** (meet at 90°), or plainly **intersecting** (meet at one slanted point).
- **Coach:**
  - **Build phase** (before a pick): line "Drag the endpoints to make the lines *parallel*, then choose the label." (no answer glow yet — the learner is constructing).
  - **Classify phase:** hint "Which relation did you build?" → glow the choice matching `target` → line "Parallel — they never meet and stay the same distance apart."
- **Explanation:** Step 1 drag to build the asked relation · Step 2 check where the lines meet — nowhere (parallel), at 90° (perpendicular), at one slanted point (intersecting) · Step 3 tap that label · Why — direction decides it · Tip — perpendicular *is* intersecting, but the 90° case gets its own name.
- **Glow:** the `.choice` matching `target`; Check. Key `r-<idx>`.
- **Unit tests:** each `target ∈ {parallel, intersecting, perpendicular}` maps to exactly one choice.

### 5_4 · Parallel Line Angle Hunt — drag transversal to a target angle
- **Actual interaction:** drag the transversal endpoint (`dragA` / `dragB`) so the named angle = `target`. `ROUNDS = [corresponding 60, alternate 45, linear 120, corresponding 75, alternate 30, linear 100]`.
- **Concept:** with parallel lines + transversal, **corresponding** and **alternate interior** angles are equal; a **linear pair** is supplementary (180°).
- **Coach:** hint "Drag so the corresponding angle is 60°." → line (live) "Now 68° — drag down about 8°." → "60° — corresponding angles are equal. Tap Check."
- **Explanation:** Step 1 read the label (corresponding / alternate / linear pair) · Step 2 equal relations → drag to the target; linear pair → drag this angle to the target while its neighbour makes 180° · Step 3 stop when the readout hits the target · Why — parallel lines keep corresponding/alternate equal; a straight line splits into two angles summing to 180° · Tip — for a linear pair the partner = 180 − target.
- **Glow:** the draggable endpoint handle + Check. Stable key `r-<idx>`; live nudge in `line`.
- **Unit tests:** each target achievable by dragging; linear-pair partner = 180 − target.

### 5_5 · Parallel Proof Mission Grid — set the corresponding angle that proves parallel
- **Actual interaction:** slider `#aSlider` + nudge; set the angle to `target`. `MISSIONS = [35, 52, 78, 110, 145]`, `tol` = 2°. **The sim locks the *corresponding* relation only** — no alternate / co-interior branch.
- **Concept:** the **converse** — if the **corresponding** angles are equal, the lines *must* be parallel. Setting the angle to the target makes them equal and proves it.
- **Coach:** hint "Set the angle to 35° so the corresponding angles match and the lines are proved parallel." → line "35° — equal corresponding angles ⇒ parallel."
- **Explanation:** Step 1 the mission asks you to make the **corresponding** angles equal · Step 2 set the angle to the target so they match · Step 3 confirm within 2° · Why — the converse of the corresponding-angles rule: equal corresponding angles force the lines parallel · Tip — this is the reverse of 5_2, and here it's always the corresponding pair.
- **Glow:** slider handle + Check. Stable key `m-<idx>`; live nudge in `line`.
- **Unit tests:** each target ∈ `[5,175]`; **coach copy names only the corresponding relation** (no alternate/co-interior claims).

### 5_6 · Transversal Pair Match Lab — select the angle pair (dual glow)
- **Actual interaction:** select **two** angle chips (`.chip[data-v]`, `st.picks` length 2); `check()` grades `sameSet(picks, r.pair)`. All 6 rounds are pairs.
- **Concept:** name the pair by position — **corresponding** (same corner at each crossing), **alternate interior** (opposite sides of the transversal, between the lines), **co-interior** (same side, between the lines, supplementary).
- **Coach:** hint "Tap the two angles that are *corresponding*." → line "a1 and a2 — same position at each intersection."
- **Explanation:** Step 1 read the relation asked · Step 2 find the two angles in that position — corresponding = same corner; alternate interior = criss-cross between the lines; co-interior = same side between the lines · Step 3 select exactly those two · Why — the transversal makes 8 angles in matched positions; the name is the position, not the size · Tip — co-interior is the only "between + same side" pair, and it's supplementary.
- **Glow:** **both** chips via `glow: [chipA, chipB]` (engine now iterates arrays); Check once the correct two are picked.
- **Unit tests (all 6 rounds):**

  | Round | `ask` | `pair` | glow chips |
  |---|---|---|---|
  | 1 | corresponding | a1, a2 | a1 + a2 |
  | 2 | alternate interior | a3, a6 | a3 + a6 |
  | 3 | co-interior | a3, a5 | a3 + a5 |
  | 4 | corresponding | a4, a8 | a4 + a8 |
  | 5 | alternate interior | a4, a5 | a4 + a5 |
  | 6 | co-interior | a4, a6 | a4 + a6 |

  Each pick-set `== pair` grades correct; both chips in `pair` receive the glow.

### 5_7 · Digit Sum Click Race — place-value build (off-theme: Number Play)
- **Note:** this is a **Number Play** activity (min clicks = digit sum), not parallel-lines geometry; it sits at `5_7` in the current set. Flag for the team if it should move to Ch 6.
- **Actual interaction:** place-value buttons `.btnpv[data-v]`, `PV = [100000,10000,1000,100,10,1]`; build `TARGETS = [5072, 8300, 40629, 56354, 66666, 367813]` in the fewest clicks (= digit sum).
- **Concept:** each press adds that place's value; the fewest presses to build a number equals the **sum of its digits** (greedy: always add the biggest place that still fits).
- **Coach:** hint "Build 5,072 in the fewest taps. Which button next?" → line "Press +1,000 (five times), +10 (seven), +1 (two) — 14 taps = 5+0+7+2." (uses the round's real target.)
- **Explanation:** Step 1 look at the biggest place still needed · Step 2 press that place-value button until that digit is filled · Step 3 move down a place; repeat to the ones · Why — each digit needs exactly its own count of presses, so total = digit sum · Tip — never overshoot; biggest-first is always the minimum.
- **Glow:** the **largest `PV` button that still fits** the remaining amount (re-computed each tick from the current build); Check when the build equals the target. Key `r-<idx>` (glow follows the live remaining amount without churning the key).
- **Unit tests (real targets):** greedy build reproduces each `TARGETS[i]`; min click count = `digitSum(TARGETS[i])` (e.g. 5072 → 14, 8300 → 11, 66666 → 30, 367813 → 28).

---

## Build order

1. **Slider / drag-to-target** (5_1, 5_2, 5_4, 5_5) — manipulation glow on slider/handle, live nudge in `line`, Check glows within tolerance, **stable round key**.
2. **Build-then-classify + chip pair** (5_3 two-phase MCQ, 5_6 dual-glow pair).
3. **Place-value** (5_7) — greedy next-button glow.

Per sim: (1) add `<script src="edu-coach.js"></script>` before `</body>`; (2) insert the native
`window.__eduRound` block at the end of `render()` (and in the Check handler for resolved/next), reading
target/relation/pair from the sim's own data so nothing is duplicated.

## Verification (before push to `main`)

- `node --check` on each extracted `<script>` — all 7 parse; plus `edu-coach.js` (multi-glow change).
- DOM shim: each sim publishes a valid `window.__eduRound{native:true, …}` in each state (question →
  build/resolved → next); 5_6 publishes `glow` as a **two-element array**.
- Geometry harness re-derives: 5_1 targets reachable (no classify); 5_2/5_4/5_5 the equal/supplementary
  target per relation (and 5_5 corresponding-only); 5_3 classify target; **all 6** 5_6 pairs; 5_7 greedy
  build = digit sum for the **real** TARGETS.
- Engine regression: re-run the Ch 4 shim so the array-glow change doesn't affect single-element glow.
- Live Chrome review (iframe harness on the deployed origin): coach bar renders; slider/handle/choice/
  chips/PV button carry the glow; **5_6 glows both chips**; dragging updates the bar **without** per-tick
  re-speak or disclosure reset.
- Deploy note: GitHub Pages serves **`main`** — the commit must land on the remote Pages reads.

## Corrections applied (from review)

- **5_1** rewritten to **set-only** — removed the acute/right/obtuse "classify" story from coach + unit
  tests (the live sim has no such UI; rounds have no 90°).
- **5_5** narrowed to the **corresponding** relation only (was overclaiming alternate / co-interior).
- **5_6** dual-glow: added **array `glow` support to `edu-coach.js`** (single element still works); pair
  table expanded to **all 6** rounds.
- **Key policy** for 5_1/5_2/5_4/5_5 (+5_7): **stable round key**, live nudge in `line` only — avoids
  per-degree disclosure reset and per-tick re-speak (verified against `emit()`).
- **5_3** now spells out the **build (drag) phase** before the classify glow.
- **5_7** example uses a **real target** (5,072 from `TARGETS`), still flagged off-theme.
- **Contract**: pointed at `EDU_ROUND_CONTRACT.md` and noted the V5 extended fields here are the ones
  `edu-coach.js` actually reads.
