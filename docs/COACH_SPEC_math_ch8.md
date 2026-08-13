# Math Chapter 8 — Working with Fractions · Coach Plan

**Status: BUILT (hooks + engine wiring on all 9 sims; verified node/shim/harness + Chrome; ready to
commit).** Teaching models and per-sim coach shapes signed off; the three blocking pre-solved-board fixes
(8_9 reseed `n=1,d=den===2?3:2`; 8_4 reseed `pos=0.08`; 8_5 softened legend) and the resolved glow
decisions are implemented (see "Corrections / open items" at the end and the ⚠ notes per sim). All 9 Ch 8
sims now load `edu-coach.js` and publish native `window.__eduRound` rounds. The build had two parts per
sim: (1) wire the engine (`<script src="edu-coach.js"></script>` before `</body>`), and (2) add the native
`window.__eduRound` hooks below. Same shape as the Ch 5 / 6 / 7 specs.

> **Corrections to apply during build (blocking — from review).** Three sims start pre-solved and must be
> reseeded/altered before hooking, exactly like Ch 7's 7_9:
> 1. **8_9** — `setup()` seeds `st.n = whole×den+num; st.d = den`, i.e. the answer. Check passes with **zero**
>    nudges. Reseed to a wrong start so the learner must adjust: `st.n = 1; st.d = (r.den===2 ? 3 : 2);`
>    (numerator always low, denominator wrong except where it coincidentally isn't) — then glow the
>    directional nudge buttons (`#nPlus`/`#nMinus`, `#dPlus`/`#dMinus`), not just Check.
> 2. **8_4** — `setup(){ st.pos = 0.5 }` and round 0's target is **1/2**, so round 0 is already solved.
>    Reseed the marker away from target: `st.pos = 0.08;` (applies to every round; round 0 stops being free).
> 3. **8_5** — the board renders `Target count = ${need()}`, printing the answer and making the coach's
>    "how many?" redundant. **Soften the legend** (e.g. "Fill the right number of cells") and make the coach
>    **count-only in `line`** (no cell glow — see 8_5 below).
>
> Chapter is **fractions**: equivalent fractions, comparing, building equivalents, placing on a numberline,
fraction of a quantity, the four operations, simplifying, and mixed ↔ improper. Interaction mix:
**multi-select set** (8_1, 8_5), **slider/marker-to-target** (8_3, 8_4, 8_7), **relation / answer MCQ**
(8_2, 8_6, 8_8), and **±nudge to a target** (8_9). The **array multi-glow** (`glow:[a,b]`, already on
`main`) is reused for 8_1 (all equivalent tiles), 8_3 (numerator slider + input), and 8_5 (the cells to
fill).

## How the coach works (contract)

Native round fields (authoritative = what `edu-coach.js` reads; see `EDU_ROUND_CONTRACT.md` for older
V4 scrape fields):

| Field | Purpose |
|---|---|
| `native:true` | sim-authored round |
| `hint` / `hintVoice` | the **question only** (never the answer) |
| `why` | the rule (equivalence / common denominator / simplify / mixed↔improper) |
| `detail` | Photomath steps (`Step 1/2/3 · Why · Tip`) |
| `line` / `voice` | the reveal; `line` may update live each tick |
| `glow` (+ `glowKind`) | correct control — single element **or array** (`glow:[a,b]`; used by 8_1, 8_3, 8_5) |
| `submit` | Check / Next (glows only after a pick where a pick is required) |
| `input` / `inputHint` | numeric field + **neutral** placeholder (never the answer). Only **8_3** uses a text input (`#nInput`); optionally 8_4's `#posInput`. The other 7 sims have no input — authors must not invent one |
| `key` | round identity — keep **stable per round** |

## Key policy for live-manipulation sims (slider / marker / nudge)

Stable per-round key (`m-<idx>` / `r-<idx>`); the live nudge (current value, direction, remaining) goes
in **`line`** only, so the bar updates live without re-speaking or resetting disclosure. The glow
re-applies each tick, so it can flip control → Check once within tolerance.

**Answer never leaks into `hint`** (the Ch 7 7_6 rule): for MCQ, `hint` asks the question and the reveal
(answer + glow) lives in `line`/`detail`; for "find the numerator/improper" sims, `hint` asks, `line`
reveals. Where the target fraction is already printed in the sim's mission (8_4, 8_7), restating it is the
task, not a leak.

**No hardcoded Unicode in hooks.** The spec examples write mixed numbers as 1½ / 2¾ for readability, but the
sims store `whole`, `num`, `den` separately — hooks must build strings from those fields (e.g.
`whole+' '+num+'/'+den`), never emit `½`/`¾` literals.

**No HCF helper exists in 8_8's code** — the simplify hook must compute the HCF itself (small inline `gcd`)
rather than assume a `hcf()` function is available.

## The four teaching models ("hint for 4 variations")

| Model (`key`) | Shows first | Answer + glow appear | Taps |
|---|---|---|---|
| **Try first** (`ask`) | the question | after one **Hint** | 1 |
| **Step-by-step** (`guided`) | answer + glow immediately | from the start | 0 |
| **Self-explain** (`self`) | question → *why* nudge | after a second tap | 2 |
| **Answer on tap** (`ondemand`) | the question | one **Show answer** | 1 |

Worked example (8_8, round `12/18`):
- **Try first** — "Write 12/18 in its lowest form. Which option is right?"; Hint reveals "2/3" and glows it.
- **Step-by-step** — reveal + glow immediately.
- **Self-explain** — question → nudge ("divide top and bottom by their HCF") → reveal.
- **Answer on tap** — question with a "Show answer" chip.

---

## Per-simulation plan

### 8_1 · Equivalent Fraction Tile Flip (multi-select set — array glow)
- **Actual interaction:** toggle the `.tile[data-i]` tiles that are **equivalent** to the base fraction; `check()` compares the picked set to the true equivalent set (`eq()`). `ROUNDS`: base + 4 tiles, e.g. base **1/2** → tiles [2/4 ✓, 3/6 ✓, 2/5 ✗, 4/8 ✓].
- **Concept:** equivalent fractions have the same value — multiply (or divide) numerator and denominator by the **same** number.
- **Coach:** hint "Which tiles equal 1/2? Tap all of them." → line "2/4, 3/6 and 4/8 all equal 1/2 (2/5 does not)."
- **Explanation:** Step 1 simplify each tile (or cross-multiply against the base) · Step 2 keep the ones equal to the base, skip the rest · Step 3 select exactly those, then Check · Why — scaling top and bottom by the same factor doesn't change the value · Tip — cross-multiply: a/b = c/d when a·d = b·c.
- **Glow:** `glow: [every equivalent tile]` (array); Check.
- **Unit tests:** for each round, `eq(tile, base)` matches the intended ✓/✗; the picked-set == equivalent-set grades correct.

### 8_2 · Fraction Compare Numberline Race (relation MCQ)
- **Actual interaction:** nudge two markers onto [0,1] (exploration), then choose the relation — `#gt` (A>B), `#lt` (A<B), `#eq` (A=B); `check()` compares `st.pick` to the true relation. `ROUNDS` pairs, e.g. **1/3 vs 1/2 → A<B**, 7/10 vs 2/3 → A>B.
- **Concept:** compare fractions by a common denominator (or cross-multiply); the larger equivalent numerator wins.
- **Coach:** hint "Is 1/3 less than, equal to, or greater than 1/2?" → line "1/3 < 1/2 (2/6 vs 3/6)."
- **Explanation:** Step 1 give both a common denominator (or cross-multiply) · Step 2 compare the numerators · Step 3 tap the matching relation · Why — same denominator means the pieces are the same size, so the count decides · Tip — cross-multiply: a/b vs c/d → compare a·d with b·c.
- **Glow:** the correct relation button (`#gt`/`#lt`/`#eq`); Check after a pick.
- **Note:** marker **placement isn't graded** (only the relation button is), and markers spawn at exact
  `val(a)`/`val(b)`, so the answer is nearly free to read off. Fine as-is (the *judgment* is the point); if
  we want real exploration later, seed the markers off-truth. No change needed for this build.
- **Unit tests:** the marked relation matches `val(a)` vs `val(b)` for every round.

### 8_3 · Fraction Equivalence Slider Lab (numerator slider + input — array glow)
- **Actual interaction:** the denominator slider is **fixed at `targetDen`**; set the **numerator** (`#nSlider` and `#nInput`) so `n/targetDen` equals the base. `check()` needs `n/d == base` **and** `d == targetDen`. `ROUNDS`: base + targetDen, e.g. **1/2 → /6** (answer 3/6), 2/3 → /12 (8/12).
- **Concept:** to rewrite a fraction with a bigger denominator, multiply top and bottom by the same factor (`targetDen ÷ base-den`).
- **Coach:** hint "Make 1/2 into sixths. What numerator over 6?" → line "3/6 (×3 top and bottom)."
- **Explanation:** Step 1 how many times bigger is the target denominator? `6 ÷ 2 = 3` · Step 2 multiply the numerator by the same factor: `1 × 3 = 3` · Step 3 set the numerator to 3, then Check · Why — scaling both parts equally keeps the value · Tip — the denominator factor and numerator factor must match.
- **Glow:** `glow: [#nSlider, #nInput]` (array) + Check; live nudge in `line` (current n vs required).
- **Notes:** the board already shows **"Equivalent ✓"** the moment `n/d` matches, so the reveal is
  confirmatory once correct (intended). If `inputHint` is used on `#nInput`, keep the placeholder **neutral**
  (e.g. "numerator") — never seed the answer (3). Denominator slider is fixed (`min==max==targetDen`), so
  only the numerator is tuned — confirmed in code (`min="${r.targetDen}" max="${r.targetDen}"`).
- **Unit tests:** required `n = (targetDen / base-den) × base-num` is an integer within the slider range for every round.

### 8_4 · Fraction Numberline Mapper (drag marker to target)
- **⚠ Reseed first (blocking):** `setup(){ st.pos = 0.5 }` and round 0 is **1/2**, so round 0 is pre-solved.
  Change to `st.pos = 0.08;` so the marker never starts on target.
- **Shared skill note:** 8_4 and **8_7** are the *same* skill (place a fraction on [0,1]); only the chrome
  differs (8_4 drags `#marker`, EPS 0.03; 8_7 slides `#pSlider`, tol 0.02). Keep both, but the hook is
  effectively one pattern — don't over-invest building two different coaches.
- **Actual interaction:** drag `#marker` to position `num/den` on [0,1]; `check()` needs `|pos − num/den| ≤ EPS`. `ROUNDS`: num/den = 1/2, 3/4, 2/3, 5/8, 7/10, 9/12.
- **Concept:** a fraction is a point on the numberline at `num ÷ den` of the way from 0 to 1.
- **Coach:** hint "Place the marker at 3/4 on the 0–1 line." → line (live) "You're at 0.62 — slide right toward 0.75 (3/4)."
- **Explanation:** Step 1 the line runs 0 to 1; 3/4 sits three-quarters along · Step 2 as a decimal, 3 ÷ 4 = 0.75 · Step 3 drag until the marker reads ~0.75 · Why — the denominator splits [0,1] into equal parts and the numerator counts them · Tip — halve to check: 3/4 is halfway between 1/2 and 1.
- **Glow:** `#marker` + Check within tolerance; stable key; live nudge in `line`.
- **Unit tests:** each `num/den` lies in [0,1] and is reachable within `EPS`.

### 8_5 · Fraction of Quantity Rescue (multi-select cells — count-only)
- **⚠ Soften the legend first (blocking):** the board renders `Target count = ${need()}`, printing the
  answer. Change it to a non-spoiler prompt (e.g. "Fill the right number of cells"). The coach then supplies
  the count.
- **Actual interaction:** a grid of `total` cells; turn on **`(num/den) × total`** of them (any cells); `check()` compares the **count** to `need()`. `MISSIONS`: frac + total, e.g. **1/3 of 12 = 4**, 3/4 of 16 = 12.
- **Concept:** a fraction of a quantity = `(numerator ÷ denominator) × total` — split the total into `den` equal groups and take `num` of them.
- **Coach:** hint "Rescue 1/3 of the 12 items. How many cells?" → line "4 cells (12 ÷ 3 = 4, then ×1)."
- **Explanation:** Step 1 split the total into `den` equal groups: 12 ÷ 3 = 4 per group · Step 2 take `num` groups: 1 × 4 = 4 · Step 3 turn on 4 cells (any), then Check · Why — the denominator makes the groups, the numerator counts them · Tip — "of" means multiply.
- **Glow:** **count-only — no cell glow.** Any cells satisfy `check()`, so glowing an arbitrary "first N"
  set would teach that *those specific* cells are the answer (wrong idea). Put the number in `line` ("turn on
  4 cells") and glow **Check** once the live count equals `need()`. Live count vs target goes in `line`.
- **Unit tests:** `need() = (num × total) / den` is a whole number ≤ `total` for every mission.

### 8_6 · Fraction Operation Duel (answer MCQ)
- **Actual interaction:** MCQ over `+ − × ÷` results; `check()` compares the picked `.opt` to `ans`. `ROUNDS`: e.g. **1/2 + 1/3 = 5/6**, 2/3 × 3/5 = 2/5, 4/7 ÷ 2/7 = 2.
- **Concept:** add/subtract over a **common denominator**; multiply **across** (top×top, bottom×bottom); divide by **inverting** the second fraction and multiplying.
- **Coach:** hint "What is 1/2 + 1/3?" → line "5/6 (3/6 + 2/6)."
- **Explanation:** Step 1 pick the right rule for the sign (common denom for +/−, across for ×, invert-and-multiply for ÷) · Step 2 apply it: 1/2 + 1/3 = 3/6 + 2/6 = 5/6 · Step 3 simplify if needed, then tap it · Why — you can only add same-size pieces, so match denominators first · Tip — ÷ by a fraction = × by its reciprocal.
- **Glow:** the correct `.opt`; Check after a pick.
- **Unit tests:** each `ans` is the correct result and `ans ∈ opts` for every round.

### 8_7 · Fraction Route Planner (position slider to target)
- **Actual interaction:** `#pSlider` (0–1, step 0.001); set `p = num/den`; `check()` needs `|p − num/den| ≤ 0.02`. `MISSIONS`: num/den, e.g. 3/5, 7/8, 5/12.
- **Concept:** every fraction is a point between 0 and 1 at its decimal value `num ÷ den`.
- **Coach:** hint "Set the route to 3/5 of the way." → line (live) "At 0.52 — nudge up toward 0.60 (3/5)."
- **Explanation:** Step 1 convert to a decimal: 3 ÷ 5 = 0.6 · Step 2 slide toward 0.60 · Step 3 stop within 0.02 · Why — the slider is the numberline; the fraction's decimal is its position · Tip — compare to landmarks (1/2 = 0.5, 3/4 = 0.75).
- **Glow:** `#pSlider` + Check within tolerance; stable key; live nudge in `line`.
- **Unit tests:** each `num/den ∈ [0,1]` and reachable within 0.02.

### 8_8 · Fraction Simplify Relay (answer MCQ)
- **Actual interaction:** MCQ — pick the **lowest form**; `check()` compares the picked `.opt` to `ans`. `ROUNDS`: e.g. **12/18 → 2/3**, 45/60 → 3/4, 81/108 → 3/4.
- **Concept:** simplify by dividing numerator and denominator by their **HCF**; lowest form has HCF 1.
- **⚠ No HCF helper in the code** — the hook must compute it inline (`function g(a,b){return b?g(b,a%b):a}`)
  to build the `detail` steps; don't call a non-existent `hcf()`.
- **Coach:** hint "Write 12/18 in its lowest form." → line "2/3 (÷6 top and bottom)."
- **Explanation:** Step 1 find the HCF of top and bottom: HCF(12,18) = 6 · Step 2 divide both: 12÷6 = 2, 18÷6 = 3 · Step 3 tap 2/3 · Why — dividing both by the same factor keeps the value but shrinks the numbers · Tip — a decoy like 6/9 is equal but **not** lowest (still ÷3).
- **Glow:** the correct `.opt` (the lowest form); Check after a pick.
- **Unit tests:** `ans = q ÷ HCF` is truly lowest (HCF 1) and `ans ∈ opts`; equal-but-unsimplified decoys are not marked correct.

### 8_9 · Mixed Fraction Converter Track (±nudge to target)
- **⚠ Reseed first (blocking):** `setup()` seeds `st.n = whole×den+num; st.d = den` — the answer, so Check
  passes with **zero** nudges (the same bug 7_9 had). Reseed wrong: `st.n = 1; st.d = (r.den===2 ? 3 : 2);`
  so both fields start off-target and the learner must adjust.
- **Actual interaction:** ± nudge the numerator (`#nMinus`/`#nPlus`) and denominator (`#dMinus`/`#dPlus`) to build the **improper** form; `check()` needs `n == whole×den + num` and `d == den`. `ROUNDS`: whole+num/den, e.g. **1½ → 3/2**, 2¾ → 11/4, 4⅓ → 13/3.
- **Concept:** a mixed number → improper: `whole × denominator + numerator`, over the same denominator.
- **Coach:** hint "Convert 1½ to an improper fraction. What's the numerator over 2?" → line "3/2 (1×2 + 1 = 3)."
- **Explanation:** Step 1 keep the denominator (2) · Step 2 numerator = whole×den + num = 1×2 + 1 = 3 · Step 3 set it to 3/2, then Check · Why — each whole is `den` more `den`ths, plus the leftover fraction · Tip — reverse to check: 3/2 = 1 remainder 1 → 1½.
- **Glow:** after reseeding (both fields start wrong), glow the **directional nudge buttons** — `#nPlus`
  when n is low / `#nMinus` when high, and `#dPlus`/`#dMinus` until `d==den` — steering via `line`
  ("numerator should be 3, currently 1"), then glow **Check** once both are right. Stable key.
- **Unit tests:** `needN = whole×den + num` for every round; both nudges can reach it in range **from the new
  wrong seed** (`n=1`, `d=2 or 3`).

---

## Build order

1. **Answer / relation MCQ** (8_2, 8_6, 8_8) — glow the correct choice; Check after pick.
2. **Slider / marker-to-target** (8_4, 8_7) single glow; **8_3** numerator slider+input (array glow).
3. **Multi-select set** (8_1 equivalent tiles, 8_5 quantity cells — array glow) and **8_9** ±nudge (Check + `line`).

Per sim: (1) add `<script src="edu-coach.js"></script>` before `</body>`; (2) insert the native
`window.__eduRound` block at the end of `render()` (and in the Check handler for resolved/next), reading
answers from the sim's own helpers (`eq`, `val`, `need`, `target`/`t`, HCF/simplify) so nothing is
duplicated.

## Verification (before push to `main`)

- `node --check` on each extracted `<script>` (9 sims).
- DOM shim: each publishes a valid `window.__eduRound{native:true, …}`; **8_1, 8_3, 8_5 publish `glow` as
  an array**.
- Fraction harness re-derives: 8_1 equivalence sets, 8_2 relations (`val(a)` vs `val(b)`), 8_3 required
  numerator, 8_4/8_7 target positions in [0,1], 8_5 `need()` whole & ≤ total, 8_6 operation results,
  8_8 lowest forms via HCF (and decoys not lowest), 8_9 improper numerators.
- Engine: array multi-glow already on `main` — no engine change needed.
- Live Chrome (iframe harness, chunked payloads at ~1000-char chunks): coach bar renders; tiles / cells /
  sliders / markers / `.opt` / relation buttons glow correctly; multi-glow paints for 8_1/8_3/8_5;
  dragging/sliding updates the bar without per-tick re-speak.
- Deploy note: GitHub Pages serves **`main`**; clear the app WebView cache to pick up new files.

## Corrections / open items (resolved at sign-off)

**Blocking pre-solved-board fixes (apply before hooking):**
- **8_9 reseed** — `setup()` currently seeds the answer; change to `st.n=1; st.d=(r.den===2?3:2)`. ✅ decided.
- **8_4 reseed** — `setup(){ st.pos=0.5 }` solves round 0 (target 1/2); change to `st.pos=0.08`. ✅ decided.
- **8_5 legend** — board prints `Target count = ${need()}`; soften to a non-spoiler prompt, coach gives the
  count. ✅ decided.

**Resolved design choices:**
- **8_5 glow** → **count-only** (no cell glow); number in `line`, glow Check when count matches. (Glowing
  arbitrary cells would teach the wrong idea.)
- **8_9 glow** → **directional** `#nPlus`/`#nMinus` (+ `#dPlus`/`#dMinus`) after reseed, then Check.
- **8_3 denominator slider** → confirmed fixed (`min==max==targetDen` in code); only the numerator tunes.

**Non-blocking (noted, no build change):** 8_2 markers spawn on-truth and placement isn't graded (fine —
the judgment is the point); 8_4 and 8_7 are the same skill in two chromes (don't over-build); 8_3 shows a
live "Equivalent ✓" so the reveal is confirmatory once correct.
