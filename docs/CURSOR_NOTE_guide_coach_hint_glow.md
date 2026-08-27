# Cursor verify — guide-coach.js: hint + glow (Science Ch.5–8 EN, Ch.3–8 KN)

**Date:** 2026-08-26 · **Files:** `Simulations/guide-coach.js` (changed), `scripts/check-coach-coverage.js`
(gate), plus Ch.3–4 KN HTML coach wiring.

> **Deploy status:** Shipped in the commit that added hint+glow (replaces text-only `d8e9c4f` on Pages).
> Confirm live with: `curl` / fetch of `guide-coach.js` contains `resolveTarget` and `inCoachUI`.
> Earlier "pass" results in this note were **local static checks** (ASCII / syntax / coverage gate /
> round-shape); on-device hint/glow is the remaining proof after Pages updates.

## What changed and why

`guide-coach.js` is the shared script that drives the V4 coach on sims that don't publish their own
`window.__eduRound` (Ch.2–4 do; Ch.5–8 and several KN chapters never did). Its **first version was
text+voice only** — it published a `line` per step and nothing else, so those sims showed a coach line
but **no hint and no glow**, while Ch.2–4 had the full experience. That's the gap the user hit.

This change makes `guide-coach.js` publish the richer round the guides already support:

- **hint** — from `coach.whenStuck[]` (rotated per step). Present in **every** guide (EN + KN), so hint
  works everywhere.
- **glow** — from a step's `target` label, resolved to an on-page control via `resolveTarget()`:
  tries `querySelector(label)`, then matches the visible text of clickable elements
  (`button,[role=button],[onclick],a,.btn,.control-btn,.option,…`), exact-then-substring, case-insensitive.
  Example: guide `target:"Start"` → `#start-btn` whose text is "🔥 Start Heating".
- **why** — from `coach.whenWrong[0]` (feeds edu-coach's "Explain" panel).
- Progression follows the glow: clicking the glowed control advances the step; otherwise any sim
  interaction advances. **Coach-UI clicks are excluded** (`#__eduBar,#__eduKnowMore,#__eduModal,
  [data-hint],[data-m]`) so tapping "Hint"/"Explain" drives edu-coach's reveal instead of skipping a step.
- Still **stands down** if the page publishes its own native round (never fights a hand-authored coach).

### edu-coach staging (why glow may appear on the 2nd beat, by design)
`edu-coach.present()` (lines ~325–345): when a round has a `hint`, in the default `ask` mode it shows the
**hint first with no glow** and reveals `line + glow` only after `curLevel>=1` (learner taps Hint / the
app's Hint affordance). This is the **same Socratic staging Ch.2–4 use** — `guide-coach` now matches it,
it is not a bug. In-app the coach is a native card, so its taps are not DOM clicks and don't advance.

## How to verify (local static checks — all pass; on-device needs the push first)

```bash
cd EduAI_app
# 1) static
grep -nP "[^\x00-\x7F]" Simulations/guide-coach.js   # expect: no output (pure ASCII)
node --check Simulations/guide-coach.js              # syntax OK

# 2) coverage gate unaffected (Science 0 silent)
node scripts/check-coach-coverage.js                 # expect: PASS, silentScience:0

# 3) round shape: mission -> steps(hint,target) -> done  (any Ch.5–8 guide)
node -e 'const g=require("./Simulations/science_7_2.guide.json");const c=g.coach||{};
 const st=(g.steps||[]).map(s=>({t:s.text,tg:s.target||null}));
 console.log("mission:",c.mission); console.log("whenStuck#:",(c.whenStuck||[]).length);
 st.forEach((s,i)=>console.log("step"+(i+1),"target="+JSON.stringify(s.tg)));'

# 4) label matcher (exact-then-substring, case-insensitive)
#    "Start" should match an element whose text is "🔥 Start Heating"; "Burn Mg" absent -> null
```

**On-device / browser check after push (the real proof):**
1. `curl -I https://anuragmn.github.io/EduAI_app/Simulations/guide-coach.js` → `200`.
2. Open a Ch.7 EN sim (Heat): coach shows mission → step lines; a **Hint** is available; on the step whose
   guide has a `target` (e.g. `science_7_2` step "Start"), after the hint reveal the control **glows**.
3. Open a Ch.3/4 KN sim: coach + hint appear in Kannada. Glow will be **sparse** (see limitation).

## Known limitations (important — verify expectations, not just code)

- **Glow depends on guide `target` data.** ~25% of **EN** steps have a `target`; **KN guides mostly have
  none** (e.g. `science_7_9_kn` → `[]`), so KN glow is limited until targets are added to those guides.
  This is a **content gap, not a code gap**. Hint is unaffected (works everywhere).
- **EN Ch.3–4 are NOT guide-coach** — they publish their own native rounds and set glow/hint in the page.
  If glow/hint is broken *there specifically*, it's a separate per-page bug in those 20 HTML files, not in
  `guide-coach.js`. Audit them directly if reported.
- The coverage gate checks **presence of a coach**, not **hint/glow parity** (it can't — glow is runtime
  DOM-dependent). Treat parity as a manual/on-device check.

## Deploy
```bash
git add Simulations/guide-coach.js docs/CURSOR_NOTE_guide_coach_hint_glow.md
git commit -m "guide-coach: add hint (whenStuck) + best-effort glow (target)"
git push
```
Shared file → all guide-coach sims (EN Ch.5–8, KN Ch.3–8) update at once; no per-page edits, no Play update.

## Why this was missed (honest post-mortem)
The first `guide-coach.js` was scoped to end the **silent coach** on Ch.5–8 as fast as possible, and shipped
"text+voice only, no glow" as an explicit but **under-flagged** limitation. The guides already carried the
`whenStuck` (hint) and `target` (glow) data — the first version simply ignored them, producing a coach that
was visibly **weaker than Ch.2–4** without that being called out or gated. And the coverage gate I added
verifies *"a coach line appears,"* not *"the coach has hint/glow parity with Ch.2–4"* — so it went green while
the experience was still degraded. Root cause: **we verified presence, not parity.** Fix here restores hint
everywhere and glow wherever the guide provides a target; the residual KN-glow gap is now explicitly a
content task (add `target`s to KN guides), not a hidden regression.
