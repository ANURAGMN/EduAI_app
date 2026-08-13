# Math Chapter 7 — A Tale of Three Intersecting Lines (Triangles & Angles) · Coach Plan

**Status: BUILT (hooks on `main`).** Spec + implementation for Ch 7 (A Tale of Three Intersecting Lines —
Triangles & Angles). Each of the 9 sims loads `edu-coach.js` and publishes native `window.__eduRound`
(over / resolved / active). Same shape as the Ch 5 / Ch 6 specs (contract · four models · per-sim plan ·
verification).

Chapter is **geometry of intersecting lines and triangles**: angle pairs, angles on a line / around a
point, exterior angle, intersection angles, perpendicular drop, supplementary angles, triangle angle
sum, congruence criteria, and the triangle inequality. Interaction mix: **single slider / drag-to-target**
(7_1, 7_2, 7_3, 7_4, 7_5, 7_6), **two/three sliders** (7_7 vertex, 7_9 three sides — array glow), and one
**classify MCQ** (7_8 congruence). No free-text.

The **array multi-glow** (`glow:[a,b]`, added in Ch 5) is reused here for 7_7 (two vertex sliders) and
7_9 (three side sliders).

## How the coach works (contract)

Native round fields (authoritative = what `edu-coach.js` reads; see `EDU_ROUND_CONTRACT.md` for the
older V4 scrape fields):

| Field | Purpose |
|---|---|
| `native:true` | sim-authored round |
| `hint` / `hintVoice` | the question + spoken form |
| `why` | the rule (angle relation / congruence / inequality) |
| `detail` | Photomath steps (`Step 1/2/3 · Why · Tip`) |
| `line` / `voice` | reveal; **`line` updates live each tick** (bar re-renders on text change) |
| `glow` (+ `glowKind`) | correct control — **single element _or an array_** (`glow:[a,b]`, used by 7_7 & 7_9) |
| `submit` | Check / Next (action glow) |
| `key` | round identity — keep **stable per round** (avoids disclosure reset / re-speak) |

## Key policy for live-manipulation sims (sliders / drag)

`emit()` re-renders the bar whenever `text` changes but only re-speaks when `vkey` changes, and the
reveal uses `vkey = 'R:'+key`. So:

- Use a **stable per-round key** (`m-<idx>` / `r-<idx>`); do **not** fold the live angle into the key.
- Put the live nudge ("a few degrees more / less", current value, current error) in **`line`** only —
  the bar updates live, the voice doesn't re-nag, and disclosure doesn't reset.
- The glow re-applies every tick, so it can flip slider → Check once within tolerance without touching
  the key.

## The four teaching models ("hint for 4 variations")

Only the reveal + answer glow are gated; the action glow (Check/Next) and manipulation glow (slider /
drag handle) show whenever the round needs an action.

| Model (`key`) | Shows first | Answer + glow appear | Taps |
|---|---|---|---|
| **Try first** (`ask`) | the question | after one **Hint** | 1 |
| **Step-by-step** (`guided`) | target/answer + glow immediately | from the start | 0 |
| **Self-explain** (`self`) | question → *why* nudge | after a second tap | 2 |
| **Answer on tap** (`ondemand`) | the question | one **Show answer** | 1 |

Worked example (7_6, round `target 52°`):
- **Try first** — "Slide angle a so a and its partner add to 180°. What's a?"; Hint reveals "52° (partner 128°)" and glows the slider + Check.
- **Step-by-step** — reveal + glow immediately.
- **Self-explain** — question → nudge ("supplementary angles sum to 180°") → reveal.
- **Answer on tap** — question with a "Show answer" chip.

---

## Per-simulation plan

### 7_1 · Angle Pair Builder Dock (slider, vertical / linear)
- **Actual interaction:** slider `#aSlider` + ±nudge + drag; set the angle so the named pair equals `target`. `MISSIONS = [vertical 42 · linear 120 · vertical 75 · linear 98 · vertical 134]`, `tol 2`. `pairVal`: vertical → the angle itself (`v.a`, equal); linear → its partner (`v.b = 180 − a`).
- **Concept:** **vertically opposite** angles are equal; a **linear pair** is supplementary (sum 180°).
- **Coach:** hint "Set the angle so the vertically-opposite angle is 42°." → line "42° — vertically opposite angles are equal." For a linear round: "linear partner = 120°, so set the angle to 60°."
- **Explanation:** Step 1 read whether the target is vertical or linear · Step 2 vertical → equal, so set the angle to the target; linear → partner = 180 − angle, so set the angle to `180 − target` · Step 3 fine-tune within 2° · Why — crossing lines make equal vertical angles and supplementary linear pairs · Tip — a straight line splits into two angles that add to 180°.
- **Glow:** `#aSlider` + Check; stable key `m-<idx>`; live nudge in `line`.
- **Target mapping (confirm against `pairVal`/`check`):** vertical → set `a = target`; linear → set `a = 180 − target` (so `v.b = target`).
- **Unit tests:** vertical targets reachable directly; linear targets reachable via `180 − target` in the slider range.

### 7_2 · Angle Sum Puzzle Wheel (slider to exact x)
- **Actual interaction:** slider `#xRange` + ±1/±5 nudge; set `x` so the angles sum to `target` (`check` needs `x === goal`, exact). `ROUNDS`: line rounds sum to 180° (e.g. 65 + 40 + x, goal 75), around-point rounds sum to 360° (e.g. 120 + 95 + x, goal 145).
- **Concept:** angles **on a straight line** sum to 180°; angles **around a point** sum to 360°.
- **Coach:** hint "65° + 40° + x = 180°. What is x?" → line "x = 75° (180 − 65 − 40)."
- **Explanation:** Step 1 note the total: a straight line is 180°, around a point 360° · Step 2 subtract the known angles: `goal = target − fixedA − fixedB` · Step 3 set x to that exact value, then Check · Why — the known angles plus x must fill the whole line / turn · Tip — this one needs the exact integer, so use ±1 to land on it.
- **Glow:** `#xRange` + Check; live nudge in `line` ("x = 70, need 75 — add 5").
- **Unit tests:** `goal === target − fixedA − fixedB` for each round, and `goal` is inside the slider range.

### 7_3 · Exterior Angle Workshop (drag + nudge, no single slider)
- **Actual interaction:** drag vertex `C` on the SVG + precision nudge buttons; set the triangle so the **exterior angle at A** (`geom().extA`) equals `target`. `MISSIONS = [120, 135, 150, 110, 98]`, `tol 2`.
- **Concept:** an **exterior angle** of a triangle equals the **sum of the two remote interior angles** (and the interior + exterior at a vertex make 180°).
- **Coach:** hint "Shape the triangle so the exterior angle at A is 120°." → line (live) "Exterior angle is 108° — open it about 12° more."
- **Explanation:** Step 1 the exterior angle at A = the two interior angles at B and C added · Step 2 drag C (or nudge) to grow/shrink those interior angles · Step 3 stop when the exterior angle reads the target (±2°) · Why — exterior + adjacent interior = 180°, and the interior angles sum to 180°, so exterior = the other two · Tip — moving C changes B and C together; watch the readout.
- **Glow:** since there's no single slider, glow **Check** once within tolerance; the manipulation guidance (which way to drag) lives in `line`. Stable key `m-<idx>`.
- **Unit tests:** each `target` is achievable by the drag range; exterior = 180 − interior-at-A holds in `geom()`.

### 7_4 · Intersection Angle Forge (arm slider)
- **Actual interaction:** slider `#armSlider` + `.nbtn` nudge + drag; rotate an arm so the marked angle `∠a` (`angs().a`) equals `targetA`. `MISSIONS = [35, 60, 90, 120, 145, 72]`, `tol 2`.
- **Concept:** two intersecting lines make two pairs of **equal vertical angles** and adjacent **linear pairs** (180°); setting one angle fixes all four.
- **Coach:** hint "Rotate the arm so ∠a is 60°." → line (live) "∠a is 52° — turn it about 8° more."
- **Explanation:** Step 1 the four angles at the crossing are two vertical pairs · Step 2 rotate the arm to grow ∠a toward the target · Step 3 stop within 2° · Why — vertical angles are equal, adjacent ones supplementary · Tip — at 90° all four are right angles.
- **Glow:** `#armSlider` + Check; stable key `m-<idx>`; live nudge in `line`.
- **Unit tests:** each target lies in the arm's degree range.

### 7_5 · Perpendicular Drop Constructor (position slider → 90°)
- **Actual interaction:** slider `#tSlider` (foot position along a track) + `#centerBtn`; slide the foot so the drop from point `p` meets the line at a **right angle** (`currentGeometry().err` → 0 within `tol`). `MISSIONS = [lineDeg 18 · 42 · 75 · 110 · 145]`.
- **Concept:** the **perpendicular** from a point to a line meets it at 90°, at the **foot** — the closest point on the line.
- **Coach:** hint "Slide the foot until the drop is perpendicular (90°)." → line (live) "Angle is 96° — keep sliding to reduce the error toward 90°." *(Say "reduce the error"; do **not** hardcode left/right — the correct direction depends on `lineDeg` and the foot position, so compute it from `currentGeometry()` if a direction is stated at all.)*
- **Explanation:** Step 1 the drop is perpendicular when the angle to the line is exactly 90° · Step 2 slide the foot; the angle passes through 90° at the closest point · Step 3 stop when the error is ~0 (±2°) · Why — the shortest distance from a point to a line is along the perpendicular · Tip — the foot is directly "below" the point relative to the line.
- **Glow:** `#tSlider` + Check; stable key `m-<idx>`; live nudge (error + direction) in `line`.
- **Unit tests:** each mission has a foot position where `err ≤ tol` (a true perpendicular exists).

### 7_6 · Supplementary Sync Station (slider to target)
- **Actual interaction:** slider `#slider` + ±nudge; set angle `a` to the target so `a + (180 − a) = 180`. `MISSIONS = [35, 52, 68, 95, 121]`, `tol 2`.
- **Concept:** **supplementary** angles sum to 180°; set the first, and the partner is `180 − a`.
- **Coach:** hint "Slide angle a so a and its partner add to 180°. What is a?" (Try-first shows only the question — no degrees) → line "52° (partner 128°, so 52° + 128° = 180°)."
- **Explanation:** Step 1 supplementary means the two angles add to 180° · Step 2 set `a` to the target; the partner auto-becomes `180 − a` · Step 3 confirm within 2° · Why — a straight angle is 180°, split into the pair · Tip — 90° is its own supplement's equal (90° + 90°).
- **Glow:** `#slider` + Check; stable key `m-<idx>`; live nudge in `line`.
- **Unit tests:** each target ∈ slider range; partner `180 − target` computed correctly.

### 7_7 · Triangle Angle Sum Builder (two vertex sliders — array glow)
- **Actual interaction:** **two sliders** `#xSlider` + `#ySlider` move vertex `C`; shape the triangle so `∠C` (`geo().c`) equals `targetC`. `MISSIONS = [40, 55, 70, 85, 100]`, `tol 2`.
- **Concept:** the three angles of a triangle always sum to **180°**; moving C changes `∠C` (and the others compensate).
- **Coach:** hint "Move C so ∠C is 40°." → line (live) "∠C is 52° — bring C in to shrink it toward 40°."
- **Explanation:** Step 1 ∠A + ∠B + ∠C = 180° always · Step 2 slide C (x and y) to open/close ∠C · Step 3 stop when ∠C hits the target (±2°) · Why — the angle sum is fixed, so changing one angle shifts the others · Tip — nudge C in small steps and watch ∠C; the two sliders move C in x and y (confirm which mostly changes ∠C against `geo()` at build).
- **Glow:** both sliders `glow: [#xSlider, #ySlider]` (array) + Check; stable key `m-<idx>`; live nudge in `line`.
- **Unit tests:** each `targetC` is reachable within the x/y slider ranges (∠C spans the target).

### 7_8 · Triangle Congruence Detector (classify MCQ)
- **Actual interaction:** choose the congruence rule from `RULES = ['SSS','SAS','ASA','RHS','Not Congruent']` (`.opt` buttons, `st.pick`); `check()` compares `RULES[pick]` to `r.ok`. `ROUNDS = [SSS→SSS · SAS→SAS · ASA→ASA · RHS→RHS · AAA→Not Congruent]`.
- **Concept:** two triangles are congruent by **SSS, SAS, ASA, or RHS**; **AAA is not** a congruence rule (equal angles only give similar, not congruent, triangles).
- **Coach:** hint "Which rule proves these triangles congruent — or are they not congruent?" → line "SSS — all three sides equal."; for the AAA round: "Not Congruent — equal angles alone don't fix the size."
- **Explanation:** Step 1 read which parts are marked equal (sides S, angles A, right angle R + hypotenuse H) · Step 2 match to a rule: SSS / SAS / ASA / RHS · Step 3 if only angles match (AAA), it's Not Congruent · Why — three sides, or two sides + included angle, etc., pin the triangle exactly; three angles don't fix size · Tip — the equal parts must be in the right positions (the "included" side/angle).
- **Glow:** the `.opt` whose index = `RULES.indexOf(r.ok)`; **`submit` (Check) glows only after a choice is picked** (`st.pick !== null`), same pattern as the other MCQs.
- **Unit tests:** `r.ok ∈ RULES` for every round; AAA maps to `Not Congruent`.

### 7_9 · Triangle Inequality Builder (three side sliders — array glow)
- **Actual interaction:** **three sliders** `#aIn`, `#bIn`, `#cIn` (sides a, b, c, 1–12); set them so the triangle is **valid** or **invalid** per the round's target. `valid()` = `a+b>c && a+c>b && b+c>a`. `MISSIONS` alternate `valid / invalid`.
- **✔ Sim change (seed the opposite state — option A):** the original `setup()` seeded each round **already in its target state** (valid mission started at 6,5,7 which is already valid), so the learner could Check with no exploration. `setup()` is reseeded so each round **starts in the opposite state** — a valid mission starts from an invalid triple (e.g. 2,3,6) and an invalid mission from a valid triple (e.g. 6,5,7) — forcing a real adjustment before Check.
- **Concept:** the **triangle inequality** — a triangle is possible only if **each side is less than the sum of the other two**. If one side ≥ the sum of the other two, it can't close.
- **Coach:**
  - **Target valid:** hint "Set a, b, c so the sides can form a triangle." → line "Valid — each side is less than the sum of the other two (e.g. 6, 5, 7)."
  - **Target invalid:** hint "Set a, b, c so the sides can't form a triangle." → line "Invalid — make one side ≥ the other two combined (e.g. 2, 3, 6: 2 + 3 < 6)."
- **Explanation:** Step 1 test all three: a+b>c, a+c>b, b+c>a · Step 2 for valid, keep every pair-sum bigger than the third side; for invalid, make one side ≥ the sum of the other two · Step 3 Check when the state matches the target · Why — the two shorter sides must be able to reach across the longest · Tip — the check that matters is (two smallest) vs (largest).
- **Glow:** all three sliders `glow: [#aIn, #bIn, #cIn]` (array) + Check; stable key `m-<idx>`; live nudge in `line` (current valid/invalid state).
- **Unit tests:** `valid()` matches the triangle inequality; each target state (valid/invalid) is reachable in the 1–12 range; **the reseeded start is the *opposite* of the target** (the learner must change ≥1 side); the seed triples classify correctly (6,5,7 valid; 2,3,6 invalid). Tests must not rely on the start already matching the target.

---

## Build order

1. **Single slider-to-target** (7_1, 7_2, 7_4, 7_5, 7_6) — glow the slider, live nudge in `line`,
   Check within tolerance, stable key.
2. **Multi-slider (array glow)** (7_7 two, 7_9 three) — `glow:[…]` all sliders + Check.
3. **Drag-only + MCQ** (7_3 exterior-angle drag → glow Check + line nudge; 7_8 congruence → glow the
   correct `.opt`).

Per sim: (1) add `<script src="edu-coach.js"></script>` before `</body>`; (2) insert the native
`window.__eduRound` block at the end of `render()` (and in the Check handler for resolved/next), reading
target/answer from the sim's own helpers (`pairVal`, `geom`, `angs`, `geo`, `valid`, `RULES`) so nothing
is duplicated.

## Verification (before push to `main`)

- `node --check` on each extracted `<script>` (9 sims).
- DOM shim: each publishes a valid `window.__eduRound{native:true, …}` per state; 7_7 and 7_9 publish
  `glow` as an **array** (2 and 3 elements).
- Geometry harness re-derives: 7_1 vertical/linear mapping, 7_2 `goal = target − fixedA − fixedB`,
  7_6 supplement `180 − a`, 7_8 rule→answer (AAA = Not Congruent), 7_9 `valid()` = triangle inequality
  (seeded 6,5,7 valid / 2,3,6 invalid), and that every slider target lies in range.
- Engine: array multi-glow already in `edu-coach.js` (from Ch 5) — no new engine change expected.
- Live Chrome (iframe harness, chunked payloads): coach bar renders; slider / two-slider / three-slider /
  drag-Check / `.opt` glow lands correctly; dragging updates the bar without per-tick re-speak.
- Deploy note: GitHub Pages serves **`main`**.

## Corrections applied (from review)

- **7_9 — option A (seed the opposite state):** `setup()` reseeded so each round starts in the state
  **opposite** its target (valid mission ← invalid triple, and vice versa); unit tests no longer rely on
  the start already matching the target.
- **7_5 — direction from geometry:** coach says "reduce the error toward 90°"; **no hardcoded left/right**
  (the correct direction depends on `lineDeg` + foot; compute from `currentGeometry()` if stated).
- **7_8 — Check after pick:** `submit` glows only once `st.pick !== null` (same as the other MCQs).
- **7_7 — tip softened:** don't assert which slider changes ∠C; confirm against `geo()` at build.

## Open items for sign-off

- **7_3** has no single manipulation control (drag + nudge) — confirmed **Check-glow + `line` steering**
  is acceptable; a nudge-button glow is optional.
- Confirm `7_1` linear mapping (`a = 180 − target`) and `7_7` slider→∠C mapping against the sim helpers
  at build time.
