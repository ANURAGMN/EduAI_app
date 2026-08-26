/*
 * guide-coach.js - generic guide-driven coach for simulations that don't publish their own rounds.
 * ------------------------------------------------------------------------------------------------
 * Works with edu-coach.js (V4 "one-clock"). edu-coach.js polls window.__eduRound every ~300ms and
 * shows any native round that has a `line`. Chapters 2-4 set __eduRound themselves; Ch.5-8 never did,
 * so their coach stayed silent. This script fills that gap WITHOUT per-page authoring: it loads the
 * page's already-hosted `<simfile>.guide.json` and drives the coach from it -
 *   - mission line on load,
 *   - one teach step per learner interaction (any click / change in the sim),
 *   - the guide's `done` line at the end.
 *
 * It is deliberately conservative:
 *   - If the page publishes its OWN native round (Ch.2-4 style), this script stands down.
 *   - Text + voice only (no per-control glow), so it is safe on any sim regardless of its DOM.
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

  var steps = [];
  var mission = "";
  var doneLine = "";
  var idx = 0; // 0 = mission, 1..N = steps, >N = done
  var ready = false;
  var standDown = false; // page drives its own coach -> we back off

  function pageHasOwnRound() {
    var r = window.__eduRound;
    return !!(r && typeof r === "object" && r.native && (r.line || r.glow || r.hint));
  }

  function publish(line, opts) {
    if (!line) return;
    opts = opts || {};
    window.__eduRound = {
      native: true,
      line: line,
      voice: opts.voice != null ? opts.voice : line,
      why: opts.why,
      detail: opts.detail,
      key: opts.key || "guide-" + idx,
    };
  }

  function render() {
    if (!ready || standDown) return;
    if (idx <= 0) {
      publish(mission || steps[0] || "", {
        voice: mission || steps[0] || "",
        detail: steps.join("\n"),
        key: "guide-mission",
      });
    } else if (idx <= steps.length) {
      var s = steps[idx - 1];
      publish(s, { voice: s, key: "guide-step-" + idx });
    } else {
      publish(doneLine || "Done - move on when you're ready.", { key: "guide-done" });
    }
  }

  function advance() {
    if (!ready || standDown) return;
    if (idx > steps.length) return; // already at done
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
        steps = (g.steps || [])
          .map(function (s) {
            return s && s.text ? String(s.text) : "";
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

  // Any interaction advances the guide. Capture phase so it fires even if the sim stops propagation.
  ["click", "change"].forEach(function (ev) {
    document.addEventListener(
      ev,
      function () {
        if (!ready) return;
        if (!standDown && pageHasOwnRound()) {
          standDown = true;
          return;
        }
        advance();
      },
      true
    );
  });
})();
