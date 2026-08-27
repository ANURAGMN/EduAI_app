/*
 * check-coach-coverage.js — pre-release gate for simulation coach coverage.
 * ------------------------------------------------------------------------------------------------
 * Why this exists: a sim shows the V4 coach only if, at runtime, SOMETHING publishes
 * `window.__eduRound`. "The guide.json exists" and "edu-coach.js is included" do NOT prove that —
 * that gap shipped Ch.5–8 with a silent coach. This script checks the ACTUAL runtime contract for
 * EVERY sim, so a silent-coach sim fails the build instead of reaching users.
 *
 * A sim page is "covered" if either:
 *   (a) it publishes its own round        — the page contains `__eduRound`, OR
 *   (b) it is guide-driven                 — it includes BOTH `edu-coach.js` and `guide-coach.js`
 *                                            AND a matching `<simfile>.guide.json` exists.
 * Anything else = SILENT coach.
 *
 *   node scripts/check-coach-coverage.js            # report; exit 1 if any Science sim is silent
 *   node scripts/check-coach-coverage.js --strict   # exit 1 if ANY sim (incl. Math) is silent
 *
 * Wire into CI / a pre-push hook so a silent coach can never ship again.
 */

const fs = require("fs");
const path = require("path");

const SIM_DIR = path.join(__dirname, "..", "Simulations");
const STRICT = process.argv.includes("--strict");

const files = fs
  .readdirSync(SIM_DIR)
  .filter((f) => /^(science|math)_\d+_\d+.*\.html$/i.test(f));

const guideSet = new Set(
  fs.readdirSync(SIM_DIR).filter((f) => f.endsWith(".guide.json"))
);

function subjectOf(f) {
  return f.toLowerCase().startsWith("math") ? "math" : "science";
}

const rows = [];
for (const f of files) {
  const html = fs.readFileSync(path.join(SIM_DIR, f), "utf8");
  const publishesOwn = html.includes("__eduRound");
  const hasEdu = html.includes("edu-coach.js");
  const hasGuideCoach = html.includes("guide-coach.js");
  const guideFile = f.replace(/\.html$/i, ".guide.json");
  const guideExists = guideSet.has(guideFile);
  const guideDriven = hasEdu && hasGuideCoach && guideExists;
  const covered = publishesOwn || guideDriven;

  let reason = "";
  if (covered) {
    reason = publishesOwn ? "publishes __eduRound" : "guide-driven";
  } else if (hasGuideCoach && !guideExists) {
    reason = "SILENT: includes guide-coach.js but " + guideFile + " missing";
  } else if (hasGuideCoach && !hasEdu) {
    reason = "SILENT: guide-coach.js without edu-coach.js";
  } else {
    reason = "SILENT: nothing publishes a coach round";
  }
  rows.push({ f, subject: subjectOf(f), covered, reason });
}

const silent = rows.filter((r) => !r.covered);
const silentScience = silent.filter((r) => r.subject === "science");
const silentMath = silent.filter((r) => r.subject === "math");

const total = rows.length;
const coveredCount = total - silent.length;
console.log(`Coach coverage: ${coveredCount}/${total} sims covered\n`);

if (silent.length === 0) {
  console.log("All sims covered.");
} else {
  console.log(`SILENT coach on ${silent.length} sim(s):`);
  for (const r of silent) console.log(`  [${r.subject}] ${r.f} — ${r.reason}`);
}

console.log(
  "\n" +
    JSON.stringify(
      {
        total,
        covered: coveredCount,
        silentScience: silentScience.length,
        silentMath: silentMath.length,
        strict: STRICT,
      },
      null,
      2
    )
);

// Gate: Science must be fully covered for release; --strict also blocks on Math (pending).
const blocking = STRICT ? silent : silentScience;
if (blocking.length > 0) {
  console.error(
    `\nFAIL: ${blocking.length} sim(s) would show no coach` +
      (STRICT ? "" : " (Science). Math gaps are informational; use --strict to block on them too.")
  );
  process.exit(1);
}
console.log("\nPASS");
