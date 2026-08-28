/*
 * guide-coach.js - generic guide-driven coach for simulations that don't publish their own rounds.
 * ------------------------------------------------------------------------------------------------
 * Works with edu-coach.js (V4 "one-clock"). edu-coach.js polls window.__eduRound every ~300ms and
 * renders the current native round: its `line` (+ voice), an optional `hint`, and glows any `glow`
 * element. Chapters 2-4 set __eduRound themselves; Ch.5-8 (and several KN chapters) never did, so
 * their coach stayed silent. This script fills that gap WITHOUT per-page authoring: it loads the
 * page's already-hosted `<simfile>.guide.json` and drives the coach from it -
 *   - mission line on load,
 *   - one teach step per learner interaction,
 *   - a `hint` from the guide's `coach.whenStuck` bank (the "if you're stuck" nudge),
 *   - a `glow` on the step's `target` control when that control can be found in the page,
 *   - the guide's `done` line at the end.
 *
 * Glow is best-effort: a step glows only when the guide gives a `target` AND that label resolves to
 * an on-page control (many science guides - especially Kannada - omit targets, so those steps guide
 * by text + voice only). Progression follows the glowed control: clicking it advances the step; if a
 * step has no resolvable target, any interaction advances.
 *
 * It stands down if the page publishes its OWN native round (Ch.2-4 style), so it never fights a
 * hand-authored coach.
 *
 * Include AFTER edu-coach.js:
 *   <script src="edu-coach.js"></script>
 *   <script src="guide-coach.js"></script>
 */
(function () {
  "use strict";

  function baseName() {
    try {
      var p = (location.pathname.split("/").pop() || "").trim();
      return p.replace(/\.html$/i, "");
    } catch (e) {
      return "";
    }
  }

  var base = baseName();
  if (!base) return;
  var GUIDE_URL = base + ".guide.json"; // hosted next to the sim (EN or _kn)

  var stepObjs = []; // { text, target }
  var mission = "";
  var doneLine = "";
  var stuck = []; // coach.whenStuck bank
  var wrong = []; // coach.whenWrong bank (used as "why")
  var idx = 0; // 0 = mission, 1..N = steps, >N = done
  var ready = false;
  var standDown = false; // page drives its own coach -> we back off
  var boundTarget = null; // element we attached a one-time advance handler to

  function pageHasOwnRound() {
    var r = window.__eduRound;
    return !!(r && typeof r === "object" && r.native && (r.line || r.glow || r.hint));
  }

  // Resolve a guide `target` label to an on-page control. Try as a CSS selector first, then match
  // the visible text of clickable elements (buttons, [onclick], [role=button], links, common btns).
  function resolveTarget(label) {
    if (!label) return null;
    try {
      var bySel = document.querySelector(label);
      if (bySel) return bySel;
    } catch (e) {}
    var norm = String(label).trim().toLowerCase();
    if (!norm) return null;
    var nodes = document.querySelectorAll(
      "button,[role=button],[onclick],a,.btn,.control-btn,.option,.choice,.card,.lane,input[type=button],input[type=submit]"
    );
    var list = [].slice.call(nodes);
    // exact text match first, then substring either way
    var exact = list.filter(function (el) {
      var t = (el.innerText || el.value || "").trim().toLowerCase();
      return t && t === norm;
    })[0];
    if (exact) return exact;
    return (
      list.filter(function (el) {
        var t = (el.innerText || el.value || "").trim().toLowerCase();
        return t && (t.indexOf(norm) > -1 || norm.indexOf(t) > -1);
      })[0] || null
    );
  }

  function bankPick(bank, n) {
    if (!bank || !bank.length) return undefined;
    return bank[n % bank.length];
  }

  function publish(round) {
    round.native = true;
    if (round.voice == null) round.voice = round.line;
    window.__eduRound = round;
  }

  function render() {
    if (!ready || standDown) return;
    boundTarget = null;

    if (idx <= 0) {
      publish({
        line: mission || (stepObjs[0] && stepObjs[0].text) || "",
        detail: stepObjs
          .map(function (s) {
            return s.text;
          })
          .join("\n"),
        hint: bankPick(stuck, 0),
        key: "guide-mission",
      });
      return;
    }

    if (idx <= stepObjs.length) {
      var s = stepObjs[idx - 1];
      var el = s.target ? resolveTarget(s.target) : null;
      publish({
        line: s.text,
        hint: bankPick(stuck, idx - 1),
        hintVoice: bankPick(stuck, idx - 1),
        why: bankPick(wrong, idx - 1),
        glow: el || undefined,
        key: "guide-step-" + idx,
      });
      // Progression: clicking the glowed control advances; otherwise any interaction does (below).
      if (el) {
        boundTarget = el;
        el.addEventListener(
          "click",
          function onHit() {
            el.removeEventListener("click", onHit, true);
            advance();
          },
          true
        );
      }
      return;
    }

    publish({ line: doneLine || "Done - move on when you're ready.", key: "guide-done" });
  }

  function advance() {
    if (!ready || standDown) return;
    if (idx > stepObjs.length) return; // already at done
    idx += 1;
    render();
  }

  // Load the guide, seed the mission, then let interaction walk the steps.
  try {
    fetch(GUIDE_URL, { cache: "no-store" })
      .then(function (r) {
        return r && r.ok ? r.json() : null;
      })
      .then(function (g) {
        if (!g) return;
        var coach = g.coach || {};
        mission = coach.mission || g.title || "";
        doneLine = coach.done || "";
        stuck = Array.isArray(coach.whenStuck) ? coach.whenStuck : [];
        wrong = Array.isArray(coach.whenWrong) ? coach.whenWrong : [];
        stepObjs = (g.steps || [])
          .map(function (s) {
            return s && s.text ? { text: String(s.text), target: s.target || null } : null;
          })
          .filter(Boolean);
        ready = true;
        idx = 0;
        // Give a page that sets its own round ~1.2s of priority before we seed.
        setTimeout(function () {
          if (pageHasOwnRound()) {
            standDown = true;
          } else {
            render();
          }
        }, 1200);
      })
      .catch(function () {});
  } catch (e) {}

  // A click lands on edu-coach.js's own UI (web only; in-app the coach is a native card, not DOM).
  // Tapping "Hint" / "Explain" must NOT advance the step - it drives edu-coach's own hint->glow
  // reveal. So ignore clicks inside the coach overlays.
  function inCoachUI(t) {
    if (!t || !t.closest) return false;
    return !!t.closest("#__eduBar,#__eduKnowMore,#__eduModal,#__eduModalBody,[data-hint],[data-m]");
  }

  // When the current step names a `target` and we found the control, ONLY that click advances —
  // otherwise any sim tap races ahead of edu-coach's Hint→glow reveal (Socratic staging).
  function allowGenericAdvance() {
    if (idx <= 0 || idx > stepObjs.length) return true;
    var s = stepObjs[idx - 1];
    if (!s || !s.target) return true;
    return !boundTarget; // unresolved target → fall back to any interaction
  }

  // Fallback progression: an interaction advances, EXCEPT (a) a click on the glowed target (its own
  // handler above advances, so we'd double-count) or (b) a click on the coach UI. Capture phase so it
  // fires even if the sim stops propagation.
  ["click", "change"].forEach(function (ev) {
    document.addEventListener(
      ev,
      function (e) {
        if (!ready) return;
        if (!standDown && pageHasOwnRound()) {
          standDown = true;
          return;
        }
        if (inCoachUI(e.target)) return; // hint/explain taps drive reveal, not progression
        if (boundTarget && e.target && (e.target === boundTarget || (boundTarget.contains && boundTarget.contains(e.target)))) {
          return; // the target's own handler advances
        }
        if (!allowGenericAdvance()) return;
        advance();
      },
      true
    );
  });
})();
