/*!
 * edu-coach.js  —  V5 shared coaching engine for EduAI simulations.
 *
 * Include ONCE per sim, just before </body>:   <script src="edu-coach.js"></script>
 *
 * What it does: reads the live sim, works out the next move, and coaches it — glow the
 * thing to tap + a short on-screen line + a short spoken line, all on one 300ms clock so
 * they can never drift out of sync.
 *
 * Two homes, one engine:
 *   - Inside the app (window.AndroidBridge present): it glows the DOM itself and routes the
 *     line/voice to the native coach bar + TTS (AndroidBridge.coachText / coachSpeak). It sets
 *     window.__eduV5 = true so the app's built-in scraper stands down and lets this drive.
 *     It only coaches when the app has unlocked the guide (window.__eduCoachV4Wanted).
 *   - Standalone in a browser: it renders its own coach bar + uses the browser's speech.
 *
 * Safe by design: if it can't understand a screen it shows nothing (no wrong glow). New sim
 * types are handled by adding a branch to solve() here — never per-sim.
 */
(function () {
  'use strict';
  if (window.__eduV5Loaded) return; window.__eduV5Loaded = true;
  window.__eduV5 = true; // tell the app's own coach loop to yield to this engine.

  var IN_APP = !!(window.AndroidBridge && window.AndroidBridge.coachText);
  function AB() { return window.AndroidBridge; }

  /* ---------- number helpers ---------- */
  var UN = { thousand: 1e3, lakh: 1e5, lakhs: 1e5, crore: 1e7, crores: 1e7, million: 1e6, billion: 1e9 };
  function nums(t) { return (t.match(/\d[\d,]*/g) || []).map(function (s) { return +s.replace(/,/g, ''); }); }
  function vals(t) {
    var re = /(\d[\d,]*(?:\.\d+)?)\s*(thousand|lakhs?|crores?|million|billion)?/gi, m, o = [];
    while ((m = re.exec(t))) {
      if (!m[1]) continue;
      var b = parseFloat(m[1].replace(/,/g, '')); if (isNaN(b)) continue;
      var u = (m[2] || '').toLowerCase();
      o.push(Math.round(b * (u ? (UN[u] || UN[u.replace(/s$/, '')] || 1) : 1)));
    }
    return o;
  }
  function fmtIN(n) {
    var s = '' + Math.abs(n); if (s.length <= 3) return s;
    var h = s.slice(0, -3), tl = s.slice(-3), g = '', c = 0;
    for (var i = h.length - 1; i >= 0; i--) { g += h[i]; c++; if (c % 2 === 0 && i) g += ','; }
    return g.split('').reverse().join('') + ',' + tl;
  }
  function fmtINTL(n) { return ('' + n).replace(/\B(?=(\d{3})+(?!\d))/g, ','); }
  function bC(seq) {
    if (seq.length < 2) return [];
    var d0 = seq[0], r = seq[1] / seq[0], df = seq[1] - seq[0], o = [];
    if (seq.every(function (x, i) { return i === 0 || x === seq[i - 1] * 10 + d0; })) o.push(seq[seq.length - 1] * 10 + d0);
    if (seq[0] !== 0 && r === Math.floor(r) && seq.every(function (x, i) { return i === 0 || x === seq[i - 1] * r; })) o.push(Math.round(seq[seq.length - 1] * r));
    if (seq.every(function (x, i) { return i === 0 || x - seq[i - 1] === df; })) o.push(seq[seq.length - 1] + df);
    return o;
  }
  function pC(seq) {
    var o = bC(seq).slice();
    var rt = seq.map(function (n) { return Math.round(Math.sqrt(n)); });
    if (rt.length >= 2 && rt.every(function (r, i) { return r * r === seq[i]; })) { var b = bC(rt); if (b.length) o.push(b[0] * b[0]); }
    return o;
  }
  function fB(re) { return [].slice.call(document.querySelectorAll('button,[role=button]')).filter(function (b) { return re.test((b.innerText || '').trim()); })[0] || null; }

  /* ---------- read the current round ---------- */
  function publish() {
    var mi = document.querySelector('.mission, .problem, .question'); if (!mi) return null;
    var prompt = (mi.innerText || '').replace(/\s+/g, ' ').trim();
    var els = [].slice.call(document.querySelectorAll('.opt,.choice,button[data-v]'));
    if (/km\/day|daily speed/i.test(prompt)) {
      [].slice.call(document.querySelectorAll('button,[role=button],.card,.option')).forEach(function (b) {
        if (/^[\d,]+\s*km\s*\/\s*day$/i.test((b.innerText || '').trim()) && els.indexOf(b) < 0) els.push(b);
      });
    }
    var opts = els.map(function (o) {
      return { id: o.getAttribute('data-v') || o.innerText.trim(), label: (o.innerText || '').trim().replace(/\s+/g, ' '), value: o.getAttribute('data-v'), el: o, selected: /(^|\s)(sel|selected|active|chosen|picked)(\s|$)/.test(o.className || '') };
    }).filter(function (o) { return o.label; });
    var fb = ((document.querySelector('.msg,.result,.feedback') || {}).innerText || '').trim();
    return { prompt: prompt, opts: opts, submitEl: fB(/check|lock|submit/i), resetEl: fB(/reset/i), nextEl: fB(/next/i), feedback: fb, round: ((document.querySelector('.v') || {}).innerText || '').trim(), phase: /correct|right|not\b|wrong|bigger|smaller|exact/i.test(fb) ? 'result' : 'answer' };
  }

  /* ---------- work out the next move (ported verbatim from the app's tested V4 solver) ---------- */
  function solve(r) {
    var t = r.prompt.toLowerCase(), L = r.opts.map(function (o) { return o.label; });
    if (r.opts.length && r.opts.every(function (o) { return ['<', '=', '>'].indexOf(o.label.trim()) >= 0; })) { var v = vals(r.prompt), a = v[0], b = v[1], an = a < b ? '<' : a > b ? '>' : '='; return { hint: 'Convert both to the same notation - which value is bigger?', hintVoice: 'Which number is bigger?', why: 'Rewrite both numbers in the same units — plain digits — first. Then the one with more digits is bigger; if the digit-counts are equal, compare left to right until they differ.', detail: 'Worked example: compare 30 thousand and 3 lakh.\nStep 1 — Write both in the same system as plain digits: 30 thousand = 30,000 and 3 lakh = 3,00,000.\nStep 2 — Count the digits. 30,000 has 5 digits; 3,00,000 has 6. More digits means a bigger number, so 3 lakh is the larger one.\nStep 3 — Pick the sign from the first number\'s point of view. 30,000 is smaller than 3,00,000, so we tap the “<” (less-than) sign.\nWhy it works: lakh, crore and million are just names for how many zeros a number has, so once both are in plain digits you can compare them fairly.\nTip: if two numbers have the SAME number of digits, compare them left to right, one digit at a time, until they differ.', glow: an, submitOnPick: 1, line: an === '=' ? 'They are equal - tap "=".' : 'Tap "' + an + '" - the first number is ' + (a < b ? 'smaller' : 'larger') + '.' }; }
    if (r.opts.length && r.opts.every(function (o) { return /^\d+(\.\d+)?\s*x$/i.test(o.label.trim()); })) { var vv = vals(r.prompt), rr = vv[0] / vv[1], bt = r.opts.reduce(function (m, o) { return Math.abs(parseFloat(o.label) - rr) < Math.abs(parseFloat(m.label) - rr) ? o : m; }); return { hint: 'Roughly how many times bigger is the first number than the second?', hintVoice: 'How many times bigger is it?', why: 'Divide the bigger number by the smaller to find how many times it fits. Round both numbers first so the division is quick to estimate.', detail: 'Worked example: how many times bigger is 1,24,42,373 than 16,84,222?\nStep 1 — Round both to friendly numbers: about 1.24 crore (124 lakh) and about 17 lakh.\nStep 2 — Divide the bigger by the smaller: 124 ÷ 17 ≈ 7.3.\nStep 3 — Choose the closest option, about 7.4×.\nWhy it works: “how many times bigger” is exactly what division measures — how many copies of the small number fit inside the big one.\nTip: rounding first keeps the division easy and the estimate still lands very close.', glow: bt.label, submitOnPick: 1, line: 'About ' + rr.toFixed(1) + ' times - tap "' + bt.label + '".' }; }
    if (L.some(function (l) { return /km\/day/i.test(l); }) && r.opts.length) { var d = nums(r.prompt)[0], dy = nums(r.prompt)[1], nd = d / dy, bs = r.opts.reduce(function (m, o) { return Math.abs(nums(o.label)[0] - nd) < Math.abs(nums(m.label)[0] - nd) ? o : m; }); return { hint: 'Distance divided by days gives the daily speed - which option is closest?', hintVoice: 'Distance divided by days - which is closest?', why: 'Speed = distance ÷ days. Work it out roughly, then pick the option closest to your estimate.', detail: 'Worked example: cover 3,84,400 km in 3,650 days.\nStep 1 — Speed needed = distance ÷ days ≈ 3,84,400 ÷ 3,650 ≈ 105 km/day.\nStep 2 — Pick the option closest to 105 → 100 km/day.\nWhy it works: total distance = speed × days, so rearranging gives speed = distance ÷ days.\nTip: round the numbers before dividing to estimate quickly.', glow: bs.label, submitOnPick: 1, line: 'Need about ' + Math.round(nd) + ' km/day - tap "' + bs.label + '".' }; }
    if (L.some(function (l) { return /indian|international/i.test(l); })) { var N = nums(r.prompt).sort(function (a, b) { return ('' + b).length - ('' + a).length; })[0], IN = fmtIN(N), IT = fmtINTL(N), cre = /indian\s+([\d,]+)\s+international\s+([\d,]+)/i, co = r.opts.filter(function (o) { var mm = cre.exec(o.label); return mm && mm[1].trim() === IN && mm[2].trim() === IT; })[0]; return { glow: co && co.label, line: 'Indian ' + IN + ', International ' + IT + ' - pick that card.' }; }
    if (/round|nearest/.test(t)) { var ov = r.opts.map(function (o) { return nums(o.label)[0]; }).filter(function (x) { return !isNaN(x); }).sort(function (a, b) { return a - b; }), gp = []; for (var i = 1; i < ov.length; i++) gp.push(ov[i] - ov[i - 1]); var pl = Math.min.apply(null, gp.filter(function (g) { return g > 0; })), Nn = Math.max.apply(null, nums(r.prompt).filter(function (x) { return x !== pl && ov.indexOf(x) < 0; })), ans = Math.round(Nn / pl) * pl, cor = r.opts.filter(function (o) { return nums(o.label)[0] === ans; })[0]; return { hint: 'Look at the digit after the rounding place - which multiple is nearest?', hintVoice: 'Which multiple is nearest?', why: 'Look at the part just after the rounding place and compare it to the halfway mark (e.g. 50,000 for a lakh). Halfway or more rounds up; less rounds down.', detail: 'Worked example: round 84,43,675 to the nearest lakh.\nStep 1 — Find the rounding place. “Nearest lakh” means we keep whole lakhs: the answer will be 84,00,000 or 85,00,000.\nStep 2 — Look at the part after the lakh place: 43,675.\nStep 3 — Compare it to half a lakh (50,000). 43,675 is LESS than 50,000, so we round DOWN and stay at 84,00,000.\nIf it had been 50,000 or more, we would round UP to 85,00,000.\nWhy it works: whichever whole lakh the number is closest to is the better estimate, and the halfway mark (50,000) is the tipping point.\nTip: rounding to the nearest ten-lakh or crore works the same way — just check the part after that place against its own halfway value.', glow: cor && cor.label, submitOnPick: 1, line: 'Rounds to ' + fmtIN(ans) + ' - tap it.' }; }
    if (/pattern|next product|next term/.test(t)) { var all = nums(r.prompt).filter(function (x) { return x > 0; }), ovp = r.opts.map(function (o) { return nums(o.label)[0]; }); for (var st = 0; st < all.length - 1; st++) { var sq = all.slice(st); if (sq.length < 2) break; var hit = pC(sq).filter(function (c) { return ovp.indexOf(c) >= 0; })[0]; if (hit != null) { var cp = r.opts.filter(function (o) { return nums(o.label)[0] === hit; })[0]; return { hint: 'What is the rule between the terms? Predict the next one.', hintVoice: 'What is the rule between the terms?', why: 'Find the rule that turns one term into the next (×10, add a digit, or square). Confirm it on two pairs, then apply it once more for the answer.', detail: 'Worked example: 3, 33, 333, …\nStep 1 — See how term 1 becomes term 2: 3 → 33 (multiply by 10 and add 3, i.e. put another 3 on the end).\nStep 2 — Check the same rule works for the next pair: 33 → 333. It does.\nStep 3 — Apply the rule once more: 333 → 3,333. That is the next term.\nStep 4 — Count its digits (3,333 has 4) and enter that if the sim asks.\nWhy it works: a pattern means one fixed rule links every pair of terms, so once you find the rule you can extend the sequence as far as you like.', glow: cp && cp.label, digits: ('' + hit).length, submitOnPick: 1, line: 'Next is ' + hit + ' (' + ('' + hit).length + ' digits).' }; } } return { hint: 'What is the rule between the terms?', line: 'Find the rule between the terms.' }; }
    // CALC (restricted calculator): one allowed button, press to reach target.
    var am = r.prompt.match(/allowed button[:\s]*\+?([\d,]+)/i); if (am) { var step = +am[1].replace(/,/g, ''), tmc = r.prompt.match(/target[:\s]*([\d,]+)/i), tg1 = tmc ? +tmc[1].replace(/,/g, '') : null, bigEl = document.querySelector('.mission .big, .big'), cur = bigEl ? +(((bigEl.innerText || '').match(/[\d,]+/) || ['0'])[0].replace(/,/g, '')) : 0, tapEl = document.querySelector('#tapBtn'), chkEl = document.querySelector('#checkBtn'); if (tg1 != null) { if (cur >= tg1) return { submitGlowEl: chkEl, line: 'Reached ' + fmtIN(tg1) + ' - tap Check.', vkey: 'ck' }; var rem = Math.max(0, Math.round((tg1 - cur) / step)); return { hint: 'How many taps of this button reach the target?', hintVoice: 'How many taps reach the target?', why: 'Each tap adds the same fixed amount. Divide the remaining gap by the step size to know exactly how many taps you need.', detail: 'Worked example: reach 9,000 from 0 when the only button is +1000.\nStep 1 — Find the gap: 9,000 − 0 = 9,000.\nStep 2 — Divide by the step size: 9,000 ÷ 1000 = 9 taps.\nStep 3 — Tap +1000 nine times, then press Check.\nWhy it works: pressing the same button again and again is repeated addition, which is multiplication — so the number of taps is just the gap divided by the step.', glowEl: tapEl, line: 'Tap +' + fmtIN(step) + ' - ' + rem + ' more (' + fmtIN(cur) + ' of ' + fmtIN(tg1) + ').', voice: 'Keep tapping ' + step + '.', vkey: 'calc' }; } }
    // MAXIMIZE A+B: greedy - biggest digits to the biggest place values.
    if (/maximize\s+a\s*\+\s*b/i.test(r.prompt)) { var am2 = r.prompt.match(/a\s*\((\d+)\s*digit/i), bm2 = r.prompt.match(/b\s*\((\d+)\s*digit/i), ml = am2 ? +am2[1] : 5, nl = bm2 ? +bm2[1] : 4, db = [].slice.call(document.querySelectorAll('button.digit')), used = db.filter(function (b) { return b.disabled || /used/.test(b.className); }).length, W = [], i, j; for (i = 0; i < ml; i++) W.push(Math.pow(10, ml - 1 - i)); for (j = 0; j < nl; j++) W.push(Math.pow(10, nl - 1 - j)); var ord = W.map(function (w, ix) { return { ix: ix, w: w }; }).sort(function (x, y) { return y.w - x.w || x.ix - y.ix; }), cards = db.map(function (b) { return +b.innerText.trim(); }).sort(function (a, b) { return b - a; }), seq = []; ord.forEach(function (o, rk) { seq[o.ix] = cards[rk]; }); var nd = seq[used], nb = db.filter(function (b) { return +b.innerText.trim() === nd && !b.disabled; })[0]; if (nb) return { hint: 'To make the largest sum, where should the biggest digits go?', hintVoice: 'Where do the biggest digits go?', why: 'A digit is worth more in a higher place, so put the biggest digits in the highest places. Share them between the two numbers, alternating from the largest.', detail: 'Worked example: make A + B largest using 9,8,7,6,5,4,3,2,1 (A = 5 digits, B = 4 digits).\nStep 1 — Sort the digits from biggest to smallest.\nStep 2 — Fill the highest place values first, alternating A and B: 9→A, 8→B, 7→A, 6→B, 5→A, then the rest.\nStep 3 — You get A = 97,531 and B = 8,642, giving the biggest possible sum.\nWhy it works: a digit in the ten-thousands place is worth 10,000×, but only 1× in the ones place — so the largest digits must sit in the highest places to count the most.', glowEl: nb, line: 'Biggest digits in the highest places - tap ' + nd + '.', voice: 'Tap ' + nd + '.', vkey: 'm' + used }; return { line: 'Place the biggest digits in the highest place values.', vkey: 'm' }; }
    // VAULT: rule-aware strategy hint.
    if (/vault rule|using digits/i.test(r.prompt)) { var big = /largest|biggest|greatest/i.test(r.prompt), sm = /smallest|least/i.test(r.prompt), end = ''; if (/multiple of 5/i.test(r.prompt)) end = ' It must end in 0 or 5.'; else if (/even/i.test(r.prompt)) end = ' It must end in an even digit.'; else if (/odd/i.test(r.prompt)) end = ' It must end in an odd digit.'; var ordr = big ? 'Place the biggest digits first, 9 down.' : sm ? 'Place the smallest digits first, no leading 0.' : 'Arrange the digits to fit the rule.'; return { hint: 'To hit the target, what order should the digits go in?', hintVoice: 'What order should the digits go in?', why: 'Place value decides size: sort digits high-to-low for the largest number (or low-to-high for the smallest), then fix the last digit for the rule.', detail: 'Worked example: make the LARGEST number from 4, 7, 1, 9 that is even.\nStep 1 — Read the rule. “Even” means the last digit must be 0, 2, 4, 6 or 8. Here only 4 qualifies, so 4 goes at the end.\nStep 2 — Arrange the remaining digits (9, 7, 1) biggest-first in the higher places: 9, 7, 1.\nStep 3 — Put it together: 9714.\nWhy it works: the leftmost digits control the size the most, so big digits go left; the rule only pins down the last digit, so satisfy it first, then maximise the rest.\nTip: for the SMALLEST number, sort low-to-high instead — but never start with 0.', line: ordr + end, voice: ordr, vkey: 'vault' }; }
    // TARGET DASH: concrete strategy hint.
    if (/build expression/i.test(r.prompt)) { var tv = r.prompt.match(/target value[:\s]*([\d,]+)/i), tt = tv ? tv[1] : 'the target'; return { line: 'Reach ' + tt + ': start with the biggest cards and x or -, then nudge with the small cards.', voice: 'Start with the biggest cards, then adjust.', vkey: 'expr' }; }
    // BUILD / place-value: add the biggest step that still fits.
    var cm = r.prompt.match(/current[:\s]*([\d,]+)/i), tm = r.prompt.match(/target[:\s]*([\d,]+)/i); if (cm && tm) { var tg = +tm[1].replace(/,/g, ''), c = +cm[1].replace(/,/g, ''); if (c === tg) return { ctrl: 'submit', line: 'You matched the target - tap Lock Build.', vkey: 'lock' }; if (c > tg) return { ctrl: 'reset', line: 'Over the target - tap Reset.', voice: 'Over the target - tap Reset.', vkey: 'reset' }; var bt2 = r.opts.map(function (o) { return +o.value; }).filter(function (x) { return !isNaN(x); }).sort(function (a, b) { return b - a; }), fit = bt2.filter(function (x) { return c + x <= tg; })[0], cb = r.opts.filter(function (o) { return +o.value === fit; })[0]; return { hint: 'Which is the biggest step that still fits under the target?', hintVoice: 'Which biggest step still fits?', why: 'Fill the highest place value first, then work downward. Always add the biggest button that still fits without overshooting — that reaches the target in the fewest taps.', detail: 'Worked example: build 5,072 using place-value buttons.\nStep 1 — Fill the biggest place first. Add +1000 five times → 5,000 (five taps).\nStep 2 — Move to the next place. The tens digit is 7, so add +10 seven times → 5,070.\nStep 3 — Finish with the ones. The ones digit is 2, so add +1 twice → 5,072. Done!\nWhy it works: each button matches one place value, and the digit in each place tells you exactly how many taps you need there. Adding the biggest button that still fits means you never waste taps or overshoot.\nTip: the fewest possible taps equals the sum of the digits (5 + 0 + 7 + 2 = 14 taps for 5,072).', glow: cb && cb.label, line: 'Add +' + fmtIN(fit) + ' (' + fmtIN(c) + ' of ' + fmtIN(tg) + ').', voice: 'Add ' + fit + '.', vkey: 'a' + fit }; }
    return { line: '' };
  }

  /* ---------- science (acids/bases): walk through testing EVERY substance ---------- */
  var ACIDBASE = { lemon: 'acid', 'lemon juice': 'acid', vinegar: 'acid', curd: 'acid', yogurt: 'acid', tamarind: 'acid', 'tamarind water': 'acid', orange: 'acid', 'orange juice': 'acid', tomato: 'acid', 'tomato juice': 'acid', 'citric acid': 'acid', 'hydrochloric acid': 'acid', apple: 'acid', grapes: 'acid', soap: 'base', 'soap solution': 'base', 'baking soda': 'base', 'washing soda': 'base', 'lime water': 'base', ammonia: 'base', 'sodium hydroxide': 'base', 'milk of magnesia': 'base', antacid: 'base', water: 'neutral', 'tap water': 'neutral', 'distilled water': 'neutral', sugar: 'neutral', 'sugar solution': 'neutral', salt: 'neutral', 'salt solution': 'neutral', 'common salt': 'neutral', milk: 'neutral' };
  function sciSel() { return document.querySelector('.solution-btn.active, .substance.active, .material.active, .option.active, .card.active, [class*=solution][class*=active]'); }
  function sciAction() { return [].slice.call(document.querySelectorAll('button,[role=button]')).filter(function (b) { var t = (b.innerText || '').toLowerCase(); return /dip|mix|test|smell|add|pour|rub|check|react/.test(t) && t.length < 30; })[0]; }
  function sciName(el) { return ((el && el.innerText) || '').replace(/\(.*?\)/g, '').replace(/[^a-zA-Z\s]/g, '').replace(/\s+/g, ' ').trim(); }
  function sciClass(el) { var label = ((el && el.innerText) || '').replace(/\s+/g, ' ').trim(); var m = label.match(/\((acid|base|basic|neutral)\)/i); return m ? m[1].toLowerCase() : ACIDBASE[sciName(el).toLowerCase()]; }
  function sciSubs() { var els = [].slice.call(document.querySelectorAll('.solution-btn, .substance, .material, .sample')); var seen = [], out = []; els.forEach(function (e) { var n = sciName(e); if (n && seen.indexOf(n) < 0) { seen.push(n); out.push(e); } }); return out; }
  function scienceSolve() {
    var subs = sciSubs(); if (!subs.length) return null;
    var key = subs.map(sciName).sort().join('|'); if (window.__eduSciKey !== key) { window.__eduSciKey = key; window.__eduSciDone = {}; }
    if (!window.__eduSciBound) { window.__eduSciBound = true; document.addEventListener('click', function (ev) { var b = ev.target && ev.target.closest ? ev.target.closest('button,[role=button]') : null; if (!b) return; if (/dip|mix|test|smell|add|pour|rub|react/i.test(b.innerText || '')) { var s = sciSel(); if (s) { var nm = sciName(s); if (nm) { window.__eduSciDone = window.__eduSciDone || {}; window.__eduSciDone[nm] = true; } } } }, true); }
    var done = window.__eduSciDone || {}, act = sciAction(), an = act ? ((act.innerText || 'test').replace(/[^\w\s]/g, '').trim()) : 'test';
    var next = subs.filter(function (b) { return !done[sciName(b)]; })[0];
    if (!next) return { line: 'You tested them all! Acids turn litmus red, bases turn it blue, neutral stays the same.', voice: 'You have tested them all.', vkey: 'sci-done:' + key };
    var nm = sciName(next), active = sciSel();
    if (active && sciName(active) === nm) { var cls = sciClass(active), norm = cls ? (cls.indexOf('acid') >= 0 ? 'an acid' : cls.indexOf('neutral') >= 0 ? 'neutral' : 'a base') : null; if (norm) return { glowEl: act, line: nm + ' is ' + norm + ' - tap ' + an + ' to test it.', voice: nm + ' is ' + norm + '.', vkey: 'sci:' + nm }; return { glowEl: act, line: 'Tap ' + an + ' to test ' + nm + ' and watch the result.', voice: '', vkey: 'sci:' + nm }; }
    return { glowEl: next, line: 'Next, test ' + nm + ' - tap it.', voice: 'Now test ' + nm + '.', vkey: 'sci-sel:' + nm };
  }

  /* ---------- science: "material tester" walkthrough (conductor/insulator etc.) ----------
   * Answer is declared in the markup, e.g. onclick="testMaterial('spoon','🥄',true,this)"
   * where the boolean = is-conductor. Testing adds a .tested class. Walk through every material. */
  function materialSolve() {
    var items = [].slice.call(document.querySelectorAll('.material-btn, [onclick*="testMaterial"]'));
    if (!items.length) return null;
    function nm(el) { var t = (el.innerText || '').replace(/[^a-zA-Z\s]/g, '').replace(/\s+/g, ' ').trim(); if (t) return t; var oc = el.getAttribute('onclick') || ''; var m = oc.match(/\(\s*'([^']+)'/); return m ? m[1] : 'this'; }
    function ans(el) { var oc = el.getAttribute('onclick') || ''; var m = oc.match(/,\s*(true|false)\s*[,)]/); return m ? (m[1] === 'true') : null; }
    var next = items.filter(function (el) { return !/\btested\b/.test(el.className); })[0];
    if (!next) return { line: 'You tested them all! Metals conduct electricity; non-metals like plastic, wood and rubber do not.', voice: 'You tested them all.', vkey: 'mat-done' };
    var name = nm(next), a = ans(next);
    var kind = a === true ? 'a conductor - the bulb glows' : a === false ? 'an insulator - no current flows' : null;
    if (kind == null) return { glowEl: next, line: 'Tap ' + name + ' to test it.', voice: 'Test ' + name + '.', vkey: 'mat:' + name };
    return { glowEl: next, line: 'Tap ' + name + ' - it is ' + kind + '.', voice: name + ' is ' + (a ? 'a conductor' : 'an insulator') + '.', vkey: 'mat:' + name };
  }

  /* ---------- math ch2 (arithmetic expressions): count terms + evaluate ---------- */
  function findActiveExpr() { var els = [].slice.call(document.querySelectorAll('[class*=active], .selected, .current, .expr, .display, .problem-display')); for (var i = 0; i < els.length; i++) { var tx = (els[i].innerText || '').replace(/\s+/g, ' ').trim(); if (tx.length < 40 && /\d/.test(tx) && /[×÷*\/+−\-]/.test(tx) && /^[\d\s×÷*\/+−\-.]+$/.test(tx)) return tx; } return null; }
  function evalExpr(s) { var t = s.replace(/\s+/g, ''); var parts = t.split(/(?=[+\-])/); var total = 0, cnt = 0; parts.forEach(function (p) { if (!p) return; cnt++; var sg = 1; if (p[0] === '+') p = p.slice(1); else if (p[0] === '-') { sg = -1; p = p.slice(1); } var fs = p.split(/([*\/])/); var v = parseFloat(fs[0]); for (var k = 1; k < fs.length; k += 2) { var op = fs[k], n = parseFloat(fs[k + 1]); if (op === '*') v *= n; else v /= n; } if (!isNaN(v)) total += sg * v; }); return { terms: cnt, value: total }; }
  function exprSolve() { var a = findActiveExpr(); if (!a) return null; var nrm = a.replace(/−/g, '-').replace(/×/g, '*').replace(/÷/g, '/').replace(/[^\d+\-*\/.]/g, ''); if (!/[+\-*\/]/.test(nrm) || !/\d/.test(nrm)) return null; var r = evalExpr(nrm); if (isNaN(r.value)) return null; var ts = r.terms + ' term' + (r.terms === 1 ? '' : 's'); return { line: a + ' -> ' + ts + ', value = ' + r.value + '.', voice: ts + ', value ' + r.value + '.', vkey: 'expr:' + a }; }

  /* ---------- rendering + routing ---------- */
  var G = [], bar = null, lastText = '', lastVoice = '', lastRK = '', hi = null, pv = null;
  function pkV() { if (pv) return pv; try { var vs = speechSynthesis.getVoices() || []; pv = vs.filter(function (v) { return /en[-_]?IN/i.test(v.lang); })[0] || vs.filter(function (v) { return /^en/i.test(v.lang); })[0] || null; } catch (e) {} return pv; }
  function say(txt) { if (!txt) return; try { speechSynthesis.cancel(); var u = new SpeechSynthesisUtterance(('' + txt).replace(/(?<=\d),(?=\d)/g, '')); u.rate = 1.05; var v = pkV(); if (v) u.voice = v; speechSynthesis.speak(u); } catch (e) {} }
  function clearGlow() { G.forEach(function (e) { try { e.style.outline = ''; e.style.outlineOffset = ''; } catch (x) {} }); G = []; if (hi) { var o = hi.getAttribute('data-ph'); if (o !== null) { hi.setAttribute('placeholder', o); hi.removeAttribute('data-ph'); } hi = null; } }
  function glow(el, kind) { if (!el) return; el.style.outline = '3px solid ' + (kind === 'submit' ? '#2e9e6b' : kind === 'input' ? '#5b8bff' : kind === 'answer' ? '#e5484d' : '#ff9500'); el.style.outlineOffset = '2px'; el.style.borderRadius = '8px'; G.push(el); }
  function setBar(txt) { if (IN_APP) return; if (!bar) { bar = document.createElement('div'); bar.id = '__eduBar'; bar.style.cssText = 'position:fixed;left:8px;right:8px;bottom:8px;z-index:2147483647;max-height:40vh;overflow:auto;background:#0e1230;color:#fff;font:600 15px/1.4 system-ui,sans-serif;padding:12px 16px;border-radius:12px;box-shadow:0 -6px 24px rgba(0,0,0,.45)'; document.body.appendChild(bar); } bar.style.display = txt ? 'block' : 'none'; bar.textContent = txt ? ('Coach: ' + txt) : ''; }
  // Speak numbers as WORDS so TTS never reads "+10000" as "plus one oh oh oh".
  function n2w(n) {
    n = Math.round(Math.abs(n)); if (n === 0) return 'zero';
    var o = ['', 'one', 'two', 'three', 'four', 'five', 'six', 'seven', 'eight', 'nine', 'ten', 'eleven', 'twelve', 'thirteen', 'fourteen', 'fifteen', 'sixteen', 'seventeen', 'eighteen', 'nineteen'];
    var t = ['', '', 'twenty', 'thirty', 'forty', 'fifty', 'sixty', 'seventy', 'eighty', 'ninety'];
    function tw(x) { return x < 20 ? o[x] : t[Math.floor(x / 10)] + (x % 10 ? ' ' + o[x % 10] : ''); }
    function th(x) { return x < 100 ? tw(x) : o[Math.floor(x / 100)] + ' hundred' + (x % 100 ? ' ' + tw(x % 100) : ''); }
    var p = [];
    if (n >= 1e7) { p.push(tw(Math.floor(n / 1e7)) + ' crore'); n %= 1e7; }
    if (n >= 1e5) { p.push(tw(Math.floor(n / 1e5)) + ' lakh'); n %= 1e5; }
    if (n >= 1e3) { p.push(tw(Math.floor(n / 1e3)) + ' thousand'); n %= 1e3; }
    if (n > 0) p.push(th(n));
    return p.join(' ');
  }
  function speakNums(s) { return ('' + s).replace(/([+])(?=\d)/g, '').replace(/\b\d[\d,]*\b/g, function (m) { var v = +m.replace(/,/g, ''); return isNaN(v) ? m : n2w(v); }); }
  function emit(text, voice, vkey) {
    if (text !== lastText) { lastText = text; if (IN_APP) { try { AB().coachText(text); } catch (e) {} } else setBar(text); }
    var vk = vkey || voice || text;
    if (vk && vk !== lastVoice) { lastVoice = vk; if (voice) { var vv = speakNums(voice); if (IN_APP) { try { AB().coachSpeak(vv); } catch (e) {} } else say(vv); } }
  }

  function pubEl(sel) { if (!sel) return null; if (sel.nodeType) return sel; try { return document.querySelector(sel); } catch (e) { return null; } }
  // A sim can drive the coach directly by setting window.__eduRound = { native:true, line, voice?, glow?, submit?, input?, inputHint?, key?, glowKind? }.
  // This is the per-sim "customized" path: correct by construction, since the sim knows its own answer.
  // Socratic staging: when an item carries a `hint` (a question), show the hint first with NO glow;
  // only after the learner lingers on the same move (they're stuck) reveal the exact move + glow.
  // Items with no hint are shown directly. `key` identifies the move, so a new move restarts the cycle.
  var REVEAL_MS = 7000, curKey = null, keyT = 0, revealed = false;

  // "Know more": a small pill that opens an enlarged, scrollable panel with the full explanation.
  // Rendered in the page (WebView), so it works in-app and standalone with no app changes.
  var pill = null, modal = null, curDetail = null, curDetailText = '', dpv = null;
  function esc(s) { return ('' + s).replace(/[&<>]/g, function (c) { return { '&': '&amp;', '<': '&lt;', '>': '&gt;' }[c]; }); }
  function ensurePill() {
    if (pill) return;
    pill = document.createElement('button'); pill.id = '__eduKnowMore'; pill.textContent = 'ⓘ Explain';
    pill.style.cssText = 'position:fixed;right:10px;top:calc(env(safe-area-inset-top, 0px) + 10px);z-index:2147483646;background:#4b3fbf;color:#fff;border:none;border-radius:20px;padding:8px 14px;font:600 13px system-ui,sans-serif;box-shadow:0 3px 12px rgba(0,0,0,.35);cursor:pointer';
    pill.onclick = showModal; document.body.appendChild(pill);
  }
  function dPickVoice() { if (dpv) return dpv; try { var vs = speechSynthesis.getVoices() || []; dpv = vs.filter(function (v) { return /en[-_]?IN/i.test(v.lang); })[0] || vs.filter(function (v) { return /^en/i.test(v.lang); })[0] || null; } catch (e) {} return dpv; }
  function speakDetail() { if (!curDetailText) return; try { speechSynthesis.cancel(); var u = new SpeechSynthesisUtterance(speakNums(curDetailText)); u.rate = 1.0; var v = dPickVoice(); if (v) u.voice = v; speechSynthesis.speak(u); } catch (e) {} }
  function stopDetail() { try { speechSynthesis.cancel(); } catch (e) {} }
  function mkBtn(txt, fn) { var b = document.createElement('button'); b.textContent = txt; b.style.cssText = 'background:#eef0ff;color:#3a34a5;border:1px solid #d7d9f5;border-radius:16px;padding:7px 13px;font:600 13px system-ui,sans-serif;cursor:pointer'; b.onclick = fn; return b; }
  function showModal() {
    if (!curDetail) return;
    if (!modal) {
      modal = document.createElement('div'); modal.id = '__eduModal';
      modal.style.cssText = 'position:fixed;inset:0;z-index:2147483647;background:rgba(6,8,24,.55);display:flex;align-items:center;justify-content:center;padding:18px';
      var card = document.createElement('div');
      card.style.cssText = 'background:#fff;color:#101433;max-width:540px;width:100%;max-height:82vh;overflow:auto;border-radius:16px;padding:14px 22px 24px;box-shadow:0 14px 44px rgba(0,0,0,.45);font:400 16px/1.55 system-ui,sans-serif';
      var close = mkBtn('✕', function () { stopDetail(); modal.style.display = 'none'; });
      close.style.cssText = 'float:right;background:none;border:none;color:#999;font-size:22px;font-weight:700;cursor:pointer';
      var controls = document.createElement('div'); controls.style.cssText = 'display:flex;gap:8px;margin:4px 0 14px;flex-wrap:wrap';
      controls.appendChild(mkBtn('▶ Listen', speakDetail));
      controls.appendChild(mkBtn('⏹ Stop', stopDetail));
      controls.appendChild(mkBtn('↻ Replay', function () { stopDetail(); speakDetail(); }));
      var body = document.createElement('div'); body.id = '__eduModalBody';
      card.appendChild(close); card.appendChild(body); card.appendChild(controls); modal.appendChild(card);
      modal.onclick = function (e) { if (e.target === modal) { stopDetail(); modal.style.display = 'none'; } };
      document.body.appendChild(modal);
    }
    document.getElementById('__eduModalBody').innerHTML = curDetail; modal.style.display = 'flex';
    speakDetail(); // auto-read on open; Stop / Replay available
  }
  function hideKnowMore() { if (pill) pill.style.display = 'none'; if (modal) modal.style.display = 'none'; stopDetail(); }
  function fmtRich(s) { return esc(s).replace(/\n/g, '<br>'); }
  function setDetail(o) {
    if (!o.why && !o.detail && !o.line) { hideKnowMore(); curDetail = null; return; }
    var p = ['<h2 style="margin:0 0 12px;font-size:19px;color:#4b3fbf">Let’s understand this</h2>'];
    if (o.hint) p.push('<p style="margin:0 0 10px"><b>The question:</b> ' + esc(o.hint) + '</p>');
    if (o.line) p.push('<p style="margin:0 0 10px"><b>The answer:</b> ' + esc(o.line) + '</p>');
    if (o.why) p.push('<p style="margin:0 0 10px"><b>Why:</b> ' + esc(o.why) + '</p>');
    if (o.detail) p.push('<div style="margin:2px 0 4px;background:#f5f6ff;border-radius:10px;padding:12px 14px"><b style="color:#4b3fbf">How it works, step by step</b><br>' + fmtRich(o.detail) + '</div>');
    curDetail = p.join('');
    curDetailText = (o.hint ? ('The question. ' + o.hint + ' ') : '') + (o.line ? ('The answer. ' + o.line + '. ') : '') + (o.why ? ('Why. ' + o.why + ' ') : '') + (o.detail ? ('Here is how it works. ' + o.detail.replace(/\n/g, ' ')) : '');
    ensurePill(); pill.style.display = 'block';
  }

  function present(o) {
    var key = o.key || o.line || '', hasHint = !!o.hint, now = Date.now();
    if (key !== curKey) { curKey = key; keyT = now; revealed = false; }
    if (!hasHint) revealed = true; else if (!revealed && now - keyT >= REVEAL_MS) revealed = true;
    // At reveal, append the "why/how" reasoning (o.why) to both the on-screen line and the voice.
    var revealLine = (o.line || '') + (o.why ? '  ·  Why: ' + o.why : '');
    var text = (hasHint && !revealed) ? o.hint : revealLine;
    if (o.submit) glow(pubEl(o.submit), 'submit'); // confirming a choice isn't the answer -> show anytime
    if (revealed) {
      if (o.glow) glow(pubEl(o.glow), o.glowKind === 'answer' ? 'answer' : 'hint');
      if (o.input) { var ie = pubEl(o.input); if (ie) { glow(ie, 'input'); if (o.inputHint != null && !ie.value) { if (ie.getAttribute('data-ph') === null) ie.setAttribute('data-ph', ie.getAttribute('placeholder') || ''); ie.setAttribute('placeholder', '' + o.inputHint); hi = ie; } } }
    }
    var revealVoice = (o.voice != null ? o.voice : (o.line || '')); if (o.why) revealVoice = (revealVoice ? revealVoice + '. ' : '') + o.why;
    var voice = revealed ? revealVoice : (o.hintVoice != null ? o.hintVoice : o.hint);
    emit(text, voice, (revealed ? 'R:' : 'H:') + key);
    setDetail(o); // keep the "Know more" panel in sync with the current move
  }
  function renderNative(pub) { present(pub); }

  function tick() {
    var on = IN_APP ? !!window.__eduCoachV4Wanted : true;
    clearGlow();
    if (!on) { if (!IN_APP) setBar(''); hideKnowMore(); return; }
    var np = window.__eduRound;
    if (np && typeof np === 'object' && np.native && (np.line || np.glow || np.hint)) { present(np); return; }
    var r; try { r = publish(); } catch (e) { r = null; }
    var p = r ? (function () { try { return solve(r); } catch (e) { return { line: '' }; } })() : null;
    var thin = !r || (p && !p.line && !p.glow && !p.glowEl && !p.ctrl && !p.submitGlowEl && !p.submitOnPick);
    if (thin) { var sp = null; try { sp = exprSolve() || scienceSolve() || materialSolve(); } catch (e) { sp = null; } if (sp) { present({ line: sp.line, voice: sp.voice, glow: sp.glowEl, submit: sp.submitGlowEl, hint: sp.hint, hintVoice: sp.hintVoice, key: sp.vkey }); return; } }
    if (!r) { emit('', ''); return; }
    window.__eduRound = { line: '', scraped: true }; // presence marker; app already yields on __eduV5.
    var isR = r.phase === 'result', picked = r.opts.some(function (o) { return o.selected; });
    var o = { hint: p.hint, hintVoice: p.hintVoice, why: p.why, detail: p.detail };
    if (!isR) {
      if (p.glow) { var ge = r.opts.filter(function (x) { return x.label === p.glow; })[0]; if (ge) o.glow = ge.el; }
      if (p.glowEl) o.glow = p.glowEl;
      if (p.submitGlowEl) o.submit = p.submitGlowEl;
      if (p.ctrl === 'submit') o.submit = r.submitEl;
      if (p.ctrl === 'reset') o.glow = r.resetEl || r.nextEl;
      if (p.submitOnPick && picked) o.submit = r.submitEl;
      if (p.digits) { o.input = 'input[type=number],input.digit,.digit input,input:not([type=hidden])'; o.inputHint = 'Type ' + p.digits; }
    }
    var sn = r.submitEl ? (r.submitEl.innerText || 'Check').trim() : 'Check';
    o.line = isR ? r.feedback : ((p.line || '') + ((p.line && p.submitOnPick && picked && r.submitEl) ? ('  -  now tap "' + sn + '"') : ''));
    o.voice = isR ? r.feedback : (p.voice != null ? p.voice : p.line);
    o.key = p.vkey || ((r.prompt || '').replace(/current.*/i, '') + '#' + r.round);
    present(o);
  }

  if (window.__eduV5Iv) clearInterval(window.__eduV5Iv);
  window.__eduV5Iv = setInterval(tick, 300); tick();
  window.EduCoachV5 = { stop: function () { clearInterval(window.__eduV5Iv); clearGlow(); if (bar) bar.remove(); try { speechSynthesis.cancel(); } catch (e) {} }, tick: tick };
})();
