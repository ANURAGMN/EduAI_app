# Coach Spec — Science Ch1 "Acids, Bases & Salts" (`science_2` family)

**Status: BUILT (all 20 files wired + verified `node --check`; review fixes applied). Ready to commit.**
Target: the 10 `science_2_*.html` sims + their `_kn` twins (20 files).

> Chapter mapping: in the live app the first Science chapter surfaces the `science_2` sim family.
> (`science_1` has only an orphan Kannada Light-&-Shadow file — out of scope here.)

---

## Why this is needed (audit result)

| | English sims | Kannada twins |
|---|---|---|
| `edu-coach.js` wired | ✅ all 10 | ❌ none (0/10) |
| native `__eduRound` hook | ❌ none (0/10) | ❌ none |
| `color-scheme: light` meta | ✅ all | ✅ all |

So today: the English sims load the coach engine but give it **nothing to say** (it falls back to the generic DOM scraper, which these SVG-heavy pages don't expose cleanly); the Kannada sims have **no coach at all**. This is the exact state `science_3_1` was in before we fixed it — just ×10.

## Design principle — guide, not grader

Unlike math ch8/ch9, these are **exploratory demos**, not scored round games: there's no answer to submit, no score/lives. So the coach's job is to **guide the next action and narrate the chemistry**, using the same native one-clock contract (`window.__eduRound = {native:true, line, voice, hint, why, detail, glow, submit, key}`) already proven on `science_3_1`.

Three reusable hook shapes cover all 10 sims:

- **G — Guided explore** (pick → test → observe): glow the control to tap; once a result appears, explain *why* that substance is acidic/basic/neutral and nudge to try the opposite type. Encourage testing at least one acid **and** one base before moving on.
- **S — Step machine** (sequential state): glow the next enabled action; narrate what just happened at each state (e.g. "acid injected → now neutralise with a base").
- **I — Info explore** (tabs/cards): steer the learner through each type and **flag the built-in misconception**.

`key` policy (as on `science_3_1`): stable key while a line just updates live (no re-speak); a fresh key (`...-<Date.now()>`) when we want the coach to *speak* a reaction (e.g. a result revealed, a misconception tapped).

**Two conventions that apply across sims (call them out once here so they aren't missed per file):**

- **Publish the result on the conclusion update, not on the click.** In the G-sims (2_2, 2_4, 2_5, 2_6) `runTest`/mix animates and sets the conclusion **after a delay** (`setTimeout`). Set the result `__eduRound` (fresh key, speaks the *why*) at the point the conclusion text is actually written, **not** in the button handler — otherwise the coach speaks before the result is on screen. Until then, keep the pre-test guide line.
- **Reset → idle.** 2_6's mix button toggles back to reset; 2_8 and 2_10 have explicit reset. On reset, republish the sim's **idle/start** line with its **stable start key** (same as `science_3_1`) so the coach quietly returns to "tap to begin" without re-speaking.

---

## Glow / key reference table

One row per publish point, so the 20 files stay consistent (fresh = `<prefix>-<Date.now()>`, speaks; stable = fixed string, live-updates silently).

| Sim | Trigger / state | Glow target | `key` | Fresh? |
|---|---|---|---|---|
| 2_1 | start | `#spray-btn` | `spray-start` | stable |
| 2_1 | sprayCount 1..2 | `#spray-btn` | `spray-start` | stable |
| 2_1 | revealed | — | `spray-done` | fresh |
| 2_2 | solution selected | `#test-btn` | `dip-<solution>` | stable |
| 2_2 | conclusion set (delayed) | — | `res-<solution>` | fresh |
| 2_2 | only-acids nudge | a basic `.solution-btn` | `nudge-base` | stable |
| 2_2 | ≥1 acid & ≥1 base tested | — | `done` | fresh once |
| 2_4 | selected / conclusion | `#test-btn` / — | `dip-<sol>` / `res-<sol>` | stable / fresh |
| 2_5 | selected / conclusion | `#test-btn` / — | `dip-<sol>` / `res-<sol>` | stable / fresh |
| 2_6 | selected | `#mix-btn` | `mix-<sol>` | stable |
| 2_6 | tested (smell result) | — | `res-<sol>` | fresh |
| 2_6 | toggle reset | `#mix-btn` | `mix-<sol>` | stable |
| 2_3 | idle | `.tab:not(.active)` | `explore` | stable |
| 2_3 | substance tapped | `[data-substance]` | `sub-<id>` | fresh |
| 2_3 | bitter-gourd tapped | `[data-substance="bitter-gourd"]` | `myth-<t>` | fresh |
| 2_7 | start / mixed | `#mix-btn` / — | `mix-start` / `mix-done` | stable / fresh |
| 2_8 | normal | `#bite-btn` | `s-normal` | stable |
| 2_8 | bitten | `#treat-btn` | `s-bitten` | fresh |
| 2_8 | treated | — | `s-treated` | fresh |
| 2_8 | reset | `#bite-btn` | `s-normal` | stable |
| 2_9 | idle | `.soil-btn` | `soil-pick` | stable |
| 2_9 | type picked | `#treat-btn` | `soil-<type>` | fresh |
| 2_9 | treated | — | `soil-done` | fresh |
| 2_10 | initial | `#release-btn` | `s-initial` | stable |
| 2_10 | polluted | `#treat-btn` | `s-polluted` | fresh |
| 2_10 | treated | — | `s-treated` | fresh |
| 2_10 | reset | `#release-btn` | `s-initial` | stable |

## Per-sim hooks

### 2_1 — Hidden Message Revealed  *(shape S)*
Spray phenolphthalein 3× to reveal a message written in a basic solution.
- Before any spray: glow `#spray-btn` — *"Spray the indicator on the blank paper — tap 3 times."* (`key: spray-start`)
- After each spray (`sprayCount` 1,2): *"Keep spraying — the message appears as more indicator lands. N of 3."* (live update, stable key)
- Revealed: *"Phenolphthalein turns pink on a base — the invisible ink was a basic solution."* (fresh key, speaks the *why*)

### 2_2 — Litmus Paper Test  *(shape G, archetype)*
9 solutions (acidic / basic / neutral) → Dip → blue & red litmus + conclusion.
- On select, before dip: glow `#test-btn` — *"You picked <name>. Dip the papers to see which litmus changes."* (`key: dip-<solution>`)
- When `conclusion` is set (**at the delayed conclusion update, not the click**): read it back and explain — acidic → *"Blue litmus turned red → <name> is an acid."*; basic → *"Red litmus turned blue → <name> is a base."*; neutral → *"Neither paper changed → <name> is neutral."* (fresh key, speaks)
- Progress nudge: track which types have been tested; if only acids tested, glow a basic solution button and say *"Now try a base — soap or lime water — to see the opposite."*
- **Soft done (all G-sims):** once **≥1 acid and ≥1 base** have been tested, emit a calm completion line with `key: done` (mirrors `science_3_1`) — *"You've tested an acid and a base and seen both litmus changes. That's the core idea."* No glow.

### 2_3 — Properties of Acids & Bases  *(shape I)*
Tabs (`.tab[data-panel]`) + substance cards (`[data-substance]`); includes **bitter-gourd = NOT a base** misconception.
- Idle: glow `.tab:not(.active)` (the next unopened tab) — *"Open each tab, then tap substances to sort them acid / base / neutral."*
- On a substance: restate its class + one property (*"Soap feels slippery and turns red litmus blue — that's a base."*).
- **Misconception guard:** tapping `[data-substance="bitter-gourd"]` → **fresh-key** speak *"Bitter, but not a base — taste alone doesn't decide. Only the litmus/pH test does."*
- Glow targets are the `.tab`/`[data-substance]` elements above — not generic "tabs".

### 2_4 — Red Rose Indicator  *(shape G)*
Same select→test→observe as 2_2, natural indicator (rose petal extract).
- Glow `#test-btn`; on result, tie colour to class: *"The rose extract went <colour> → <name> is <acidic/basic>. Natural indicators work like litmus."*
- Same acid-and-base nudge.

### 2_5 — Turmeric Indicator  *(shape G, +paper state)*
State also tracks `currentPaper`. Turmeric turns **red only with a base**; it stays yellow for **both acids and neutrals**.
- Glow `#test-btn`; on result:
  - base → *"Turmeric went red → <name> is a base. Only a base reddens turmeric."*
  - acid/neutral → *"Turmeric stayed yellow → <name> is **not a base** (it's acidic or neutral). Yellow alone can't tell acid from neutral — that's what litmus is for."*
- `why`/`detail` must keep this: **yellow ≠ acid**; yellow = acidic **or** neutral, red = base (mirrors the sim's own takeaway).
- Classic case worth a line: soap/base → red (why a turmeric stain reddens with soap).

### 2_6 — Olfactory Indicators  *(shape G/S)*
Onion is the smell indicator; `#mix-btn`, `tested` flag. Data: `smellRemains:true` = **acidic** (tamarind, vinegar), `smellRemains:false` = **basic** (baking soda, soap). Mix button **toggles** (tested → reset).
- Glow `#mix-btn` — *"Smell indicators work with acids and bases. Mix the onion and check the smell."*
- On `tested`, spell out **both branches explicitly** (result key, speaks):
  - smell **remains** → *"The onion smell is still there → <name> is an **acid**. Acids keep the smell."*
  - smell **disappears** → *"The onion smell is gone → <name> is a **base**. Bases destroy the smell."*
- On toggle-back to reset: idle line + stable start key.

### 2_7 — Neutralisation Reaction  *(shape S)*
Single guided animation: acid + base → salt + water (`#mix-btn`).
- Glow `#mix-btn` — *"Mix the acid and the base and watch what forms."*
- After mix: *"Acid + base → salt + water. The strong properties cancel — that's neutralisation."* (fresh key, speaks)

### 2_8 — Treating Ant Bites  *(shape S, archetype)*
State `normal → bitten → treated` (+ reset). Formic acid → baking-soda base.
- `normal`: glow `#bite-btn` — *"An ant bite injects formic acid. Simulate the bite."* (stable start key)
- `bitten`: glow `#treat-btn` — *"Skin is acidic (~pH 4). Apply a base — baking soda — to neutralise it."*
- `treated`: *"The base neutralised the acid → back to pH 7, pain gone. Neutralisation in daily life."* (fresh key)
- reset → back to the `normal` idle line + stable start key.

### 2_9 — Soil Treatment  *(shape S)*
Pick soil type → treat with the opposite. `.soil-btn[data-type]` (`acidic`/`basic`) + `#treat-btn`. UI has **two** treatments only: **lime** and **compost**.
- Idle: glow the `.soil-btn`s — *"Is the soil too acidic or too basic? Pick one."*
- After pick: glow `#treat-btn` — acidic soil → *"Add **lime** (a base) to raise the pH."*; basic soil → *"Add **compost** (acidic) to lower the pH."*
- Treated: *"Neutralising the soil lets crops grow — neutralisation in agriculture."*

### 2_10 — Industrial Waste Treatment  *(shape S)*
State machine `initial → polluted → treated` (+ reset). Buttons: `#release-btn` ("Release Untreated"), `#treat-btn` ("Neutralise First"), `#reset-btn`. Acidic effluent neutralised before it reaches water.
- `initial`: glow `#release-btn` — *"Factories release acidic waste. Release it to see the effect."* (stable start key)
- `polluted`: glow `#treat-btn` — *"Neutralise the acid with a base before it reaches the river."*
- `treated`: *"Treated to ~pH 7 → safe for aquatic life. Neutralisation protects our waters."* (fresh key)
- reset → back to the `initial` idle line + stable start key.

---

## Kannada twins (`_kn`)

Each `_kn` currently has **no** `<script src="edu-coach.js">`. For every twin:
1. Add `<script src="edu-coach.js"></script>` before `</body>` (as done for `science_3_1_kn`).
2. Port the same hook, with `line`/`voice`/`hint`/`why`/`detail` written in Kannada.
3. Keep element IDs/keys identical to the English twin (only the copy differs).

## Verification plan (per file)
- `node --check` on each extracted `<script>`.
- Confirm exactly one `edu-coach.js` include and one hook block per file.
- Spot-check 2–3 on the deployed origin in Chrome (glow lands on the intended control; result lines speak) using the established chunked-payload harness — the glow/speak mechanism is already validated on `science_3_1` and math ch8/ch9, so this is a sanity pass, not full re-validation.

## Build order (suggested)
**Language policy:** do **2_2** and **2_8** in **both en + kn** first (they lock the two templates in both languages). For the remaining sims, build all English hooks, then do one Kannada copy pass — stated once here so it isn't contradictory later.
1. Archetypes first, **en + kn**: **2_2** (G) and **2_8** (S) — lock both templates in both languages; you review before the roll-out.
2. Roll G to 2_4, 2_5, 2_6; roll S to 2_1, 2_7, 2_9, 2_10 (English).
3. **2_3** (I) — bespoke misconception handling (English).
4. Kannada copy pass across the remaining 8 twins.

## Build notes — Kannada mistranslation fixes (found & fixed during wiring)

The machine-translated `_kn` twins had **code tokens** (not just display text) translated into Kannada. These were mostly internally consistent (the sim still ran) but broke CSS class styling and would have broken the shared coach logic (`sol.type==='basic'`, `findSolBtn('basic')`, `testedTypes`). Normalized the **logic tokens** back to canonical English, leaving genuine display text in Kannada:

- **2_2_kn** — `type:'basic'` was `'ಮೂಲ'` (3×) + CSS `.metric-value.ಮೂಲ` + className strings → normalized. (Kannada word "ಮೂಲ" had also been used for the display word *"Original"* — those were left as-is.)
- **2_5_kn** — `type:'ಮೂಲ'` (3×) + two className strings + one CSS selector → normalized. (The translated **property key** `ಬದಲಾಗುತ್ತದೆ` for the `changes` boolean is internally consistent and was left; `type` is display-only in this sim.)
- **2_6_kn** — `type:'ಮೂಲ'` (2×) + CSS selector + the base-result className → normalized.
- **2_9_kn** — the **basic-soil button** had `data-type="ಮೂಲ"` and `class="soil-btn ಮೂಲ"` (+ its CSS) → normalized to `basic` so `selectedSoil==='basic'` and `.soil-btn.basic` resolve.
- **2_3_kn** — `type:'ಮೂಲ'` (2×) → `'Basic'` (this sim uses capitalized `Acidic/Basic/Neither/Neutral`).
- **2_7_kn** — base-result className + its CSS selector → normalized (cosmetic).

Left intentionally (consistent Kannada display-class pairs, render correctly, untouched by the coach): 2_5_kn legend `.legend-color.ಮೂಲ`, 2_9_kn `.soil-ಸೂಚಕ.ಮೂಲ`, and the "Original" text `ಮೂಲ`.

All 10 `_kn` twins were also missing the `<script src="edu-coach.js">` include — added to each.

## Verification done
- `node --check` on all 20 extracted scripts → 20/20 OK.
- Each file publishes `window.__eduRound` at its guide/idle and result/step points; each has exactly one `edu-coach.js` include and the `color-scheme: light` meta.
- **Not yet done: live Chrome glow/speak check** — these are local edits; the deployed origin (`main` via GitHub Pages) must be updated first. The glow/array-glow/speak mechanism itself is already validated (science_3_1, math ch8/ch9), so this is a post-deploy spot-check, not full re-validation.

## Deploy
GitHub Pages serves `main`; after push, ~5 min CDN cache + clear the app WebView cache.
