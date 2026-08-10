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
    if (r.opts.length && r.opts.every(function (o) { return ['<', '=', '>'].indexOf(o.label.trim()) >= 0; })) { var v = vals(r.prompt), a = v[0], b = v[1], an = a < b ? '<' : a > b ? '>' : '='; return { hint: 'Compare ' + fmtIN(a) + ' and ' + fmtIN(b) + '. Which is larger — tap “<”, “=” or “>”?', hintVoice: 'Compare ' + fmtIN(a) + ' and ' + fmtIN(b) + '. Which is larger?', why: 'Rewrite both numbers in the same units — plain digits — first. Then the one with more digits is bigger; if the digit-counts are equal, compare left to right until they differ.', detail: 'Step 1 — Write both as plain digits: ' + fmtIN(a) + ' and ' + fmtIN(b) + '.\nStep 2 — ' + fmtIN(a) + ' has ' + ('' + a).length + ' digits and ' + fmtIN(b) + ' has ' + ('' + b).length + ' digits, so ' + (a === b ? 'they are equal' : (fmtIN(a > b ? a : b) + ' is the larger one')) + '.\nStep 3 — Reading from the first number, tap “' + an + '”.\nWhy it works — lakh, crore and million just name how many zeros a number has; once both are plain digits you can compare them fairly.\nTip — if the digit-counts match, compare left to right until they differ.', glow: an, submitOnPick: 1, line: an === '=' ? 'They are equal - tap "=".' : 'Tap "' + an + '" - the first number is ' + (a < b ? 'smaller' : 'larger') + '.' }; }
    if (r.opts.length && r.opts.every(function (o) { return /^\d+(\.\d+)?\s*x$/i.test(o.label.trim()); })) { var vv = vals(r.prompt), rr = vv[0] / vv[1], bt = r.opts.reduce(function (m, o) { return Math.abs(parseFloat(o.label) - rr) < Math.abs(parseFloat(m.label) - rr) ? o : m; }); return { hint: 'About how many times bigger is ' + fmtIN(vv[0]) + ' than ' + fmtIN(vv[1]) + '? Tap the closest option.', hintVoice: 'About how many times bigger is the first number than the second?', why: 'Divide the bigger number by the smaller to find how many times it fits. Round both numbers first so the division is quick to estimate.', detail: 'Step 1 — Divide the bigger by the smaller: ' + fmtIN(vv[0]) + ' ÷ ' + fmtIN(vv[1]) + '.\nStep 2 — That is about ' + rr.toFixed(1) + ', so the first is roughly ' + Math.round(rr) + ' times the second.\nStep 3 — Tap the option closest to ' + rr.toFixed(1) + '×.\nWhy it works — division counts how many copies of the smaller number fit inside the bigger one.\nTip — round both numbers first to estimate the division quickly.', glow: bt.label, submitOnPick: 1, line: 'About ' + rr.toFixed(1) + ' times - tap "' + bt.label + '".' }; }
    if (L.some(function (l) { return /km\/day/i.test(l); }) && r.opts.length) { var d = nums(r.prompt)[0], dy = nums(r.prompt)[1], nd = d / dy, bs = r.opts.reduce(function (m, o) { return Math.abs(nums(o.label)[0] - nd) < Math.abs(nums(m.label)[0] - nd) ? o : m; }); return { hint: 'Distance divided by days gives the daily speed - which option is closest?', hintVoice: 'Distance divided by days - which is closest?', why: 'Speed = distance ÷ days. Work it out roughly, then pick the option closest to your estimate.', detail: 'Worked example: cover 3,84,400 km in 3,650 days.\nStep 1 — Speed needed = distance ÷ days ≈ 3,84,400 ÷ 3,650 ≈ 105 km/day.\nStep 2 — Pick the option closest to 105 → 100 km/day.\nWhy it works: total distance = speed × days, so rearranging gives speed = distance ÷ days.\nTip: round the numbers before dividing to estimate quickly.', glow: bs.label, submitOnPick: 1, line: 'Need about ' + Math.round(nd) + ' km/day - tap "' + bs.label + '".' }; }
    if (L.some(function (l) { return /indian|international/i.test(l); })) { var N = nums(r.prompt).sort(function (a, b) { return ('' + b).length - ('' + a).length; })[0], IN = fmtIN(N), IT = fmtINTL(N), cre = /indian\s+([\d,]+)\s+international\s+([\d,]+)/i, co = r.opts.filter(function (o) { var mm = cre.exec(o.label); return mm && mm[1].trim() === IN && mm[2].trim() === IT; })[0]; return { glow: co && co.label, line: 'Indian ' + IN + ', International ' + IT + ' - pick that card.' }; }
    if (/round|nearest/.test(t)) { var ov = r.opts.map(function (o) { return nums(o.label)[0]; }).filter(function (x) { return !isNaN(x); }).sort(function (a, b) { return a - b; }), gp = []; for (var i = 1; i < ov.length; i++) gp.push(ov[i] - ov[i - 1]); var pl = Math.min.apply(null, gp.filter(function (g) { return g > 0; })), Nn = Math.max.apply(null, nums(r.prompt).filter(function (x) { return x !== pl && ov.indexOf(x) < 0; })), ans = Math.round(Nn / pl) * pl, cor = r.opts.filter(function (o) { return nums(o.label)[0] === ans; })[0]; return { hint: 'Round ' + fmtIN(Nn) + ' to the nearest ' + ((r.prompt.match(/nearest\s+((?:ten\s+)?(?:lakh|crore|thousand|hundred)s?|[\d,]+)/i) || [])[1] || 'given place').trim() + '. Which of the options is closest to ' + fmtIN(Nn) + '?', hintVoice: 'Which multiple is nearest?', why: 'Look at the part just after the rounding place and compare it to the halfway mark (e.g. 50,000 for a lakh). Halfway or more rounds up; less rounds down.', detail: 'Step 1 — We are rounding ' + fmtIN(Nn) + ' to the nearest ' + ((r.prompt.match(/nearest\s+((?:ten\s+)?(?:lakh|crore|thousand|hundred)s?|[\d,]+)/i) || [])[1] || 'given place').trim() + '.\nStep 2 — Look at the part just after that place and compare it to the halfway mark.\nStep 3 — ' + fmtIN(Nn) + ' rounds to ' + fmtIN(ans) + '.\nWhy it works — round to whichever multiple is nearest; the halfway value is the exact tipping point (below rounds down, at-or-above rounds up).', glow: cor && cor.label, submitOnPick: 1, line: 'Rounds to ' + fmtIN(ans) + ' - tap it.' }; }
    if (/pattern|next product|next term/.test(t)) { var _seqEls = [].slice.call(document.querySelectorAll('.sequence .pill, .sequence span, .seq .pill')), _seqNums = _seqEls.map(function (e) { return +(('' + e.innerText).replace(/,/g, '')); }).filter(function (x) { return !isNaN(x) && x > 0; }), all = _seqNums.length >= 2 ? _seqNums : nums(r.prompt).filter(function (x) { return x > 0; }), ovp = r.opts.map(function (o) { return nums(o.label)[0]; }); for (var st = 0; st < all.length - 1; st++) { var sq = all.slice(st); if (sq.length < 2) break; var hit = pC(sq).filter(function (c) { return ovp.indexOf(c) >= 0; })[0]; if (hit != null) { var cp = r.opts.filter(function (o) { return nums(o.label)[0] === hit; })[0]; return { hint: 'The sequence so far is ' + all.join(', ') + ', … What number comes next?', hintVoice: 'What number comes next in the sequence?', why: 'Find the rule that turns one term into the next (×10, add a digit, or square). Confirm it on two pairs, then apply it once more for the answer.', detail: 'Step 1 — Look at how each term becomes the next in ' + all.join(', ') + ', ….\nStep 2 — Find the rule (×10, add a digit, or square) and check it holds for two pairs.\nStep 3 — Apply the rule once more → ' + hit + ' (' + ('' + hit).length + ' digits).\nWhy it works — a pattern means one fixed rule links every pair of terms, so once you find it you can extend the sequence.', glow: cp && cp.label, digits: ('' + hit).length, submitOnPick: 1, line: 'Next is ' + hit + ' (' + ('' + hit).length + ' digits).' }; } } return { hint: 'What is the rule between the terms?', line: 'Find the rule between the terms.' }; }
    // CALC (restricted calculator): one allowed button, press to reach target.
    var am = r.prompt.match(/allowed button[:\s]*\+?([\d,]+)/i); if (am) { var step = +am[1].replace(/,/g, ''), tmc = r.prompt.match(/target[:\s]*([\d,]+)/i), tg1 = tmc ? +tmc[1].replace(/,/g, '') : null, bigEl = document.querySelector('.mission .big, .big'), cur = bigEl ? +(((bigEl.innerText || '').match(/[\d,]+/) || ['0'])[0].replace(/,/g, '')) : 0, tapEl = document.querySelector('#tapBtn'), chkEl = document.querySelector('#checkBtn'); if (tg1 != null) { if (cur >= tg1) return { submitGlowEl: chkEl, line: 'Reached ' + fmtIN(tg1) + ' - tap Check.', vkey: 'ck' }; var rem = Math.max(0, Math.round((tg1 - cur) / step)); return { hint: 'Target: ' + fmtIN(tg1) + '. The only button adds +' + fmtIN(step) + ' each tap, and you are at ' + fmtIN(cur) + '. How many taps will reach ' + fmtIN(tg1) + '?', hintVoice: 'How many taps reach the target?', why: 'Each tap adds the same fixed amount. Divide the remaining gap by the step size to know exactly how many taps you need.', detail: 'Step 1 — Find the gap to the target: ' + fmtIN(tg1) + ' − ' + fmtIN(cur) + ' = ' + fmtIN(tg1 - cur) + '.\nStep 2 — Divide by the step size: ' + fmtIN(tg1 - cur) + ' ÷ ' + fmtIN(step) + ' = ' + rem + ' taps.\nStep 3 — Tap +' + fmtIN(step) + ' ' + rem + ' times, then press Check.\nWhy it works — pressing the same button repeatedly is repeated addition, so the number of taps is just the gap divided by the step.', glowEl: tapEl, line: 'Tap +' + fmtIN(step) + ' - ' + rem + ' more (' + fmtIN(cur) + ' of ' + fmtIN(tg1) + ').', voice: 'Keep tapping ' + step + '.', vkey: 'calc' }; } }
    // MAXIMIZE A+B: greedy - biggest digits to the biggest place values.
    if (/maximize\s+a\s*\+\s*b/i.test(r.prompt)) { var am2 = r.prompt.match(/a\s*\((\d+)\s*digit/i), bm2 = r.prompt.match(/b\s*\((\d+)\s*digit/i), ml = am2 ? +am2[1] : 5, nl = bm2 ? +bm2[1] : 4, db = [].slice.call(document.querySelectorAll('button.digit')), used = db.filter(function (b) { return b.disabled || /used/.test(b.className); }).length, W = [], i, j; for (i = 0; i < ml; i++) W.push(Math.pow(10, ml - 1 - i)); for (j = 0; j < nl; j++) W.push(Math.pow(10, nl - 1 - j)); var ord = W.map(function (w, ix) { return { ix: ix, w: w }; }).sort(function (x, y) { return y.w - x.w || x.ix - y.ix; }), cards = db.map(function (b) { return +b.innerText.trim(); }).sort(function (a, b) { return b - a; }), seq = []; ord.forEach(function (o, rk) { seq[o.ix] = cards[rk]; }); var nd = seq[used], nb = db.filter(function (b) { return +b.innerText.trim() === nd && !b.disabled; })[0]; if (nb) return { hint: 'Make A + B as large as possible — A has ' + ml + ' digits and B has ' + nl + '. Which digit should go in the highest empty place next?', hintVoice: 'Where do the biggest digits go?', why: 'A digit is worth more in a higher place, so put the biggest digits in the highest places. Share them between the two numbers, alternating from the largest.', detail: 'Worked example: make A + B largest using 9,8,7,6,5,4,3,2,1 (A = 5 digits, B = 4 digits).\nStep 1 — Sort the digits from biggest to smallest.\nStep 2 — Fill the highest place values first, alternating A and B: 9→A, 8→B, 7→A, 6→B, 5→A, then the rest.\nStep 3 — You get A = 97,531 and B = 8,642, giving the biggest possible sum.\nWhy it works: a digit in the ten-thousands place is worth 10,000×, but only 1× in the ones place — so the largest digits must sit in the highest places to count the most.', glowEl: nb, line: 'Biggest digits in the highest places - tap ' + nd + '.', voice: 'Tap ' + nd + '.', vkey: 'm' + used }; return { line: 'Place the biggest digits in the highest place values.', vkey: 'm' }; }
    // VAULT: rule-aware strategy hint.
    if (/vault rule|using digits/i.test(r.prompt)) { var big = /largest|biggest|greatest/i.test(r.prompt), sm = /smallest|least/i.test(r.prompt), end = ''; if (/multiple of 5/i.test(r.prompt)) end = ' It must end in 0 or 5.'; else if (/even/i.test(r.prompt)) end = ' It must end in an even digit.'; else if (/odd/i.test(r.prompt)) end = ' It must end in an odd digit.'; var ordr = big ? 'Place the biggest digits first, 9 down.' : sm ? 'Place the smallest digits first, no leading 0.' : 'Arrange the digits to fit the rule.'; return { hint: 'To hit the target, what order should the digits go in?', hintVoice: 'What order should the digits go in?', why: 'Place value decides size: sort digits high-to-low for the largest number (or low-to-high for the smallest), then fix the last digit for the rule.', detail: 'Worked example: make the LARGEST number from 4, 7, 1, 9 that is even.\nStep 1 — Read the rule. “Even” means the last digit must be 0, 2, 4, 6 or 8. Here only 4 qualifies, so 4 goes at the end.\nStep 2 — Arrange the remaining digits (9, 7, 1) biggest-first in the higher places: 9, 7, 1.\nStep 3 — Put it together: 9714.\nWhy it works: the leftmost digits control the size the most, so big digits go left; the rule only pins down the last digit, so satisfy it first, then maximise the rest.\nTip: for the SMALLEST number, sort low-to-high instead — but never start with 0.', line: ordr + end, voice: ordr, vkey: 'vault' }; }
    // TARGET DASH: concrete strategy hint.
    if (/build expression/i.test(r.prompt)) { var tv = r.prompt.match(/target value[:\s]*([\d,]+)/i), tt = tv ? tv[1] : 'the target'; return { hint: 'Reach ' + tt + ' by combining the number cards with + − × ÷. What is your first move?', hintVoice: 'How can you reach ' + tt + '?', why: 'Use the biggest cards with × or − to cover most of the distance fast, then the small cards with + or − to land exactly.', detail: 'Step 1 — Note the target ' + tt + ' and pick out your biggest cards.\nStep 2 — Use a big card with × (to jump far) or − (to cut down) to get close to ' + tt + '.\nStep 3 — Use the small cards with + or − to close the last gap exactly.\nWhy it works — multiplication moves in big steps and addition or subtraction in small ones, so combining both lets you land precisely on the target.', line: 'Reach ' + tt + ': start with the biggest cards and × or −, then nudge with the small cards.', voice: 'Start with the biggest cards, then adjust.', vkey: 'expr' }; }
    // BUILD / place-value: add the biggest step that still fits.
    var cm = r.prompt.match(/current[:\s]*([\d,]+)/i), tm = r.prompt.match(/target[:\s]*([\d,]+)/i); if (cm && tm) { var tg = +tm[1].replace(/,/g, ''), c = +cm[1].replace(/,/g, ''); if (c === tg) return { ctrl: 'submit', line: 'You matched the target - tap Lock Build.', vkey: 'lock' }; if (c > tg) return { ctrl: 'reset', line: 'Over the target - tap Reset.', voice: 'Over the target - tap Reset.', vkey: 'reset' }; var bt2 = r.opts.map(function (o) { return +o.value; }).filter(function (x) { return !isNaN(x); }).sort(function (a, b) { return b - a; }), fit = bt2.filter(function (x) { return c + x <= tg; })[0], cb = r.opts.filter(function (o) { return +o.value === fit; })[0]; return { hint: 'Target: ' + fmtIN(tg) + '. You have built ' + fmtIN(c) + ', so ' + fmtIN(tg - c) + ' still to go. Which is the biggest button you can add without going over ' + fmtIN(tg) + '?', hintVoice: 'Which biggest step still fits?', why: 'Fill the highest place value first, then work downward. Always add the biggest button that still fits without overshooting — that reaches the target in the fewest taps.', detail: 'Step 1 — Target ' + fmtIN(tg) + '; you have ' + fmtIN(c) + ', so ' + fmtIN(tg - c) + ' to go.\nStep 2 — The biggest button that still fits is +' + fmtIN(fit) + ', so add it.\nStep 3 — Keep adding the biggest button that fits until you reach ' + fmtIN(tg) + '.\nWhy it works — filling the highest place value first reaches the target in the fewest taps (the total taps equals the sum of the digits).', glow: cb && cb.label, line: 'Add +' + fmtIN(fit) + ' (' + fmtIN(c) + ' of ' + fmtIN(tg) + ').', voice: 'Add ' + fit + '.', vkey: 'a' + fit }; }
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
  // Hint model (student-switchable, fixed): 'ask' = Try-first, 'guided' = Step-by-step,
  // 'self' = Self-explain, 'ondemand' = Answer-on-tap. Persisted in localStorage; the app can
  // also set window.__eduHintMode. Per-move disclosure level advanced by window.__eduHint().
  var curKey = null, curLevel = 0, HMODE = 'ask';
  try { HMODE = window.__eduHintMode || localStorage.getItem('edu_hint_mode') || 'ask'; } catch (e) {}
  window.__eduHint = function () { curLevel++; tick(); };
  window.__eduSetHintMode = function (m) { HMODE = m; window.__eduHintMode = m; try { localStorage.setItem('edu_hint_mode', m); } catch (e) {} curLevel = 0; tick(); };

  // "Know more": a small pill that opens an enlarged, scrollable panel with the full explanation.
  // Rendered in the page (WebView), so it works in-app and standalone with no app changes.
  var pill = null, modal = null, curDetail = null, curDetailText = '', dpv = null;
  function esc(s) { return ('' + s).replace(/[&<>]/g, function (c) { return { '&': '&amp;', '<': '&lt;', '>': '&gt;' }[c]; }); }
  function ensurePill() {
    if (pill) return;
    pill = document.createElement('button'); pill.id = '__eduKnowMore'; pill.textContent = 'ⓘ Explain';
    pill.style.cssText = 'position:fixed;right:14px;bottom:66px;z-index:2147483646;background:#4b3fbf;color:#fff;border:none;border-radius:20px;padding:8px 14px;font:600 13px system-ui,sans-serif;box-shadow:0 3px 12px rgba(0,0,0,.35);cursor:pointer';
    pill.onclick = showModal; document.body.appendChild(pill);
  }
  function dPickVoice() { if (dpv) return dpv; try { var vs = speechSynthesis.getVoices() || []; dpv = vs.filter(function (v) { return /en[-_]?IN/i.test(v.lang); })[0] || vs.filter(function (v) { return /^en/i.test(v.lang); })[0] || null; } catch (e) {} return dpv; }
  function speakDetail() {
    if (!curDetailText) return; var t = speakNums(curDetailText);
    if (IN_APP) { try { AB().coachSpeak(t); } catch (e) {} return; } // native TTS (WebView speechSynthesis has no voices in-app)
    try { speechSynthesis.cancel(); var u = new SpeechSynthesisUtterance(t); u.rate = 1.0; var v = dPickVoice(); if (v) u.voice = v; speechSynthesis.speak(u); } catch (e) {}
  }
  function stopDetail() { try { speechSynthesis.cancel(); } catch (e) {} if (IN_APP) { try { if (AB() && AB().coachStop) AB().coachStop(); } catch (e) {} } }
  function setExplainVisible(on) {
    if (IN_APP) { try { if (AB() && AB().coachExplainVisible) AB().coachExplainVisible(!!on); } catch (e) {} }
  }
  function closeExplain() {
    stopDetail();
    if (modal) modal.style.display = 'none';
    setExplainVisible(false);
  }
  window.__eduCloseExplain = closeExplain;
  function mkBtn(txt, fn) { var b = document.createElement('button'); b.textContent = txt; b.style.cssText = 'background:#eef0ff;color:#3a34a5;border:1px solid #d7d9f5;border-radius:16px;padding:7px 13px;font:600 13px system-ui,sans-serif;cursor:pointer'; b.onclick = fn; return b; }
  function showModal() {
    if (!curDetail) return;
    if (!modal) {
      modal = document.createElement('div'); modal.id = '__eduModal';
      modal.style.cssText = 'position:fixed;top:0;left:0;right:0;bottom:0;z-index:2147483647;background:rgba(6,8,24,.6);overflow-y:auto;-webkit-overflow-scrolling:touch;padding:0;display:none;align-items:flex-start;justify-content:center';
      var card = document.createElement('div');
      // Size to content (no vh/percentage height — unreliable in this WebView); the overlay scrolls if tall.
      // Leave bottom room so the slim native "Explaining…" strip stays tappable above the WebView.
      card.style.cssText = 'background:#fff;color:#101433;max-width:540px;width:calc(100% - 32px);margin:24px auto 72px;border-radius:16px;padding:16px 20px 24px;box-shadow:0 14px 44px rgba(0,0,0,.45);font:400 16px/1.55 system-ui,sans-serif';
      var close = mkBtn('✕', closeExplain);
      close.style.cssText = 'float:right;background:none;border:none;color:#999;font-size:22px;font-weight:700;cursor:pointer';
      var controls = document.createElement('div'); controls.style.cssText = 'display:flex;gap:8px;margin:4px 0 14px;flex-wrap:wrap';
      controls.appendChild(mkBtn('▶ Listen', speakDetail));
      controls.appendChild(mkBtn('⏹ Stop', stopDetail));
      controls.appendChild(mkBtn('↻ Replay', function () { stopDetail(); speakDetail(); }));
      var body = document.createElement('div'); body.id = '__eduModalBody';
      card.appendChild(close); card.appendChild(body); card.appendChild(controls); modal.appendChild(card);
      modal.onclick = function (e) { if (e.target === modal) closeExplain(); };
      document.body.appendChild(modal);
    }
    document.getElementById('__eduModalBody').innerHTML = curDetail; modal.style.display = 'flex';
    setExplainVisible(true);
    speakDetail(); // auto-read on open; Stop / Replay available
  }
  function hideKnowMore() { window.__eduHasExplain = false; if (pill) pill.style.display = 'none'; closeExplain(); }
  function stepCard(n, txt) { return '<div style="display:flex;gap:10px;align-items:flex-start;border:1px solid #e3e5f0;border-radius:12px;padding:10px 12px;margin-bottom:8px"><div style="width:22px;height:22px;border-radius:50%;background:#eeecfb;color:#4b3fbf;font-size:12px;font-weight:700;display:flex;align-items:center;justify-content:center;flex:0 0 auto">' + n + '</div><div style="font-size:14px;line-height:1.5">' + esc(txt) + '</div></div>'; }
  function renderDetailHTML(o) {
    var p = ['<h2 style="margin:0 0 10px;font-size:19px;color:#4b3fbf">Let’s understand this</h2>'];
    if (o.hint) p.push('<p style="margin:0 0 8px;font-size:14px"><b>Problem:</b> ' + esc(o.hint) + '</p>');
    if (o.line) p.push('<div style="display:inline-block;margin:2px 0 12px;font-size:13px;background:#e2f5ec;color:#0f7a52;border-radius:16px;padding:4px 12px">Answer · ' + esc(o.line) + '</div>');
    if (o.detail) {
      var n = 0, html = '';
      ('' + o.detail).split('\n').forEach(function (ln) {
        ln = ln.trim(); if (!ln) return;
        var sm = ln.match(/^Step\s*\d+\s*[—:.\-]?\s*(.*)$/i);
        if (sm) { n++; html += stepCard(n, sm[1]); }
        else if (/^why/i.test(ln)) html += '<div style="background:#f5f6ff;border-radius:10px;padding:10px 12px;margin-bottom:8px;font-size:13px;color:#4b3fbf">' + esc(ln) + '</div>';
        else if (/^tip/i.test(ln)) html += '<div style="font-size:13px;color:#6b7093;margin-bottom:8px">' + esc(ln) + '</div>';
        else html += '<div style="font-size:14px;line-height:1.5;margin-bottom:8px">' + esc(ln) + '</div>';
      });
      p.push(html);
    } else if (o.why) {
      p.push('<div style="background:#f5f6ff;border-radius:10px;padding:10px 12px;font-size:14px"><b style="color:#4b3fbf">Why</b><br>' + esc(o.why) + '</div>');
    }
    return p.join('');
  }
  function setDetail(o) {
    if (!o.why && !o.detail) { hideKnowMore(); curDetail = null; return; } // only offer Explain when there's real reasoning
    curDetail = renderDetailHTML(o);
    curDetailText = (o.hint ? ('The question. ' + o.hint + ' ') : '') + (o.line ? ('The answer. ' + o.line + '. ') : '') + (o.why ? ('Why. ' + o.why + ' ') : '') + (o.detail ? ('Here is how it works. ' + o.detail.replace(/\n/g, ' ')) : '');
    window.__eduHasExplain = true;
    if (!IN_APP) { ensurePill(); pill.style.display = 'block'; } // in-app: the native coach card shows the Explain chip instead
  }
  window.__eduExplain = function () { showModal(); }; // called by the app's Explain chip on the coach card

  function renderReveal(o, key) {
    if (o.submit) glow(pubEl(o.submit), 'submit');
    if (o.glow) glow(pubEl(o.glow), o.glowKind === 'answer' ? 'answer' : 'hint');
    if (o.input) { var ie = pubEl(o.input); if (ie) { glow(ie, 'input'); if (o.inputHint != null && !ie.value) { if (ie.getAttribute('data-ph') === null) ie.setAttribute('data-ph', ie.getAttribute('placeholder') || ''); ie.setAttribute('placeholder', '' + o.inputHint); hi = ie; } } }
    var revealLine = (o.line || '') + (o.why ? '  ·  Why: ' + o.why : '');
    emit(revealLine, revealLine, 'R:' + key); // speak exactly what's shown on the coach
  }
  function exposeHint(mode, canHint) {
    window.__eduHintMode = mode;
    window.__eduCanHint = (mode !== 'guided') && !!canHint;
    window.__eduHintLabel = (mode === 'ondemand') ? 'Show answer' : 'Hint';
    if (!IN_APP) renderControls(mode);
  }
  function present(o) {
    var key = o.key || o.line || '', hasHint = !!o.hint, hasWhy = !!o.why;
    if (key !== curKey) { curKey = key; curLevel = 0; }
    var mode = window.__eduHintMode || HMODE || 'ask';
    if (!hasHint) { renderReveal(o, key); setDetail(o); exposeHint(mode, false); return; } // feedback / no-question states show at once
    var reveal;
    if (mode === 'guided') reveal = true;
    else if (mode === 'ondemand' || mode === 'ask') reveal = curLevel >= 1; // one tap reveals move + why (no separate nudge)
    else reveal = curLevel >= (hasWhy ? 2 : 1); // self keeps: problem → nudge → reveal
    if (reveal) { renderReveal(o, key); }
    else {
      if (o.submit) glow(pubEl(o.submit), 'submit');
      if (mode === 'self' && curLevel === 1 && hasWhy) {
        emit(o.why, o.why, 'N:' + key); // nudge (the reasoning, no glow)
      } else {
        var q = (mode === 'self') ? (o.hint + '  ·  Explain your thinking, then tap Hint.') : o.hint;
        emit(q, q, 'H:' + key); // speak exactly what's shown on the coach
      }
    }
    setDetail(o);
    exposeHint(mode, !reveal);
  }
  var ctrlBar = null, _ctrlSig = '';
  function renderControls(mode) {
    if (IN_APP) return;
    var sig = mode + '|' + window.__eduCanHint + '|' + window.__eduHintLabel;
    if (sig === _ctrlSig && ctrlBar) return; _ctrlSig = sig;
    if (!ctrlBar) { ctrlBar = document.createElement('div'); ctrlBar.style.cssText = 'position:fixed;left:8px;right:120px;bottom:66px;z-index:2147483645;display:flex;gap:6px;flex-wrap:wrap;align-items:center;font:600 12px system-ui,sans-serif'; document.body.appendChild(ctrlBar); }
    var modes = [['ask', 'Try first'], ['guided', 'Step-by-step'], ['self', 'Self-explain'], ['ondemand', 'Answer on tap']];
    var html = modes.map(function (m) { var on = m[0] === mode; return '<button data-m="' + m[0] + '" style="border:1px solid ' + (on ? '#4b3fbf' : '#c9cbe0') + ';background:' + (on ? '#4b3fbf' : '#fff') + ';color:' + (on ? '#fff' : '#333') + ';border-radius:14px;padding:5px 9px;cursor:pointer">' + m[1] + '</button>'; }).join('');
    if (window.__eduCanHint) html += '<button data-hint="1" style="border:1px solid #ff9500;background:#fff;color:#b06f00;border-radius:14px;padding:5px 11px;cursor:pointer">' + (window.__eduHintLabel || 'Hint') + '</button>';
    ctrlBar.innerHTML = html;
    [].forEach.call(ctrlBar.querySelectorAll('button'), function (b) { b.onclick = function () { if (b.dataset.hint) window.__eduHint(); else window.__eduSetHintMode(b.dataset.m); }; });
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
