# Math Chapter 9 — Data Handling · Coach Plan

**Status: BUILT — hooks + blocking fixes on all 8 sims; verified node/shim/harness + Chrome; ready to
commit.** All 8 sims now load `edu-coach.js` and publish native `window.__eduRound` rounds, and the three
blocking fixes (9_4 target hidden, 9_2 `check()` value-in-bin + header leak dropped, 9_6 reframed to the
cf ≥ N/2 rule with `ans=[1,2,2,1,2]`) are applied. Same shape as the Ch 5 / 6 / 7 / 8 specs.

> **Decisions from review (all fold into this build):**
> - **9_6** → *reframe as "which class holds the middle value"* using the textbook **cf ≥ N/2** rule.
>   Grading + `ans` change accordingly (only round 0 moves: `2 → 1`, giving `ans = [1,2,2,1,2]`); mission
>   copy reworded to match.
> - **9_2** → *fix the grading bug*: `check()` must verify each placed value **belongs in its bin**, not
>   just that bin counts match. Also drop the "target freq N" from bin headers (soft leak). **Richer glow**:
>   glow the correct bin for the currently-selected token.
> - **9_7** → **richer glow**: glow the smallest remaining chip during the sort.
> - **9_4** → *soften the answer leak*: hide the precomputed green `target` on each control; keep the blue
>   bar + signed diff, so the coach actually teaches `base + diff`.
> - These UI/grading fixes ship **with** the coach hooks, not separately.

Chapter is **data handling**: reading and building bar graphs, organizing raw data into frequency tables,
range and outliers, double-bar comparison, mean and median, grouped-median class, and pictographs.
Interaction mix is heavier on **multi-value construction** than any prior chapter, which shapes the
coaching model (below).

## The three coaching shapes this chapter needs

Unlike ch5–8 (mostly one control per round), Ch 9 rounds usually have **many controls and no single
"tap the answer" element**. Three patterns cover all 8 sims:

1. **Multi-value construct → directional glow.** Several bars / icon counts to set (9_1, 9_4, 9_8, and the
   9_6 class selector). Glow the control of the **first still-wrong item**, steer via `line`
   ("Bar C should be 2, it's 0"), then glow **Check** once every value matches. (Same idea as 7_3 / 8_9.)
2. **Two numeric fields → array glow.** Where a round needs a pair of inputs (9_3 min+max, 9_7 mean+median),
   reuse the **array multi-glow** (`glow:[#a,#b]`, already on `main`) to highlight both, then Check.
3. **MCQ / pick → single glow.** 9_5 (frequency option). 9_6 is a *nudge-selector* over class rows, so it's
   pattern 1 applied to a one-dimensional pick.

Classify/sort sims (9_2, 9_7-sort) have no single correct element mid-task, so the coach **steers via
`line`** (which value goes where / tap the smallest remaining) and glows **Check** at the end.

## How the coach works (contract)

| Field | Purpose |
|---|---|
| `native:true` | sim-authored round |
| `hint` / `hintVoice` | the **question only** (never the answer) |
| `why` | the rule (frequency / range / mean-median / scale) |
| `detail` | Photomath steps (`Step 1/2/3 · Why · Tip`) |
| `line` / `voice` | the reveal / live steer; `line` may update live each tick |
| `glow` (+ `glowKind`) | correct control — single element **or array** (`glow:[a,b]`; used by 9_3, 9_7) |
| `submit` | Check / Next (glows only after the board is correct / a pick is made) |
| `input` / `inputHint` | numeric field + **neutral** placeholder. Only 9_1 (per-bar), 9_3, 9_7 have inputs |
| `key` | round identity — keep **stable per round** |

**Key policy:** stable per-round key (`m-<idx>` / `r-<idx>`); live steering (current value, which item is
wrong, remaining) goes in **`line`** only, so the bar updates live without re-speaking or resetting
disclosure. **Answer never leaks into `hint`** (the 7_6 rule).

## The four teaching models

| Model (`key`) | Shows first | Answer + glow appear | Taps |
|---|---|---|---|
| **Try first** (`ask`) | the question | after one **Hint** | 1 |
| **Step-by-step** (`guided`) | answer + glow immediately | from the start | 0 |
| **Self-explain** (`self`) | question → *why* nudge | after a second tap | 2 |
| **Answer on tap** (`ondemand`) | the question | one **Show answer** | 1 |

Worked example (9_5, raw `[1,2,1,3,2,1,4,2]`, category **2**):
- **Try first** — "How many times does 2 appear in the data? Pick the count."; Hint reveals "3" and glows it.
- **Step-by-step** — reveal + glow the option "3" immediately.
- **Self-explain** — question → nudge ("tally each 2 you see") → reveal.
- **Answer on tap** — question with a "Show answer" chip.

---

## Explicit DOM glow targets (per sim)

| Sim | Controls | Glow target |
|---|---|---|
| 9_1 | `.btnMini` (data-i/data-d), `[data-set]` inputs, `#checkBtn` | the `[data-set]` input (or `.btnMini`) of the first wrong bar; `#checkBtn` when all match |
| 9_2 | `.tok` (data-i), `[data-bin]` bins, `#checkBtn` | the `[data-bin]` matching the selected token; `#checkBtn` when all 8 placed & correct |
| 9_3 | `#minIn`, `#maxIn`, `.chip` (data-v), `#checkBtn` | `glow:[#minIn,#maxIn]` (array); then the outlier `.chip`; `#checkBtn` on no-outlier rounds |
| 9_4 | `.mini` (data-i/data-d), `#checkBtn` | the `.mini` group of the first wrong green bar; `#checkBtn` when all match |
| 9_5 | `.opt` (data-v), `#checkBtn` | the correct `.opt`; `#checkBtn` after a pick |
| 9_6 | `#p1`/`#m1` (class nudge), `#checkBtn` | `#p1`/`#m1` toward the target class index; `#checkBtn` when `pick===ans` |
| 9_7 | `.chip` (data-i), `#moveBtn`, `#meanIn`, `#medIn`, `#checkBtn` | smallest remaining `.chip` during sort; then `glow:[#meanIn,#medIn]`; `#checkBtn` at the end |
| 9_8 | `.mini` (data-i/data-d), `#checkBtn` | the `.mini` group of the first wrong category; `#checkBtn` when all match |

## Per-simulation plan

### 9_1 · Bar Graph Constructor Bay (multi-value construct — directional glow)
- **Actual interaction:** for 4 categories, set each bar's height to the target frequency using ±1/±2
  buttons **and** a per-bar number input (`[data-set]`, 0–9). `check()` needs every `bars[i] === target[i]`.
  `ROUNDS`: cats + target, e.g. `[3,5,2,4]`.
- **Concept:** a bar graph shows each category's frequency as a bar of matching height.
- **Coach:** hint "Build the bars to match the target frequencies." → line (live) "Bar A = 3 ✓; Bar C should be 2 (it's 0)."
- **Explanation:** Step 1 read each target frequency · Step 2 raise that bar to the same height · Step 3 match all four, then Check · Why — bar height *is* the count · Tip — the number under each bar should equal its target.
- **Glow:** the ±controls / input of the first mismatching bar; Check once all match; steer via `line`.
- **Unit tests:** every target ∈ [0,9] (reachable). ✅ verified.

### 9_2 · Data Organization Command Center (classify into bins — steer + Check)
- **Actual interaction:** select a raw value token (`.tok`), then tap its bin (`[data-bin]`); repeat for all
  8 values. `check()` needs each bin's count === `freq[bin]` (and all values placed).
- **Concept:** a frequency table groups raw values — each value goes in the bin equal to its value.
- **Coach:** hint "Sort each value into its matching bin." → line (live) "Placed 5 of 8. Next: put 2 in bin 2."
- **Explanation:** Step 1 pick a value · Step 2 drop it in the bin with the same label · Step 3 place all 8, then Check · Why — the frequency of a value is how many times it lands in its bin · Tip — the bin counts must sum to the number of values (8).
- **⚠ Grading fix (blocking, ships with hooks):** `check()` currently compares only bin **counts** to
  `freq`, so wrong tokens in a bin can still pass. Fix it to verify each placed value **belongs in its bin**
  (`r.raw[i] === Number(bin)` for every placed index). Also **drop the "target freq N"** from the bin headers
  (soft answer leak).
- **Glow (richer):** when a token is selected, glow the **correct `[data-bin]`** for it; steer via `line`;
  glow **Check** once all 8 are placed & correct.
- **Unit tests:** each round's `freq[bin]` === count of that value in `raw`; Σfreq === raw.length; and (post-fix)
  a bin passes only if every value in it equals the bin label. ✅ data verified.

### 9_3 · Data Range Outlier Patrol (two inputs + outlier pick — array glow)
- **Actual interaction:** enter **min** (`#minIn`) and **max** (`#maxIn`), and tap the **outlier** chip
  (`.chip`) — or select **nothing** when there's no outlier. `check()` needs min===sorted-min,
  max===sorted-max, and outlier pick correct. `ROUNDS`: data + `out` (value or `null`).
- **Concept:** range = max − min; an outlier is a value far from the rest.
- **Coach:** hint "Find the smallest and largest, and spot any outlier." → line "min 12, max 55 (range 43); 55 is the outlier — tap it." For no-outlier rounds: "min 19, max 25 — the values are all close, no outlier. Tap Check."
- **Explanation:** Step 1 sort mentally; smallest = min, largest = max · Step 2 range = max − min · Step 3 an outlier sits far from the pack — pick it, or none · Why — range measures spread; outliers distort it · Tip — no-outlier rounds: leave every chip unselected.
- **Glow:** `glow:[#minIn,#maxIn]` (array) until filled; then the outlier chip (or Check on no-outlier rounds).
- **Unit tests:** min/max from sorted; every non-null `out` equals the sorted min or max (a true extreme). ✅ verified.

### 9_4 · Double Bar Comparator Lab (multi-value construct — directional glow)
- **⚠ Soften first (blocking, ships with hooks):** each control currently prints the precomputed green
  `target` (the answer), so the coach teaches nothing. **Hide the green target**; keep the blue value + the
  **signed diff** visible, so the learner computes `base + diff`.
- **Actual interaction:** the first (blue) bars are fixed at `base`; set each **second** (green) bar with ±
  buttons (`.mini`) to `base[i] + diff[i]`. `check()` needs `b[i] === base[i]+diff[i]`. `ROUNDS`: 3 cats.
- **Concept:** a double bar graph compares two data sets side by side per category.
- **Coach:** hint "Set each green bar to compare against the blue one." → line (live) "Cat X green should be 7 (5 + 2); it's 0."
- **Explanation:** Step 1 read the blue bar's value · Step 2 add the difference (up or down) · Step 3 set the green bar, all three, then Check · Why — pairing bars lets you compare two series at a glance · Tip — a negative difference means the green bar is shorter.
- **Glow:** ±controls of the first wrong green bar; Check when all match; steer via `line`.
- **Unit tests:** every `base[i]+diff[i]` ∈ [0,9] (reachable). ✅ verified.

### 9_5 · Frequency Table Rescue (answer MCQ)
- **Actual interaction:** MCQ — pick the frequency (count) of a given category in the raw data (`.opt`
  `data-v`). `check()` compares `pick` to `ans`. `ROUNDS`: raw + cat + ans + opts.
- **Concept:** frequency = how many times a value occurs.
- **Coach:** hint "How many times does 2 appear? Pick the count." → line "2 appears 3 times."
- **Explanation:** Step 1 scan the data for the category · Step 2 tally each occurrence · Step 3 tap the count · Why — that tally is the value's frequency · Tip — cross off each one as you count so you don't double-count.
- **Glow:** the correct `.opt`; Check after a pick.
- **Unit tests:** `ans` === count of `cat` in `raw`; `ans ∈ opts`. ✅ verified.

### 9_6 · Middle-Value Class (class selector — directional glow) — *reframed*
- **⚠ Reframe + rule fix (blocking, ships with hooks):** simplify from "grouped-data median class" (a
  class-10 topic) to **"which class interval holds the middle value."** Grade with the textbook **cf ≥ N/2**
  rule. Under `≥`, `ans` becomes **`[1, 2, 2, 1, 2]`** — only round 0 changes (`2 → 1`, since f=`[3,7,8,2]`
  reaches cumulative 10 = N/2 at class 1). Reword the mission from "reaches/exceeds" ambiguity to
  "the first class where the running total **reaches half** (N/2)."
- **Actual interaction:** a ±nudge selector (`#p1` down / `#m1` up) moves the highlight across the
  class-interval rows; land on the class holding the middle value. `check()` needs `pick === ans`. `ROUNDS`:
  cls + f + ans.
- **Concept:** with the values in order, the middle value falls inside one interval — the one where the
  running (cumulative) total first reaches half the data.
- **Coach:** hint "Which class holds the middle value?" → line (live) "N = 20, half = 10; move to the first class where the running total reaches 10."
- **Explanation:** Step 1 add the frequencies for N and the cumulative totals · Step 2 half is N/2 · Step 3 the middle class is the first whose cumulative total reaches N/2 · Why — half the data sits at or below that class · Tip — the cumulative column is shown beside each class.
- **Glow:** `#p1`/`#m1` toward the target class index; Check when `pick === ans`; steer via `line`.
- **Unit tests:** `ans === [1,2,2,1,2]` = first class whose cumulative frequency **≥ N/2**, for all 5 rounds. ✅ recomputed.

### 9_7 · Mean Median Sort Arena (sort + two inputs — steer + array glow)
- **Actual interaction:** move chips from the unsorted pool into the sorted list in ascending order (`select`
  then move), then enter **mean** (`#meanIn`) and **median** (`#medIn`). `check()` needs the sorted list
  correct, mean within 0.01, and median exact. `ROUNDS`: 5 values each.
- **Concept:** median = middle of the sorted data; mean = sum ÷ count.
- **Coach:** hint "Sort the data, then find the mean and median." → line (live) "Tap the smallest remaining (3)."; after sorting: "Median is the middle value; mean = sum ÷ 5."
- **Explanation:** Step 1 sort smallest → largest · Step 2 median = the middle value (3rd of 5) · Step 3 mean = add all ÷ 5 · Why — sorting exposes the middle; the mean balances the values · Tip — median needs the sorted order; mean doesn't.
- **Glow (richer):** during sort, glow the **smallest remaining `.chip`** (the next one to move) and steer via `line`; once sorted, `glow:[#meanIn,#medIn]` (array); Check when all three are right.
- **Unit tests:** each set has 5 values; median = sorted[2]; mean computed. ✅ verified (medians 7/9/6/9/9; means 6/9/6/9/9).

### 9_8 · Pictograph Icon Scale Studio (multi-value construct — directional glow)
- **Actual interaction:** for 3 categories, set the **icon count** with ± buttons (`.mini`) so
  `icons[i] × scale === vals[i]`. `MISSIONS`: scale + cats + vals + icon.
- **Concept:** in a pictograph each icon stands for `scale` units, so icons = value ÷ scale.
- **Coach:** hint "Each icon = <scale>. How many icons for this value?" → line (live) "B needs 6 ÷ 2 = 3 icons; you have 1."
- **Explanation:** Step 1 read the scale (one icon = `scale` units) · Step 2 icons needed = value ÷ scale · Step 3 set each category's icons, then Check · Why — the key/scale converts icons to a count · Tip — if it doesn't divide evenly you'd need a part-icon (these all divide evenly).
- **Glow:** ±controls of the first wrong category; Check when all match; steer via `line`.
- **Unit tests:** every `vals[i]` is divisible by `scale` (whole icon count). ✅ verified.

---

## Build order

1. **MCQ** (9_5) — the clean single-glow case; establishes the pattern.
2. **Two-input + array glow** (9_3, 9_7) — reuse the deployed array multi-glow for the input pair.
3. **Multi-value construct** (9_1, 9_4, 9_8) and the **9_6** class nudge — directional glow of the first
   wrong control + Check.
4. **Classify/sort steering** (9_2, and 9_7's sort phase) — `line`-driven, Check at the end.

Per sim: (1) add `<script src="edu-coach.js"></script>` before `</body>`; (2) insert the native
`window.__eduRound` block at the end of `render()`, reading answers from the sim's own data/helpers
(`target`, `freq`, `targetB`, `mean`/`median`, cumulative frequency) so nothing is duplicated.

## Verification (before push to `main`)

- `node --check` on each extracted `<script>` (8 sims).
- DOM shim: each publishes a valid `window.__eduRound{native:true, …}`; **9_3 and 9_7 publish `glow` as an
  array**.
- Data harness (done): **107/107** — 9_1 target ranges, 9_2 frequencies (and Σ = n), 9_3 min/max/outlier,
  9_4 double-bar reachability, 9_5 counts + options, 9_6 median-class rule, 9_7 mean/median, 9_8 divisibility.
- Live Chrome (iframe harness, chunked payloads ~1000 chars): coach bar renders; bars / bins / chips /
  inputs / options / nudge glow correctly; array glow paints for 9_3/9_7; multi-value steering updates the
  bar without per-tick re-speak.
- Deploy note: GitHub Pages serves **`main`**; clear the app WebView cache to pick up new files.

## Corrections / open items (resolved at sign-off)

**Blocking fixes — ship with the hooks:**
- **9_4** — hide the precomputed green `target`; keep blue value + signed diff. ✅ decided.
- **9_2** — fix `check()` to verify each placed value belongs in its bin (not just counts); drop the
  "target freq N" from bin headers. ✅ decided.
- **9_6** — reframe to "middle-value class", grade with **cf ≥ N/2**, set `ans = [1,2,2,1,2]`, reword mission. ✅ decided.

**Resolved design choices:**
- **9_2 glow** → richer: glow the correct `[data-bin]` for the selected token. ✅
- **9_7 glow** → richer: glow the smallest remaining `.chip` during sort. ✅
- **9_3 no-outlier rounds** → coach says the values are close, no outlier, tap Check (select nothing). An
  optional "None" chip is a nicer UX but not required for v1.
- **Array-glow reuse** — 9_3 (`[#minIn,#maxIn]`) and 9_7 (`[#meanIn,#medIn]`) use the multi-glow already on
  `main`; no engine change.

**Non-blocking:** 9_1 and 9_8 print the full brief / target values — fine as the construction brief; the coach
teaches *matching heights* (9_1) and *value ÷ scale* (9_8), not the printed table.
