# Science Sim Fixes — "Uses of Electricity" + app-wide contrast

**Status: BUILT (applied to the sim files; verified `node --check` + meta sweep count). Ready to commit.**
Covers the two issues raised for the Science simulation ("microwave shows a parcel icon; changes have no
feedback / white background + white text") and the broader white-on-white problem across all sims.

> Numbering note: the sim in question is `science_3_1.html` (+ `science_3_1_kn.html`), title *"Uses of
> Electricity – Interactive Classification."* There is no separate English "Science ch1" sim (the only
> `science_1_*` file is a single Kannada Light-&-Shadow sim). This doc records the actual files changed.

---

## 1. App-wide white-on-white fix (all 273 sims)

**Symptom.** Some sims render as white text on a white background (invisible) inside the Android app.

**Root cause.** Android WebView **force-dark** (algorithmic darkening) inverts a light page's text toward
white while leaving light card backgrounds light → white-on-white. Sims that relied on the *default* text
colour (no explicit `color`) were the most affected.

**Fix.** Declare the page as light-scheme so the WebView leaves each sim's own palette alone:

```html
<meta name="color-scheme" content="light">
```

Inserted after `<meta charset>` in **all 273** `Simulations/*.html` files (1 already had it → skipped). This
is the standard, declarative opt-out of forced-dark — one line per file, no per-sim CSS rewrites.

**Scope / verification.**
- 272 files edited, 1 skipped; `grep` confirms 273/273 now carry the meta.
- Effect only manifests in the Android WebView (force-dark on), **not** desktop Chrome — so there is nothing
  meaningful to Chrome-test; correctness is by construction (standard WebView behaviour).
- The single Kannada Light-&-Shadow sim (`science_1_1_kn.html`) additionally got explicit dark `color`s on
  its text-over-white elements as belt-and-suspenders.

---

## 2. `science_3_1` — Uses of Electricity (en + kn)

**What the sim is.** Classification game: pick a category (Cooking / Lighting / Heating-Cooling /
Communication / Entertainment / Transport), then tap the appliances that belong to it. 12 appliances, live
score bar, results + takeaway on completion.

### Issue A — Microwave icon was a parcel
`appliances[]` had `{ name: 'Microwave', icon: '📦' }` — 📦 reads as a package/parcel.
**Fix:** `📦 → 🍲` in both `science_3_1.html` and `science_3_1_kn.html`. (There is no true "microwave"
emoji; 🍲 reads as cooking/heating and is distinct from the kettle 🫖.)

### Issue B — "changes have no feedback / attribution nowhere to be seen"
The sim was wired to `edu-coach.js` (EN) but had **no `__eduRound` hook**, and its markup has no
`.mission/.problem/.question` the generic scraper can read — so the coach never reacted, and a wrong tap only
shook briefly with no explanation. The Kannada twin wasn't even wired to the engine.

**Fix — a native one-clock coach hook (`updateCoach()`):**
- **Guides + glows:** when a category is selected, the coach glows every appliance that belongs to it
  (array glow) and shows *"Tap the N appliances that belong to <category>."* The line updates as you sort;
  when the category is emptied it says *"pick another category."*
- **Wrong-tap attribution (the missing feedback):** tapping an appliance into the wrong category now names
  where it actually belongs — *"Microwave isn't here — it belongs to Cooking."*
- **Completion:** *"All 12 appliances classified…"*
- Wired `edu-coach.js` into the **Kannada** twin (it wasn't before) and wrote the coach copy in Kannada.

**Key policy:** stable per-category key (`cat-<id>`) so the line updates live as items are sorted without
re-speaking; wrong taps use a fresh key so each gives spoken attribution; array `glow` is the deployed
multi-glow (no engine change).

### Verification
- `node --check` on both extracted scripts → OK.
- Icon (`🍲`), `edu-coach.js` wiring, and `updateCoach` each present exactly once per file.
- Live Chrome not run for the coach glow here (same array-glow mechanism already validated in math ch8/ch9),
  but can be spot-checked on the deployed origin if wanted.

---

## Files changed
- **All** `Simulations/*.html` — `color-scheme: light` meta (273 files).
- `Simulations/science_3_1.html` — icon fix + `updateCoach()` hook + wrong-tap attribution.
- `Simulations/science_3_1_kn.html` — icon fix + `updateCoach()` hook (Kannada) + engine wiring.

## Deploy
GitHub Pages serves `main`; after push, allow ~5 min CDN cache and clear the app WebView cache to pick up
the new files.
