# Math Chapter 6 — Number Play · Coach Plan

**Status: BUILT (hooks on `main`).** Spec + implementation for Ch 6 Number Play. Each sim loads
`edu-coach.js` and publishes native `window.__eduRound`. Same shape as the Ch 5 spec (contract · four
models · per-sim plan · verification).

Chapter is **Number Play**: divisibility rules, divisors/factors, prime factor chains, HCF/LCM, parity,
and prime vs composite. Interaction mix: **multi-toggle** (6_1), **multi-select set** (6_2),
**sequential factor picks** (6_3), **two numeric inputs** (6_4), **HCF/LCM word problems** (6_5),
**odd/even switch** (6_6), and **select-then-route** (6_7). Array multi-glow (`glow:[a,b]`) is used for
6_1 (several valid rules) and 6_4 (two input fields).

> **Note on 5_7:** the "Digit Sum Click Race" (place-value build, min taps = digit sum) is a Number-Play
> activity that currently sits at `5_7`. If the team relocates it, it belongs in this chapter.

## How the coach works (contract)

Native round fields (authoritative = what `edu-coach.js` reads; see `EDU_ROUND_CONTRACT.md` for the
older V4 scrape fields):

| Field | Purpose |
|---|---|
| `native:true` | sim-authored round |
| `hint` / `hintVoice` | the question + spoken form |
| `why` | the rule (divisibility / gcd / parity / primality) |
| `detail` | Photomath steps (`Step 1/2/3 · Why · Tip`) |
| `line` / `voice` | reveal; `line` may update live each tick (bar re-renders on text change) |
| `glow` (+ `glowKind`) | correct control — **single element _or an array_** (`glow:[a,b]`, used by 6_1 & 6_4/6_5) |
| `input` / `inputHint` | a numeric field + placeholder hint (6_4/6_5) |
| `submit` | Check / Next |
| `key` | round identity — keep **stable per round** (avoids disclosure reset / re-speak) |

## The four teaching models ("hint for 4 variations")

Only the reveal + answer glow are gated; the action glow (Check/Next) and manipulation glow (the control
the learner must operate) show whenever the round needs an action.

| Model (`key`) | Shows first | Answer + glow appear | Taps |
|---|---|---|---|
| **Try first** (`ask`) | the question | after one **Hint** | 1 |
| **Step-by-step** (`guided`) | answer + glow immediately | from the start | 0 |
| **Self-explain** (`self`) | question → *why* nudge | after a second tap | 2 |
| **Answer on tap** (`ondemand`) | the question | one **Show answer** | 1 |

Worked example (6_6, round `6 × 7`):
- **Try first** — "Is 6 × 7 odd or even?"; Hint reveals "even" and glows the **even** switch.
- **Step-by-step** — reveal + glow immediately.
- **Self-explain** — question → nudge ("any product with an even factor is even") → reveal.
- **Answer on tap** — question with a "Show answer" chip.

## Across rounds and iterations

- **Stable per-round key** (`r-<idx>`); live progress (e.g. "next divisor", remaining count) goes in
  `line`, which the bar re-renders without resetting disclosure or re-speaking.
- **Multi-toggle** (6_1) — glow all the switches that should be ON via `glow:[rule2, rule3, rule9]`;
  Check once the on-set matches.
- **Multi-select set** (6_2 divisor ladder) — **tap order is free** (the sim auto-sorts `picked` and
  grades the final set), so glow any still-**missing** divisor (or all missing via the array) and let
  `line` list what's left. Smallest→largest is only the display, not the tap rule.
- **Sequential pick** (6_3 factor chain) — glow a valid next factor for `cur`; the key stays stable
  while `line`/`glow` follow the current state.
- **Two numeric inputs** (6_4/6_5) — glow both fields (`glow:[#hcf, #lcm]`); reveal states both answers;
  guided pre-fills the focused field via `inputHint`.
- **Select-then-route** (6_7) — once a number is selected, glow the correct lane button (Prime vs
  Composite) for that number.
- **Feedback / done** states carry no `hint`, so they reveal at once.

---

## Per-simulation plan

### 6_1 · Divisibility Signal Studio (multi-toggle rules)
- **Actual interaction:** toggle the rule switches (`RULES = ['2','3','5','9']`) ON for the rules that divide `n`; `check()` compares the on-set to `ROUNDS[i].valid`. `ROUNDS = [126→2,3,9 · 245→5 · 360→2,3,5,9 · 111→3 · 999→3,9 · 420→2,3,5]`.
- **Concept:** divisibility rules — ÷2 (last digit even), ÷3 (digit sum ÷3), ÷5 (ends 0 or 5), ÷9 (digit sum ÷9).
- **Coach:** hint "Which rules does 126 pass — 2, 3, 5, 9?" → line "2, 3 and 9 (126 is even, 1+2+6=9 is divisible by 3 and 9; it doesn't end in 0/5)."
- **Explanation:** Step 1 test each rule on `n` · Step 2 turn ON exactly the rules that pass · Step 3 leave the rest OFF, then Check · Why — each rule is a shortcut for the actual division · Tip — the ÷9 rule implies ÷3, but not the reverse.
- **Glow:** `glow: [switch for each valid rule]` (array) + Check.
- **Unit tests:** for each round, `n % rule === 0` iff `rule ∈ valid`, for every rule in RULES.

### 6_2 · Divisor Ladder Duel (multi-select set — order-free)
- **Actual interaction:** toggle on **every** divisor of `n` from a chip set (`options(n)` = every divisor + a few decoys `n−1..n+3`). **Tap order is free** — the sim auto-sorts `picked` (`st.picked=[...].sort()`), and `check()` only needs the final set to equal all divisors, with ≤2 wrong taps. `ROUNDS = [24, 36, 45, 48, 60]`.
- **Concept:** a divisor divides `n` with no remainder; find **every** one (1 and `n` included). Smallest→largest is just the sorted display, not the order you must tap.
- **Coach:** hint "Tap every number that divides 24 exactly." → while picking, line "Still missing: 8, 12." → complete, line "Divisors of 24: 1, 2, 3, 4, 6, 8, 12, 24."
- **Explanation:** Step 1 test each chip — does it divide 24 with no remainder? · Step 2 turn on all that do (any order) · Step 3 leave the decoys (23, 25, …) off, then Check · Why — divisors come in pairs that multiply to `n` (1×24, 2×12, 3×8, 4×6) · Tip — you only get 2 wrong taps, so skip anything that leaves a remainder.
- **Glow:** any still-**missing** divisor chip — `glow: [missing divisors]` (array), narrowing as they pick; Check once the set is complete.
- **Unit tests:** `divisors(n)` matches the expected set; the decoys `n−1..n+3` don't divide `n`; grading is **order-independent** (sorted-set equality).

### 6_3 · Factor Chain Builder (sequential division to 1)
- **Actual interaction:** from `cur` (starts at the mission number), repeatedly pick a divisor `v` (`cur % v === 0`, `v < cur`) to set `cur = v`; reach `cur = 1`. `MISSIONS = [36, 48, 54, 72, 84, 90]`.
- **Concept:** each step **replaces** the current number with one of its **proper divisors** (smaller than it, divides exactly); keep stepping down until you reach 1.
- **Coach:** hint "Bring 36 down to 1 — each step, replace it with one of its proper divisors. What's a good first move?" → line "e.g. 36 → 6 → 2 → 1, or 36 → 4 → 2 → 1 (each number divides the one before it)."
- **Explanation:** Step 1 pick a proper divisor of the current number (smaller, `cur % v === 0`) · Step 2 the number becomes that divisor; repeat · Step 3 a prime's only proper divisor is 1 — pick 1 to finish · Why — every number has a descending chain of divisors down to 1 · Tip — replacing with the smallest prime factor each step gives the cleanest chain.
- **Glow:** a valid next factor chip for `cur` (prefer its smallest prime factor); Check when `cur === 1`.
- **Unit tests:** each glowed step satisfies `cur % v === 0` and `v < cur`; a greedy smallest-prime chain reaches 1.

### 6_4 · LCM HCF Sync Lab (two numeric inputs)
- **Actual interaction:** type **HCF** (`#hcf`) and **LCM** (`#lcm`) for the pair; `check()` needs `gcd(a,b)` and `lcm(a,b)`. `ROUNDS = [[12,18],[16,24],[14,21],[18,30],[20,32],[27,36]]`.
- **Concept:** HCF = largest number dividing both; LCM = smallest number both divide; and `HCF × LCM = a × b`.
- **Coach:** hint "For 12 and 18, what are the HCF and LCM?" → line "HCF = 6, LCM = 36 (6 × 36 = 12 × 18)."
- **Explanation:** Step 1 factor both (12 = 2²·3, 18 = 2·3²) · Step 2 HCF = common factors (2·3 = 6); LCM = highest power of every prime (2²·3² = 36) · Step 3 check `HCF × LCM = a × b` · Why — shared primes give the HCF, all primes give the LCM · Tip — once you know HCF, LCM = a×b ÷ HCF.
- **Glow / input policy (single-field engine):** `edu-coach.js` drives **one** `input` + `inputHint`, so:
  glow **both** fields for attention via `glow: [#hcf, #lcm]` (array); set `input: #hcf` with a
  **neutral** `inputHint` (e.g. `"HCF"`) — **never the exact answer** (same rule as Ch 4 4_10, no
  spoiler in `ask`). The HCF and LCM values live in `line`/`detail`, which are model-gated, so
  `ask`/`self` don't reveal them and `guided` does. Check once both fields are filled.
- **Unit tests:** for each pair, `gcd × lcm = a × b`, the stated HCF/LCM match `gcd`/`lcm`, and
  `inputHint` never equals the answer.

### 6_5 · HCF & LCM Word Lab (differentiated — rebuilt)
- **Resolved:** 6_5 was a byte-identical copy of 6_4; per sign-off it's **rebuilt as HCF/LCM word problems** (title "HCF & LCM Word Lab"). Same number pairs as 6_4, **alternating LCM / HCF**, one numeric answer each: bells/buses "together again" → LCM(12,18)=36, LCM(14,21)=42, LCM(20,32)=160; identical boxes / greatest length / largest tile "no leftovers" → HCF(16,24)=8, HCF(18,30)=6, HCF(27,36)=9.
- **Actual interaction:** read the story, decide **HCF vs LCM**, type the single answer into `#ans`; `check()` compares to the derived answer.
- **Concept:** cue words map to the operation — "together again / same time" → **LCM** (smallest common multiple); "largest equal share / no leftovers" → **HCF** (highest common factor).
- **Coach:** hint "…After how many seconds do they flash together? Is this HCF or LCM?" → line "This is an LCM problem — answer 36."
- **Explanation:** Step 1 spot the cue (together→LCM, largest-equal→HCF) · Step 2 compute LCM/HCF of the pair · Step 3 type it, then Check · Why — periodic events line up at common multiples; equal shares need a common factor · Tip — HCF × LCM = a × b.
- **Glow / input policy:** single `input: #ans` with a **neutral** `inputHint` (`"answer"`) — never the number (no spoiler in `ask`); the answer + HCF/LCM reasoning live in model-gated `line`/`detail`.
- **Unit tests:** each round's answer = `lcm`/`gcd` of its pair per `type`; `inputHint` never equals the answer.

### 6_6 · Number Parity Switchboard (odd/even switch)
- **Actual interaction:** pick **odd** or **even** for the value of an expression (`.sw` switches, `['odd','even']`); `check()` compares to `ans`. `ROUNDS = [17+24 odd · 31+9 even · 22+14 even · 41−18 odd · 6×7 even · 9×11 odd]`.
- **Concept:** parity without computing — odd+odd = even, even+even = even, odd+even = odd; a product is even if **any** factor is even, odd only if **all** factors are odd (subtraction follows the same odd/even rule as addition).
- **Coach:** hint "Is 17 + 24 odd or even?" → line "Odd — odd + even = odd."
- **Explanation:** Step 1 note each number's parity · Step 2 apply the rule (sum: same → even, different → odd; product: any even → even) · Step 3 pick that switch · Why — parity only depends on the last bit, not the full value · Tip — you don't need the actual total (41); the rule is enough.
- **Glow:** the correct `odd`/`even` switch; Check.
- **Unit tests:** the stated `ans` matches the actual parity of each expression's value.

### 6_7 · Prime Composite Sort Circuit (select-then-route)
- **Actual interaction:** select a number token, then route it to the **Prime** lane (`left`) or **Composite** lane (`right`); `check()` needs every left prime and every right composite. `ROUNDS` = consecutive blocks `[2–9], [11–18], [19–26], [27–34], [35–42]`.
- **Concept:** a **prime** has exactly two factors (1 and itself); a **composite** has more. (1 is neither — none appear here; 2 is the only even prime.)
- **Coach:**
  - **No selection yet** (`st.sel === null`): hint "Pick a number first, then send it to Prime or Composite." → glow a remaining (un-routed) token, or nothing.
  - **Number selected:** hint "Route 9 — is it prime or composite?" → line "Composite — 9 = 3 × 3, so it has a factor besides 1 and 9." → glow the correct lane button.
- **Explanation:** Step 1 take the selected number · Step 2 test small factors (2, 3, 5, …) up to its square root · Step 3 no factor found → Prime lane; a factor found → Composite lane · Why — a prime resists all such divisions by definition · Tip — even numbers above 2 are always composite; so is any multiple of 3, 5, 7…
- **Glow:** with a token selected, glow `#primeBtn` if `isPrime(n)` else `#compBtn`; with no selection, glow a remaining token; Check when all numbers are routed.
- **Unit tests:** primality of each number in every block matches its correct lane.

---

## Build order

1. **Toggle / pick group** (6_1 multi-toggle rules, 6_2 ordered divisors, 6_3 factor chain) — glow the
   valid rule set / next correct chip; stable key.
2. **Numeric inputs** (6_4, and 6_5 **only after** the duplicate is resolved) — glow both fields, reveal
   HCF + LCM.
3. **Switch / route** (6_6 parity switch, 6_7 select-then-route lanes).

Per sim: (1) add `<script src="edu-coach.js"></script>` before `</body>`; (2) insert the native
`window.__eduRound` block at the end of `render()` (and in the Check handler for resolved/next), reading
answers from the sim's own helpers (`divisors`, `gcd`/`lcm`, `isPrime`, digit-sum) so nothing is
duplicated.

## Verification (before push to `main`)

- `node --check` on each extracted `<script>` (7 sims).
- DOM shim: each publishes a valid `window.__eduRound{native:true, …}` per state; 6_1 and 6_4/6_5
  publish `glow` as an **array**.
- Number-theory harness re-derives: 6_1 valid-rule sets (`n % rule === 0`), 6_2 `divisors(n)` + decoys,
  6_3 a smallest-prime chain reaches 1, 6_4/6_5 `gcd × lcm = a × b`, 6_6 expression parity, 6_7 primality
  per block.
- Engine: the array multi-glow path is already in `edu-coach.js` (added for Ch 5) and unit-checked; no
  new engine change expected.
- Live Chrome (iframe harness on the deployed origin): coach bar renders; multi-toggle/next-chip/two-input/
  switch/lane glow lands correctly; dragging/typing updates the bar without per-tick re-speak.
- Deploy note: GitHub Pages serves **`main`**.

## Corrections applied (from review)

- **6_2** rewritten as an **order-free multi-select set** (tap order free; sim auto-sorts; grade the
  final set). Glow shifts from "next-in-sequence" to **any still-missing divisor** (array); `line` lists
  what's left; smallest→largest is display-only. Confirmed against `st.picked=[...].sort()`.
- **6_4/6_5 input policy** spelled out for the **single-field engine**: array-glow both fields, `input`
  on one with a **neutral** `inputHint` (never the answer in `ask`), answers in model-gated `line`/`detail`.
- **6_3** wording tightened to "**replace `cur` with a proper divisor**" (not "divide out a prime");
  unit tests stay on `cur % v === 0 && v < cur`.
- **6_7** added the **no-selection** state ("Pick a number first"); lane IDs confirmed `#primeBtn` /
  `#compBtn`.

## Open items for sign-off

- ~~**6_5 duplicate of 6_4**~~ — **resolved:** rebuilt as HCF/LCM word problems (see 6_5 above).
- **5_7 placement** — Number-Play activity currently at `5_7`; relocate to Ch 6 if the team agrees.
- Confirm the exact rule set surfaced in 6_1's UI is `['2','3','5','9']` only (no ÷4/÷6/÷11 switches) —
  matches the file today.
