# Coach Spec — Science Ch2 "Electric Current and its Effects" (`science_3` family)

**Status: SPEC v2 (thorough; review blockers B1–B3 + EN/timing fixes folded in) — awaiting go-ahead to build.**
Target: the 10 `science_3_*.html` sims + their `_kn` twins (20 files).

> Chapter mapping: in the live app the **second** Science chapter surfaces the `science_3` sim family
> (Electricity). `science_2` (Acids/Bases) is the first and is already coached.

---

## Audit result (what exists today)

| | English sims | Kannada twins |
|---|---|---|
| `edu-coach.js` wired | ✅ all 10 | ❌ only `3_1` (9/10 unwired) |
| native `__eduRound` hook | ✅ **only `3_1`** | ✅ only `3_1` |
| `color-scheme: light` meta | ✅ all | ✅ all |

`science_3_1` (Uses of Electricity classification) was already coached in earlier work — **leave it, just re-verify**. The other 9 English sims load the engine but feed it nothing; the 9 `_kn` twins aren't wired at all. This spec covers 3_2–3_10 (en + kn) and a re-verify of 3_1.

---

## ⛔ Build order — Step 0: Kannada-twin readiness gate (HARD GATE, do before any hook)

The spec's per-sim copy assumes EN and KN twins share identical ids/keys. **That is false for this chapter until the twins are repaired.** Do not wire a `_kn` hook until its row here is green. (Same class of issue documented in `COACH_SPEC_science_ch1_acids.md` → "Build notes"; that checklist pattern applies here too.)

### B1 — `science_3_5_kn.html` is the **wrong lesson** *(blocker)*
- **Evidence:** EN `3_5` = *"Battery – Connecting Cells Together"* (`setCells(1/2/3)`, series voltage). KN twin title = **"ವಿದ್ಯುತ್ ವಾಹಕತೆ – ಸರ್ಕ್ಯೂಟ್ ಪರೀಕ್ಷಕ"** (*Electric Conductivity – Circuit Tester*) with `selectMaterial(copper/iron…)`. It is a **conductivity sim mis-filed as the battery twin**.
- **Consequence:** the EN 3_5 series-cells coach cannot be shared to `3_5_kn` at all.
- **Action (product call needed):** **rebuild `3_5_kn` from EN `3_5`** (translate the real battery sim), or renumber the mis-filed file. Until then, `3_5_kn` is **out of scope** and must not receive the 3_5 hook.

### B2 — Logic tokens translated into Kannada in several twins *(blocker per file)*
- **Confirmed:** `3_7_kn` has `id="btn-ಸ್ವಿಚ್"` + `placeComponent('ಸ್ವಿಚ್')`; `3_9_kn` has `showSymbol('ಬ್ಯಾಟರಿ')`, `showSymbol('ಸ್ವಿಚ್-open')`, `showSymbol('ಸ್ವಿಚ್-closed')` (mixed KN+EN keys).
- **Checked clean (English tokens):** `3_2_kn`, `3_3_kn` use English `onclick` tokens — but still grep every twin for `id=`/`data-*`/CSS-class translation before trusting it.
- **Consequence:** any coach that references `#btn-switch`, `placeComponent('switch')`, or `showSymbol('battery')` will **no-op** on these twins (glow target null, wrong branch).
- **Action:** normalize logic tokens back to canonical English (ids, `data-*`, function-arg keys, CSS class names), leaving genuine display text in Kannada — **then** wire.

### B3 — Per-twin gate checklist (copy of the acids pattern)
Before wiring any `_kn`:

1. `grep` the twin for Kannada in `id="…"`, `data-…="…"`, `onclick="fn('…')"` args, and `class="…"`/CSS selectors. Normalize any **logic** token to English.
2. Confirm the twin is the **same lesson** as its EN sibling (title + primary function names). If not → B1 path.
3. Confirm `edu-coach.js` is included (all 9 twins currently miss it — add it).
4. Only then port the hook; keep ids/keys identical to EN, translate only the six copy fields.

| Twin | Same lesson? | Token state | Gate |
|------|-------------|-------------|------|
| 3_2_kn | ✅ | EN tokens (verify ids) | wire after grep |
| 3_3_kn | ✅ | EN onclick tokens (verify) | wire after grep |
| 3_4_kn | verify | verify | grep first |
| 3_5_kn | ❌ **wrong lesson** | — | **B1: rebuild first** |
| 3_6_kn | verify | verify | grep first |
| 3_7_kn | ✅ | ❌ `ಸ್ವಿಚ್` tokens | **B2: normalize first** |
| 3_8_kn | verify | verify | grep first |
| 3_9_kn | ✅ | ❌ translated symbol keys | **B2: normalize first** |
| 3_10_kn | verify | verify | grep first |

---

## The enrichment bar (what "enriching" means here)

The native one-clock contract has six teaching fields. This chapter should use **all** of them, each with a distinct job, so the coach *teaches* rather than just points:

| Field | Job | Length | Enrichment rule |
|-------|-----|--------|-----------------|
| `line` | on-screen next step | ≤1 sentence | Concrete + names the control/observation ("Tap the switch — watch the bulb"). Never generic ("continue"). |
| `voice` | spoken `line` | ≤6 words | Action only ("Close the switch."). |
| `hint` | gentle "what to notice/do" (ask/guided modes) | 1 sentence | Points at the *mechanism*, not just the button ("Current only flows in a closed loop."). |
| `hintVoice` | spoken `hint` | ≤6 words | — |
| `why` | the concept in one line | 1 sentence | Real physics cause, not a restatement ("A cell pushes current from + through the circuit and back to −."). |
| `detail` | the full teach (on-demand) | 3–6 lines | **Step 1/2/3 → "Why it works" → "Tip"/real-world.** This is where the enrichment lives: mechanism + a concrete Indian/everyday example. |

**Chapter physics the copy must get right (and weave in):**
- A cell/battery has a **+ terminal (metal cap/bump)** and **− terminal (flat disc)**; current is driven from + around the circuit back to −.
- Current flows **only in a closed loop**; a switch **closes (ON)** or **breaks (OFF)** that loop.
- Cells in **series add voltage**: N cells ≈ N × 1.5 V → brighter bulb.
- **Incandescent** = heated filament (~2500 °C, wastes heat); **LED** = no filament, efficient, **needs correct polarity**.
- **Conductors** (metals) let current through; **insulators** (plastic, rubber, wood, glass) don't.
- **Sources**: hydro/solar/wind are **renewable**; **thermal (coal)** is non-renewable and polluting.
- Standard **circuit symbols** are universal (cell: long line = +; open vs closed switch; bulb = circle with cross).

`key` policy (as on `science_3_1`): **stable** key while a line updates live (no re-speak); **fresh** key (`<prefix>-<Date.now()>`) whenever the coach should *speak* a reaction (result revealed, quiz answered, circuit completed, misconception tapped).

---

## Coach shapes used in this chapter

- **T — Test/classify** (tap items, bulb/indicator reacts): `3_10` (conductors/insulators), and the already-built `3_1`.
- **E — Explore / build with state** (assemble or toggle, watch the circuit respond): `3_3` torch, `3_5` battery cells, `3_6` lamps, `3_7` build-a-circuit, `3_8` switch.
- **I — Info-explore** (cards/sources, read + compare): `3_2` sources, `3_9` symbols.
- **Q — Quiz** (choose the right answer, get feedback): `3_4` terminal, `3_9` circuit-behaviour question.

---

# Per-sim specs (with drafted enriched copy)

Each sim below gives the interaction model, the coach shape, the glow/key plan, and **ready-to-use draft copy** for the key states. English copy is shown; Kannada twins use the same structure (see "Kannada twins").

---

## 3_1 — Uses of Electricity *(BUILT — re-verify only)*
Classification game (already coached: glows appliances of the selected category, wrong-tap attribution, completion). **Action:** re-run `node --check` + confirm the hook still fires; **no rewrite.**
**Enrichment-guard exception:** 3_1's wrong-tap rounds are terse attribution lines (`"Microwave belongs to Cooking"`) and legitimately omit `why`/`detail`. The enrichment guard below is for the **new** 3_2–3_10 hooks — do **not** rewrite 3_1's classification wrong-taps to satisfy it. Classification wrong-taps run under a **lighter contract**: a correct, specific `line`/`voice` is sufficient; `why`/`detail` optional.

---

## 3_2 — Sources of Electricity *(shape I)*
**Interaction:** four source buttons — hydro 💧, solar ☀️, wind 🌬️, thermal 🏭 — `showSource(type)` swaps the SVG + info panel + energy-flow animation.
**Glow selector:** `.source-btn:not(.active)` (idle, array).
**Glow/key:** idle → glow the unselected source buttons (array), `key: sources`. On a source → fresh key `src-<type>` (speaks the *how* + renewable/non-renewable). **Coach owns "seen"** — it keeps its own `Set` of viewed sources (the sim doesn't track this); soft-done `key: done` once **thermal + ≥1 renewable** have both been viewed.

Draft copy — **idle** (note: don't overclaim "all spin a turbine" — solar doesn't):
- `line`: "Tap each source to see how it makes electricity — and which ones are renewable."
- `voice`: "Explore each source."
- `hint`: "Most sources spin a generator; solar is the exception — it converts light directly."
- `why`: "Electricity comes from a generator being spun — except solar, which converts sunlight straight to current."
- `detail`: "Step 1 — Tap a source.\nStep 2 — Read how it turns energy into electricity.\nStep 3 — Note renewable (sun, wind, water) vs non-renewable (coal, gas).\nWhy it works — spinning a magnet past coils makes current; solar cells skip the spinning and use light directly.\nTip — India uses coal for most power, but hydro (Bhakra Nangal) and solar are growing."

Draft copy — **on `solar`** (fresh key, the explicit no-turbine branch):
- `line`: "Solar: panels convert sunlight straight into electricity — no turbine, no moving parts. Renewable."
- `voice`: "Panels convert light directly."
- `why`: "A solar cell releases current when light hits it, so there's no turbine or generator to spin."
- `detail`: "Step 1 — Sunlight hits the solar cells.\nStep 2 — The cells release electric current directly.\nStep 3 — No turbine or moving parts are needed.\nWhy renewable — the sun shines daily and free.\nContrast — hydro, wind, and thermal all spin a turbine; solar does not."

Draft copy — **on `hydro`** (fresh key):
- `line`: "Hydro: falling dam water spins a turbine → generator → electricity. Renewable."
- `voice`: "Falling water spins a turbine."
- `why`: "Moving water carries energy that spins the generator; the water cycle refills it, so it's renewable."
- `detail`: "Step 1 — Water stored behind a dam falls with force.\nStep 2 — It spins a turbine linked to a generator.\nStep 3 — The generator produces electricity.\nWhy renewable — the water cycle keeps refilling the dam.\nExample — Bhakra Nangal Dam."

Draft copy — **on `thermal`** (fresh key, the contrast):
- `line`: "Thermal: burning coal heats water → steam spins the turbine. Non-renewable, polluting."
- `voice`: "Burning coal makes steam."
- `why`: "Fossil fuels release heat to make steam, but they run out and cause pollution."
- `detail`: "Step 1 — Coal, oil, or gas is burned.\nStep 2 — The heat boils water into steam.\nStep 3 — Steam spins the turbine + generator.\nWhy non-renewable — fossil fuels are finite and pollute.\nContrast — solar/wind/hydro don't burn fuel."

(Solar/wind analogous: solar = panels convert sunlight directly, no moving parts; wind = blades spin the generator.)

---

## 3_3 — Inside a Torch *(shape E)*
**Interaction:** `setMode('assembled'/'exploded')` to view parts; `toggleSwitch()` closes the loop → bulb lights; `showComponent(type)` highlights cells/lamp/switch.
**Glow selector:** the switch group / `#switch-toggle`.
**Glow/key:** idle → glow the switch group, `key: torch-idle`. Explode view encouraged first. On switch ON → fresh `torch-on` (bulb lights, closed loop). On OFF → `torch-off`.

Draft copy — **idle**:
- `line`: "A torch is a tiny circuit: cells, lamp, switch. Tap the switch to complete the loop."
- `voice`: "Tap the switch."
- `hint`: "The bulb only lights when the switch closes the loop between the cells and the lamp."
- `why`: "The cells push current through a closed loop; the switch decides if the loop is closed."
- `detail`: "Step 1 — Cells provide the energy (+ and − terminals).\nStep 2 — The lamp produces light when current flows.\nStep 3 — The switch closes or breaks the loop.\nWhy it works — current needs an unbroken path from + back to −.\nTip — open the exploded view to see how the parts line up."

Draft copy — **switch ON** (fresh key):
- `line`: "Loop closed — current flows from the cells through the lamp, and it glows."
- `voice`: "Closed loop — it glows."
- `why`: "Closing the switch completes the path, so current flows and heats the lamp filament."

---

## 3_4 — Electric Cell (terminals) *(shape Q)*
**Interaction:** inspect the cell; the sim's quiz literally asks **"Quick Check: Which terminal has the metal cap (bump)?"** with buttons **Positive (+)** / **Negative (−)** → `checkAnswer(btn, isCorrect)`. Correct = Positive (the metal cap); the flat disc is negative. **Align coach wording to the sim's phrasing** so coach and quiz don't diverge.
**Glow selector:** `.quiz-btn` (array).
**Glow/key:** idle → glow the two quiz buttons (array), `key: cell-q`. On answer → fresh `cell-a-<t>` (speaks the reason). Right and wrong both explain.

Draft copy — **idle / question** (matches the sim's question):
- `line`: "Quick check: which terminal has the metal cap (bump) — positive or negative?"
- `voice`: "Which terminal has the cap?"
- `hint`: "The metal cap (bump) is the + terminal; the flat disc is the − terminal."
- `why`: "A cell drives current out of its + terminal and back into its − terminal."
- `detail`: "Step 1 — Look at the two ends of the cell.\nStep 2 — The raised metal cap is the positive (+) terminal.\nStep 3 — The flat disc is the negative (−) terminal.\nWhy it matters — current leaves +, travels the circuit, and returns to −.\nTip — LEDs and many devices only work if + and − are the right way round."

Draft copy — **correct** (fresh key):
- `line`: "Correct — the raised metal cap is the + terminal; the flat disc is −."
- `voice`: "Right — the cap is positive."

Draft copy — **wrong** (fresh key, correction):
- `line`: "Not quite — the flat disc is negative. The raised metal cap is the + terminal."
- `voice`: "The cap is the positive end."

---

## 3_5 — Battery: connecting cells *(shape E)*
**Interaction:** `setCells(1/2/3)` → total voltage = N × 1.5 V shown; bulb gets brighter.
**Glow selector:** `#btn-1cell` / `#btn-2cells` / `#btn-3cells` (idle array).
**Glow/key:** idle → glow the cell buttons (array), `key: cells`. On a count → fresh `cells-<n>` (speaks the series-addition result). **Soft-done** (coach-owned Set of counts seen), `key: done`, once the learner has seen **≥2 different counts** (felt the change). *(⚠ `3_5_kn` is the wrong lesson — see B1; do NOT apply this hook to the twin.)*

Draft copy — **idle**:
- `line`: "Add cells and watch the voltage — and the brightness — add up."
- `voice`: "Add cells and watch."
- `hint`: "Joining + of one cell to − of the next stacks their voltages."
- `why`: "Cells in series add: two 1.5 V cells give 3 V, so more current flows and the bulb is brighter."
- `detail`: "Step 1 — One cell ≈ 1.5 V.\nStep 2 — Connect + of one to − of the next (series).\nStep 3 — Voltages add: 2 cells ≈ 3 V, 3 cells ≈ 4.5 V.\nWhy it works — each cell adds its push, so more current flows.\nTip — this is why a torch with 2–3 cells is brighter than one."

Draft copy — **on `setCells(3)`** (fresh key):
- `line`: "3 cells in series ≈ 4.5 V — the brightest here, because the pushes add up."
- `voice`: "Three cells, about 4.5 volts."

---

## 3_6 — Lamp types: Incandescent vs LED *(shape E)*
**Interaction (verified):** `selectLamp('incandescent'/'led')` only switches the **info panel** + active box; `toggleSwitch()` lights **both** lamps whenever ON (`selectedLamp` is set but **unused** in the toggle). **There is no polarity control** — polarity is stated as *text* in the LED info panel ("emits light when current flows in the correct direction"), and the incandescent panel says it works either way. So the coach must **not** frame LED polarity as "watch it fail" — treat it as a textbook fact in `why`/`detail`, and keep ON copy to efficiency/heat compare.
**Glow selector:** `#incandescent-box` / `#led-box` (idle array); the switch toggle for testing.
**Glow/key:** idle → glow the two lamp boxes (array), `key: lamp`. On select → stable `lamp-<type>` (guide to tap the switch). On toggle ON → fresh `lamp-on` (efficiency/heat compare, not polarity failure). **Soft-done** (coach-owned): once **both lamp types have been selected AND the switch tested** — not from independent lighting.

Draft copy — **idle**:
- `line`: "Pick a lamp, then tap the switch — compare how each makes light."
- `voice`: "Pick a lamp and test it."
- `hint`: "One glows by heating a wire; the other uses a chip and wastes far less heat."
- `why`: "Incandescent bulbs make light by heating a filament (lots of wasted heat); LEDs make light electronically and are efficient."
- `detail`: "Step 1 — Choose incandescent or LED to read how it works.\nStep 2 — Tap the switch to power the circuit.\nStep 3 — Compare heat and efficiency.\nWhy it works — a filament must reach ~2500 °C to glow; an LED emits light directly with little heat.\nFact — an incandescent works either way round; an LED only lights with correct polarity (+ to +, − to −)."

Draft copy — **switch ON** (fresh key; both lamps lit — compare, don't single one out):
- `line`: "Both light — but the filament runs white-hot and wastes energy, while the LED stays cool and efficient."
- `voice`: "The LED runs cool and efficient."
- `why`: "The filament converts most energy to heat; the LED converts far more to light, so it lasts longer and saves power."

Draft copy — **on `selectLamp('led')`** (stable; where the polarity fact belongs):
- `line`: "LED selected — no filament, just a chip. It's efficient, but only lights with correct polarity."
- `voice`: "LED — efficient, needs correct polarity."

---

## 3_7 — Build a simple circuit *(shape E, assembly state machine)*
**Interaction (verified):** `placeComponent('cell'/'bulb'/'switch'/'wire')` fills 4 slots; when all four placed, `#test-btn` enables; `testCircuit()` is a **toggle** — its label flips `🔌 Turn OFF` ↔ `⚡ Turn ON`, so it opens/closes the switch repeatedly (not one-shot). `resetCircuit()` clears the build.
**Glow selectors:** next missing of `#btn-cell` / `#btn-bulb` / `#btn-switch` / `#btn-wire`; then `#test-btn`.
**Glow/key:** guide the **next missing component** (glow its button), `key: build-<remaining>`; when all placed → glow `#test-btn`, `key: build-ready`; on test **toggle** → **`build-on`** (loop closed, bulb glows) or **`build-off`** (loop broken) — re-speak on each toggle, **not** a single `build-done`. `resetCircuit()` → back to the build guide (`build-<remaining>`).

Draft copy — **missing components** (stable, updates live):
- `line`: "Add the {remaining} to finish the loop — a circuit needs a cell, a bulb, a switch, and wires."
- `voice`: "Add the {next component}."
- `hint`: "Current needs one unbroken path from the cell's + terminal back to its −."
- `why`: "A bulb only lights inside a complete loop: source → path → back to source."
- `detail`: "Step 1 — Place the cell (the energy source, + and −).\nStep 2 — Add the bulb (it glows when current flows).\nStep 3 — Add the switch (to open/close the loop) and wires (the path).\nWhy it works — a break anywhere stops the current everywhere.\nTip — build all four, then test."

Draft copy — **all placed** (stable):
- `line`: "All four placed — tap Test to close the loop and light the bulb."
- `voice`: "Test the circuit."

Draft copy — **test → ON** (`build-on`, fresh key):
- `line`: "Complete loop — current flows from + through the bulb and switch back to −, so it glows."
- `voice`: "Closed loop — it glows."

Draft copy — **test → OFF** (`build-off`, fresh key):
- `line`: "Loop broken at the switch — no path, so the bulb goes dark. Turn it on again to close it."
- `voice`: "Open switch — it's dark."

---

## 3_8 — Electric switch *(shape E)*
**Interaction:** tap the switch → `toggleSwitch()` ON/OFF (bulb follows); `setSwitchType('lever'/'push'/'toggle')` changes the switch style. Note a short ~200 ms click animation.
**Glow selector:** `#switch-group`.
**Timing:** `isOn` flips synchronously but there's a 200 ms click anim — **publish after the state/visual settles** (end of `toggleSwitch`, after the bulb update), not mid-animation.
**Glow/key:** idle → glow the switch group, `key: sw-idle`. On ON → fresh `sw-on` (closed loop, current flows). On OFF → fresh `sw-off` (broken loop). Switch-type change → stable guide `sw-type-<t>`.

Draft copy — **idle / OFF**:
- `line`: "Tap the switch to close the gap — current only flows when the loop is closed."
- `voice`: "Close the switch."
- `hint`: "OFF leaves a gap in the loop; ON bridges it so current can pass."
- `why`: "A switch is just a controllable gap in the circuit: closed = current flows, open = it stops."
- `detail`: "Step 1 — OFF: the switch leaves a gap, breaking the loop.\nStep 2 — ON: it bridges the gap, closing the loop.\nStep 3 — The bulb follows the loop.\nWhy it works — current needs an unbroken path; the switch controls that one spot.\nTip — lever, push, and toggle switches all do the same job in different shapes."

Draft copy — **ON** (fresh key):
- `line`: "Closed — the gap is bridged, current flows, the bulb lights."
- `voice`: "Closed — it glows."

---

## 3_9 — Circuit symbols + quiz *(shapes I + Q)*
**Interaction:** 8 symbol cards — `showSymbol(type)` (cell, battery, bulb, LED, switch-open, switch-closed, wire, wire-cross); plus a quiz — `checkQuiz(btn, isCorrect)` "what happens if the switch is closed?" (answer: bulb glows).
**Glow selectors:** `.symbol-card:not(.active)` (idle array); `.quiz-btn` (quiz).
**⚠ Twin note:** `3_9_kn` has **translated symbol keys** (`showSymbol('ಬ್ಯಾಟರಿ')`, `'ಸ್ವಿಚ್-open'`) — normalize to English keys (B2) before the twin hook, or the `sym-<type>` branch/glow will mismatch.
**Glow/key:** idle → glow unopened symbol cards (array), `key: symbols`. On a symbol → fresh `sym-<type>` (names it + reading tip). Quiz question → glow quiz buttons `key: sym-q`; on answer → fresh `sym-a-<t>`.

Draft copy — **idle**:
- `line`: "Tap each symbol to learn how circuits are drawn — then answer the question."
- `voice`: "Explore the symbols."
- `hint`: "In a cell symbol the long line is +, the short line is −."
- `why`: "Engineers use the same symbols worldwide so any circuit diagram reads the same everywhere."
- `detail`: "Step 1 — Tap a symbol to see what it represents.\nStep 2 — Note cell (long line = +), bulb (circle with a cross), switch (gap = open, line = closed).\nStep 3 — Use them to read the quiz circuit.\nWhy it works — standard symbols make diagrams universal.\nTip — an open switch = gap = no current; closed = line = current."

Draft copy — **on `switch-closed`** (fresh key):
- `line`: "Closed switch — the line bridges the gap, so current can flow."
- `voice`: "Closed switch lets current flow."

Draft copy — **quiz correct** ("Bulb will glow", fresh key):
- `line`: "Right — closing the switch completes the loop, so the bulb glows."
- `voice`: "Correct — it glows."

Draft copy — **quiz wrong** (fresh key):
- `line`: "Closing the switch completes the loop — so the bulb glows (nothing breaks or melts)."
- `voice`: "It completes the loop — it glows."

---

## 3_10 — Conductors & Insulators *(shape T)*
**Interaction:** 8 material buttons — conductors (spoon 🥄, key 🔑, coin 🪙, foil 📄) light the bulb; insulators (plastic 📏, rubber 🧽, wood 🪵, glass 🥛) don't. `testMaterial(id, icon, isConductor)` runs a **`setTimeout` animation** before showing the result badge + bulb.
**Glow selector:** `.material-btn:not(.tested)` (idle array).
**Timing:** the result appears **after the `setTimeout` delay** (same class as the acids litmus dip) — publish the `mat-<id>` result **inside the timeout callback**, not on tap. Keep a pre-tap guide line until then.
**Glow/key:** idle → glow the untested material buttons (array), `key: materials`. On result (post-delay) → fresh `mat-<id>` (speaks conductor/insulator + why). **Soft-done** (coach-owned Sets, `key: done`) once **≥1 conductor and ≥1 insulator** tested (the sim's `conductors`/`insulators` arrays can be read, but the coach should keep its own to be safe).

Draft copy — **idle**:
- `line`: "Drop each material into the circuit — if the bulb lights, it's a conductor."
- `voice`: "Test each material."
- `hint`: "Metals let current pass; plastic, rubber, wood, and glass don't."
- `why`: "Conductors have free electrons that carry current; insulators hold their electrons tightly, so no current flows."
- `detail`: "Step 1 — Tap a material to place it in the gap.\nStep 2 — Bulb lights → conductor; stays dark → insulator.\nStep 3 — Try metals and non-metals.\nWhy it works — metals share free electrons that move as current; insulators don't.\nTip — that's why wires are metal inside and plastic (insulator) outside for safety."

Draft copy — **conductor tapped** (fresh key):
- `line`: "Bulb glows — {material} is a conductor; metals carry current."
- `voice`: "{material} is a conductor."

Draft copy — **insulator tapped** (fresh key):
- `line`: "Stays dark — {material} is an insulator; it blocks the current."
- `voice`: "{material} is an insulator."

Draft copy — **soft done**:
- `line`: "You've seen a conductor light the bulb and an insulator block it — that's the core idea."
- `voice`: "Conductors carry, insulators block."

---

# Glow / key reference table

Fresh = `<prefix>-<Date.now()>` (speaks); stable = fixed string (live-updates silently). **Soft-done keys are coach-owned** — the sims don't track "seen"; the coach keeps its own `Set`.

| Sim | Trigger | Glow selector | `key` | Fresh? |
|---|---|---|---|---|
| 3_2 | idle | `.source-btn:not(.active)` (array) | `sources` | stable |
| 3_2 | source picked | — | `src-<type>` | fresh |
| 3_2 | thermal + ≥1 renewable seen *(coach Set)* | — | `done` | fresh once |
| 3_3 | idle | switch group / `#switch-toggle` | `torch-idle` | stable |
| 3_3 | switch on / off | — | `torch-on` / `torch-off` | fresh |
| 3_4 | question | `.quiz-btn` (array) | `cell-q` | stable |
| 3_4 | answered | — | `cell-a-<t>` | fresh |
| 3_5 | idle | `#btn-1cell` / `#btn-2cells` / `#btn-3cells` | `cells` | stable |
| 3_5 | count set | — | `cells-<n>` | fresh |
| 3_5 | ≥2 counts seen *(coach Set)* | — | `done` | fresh once |
| 3_6 | idle | `#incandescent-box` / `#led-box` (array) | `lamp` | stable |
| 3_6 | lamp picked | that box | `lamp-<type>` | stable |
| 3_6 | switch toggled on | — | `lamp-on` | fresh |
| 3_6 | both selected + tested *(coach Set)* | — | `done` | fresh once |
| 3_7 | building | next missing `#btn-cell/bulb/switch/wire` | `build-<remaining>` | stable |
| 3_7 | all placed | `#test-btn` | `build-ready` | stable |
| 3_7 | test → ON / OFF (toggle) | — | `build-on` / `build-off` | fresh (each toggle) |
| 3_7 | reset | next missing btn | `build-<remaining>` | stable |
| 3_8 | idle / off | `#switch-group` | `sw-idle` | stable |
| 3_8 | on / off *(after settle)* | — | `sw-on` / `sw-off` | fresh |
| 3_8 | switch-type change | `#switch-group` | `sw-type-<t>` | stable |
| 3_9 | idle | `.symbol-card:not(.active)` (array) | `symbols` | stable |
| 3_9 | symbol tapped | — | `sym-<type>` | fresh |
| 3_9 | quiz | `.quiz-btn` (array) / answered | `sym-q` / `sym-a-<t>` | stable / fresh |
| 3_10 | idle | `.material-btn:not(.tested)` (array) | `materials` | stable |
| 3_10 | material result *(after `setTimeout`)* | — | `mat-<id>` | fresh |
| 3_10 | ≥1 conductor + ≥1 insulator *(coach Set)* | — | `done` | fresh once |

**Reset paths:** `3_7` `resetCircuit()` → back to `build-<remaining>` (handled). `3_6`/`3_8` toggle back to their off/idle line on OFF. `3_2`/`3_4`/`3_9`/`3_10`/`3_5`/`3_3` have no reset control (nothing to handle).

---

## Kannada twins (`_kn`)

For each of `3_2`–`3_10` (`3_1_kn` already done):
1. Add `<script src="edu-coach.js"></script>` before `</body>` (all 9 twins are currently unwired).
2. Port the same hook, with `hint/hintVoice/why/detail/line/voice` written in **Kannada**, keeping the physics accurate (terminals = ಧನ/ಋಣ, current = ವಿದ್ಯುತ್ ಪ್ರವಾಹ, closed loop = ಮುಚ್ಚಿದ ಮಂಡಲ, conductor = ವಾಹಕ, insulator = ಅವಾಹಕ, renewable = ನವೀಕರಿಸಬಹುದಾದ).
3. Keep element ids / `key`s identical to the English twin (only the copy differs).
4. **Watch for machine-translation of logic tokens** — as found across `science_2_*_kn` (e.g. `type:'basic'` → `'ಮೂಲ'`, translated `data-*` values, CSS class names). Before wiring each twin, grep for translated identifiers and normalize logic tokens back to English; leave genuine display text in Kannada. (See `COACH_SPEC_science_ch1_acids.md` → "Build notes" for the exact classes of issue.)

---

## Verification plan (per file)
- `node --check` on each extracted `<script>`.
- Exactly one `edu-coach.js` include and one hook block per file.
- **Enrichment guard:** every publish point in the **new** 3_2–3_10 hooks sets `window.__eduRound` with a `line` **and** at least one of `why`/`detail` populated (no bare pointers). *Exception:* classification wrong-tap rounds (3_1-style) run under the lighter contract (see 3_1) — a correct, specific `line`/`voice` suffices.
- Spot-check 2–3 on the deployed origin in Chrome (glow lands on the intended control; result/why lines speak). The glow/array-glow/speak mechanism is already validated (3_1, math ch8/ch9), so this is a sanity pass.

## Build order (suggested)
0. **HARD GATE (Step 0 above):** run the per-twin readiness gate (B1–B3). Resolve `3_5_kn` (wrong lesson — rebuild or renumber; product call) and normalize logic tokens in `3_7_kn` / `3_9_kn` (and any others found on grep) **before** touching KN hooks. English hooks are unaffected and can start immediately.
1. **Archetypes first:** `3_7` (E, build-a-circuit — richest state machine, note the **test toggle** → build-on/off) and `3_10` (T, conductors — clean classify, note the **post-`setTimeout`** publish) — English first, then KN **only if the twin passed Step 0**; you review before roll-out.
2. Roll **E** to `3_3`, `3_5`, `3_6`, `3_8`; roll **I** to `3_2`, `3_9`; **Q** into `3_4` (and the quiz half of `3_9`). English across all; KN per-twin as each passes Step 0.
3. Re-verify `3_1` (do **not** rewrite it to the enrichment guard).
4. Kannada copy pass across the twins that cleared Step 0. **`3_5_kn` stays out of scope** until B1 is resolved.

## Deploy
GitHub Pages serves `main`; after push, ~5 min CDN cache + clear the app WebView cache.
